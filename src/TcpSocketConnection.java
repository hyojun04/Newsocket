import java.io.*;
import java.net.Socket;

public class TcpSocketConnection {
    private static final int PORT = 8189;
    private Socket socket;
    private Client_Tcp client; // SenderViewModel 인스턴스
    

    
    public void startClient(String serverIP) {
        try {
            socket = new Socket(serverIP, PORT);
            client = new Client_Tcp(socket);
            
            
            System.out.println("Server: " + serverIP + " is connected by TCP");
            System.out.println("My IP: " + socket.getLocalAddress());
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    // TCP 에코 메시지를 전송하는 메서드
    public void sendAckMessage(String message) {
        if (client != null) {
            client.sendMessage_tcp(message); // Client_Tcp을 사용하여 메시지 전송
        } else {
            System.out.println("SenderViewModel이 초기화되지 않았습니다.");
        }
    }
 // byte배열의 TCP check 메시지를 전송하는 메서드
    public void sendAckMessage(byte[] message) {
        if (client != null) {
            client.sendMessage_tcp(message); // Client_Tcp을 사용하여 메시지 전송
        } else {
            System.out.println("SenderViewModel이 초기화되지 않았습니다.");
        }
    }
    
    // 소켓 종료 메서드 추가
    public void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("TCP 소켓이 닫혔습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}