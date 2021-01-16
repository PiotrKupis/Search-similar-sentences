import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.exception.ND4JIllegalStateException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class of the server, prepares Word2Vec model, list of sentences and list of users
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class SearchSimilarSentencesServer implements Runnable {

    private ArrayList<Sentence> serverSentences;
    private String absolutePath;
    private ServerSocket serverSocket;
    private ExecutorService serverThreadpool;

    public SearchSimilarSentencesServer() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        this.serverSentences = new ArrayList<>();
        this.absolutePath = (new File("src/main/resources")).getAbsolutePath();
        this.serverThreadpool = Executors.newFixedThreadPool(20);
        serverSocketInitialization();
    }

    /**
     * Method sets up encrypted communication
     */
    private void serverSocketInitialization() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(absolutePath + "/searchSentences.jks"), "SearchSentences".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, "SearchSentences".toCharArray());
        KeyManager[] km = keyManagerFactory.getKeyManagers();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);
        TrustManager[] tm = trustManagerFactory.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLSv1");
        sslContext.init(km, tm, null);

        ServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        serverSocket = serverSocketFactory.createServerSocket(6000);
    }

    @Override
    public void run() {

        Word2Vec word2Vec=null;
        File booksDirectory;
        Socket clientSocket;
        Scanner in;
        LearnWordsMeaning learnWordsMeaning;
        FileSentencesReader fileSentencesReader;
        String updateWord2Vec;
        Thread sentencesThread;
        JAXBContext jaxbContext;
        Marshaller jaxbMarshaller;
        Unmarshaller jaxbUnmarshaller;
        UserEntries userEntries;

        try {
            booksDirectory = new File(absolutePath + "/books");
            learnWordsMeaning = new LearnWordsMeaning(booksDirectory);
            fileSentencesReader = new FileSentencesReader();

            System.out.println("Update file list? (yes/no)");
            in = new Scanner(System.in);
            updateWord2Vec = in.nextLine();

            //creating Word2Vec model and list of sentences
            sentencesThread = new Thread() {
                public void run() {
                    try {
                        serverSentences = fileSentencesReader.createNewSentencesList(booksDirectory);
                        System.out.println("Sentences list was created");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            sentencesThread.start();

            if (updateWord2Vec.equals("yes")) {
                word2Vec = learnWordsMeaning.createNewModel();
                learnWordsMeaning.saveModel(absolutePath + "/model.txt");
            } else {
                try{
                    word2Vec = learnWordsMeaning.loadModel(absolutePath + "/model.txt");
                }catch (ND4JIllegalStateException e){
                    System.out.println("Error: lack of saved word2Vec model. Please update file list to create a new model");
                }
            }

            if(word2Vec!=null){
                System.out.println("Model was prepared");

                sentencesThread.join();
                fileSentencesReader.calculateMeanVector(word2Vec);
                System.out.println("List of sentences was prepared");

                //getting list of registered users
                jaxbContext = JAXBContext.newInstance(UserEntries.class);

                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                userEntries = (UserEntries) jaxbUnmarshaller.unmarshal(new File(absolutePath + "/database.xml"));
                System.out.println("List of registered users was prepared");

                System.out.println("Waiting for users");
                while (true) {
                    clientSocket = serverSocket.accept();

                    ArrayList<Sentence> sentences = new ArrayList<Sentence>();
                    sentences.addAll(serverSentences);

                    serverThreadpool.execute(new Client(clientSocket, sentences, word2Vec, userEntries, jaxbMarshaller));
                }
            }

        } catch (JAXBException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
