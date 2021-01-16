import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Class that represents a single sentence
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class Sentence {

    private String sentence;
    private String fileName;
    private int fileLine;
    private INDArray vectorMean;
    private double similarity;

    public Sentence(String sentence, String fileName, int fileLine) {
        this.sentence = sentence;
        this.fileName = fileName;
        this.fileLine = fileLine;
    }

    public String toString() {
        return sentence + "\n" + fileName + "\n " + fileLine + "\n " + vectorMean + "\n " + similarity;
    }

    public String getSentence() {
        return sentence;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileLine() {
        return fileLine;
    }

    public INDArray getVectorMean() {
        return vectorMean;
    }

    public void setVectorMean(INDArray vectorMean) {
        this.vectorMean = vectorMean;
    }
}
