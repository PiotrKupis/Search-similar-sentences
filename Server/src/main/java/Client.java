import com.google.common.base.Splitter;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Main client's thread
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class Client implements Runnable {

    private ArrayList<Sentence> serverSentences;
    private Word2Vec word2Vec;
    private BufferedReader getInformation;
    private PrintWriter sendInformation;
    private UserEntries userEntries;
    private Marshaller jaxbMarshaller;
    private String absolutePath;

    public Client(Socket clientSocket, ArrayList<Sentence> serverSentences, Word2Vec word2Vec, UserEntries userEntries, Marshaller jaxbMarshaller) throws IOException {
        this.serverSentences = serverSentences;
        this.word2Vec = word2Vec;
        this.getInformation = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.sendInformation = new PrintWriter(clientSocket.getOutputStream(), true);
        this.userEntries = userEntries;
        this.jaxbMarshaller = jaxbMarshaller;
        this.absolutePath = (new File("src/main/resources")).getAbsolutePath();
    }

    /**
     * Method that sends similar sentences to the user's sentence
     *
     * @param userSentence sentence entered by the user
     */
    private void sendSimilarSentences(String userSentence) {

        Collection<String> sentenceWords;
        INDArray sentenceWordsArray;
        int i;

        //calculating the mean vector
        sentenceWords = Splitter.on(' ').splitToList(userSentence);
        sentenceWordsArray = word2Vec.getWordVectorsMean(sentenceWords);

        //calculating the similarity of sentences
        for (i = 0; i < serverSentences.size(); ++i) {
            double cosineSimilarity = Transforms.cosineSim(serverSentences.get(i).getVectorMean(), sentenceWordsArray);
            serverSentences.get(i).setSimilarity(cosineSimilarity);
        }

        Collections.sort(serverSentences, new Comparator<Sentence>() {
            @Override
            public int compare(Sentence s1, Sentence s2) {
                return Double.compare(s1.getSimilarity(), s2.getSimilarity());
            }
        }.reversed());

        for (i = 0; i < 10; ++i) {
            sendInformation.println("" +
                    (i + 1) + ". " + serverSentences.get(i).getSentence() + "\n" +
                    "File: " + serverSentences.get(i).getFileName() + "\n" +
                    "Line: " + serverSentences.get(i).getFileLine() + "\n" +
                    "Similarity: " + serverSentences.get(i).getSimilarity() + "\n");
        }
    }

    @Override
    public void run() {

        String userMessage, login, password;
        boolean loggedIn = false;

        try {
            sendInformation.println("" +
                    "Enter:\n" +
                    "'log in' - to log in\n" +
                    "'registration' - to register\n" +
                    "'remind password' - to remind the password\n" +
                    "'exit' - to exit the application");

            do {
                userMessage = getInformation.readLine();

                if (!loggedIn && (userMessage.equals("registration") || userMessage.equals("log in"))) {

                    sendInformation.println("Enter your login and then your password");
                    login = getInformation.readLine();
                    password = getInformation.readLine();

                    if (userMessage.equals("registration")) {

                        synchronized (userEntries) {
                            if (!userEntries.containsLogin(login)) {
                                userEntries.getUserEntries().add(new UserEntry(login, password));

                                //saving the user to the database
                                synchronized (jaxbMarshaller) {
                                    jaxbMarshaller.marshal(userEntries, new File(absolutePath + "/database.xml"));
                                }

                                loggedIn = true;
                                sendInformation.println("" +
                                        "Registration was successful, you are logged in\n" +
                                        "Enter: \n" +
                                        "'search' - to search similar sentences to your sentence\n" +
                                        "'log out' - to log out");
                            } else
                                sendInformation.println("" +
                                        "The login is already taken\n" +
                                        "Enter:\n" +
                                        "'log in' - to log in\n" +
                                        "'registration' - to register\n" +
                                        "'remind password' - to remind the password\n" +
                                        "'exit' - to exit the application");
                        }
                    } else {
                        synchronized (userEntries) {
                            if (userEntries.containsLogin(login)) {

                                if (userEntries.getUserPassword(login) != null) {
                                    loggedIn = true;
                                    sendInformation.println("" +
                                            "You are logged in\n" +
                                            "Enter: \n" +
                                            "'search' - to search similar sentences to your sentence\n" +
                                            "'log out' - to log out");
                                } else
                                    sendInformation.println("" +
                                            "Incorrect data was entered\n " +
                                            "Enter:\n" +
                                            "'log in' - to log in\n" +
                                            "'registration' - to register\n" +
                                            "'remind password' - to remind the password\n" +
                                            "'exit' - to exit the application");
                            } else
                                sendInformation.println("" +
                                        "Incorrect data was entered\n " +
                                        "Enter:\n" +
                                        "'log in' - to log in\n" +
                                        "'registration' - to register\n" +
                                        "'remind password' - to remind the password\n" +
                                        "'exit' - to exit the application");
                        }
                    }
                } else if (!loggedIn && userMessage.equals("remind password")) {

                    sendInformation.println("Enter your login");
                    login = getInformation.readLine();

                    synchronized (userEntries) {
                        if (userEntries.containsLogin(login)) {
                            sendInformation.println("Your password: " + userEntries.getUserPassword(login));
                            sendInformation.println("" +
                                    "Enter:\n" +
                                    "'log in' - to log in\n" +
                                    "'registration' - to register\n" +
                                    "'remind password' - to remind the password\n" +
                                    "'exit' - to exit the application");
                        } else
                            sendInformation.println("" +
                                    "Incorrect login was entered\n " +
                                    "Enter:\n" +
                                    "'log in' - to log in\n" +
                                    "'registration' - to register\n" +
                                    "'remind password' - to remind the password\n" +
                                    "'exit' - to exit the application");
                    }
                } else if (loggedIn) {

                    if (userMessage.equals("search")) {
                        sendInformation.println("Enter your sentence");
                        userMessage = getInformation.readLine();

                        if (userMessage.matches(".*\\d.*") || !userMessage.contains(" ") || userMessage.matches(".*[—–;αβγλ/].*")) {

                            sendInformation.println("" +
                                    "The sentence must contains at least 2 words and cannot contain numbers or special characters\n" +
                                    "Enter: \n" +
                                    "'search' - to search similar sentences to your sentence\n" +
                                    "'log out' - to log out");
                        } else {
                            sendSimilarSentences(userMessage.toLowerCase());
                            sendInformation.println("" +
                                    "Enter: \n" +
                                    "'search' - to search similar sentences to your sentence\n" +
                                    "'log out' - to log out");
                        }
                    } else if (!userMessage.equals("exit") && !userMessage.equals("log out")) {
                        sendInformation.println("" +
                                "Incorrect data was entered" +
                                "Enter:\n" +
                                "'search' - to search similar sentences to your sentence\n" +
                                "'log out' - to log out");
                    }
                } else if (!userMessage.equals("exit") && !userMessage.equals("log out")) {
                    sendInformation.println("" +
                            "Incorrect data was entered\n " +
                            "Enter:\n" +
                            "'log in' - to log in\n" +
                            "'registration' - to register\n" +
                            "'remind password' - to remind the password\n" +
                            "'exit' - to exit the application");
                }

            } while (!userMessage.equals("exit") && !userMessage.equals("log out"));

            //stop client application thread
            sendInformation.println("exit");

        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }
}
