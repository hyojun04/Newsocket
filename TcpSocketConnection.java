import java.io.*;
import java.net.Socket;

public class TcpSocketConnection {
    private static final int PORT = 8189;
    private Socket socket;
    private Client_Tcp client; // SenderViewModel �ν��Ͻ�
    

    
    public void startClient(String serverIP) {
        try {
            socket = new Socket(serverIP, PORT);
            client = new Client_Tcp(socket);
            
            
            System.out.println("Client: " + serverIP + " is connected by TCP" + " & index: " + NewSocket.clients_tcp_index);
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    // TCP ���� �޽����� �����ϴ� �޼���
    public void sendAckMessage(String message) {
        if (client != null) {
            client.sendMessage_tcp(message); // Client_Tcp�� ����Ͽ� �޽��� ����
        } else {
            System.out.println("SenderViewModel�� �ʱ�ȭ���� �ʾҽ��ϴ�.");
        }
    }
 // byte�迭�� TCP check �޽����� �����ϴ� �޼���
    public void sendAckMessage(byte[] message) {
        if (client != null) {
            client.sendMessage_tcp(message); // Client_Tcp�� ����Ͽ� �޽��� ����
        } else {
            System.out.println("SenderViewModel�� �ʱ�ȭ���� �ʾҽ��ϴ�.");
        }
    }
    
    // ���� ���� �޼��� �߰�
    public void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("TCP ������ �������ϴ�.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
