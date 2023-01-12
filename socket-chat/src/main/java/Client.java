import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String currentUsername;

    public Client(Socket socket, String currentUsername) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.currentUsername = currentUsername;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            writer.write(currentUsername);
            writer.newLine();
            writer.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                writer.write(currentUsername + ": " + message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeResources();
        }
    }

    public void receiveMessage() {
        new Thread(() -> {
            String receivedMessage;
            while (socket.isConnected()) {
                try {
                    receivedMessage = reader.readLine();
                    System.out.println(receivedMessage);
                } catch (IOException e) {
                    closeResources();
                }
            }
        }).start();
    }

    private void closeResources() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Error while closing resources");
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1222);
        Client client = new Client(socket, username);

        client.receiveMessage();
        client.sendMessage();
    }
}
