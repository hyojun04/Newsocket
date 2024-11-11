import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;

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
            
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                
                /*UDP broad로 Server쪽 IP 전송하는 매커니즘 추가*/
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientIP+"\n");
                consoleArea.append("Client connected: " + clientIP+"\n");
              
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

    public class ClientHandler implements Runnable {
        private final Server_Tcp receiverTcp;
        public int permanent_id;
        private Socket clientSocket;
        private boolean running = true; // Socket이 열려있는지 나타내는 변수
        
        private StartTCPCheckThread tcpCheckThread;
        private Thread checkThread;
        
        public ClientHandler(Socket clientSocket, TcpConnectionAccepter tcpAccepter, JTextArea receivedMessagesArea) {
            this.clientSocket = clientSocket;

            // 인덱스를 설정하고, 연결된 클라이언트를 처리할 수 있도록 설정
            synchronized (NewSocket.tcpconnectionmanager) {
                if (NewSocket.clients_tcp_index == 0) {
                    permanent_id = NewSocket.clients_tcp_index;
                    System.out.println("Client index 0 is added");
                    TcpConnectionManager.addClient(clientSocket.getInetAddress().getHostAddress(),clientSocket,true,false);
                    NewSocket.clients_tcp_index++;
                } else {
                    permanent_id = NewSocket.clients_tcp_index;
                    System.out.println("Client index: "+permanent_id+ " is added");
                    TcpConnectionManager.addClient(clientSocket.getInetAddress().getHostAddress(),clientSocket,true,false);
                    NewSocket.clients_tcp_index++;
                }
                System.out.println("Client: " + clientSocket.getInetAddress() + " is connected by TCP"
                        + " & index: " + (NewSocket.clients_tcp_index - 1));
            }

            this.receiverTcp = new Server_Tcp(clientSocket, receivedMessagesArea);
        }

        @Override
        public void run() {
            try {
                // TCP Ack 메시지를 수신하였는지 체크하는 스레드 생성
            	tcpCheckThread = new StartTCPCheckThread(receiverTcp, this);
                checkThread = new Thread(tcpCheckThread);
                checkThread.start();
                
                // TCP Ack 수신  시작
                receiverTcp.startReceiving();

                // Check for socket status in a loop
                while (running) {
                    if (clientSocket.isClosed() || !clientSocket.isConnected()) {
                        System.out.println("Client socket is closed or disconnected. Stopping handler for client: " + clientSocket.getInetAddress());
                        break; // Exit the loop if the socket is closed or disconnected
                    }

                    // 스레드 복잡도를 낮추기위해 1초마다 검사
                    Thread.sleep(1000);
                }
                
            }catch (IOException e) {
                System.out.println("IOException in ClientHandler: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in ClientHandler: " + e.getMessage());
                e.printStackTrace();
                
            } finally {
                // while문을 빠져나오면 Handler를 종료함
                
                stopTCPCheckThread();
                stopHandler();
            }
        }

        // ClientHandler 스레드를 종료하는 메소드 
        public void stopHandler() {
            running = false;
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Client socket closed for client: " + clientSocket.getInetAddress());
                }
            }catch (ClosedByInterruptException e) {
            	System.out.println("Thread ends by Interruption");
            }catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            } 
        }
        public void stopTCPCheckThread() {
        	//checkThread.interrupt();
        	//TCPCheckThread를 중단시킴
        	tcpCheckThread.stopThread();
        }
    }

}
