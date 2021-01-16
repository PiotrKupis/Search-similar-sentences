public class Client {

    public static void main(String[] args) {
        Thread serverThread;
        try {
            serverThread = new Thread(new SearchSimilarSentencesClient());
            serverThread.start();
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
