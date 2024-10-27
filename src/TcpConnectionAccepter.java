import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JTextArea;

public class TcpConnectionAccepter implements Runnable {
    private static final int PORT = 8189; // 수신할 포트
    private ServerSocket serverSocket;
    
    private JTextArea receivedMessagesArea;
    private JTextArea consoleArea;

    public TcpConnectionAccepter(JTextArea receivedMessagesArea, JTextArea consoleArea) {
        this.receivedMessagesArea = receivedMessagesArea;
        this.consoleArea = consoleArea;
    }
    

    public void run() {
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for connection...");

            // 클라이언트 연결을 대기하면서, 각 연결에 대해 새로운 스레드를 생성
            // 클라이언트 최대 개수 설정
            
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                
                /*UDP broad로 Server쪽 IP 전송하는 매커니즘 추가*/
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // 각 클라이언트에 대해 새로운 핸들러 스레드를 생성
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, receivedMessagesArea);
                new Thread(clientHandler).start();
            }
            //consoleArea.append("TCP 소켓 연결완료 \n");
            //System.out.println("All TCP Sockets are connnected");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 각 클라이언트와의 통신을 처리하는 클래스
    public class ClientHandler implements Runnable {
        
        private final Server_Tcp receiverTcp;
        public int permanent_id;
        

        public ClientHandler(Socket clientSocket, TcpConnectionAccepter tcpAccepter, JTextArea receivedMessagesArea) {
            
            
            

            // 인덱스를 설정하고, 연결된 클라이언트를 처리할 수 있도록 설정
            synchronized (NewSocket.clients_tcp) {
                if (NewSocket.clients_tcp_index == 0) {
                	permanent_id = NewSocket.clients_tcp_index;
                	
                    NewSocket.clients_tcp.set(NewSocket.clients_tcp_index, false);
                    NewSocket.clients_tcp_index++;
                } else {
                	permanent_id = NewSocket.clients_tcp_index;
                	
                    NewSocket.clients_tcp.add(NewSocket.clients_tcp_index, false);
                    NewSocket.clients_tcp_index++;
                }
                System.out.println("Client: " + clientSocket.getInetAddress() + " is connected by TCP"
                        + " & index: " + NewSocket.clients_tcp_index);
                
            }

            this.receiverTcp = new Server_Tcp(clientSocket, receivedMessagesArea);
        }

        @Override
        public void run() {
            try {
            	// TCP Echo 메시지를 수신하였는지 체크하는 스레드 생성
                StartTCPCheckThread tcpCheckThread = new StartTCPCheckThread(receiverTcp, this);
                new Thread(tcpCheckThread).start();
                
                // 수신 스레드 시작
                receiverTcp.startReceiving();

                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
