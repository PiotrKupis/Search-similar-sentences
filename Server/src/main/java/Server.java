import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class Server {

    public static void main(String[] args) {

        Thread serverThread;
        try {
            serverThread = new Thread(new SearchSimilarSentencesServer());
            serverThread.start();
            serverThread.join();
        } catch (InterruptedException | KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
