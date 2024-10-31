import java.io.*;
import java.net.Socket;
import javax.swing.JTextArea;

public class Server_Tcp {
    /* �޽����� �ޱ⸸ �ϴ� ��� ���� */
    private Socket socket;
    private static final int PORT = 8189;
    private JTextArea receivedMessagesArea;  // GUI�� receive message â
    private volatile boolean newEchoReceived_tcp = false; // ���� �޽��� ���� ����
    private int receive_message_num = 0;
    private String lastReceivedMessage = ""; // �ֱ� ���ŵ� �޽��� ����

    // �����ڿ��� JTextArea ���� ����
    public Server_Tcp(Socket socket, JTextArea receivedMessagesArea) {
        this.socket = socket;
        this.receivedMessagesArea = receivedMessagesArea;
    }
    
    public void reset_message_num() {
        receive_message_num = 0;
    }
    
    public boolean hasNewEchoMessage() {
        return newEchoReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newEchoReceived_tcp = false;
    }
    
    // �ֱ� ���ŵ� �޽��� ��ȯ �޼��� �߰�
    public String getReceivedMessage() {
        return lastReceivedMessage;
    }

    public void startReceiving() throws IOException { //���� throw�Ͽ� ClientHandler���� ó���ϵ�����
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // BufferedReader�� ����Ͽ� �����͸� �ۼ���
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("Server_TCP is open");

            // Ŭ���̾�Ʈ���� ������ �����ϸ鼭 �޽����� ���������� ����
            String receivedMessage;
            while (!socket.isClosed() && (receivedMessage = in.readLine()) != null) {
                // ���ŵ� �޽��� ó��
                receive_message_num++;
                lastReceivedMessage = receivedMessage; // �ֱ� ���ŵ� �޽��� ����
                receivedMessagesArea.append("[" + receive_message_num + "] ���ŵ� �޽��� from " + clientIP + ": " + receivedMessage + "\n");
                System.out.println("���ŵ� �޽��� from " + clientIP + ": " + receivedMessage);
                
                synchronized (this) {
                    newEchoReceived_tcp = true; // ���� �޽����� �޾��� ���
                    System.out.println("newEchoMessage was coming");
                    notifyAll();
                }
            }
        } finally {
            // Exception�� throw�Ͽ� �ܺο��� ó���ϵ��� ��.
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("TCP ������ �������ϴ�.");
        }
    }
}
