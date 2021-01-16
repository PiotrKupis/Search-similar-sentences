import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Main class of the client
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class SearchSimilarSentencesClient implements Runnable {

    private Socket clientSocket = null;
    private Thread sendThread, listenThread;

    public SearchSimilarSentencesClient() {
    }

    /**
     * Method sets up encrypted communication
     */
    private void clientSocketInitialization() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        String absolutePath = (new File("src/main/resources")).getAbsolutePath();
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

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        clientSocket = sslSocketFactory.createSocket("localhost", 6000);
    }

    @Override
    public void run() {
        try {
            clientSocketInitialization();

            sendThread = new Thread(new ClientSend(clientSocket));
            listenThread = new Thread(new ClientGet(clientSocket));

            listenThread.start();
            sendThread.start();
            sendThread.join();
            listenThread.join();

        } catch (IOException | InterruptedException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException | CertificateException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
