import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpConnectionAccepter { //Server
    private static final int PORT = 8189; // 수신할 포트
    private ServerSocket serverSocket;
    
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for connection...");
          
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}