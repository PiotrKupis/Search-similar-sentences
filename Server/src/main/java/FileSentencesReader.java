import com.google.common.base.Splitter;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that reads sentences from the file and calculate their mean vector
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class FileSentencesReader {

    private ArrayList<Sentence> sentences;

    public FileSentencesReader() {
    }

    /**
     * Method that reads sentences from the file and create list of them
     *
     * @param filesDirectory directory with books
     * @return list of sentences
     * @throws IOException
     */
    public ArrayList<Sentence> createNewSentencesList(File filesDirectory) throws IOException {

        String fileName, fileLine, sentenceRegex, sentence;
        Pattern sentencePattern;
        Matcher sentenceMatcher;
        int fileLineNumber;
        BufferedReader fileReader;

        sentences = new ArrayList<>();

        for (File fileEntry : filesDirectory.listFiles()) {

            fileName = fileEntry.getName();

            fileReader = new BufferedReader(new FileReader(filesDirectory + "\\" + fileName));
            fileLineNumber = 1;

            sentenceRegex = "([^.!?]|(?<=dr|fig|p|m.in|ang|łac|art|fr|tj|rys|tzn|np|str|\\b[A-Za-z]|\\s)[.!?])+";
            sentencePattern = Pattern.compile(sentenceRegex);

            while ((fileLine = fileReader.readLine()) != null) {

                fileLine = fileLine.toLowerCase();
                sentenceMatcher = sentencePattern.matcher(fileLine);

                //searching sentences in the line
                while (sentenceMatcher.find()) {

                    sentence = sentenceMatcher.group();

                    //removing redundant chars
                    sentence = sentence.replace("*", "");
                    sentence = sentence.replace("…", "");
                    sentence = sentence.replace("...", "");
                    sentence = sentence.replace("..", "");
                    sentence = sentence.replace("”", "");
                    sentence = sentence.replace("„", "");
                    sentence = sentence.replace("]", "");
                    sentence = sentence.replace("[", "");
                    sentence = sentence.replace("«", "");
                    sentence = sentence.replace("»", "");
                    sentence = sentence.trim();

                    if (!sentence.matches(".*\\d.*") && sentence.length() > 5 && sentence.contains(" ") && !sentence.matches(".*[—–;αβγλ/].*")) {

                        if (sentence.charAt(sentence.length() - 1) == ':') {
                            sentence = sentence.substring(0, sentence.length() - 1);
                            sentence = sentence.trim();
                        }
                        sentences.add(new Sentence(sentence, fileName, fileLineNumber));
                    }
                }
                fileLineNumber++;
            }
            fileReader.close();
        }

        return sentences;
    }

    /**
     * Method that calculate mean vectors of sentences
     *
     * @param word2Vec
     */
    public void calculateMeanVector(Word2Vec word2Vec) {

        Collection<String> sentenceWords;
        INDArray sentenceWordsArray;

        for (int i = 0; i < sentences.size(); ++i) {
            try {
                sentenceWords = Splitter.on(' ').splitToList(sentences.get(i).getSentence());
                sentenceWordsArray = word2Vec.getWordVectorsMean(sentenceWords);
                sentences.get(i).setVectorMean(sentenceWordsArray);
            } catch (IllegalStateException e) {
                //removing sentences whose words were found too rarely
                sentences.remove(i);
                --i;
            }
        }
    }
}
