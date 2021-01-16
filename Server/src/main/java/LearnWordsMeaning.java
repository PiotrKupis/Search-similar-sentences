import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.exception.ND4JIllegalStateException;

import java.io.File;

/**
 * Class that prepares Word2Vec model and allow to save/load it
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class LearnWordsMeaning {

    private Word2Vec word2Vec;
    private File filesDirectory;

    public LearnWordsMeaning(File filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    /**
     * Method that creates new Word2Vec model
     *
     * @return Word2Vec model
     */
    public Word2Vec createNewModel() {

        SentenceIterator sentenceIterator = new FileSentenceIterator(filesDirectory);
        sentenceIterator.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String s) {
                s = s.toLowerCase();
                s = s.replace("*", " ");
                s = s.replace("…", " ");
                s = s.replace("...", " ");
                s = s.replace("..", " ");
                s = s.replace(";", " ");
                s = s.replace("”", " ");
                s = s.replace("„", " ");
                s = s.replace("]", " ");
                s = s.replace("[", " ");
                s = s.replace("«", " ");
                s = s.replace("»", " ");
                s = s.replace(". ", " ");
                s = s.replace(",", " ");
                s = s.replace("?", " ");
                s = s.replace("!", " ");
                s = s.replace("\\d", " ");
                return s;
            }
        });

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        word2Vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .learningRate(0.05)
                .layerSize(350)
                .seed(42)
                .workers(4)
                .windowSize(5)
                .iterate(sentenceIterator)
                .tokenizerFactory(tokenizerFactory)
                .build();
        word2Vec.fit();

        return word2Vec;
    }

    /**
     * Method that saves the model to the file
     *
     * @param modelPath path of the file
     */
    public void saveModel(String modelPath) {
        WordVectorSerializer.writeWord2VecModel(word2Vec, modelPath);
    }

    /**
     * Method that loads the model from the file
     *
     * @param modelPath path of the file
     * @return Word2Vec model
     */
    public Word2Vec loadModel(String modelPath) throws ND4JIllegalStateException {

        word2Vec = WordVectorSerializer.readWord2VecModel(modelPath);
        return word2Vec;
    }
}
