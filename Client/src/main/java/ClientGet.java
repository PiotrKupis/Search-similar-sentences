import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Class that gets data from the server
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class ClientGet implements Runnable {

    private BufferedReader getInformation;

    public ClientGet(Socket clientSocket) throws IOException {
        getInformation = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        String message = "";

        try {
            do {
                message = getInformation.readLine();

                if (!message.equals("exit"))
                    System.out.println(message);

            } while (!message.equals("exit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
