import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements Runnable {
    private static List<MessageHandler> clientsToBroadcast= new ArrayList<>();

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String currentUsername;

    public MessageHandler(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.currentUsername = reader.readLine();
            clientsToBroadcast.add(this);
            
            broadcastMessage("INFO: " + currentUsername + " joined the chat");
        } catch (IOException e) {
            closeResources();
        }
    }

    @Override
    public void run() {
        String newClientMessage;
        while (socket.isConnected()) {
            try {
                newClientMessage = reader.readLine();
                broadcastMessage(newClientMessage);
            } catch (Exception e) {
                closeResources();
                break;
            }
        }
    }


    private void broadcastMessage(String message) {
        for (MessageHandler messageHandler: clientsToBroadcast) {
            try {
                if (!messageHandler.currentUsername.equals(currentUsername)) {
                    messageHandler.writer.write(message);
                    messageHandler.writer.newLine();
                    messageHandler.writer.flush();
                }
            } catch (IOException e) {
                closeResources();
            }
        }
    }

    private void leaveChat() {
        clientsToBroadcast.remove(this);
        broadcastMessage("INFO: " + currentUsername + " left the chat");
    }

    private void closeResources() {
        leaveChat();
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
            System.out.println("Error while leaving chat and closing resources");
        }
    }
}
