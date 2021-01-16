import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class that sends data to the server
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
public class ClientSend implements Runnable {

    private PrintWriter sendInformation;
    private Scanner input;

    public ClientSend(Socket clientSocket) throws IOException {
        sendInformation = new PrintWriter(clientSocket.getOutputStream(), true);
        input = new Scanner(System.in);
    }

    @Override
    public void run() {
        String message = "";

        do {
            message = input.nextLine();
            sendInformation.println(message);

        } while (!message.equals("exit") && !message.equals("log out"));
    }
}
