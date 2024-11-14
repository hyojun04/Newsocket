import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Server_Tcp {
    private Socket socket;
    private static final int PORT = 8189;
   
    private volatile boolean newEchoReceived_tcp = false; // ���� �޽��� ���� ����

    public Server_Tcp(Socket socket) {
        this.socket = socket;
    }

    public boolean hasNewEchoMessage() {
        return newEchoReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newEchoReceived_tcp = false;
    }

    public void startReceiving() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            String clientIP = socket.getInetAddress().getHostAddress();

            // Ŭ���̾�Ʈ���� ������ �����ϸ鼭 �޽����� ���������� ����
            while (!socket.isClosed()) {
                try {
                    Object receivedObject = in.readObject();
                    if (receivedObject instanceof String) {
                        String receivedMessage = (String) receivedObject;

                        // ���ŵ� �޽��� ó��
                        System.out.println("���ŵ� �޽��� from " + clientIP + ": " + receivedMessage);
                        newEchoReceived_tcp = true;

                        out.flush();
                    }
                } catch (EOFException e) {
                    System.out.println("Client Connection is disconnected.");
                    break; // Ŭ���̾�Ʈ�� ������ �������� �� while ������ Ż��
                } catch (SocketException e) {
                    System.out.println("Connection gets reset: " + e.getMessage());
                    break; // ������ ���µǾ��� �� while ������ Ż��
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break; // ��Ÿ ������ �߻����� �� while ������ Ż��
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
