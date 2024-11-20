import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class TcpConnectionAccepter implements Runnable {
    private static final int PORT = 8189; // 수신할 포트
    private ServerSocket serverSocket;
    
    private JTextArea receivedMessagesArea;
    private JTextArea consoleArea;
    private Thread clientHandlerThread;
    
    // 모든 ClientHandler 인스턴스를 저장할 리스트 -> ClientInfo에 clientHandler를 넣는게 나을 듯
    private final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    public TcpConnectionAccepter(JTextArea receivedMessagesArea, JTextArea consoleArea) {
        this.receivedMessagesArea = receivedMessagesArea;
        this.consoleArea = consoleArea;
    }
    

    public void run() {
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for connection...");

            // 클라이언트 연결을 대기하면서, 각 연결에 대해 새로운 스레드를 생성
            
            
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                
                /*UDP broad로 Server쪽 IP 전송하는 매커니즘 추가*/
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientIP);
                consoleArea.append("Client connected: " + clientIP+"\n");
              
                // 각 클라이언트에 대해 새로운 핸들러 스레드를 생성
                TcpConnectionManager.addClient(clientSocket.getInetAddress().getHostAddress(),clientSocket,true,false);                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, receivedMessagesArea);
                //clientHandler객체를 담아두는 리스트 생성 -> 모든 객체를 반납하기위해서 만듦
                clientHandlers.add(clientHandler);
                clientHandlerThread = new Thread(clientHandler);
                clientHandlerThread.start();
                
                
                	
                
            }
           
            
            
        
        } catch (IOException e) {
        	if(Thread.currentThread().isInterrupted()) {
            	//Interrupt가 발생하면 while문 종료
            	//스레드가 죽기전 모든 clientHandler를 종료시킨다.
            	System.out.println("Program is clear by Thread Interruption");
            	killAllClientHandlers();  
            	
            	
            }
            e.printStackTrace();            
        }
        
    }
 // 모든 ClientHandler의 stopHandler를 호출하여 종료
    private void killAllClientHandlers() {
        for (ClientHandler handler : clientHandlers) {
        	System.out.println("Handler of index "+ handler.permanent_id + " is killed");
            handler.stopHandler();
        }
        clientHandlers.clear(); // 리스트를 비워줌
    }
    
    
    public void closeTcpSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("TCP socket closed successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error closing TCP socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    

    public class ClientHandler implements Runnable {
        private final Server_Tcp receiverTcp;
        public int permanent_id;
        private Socket clientSocket;            
        private StartTCPCheck tcpCheck;
        
        
        public ClientHandler(Socket clientSocket, TcpConnectionAccepter tcpAccepter, JTextArea receivedMessagesArea) {
            this.clientSocket = clientSocket;

            // 인덱스를 설정하고, 연결된 클라이언트를 처리할 수 있도록 설정
            synchronized (NewSocket.tcpconnectionmanager) {
                if (NewSocket.clients_tcp_index == 0) {
                    permanent_id = NewSocket.clients_tcp_index;
                    System.out.println("Client index 0 is added");
                    //TcpConnectionManager.setClient(clientSocket.getInetAddress().getHostAddress(),clientSocket,true,false);
                    NewSocket.clients_tcp_index++;
                } else {
                    permanent_id = NewSocket.clients_tcp_index;
                    System.out.println("Client index: "+permanent_id+ " is added");
                    //TcpConnectionManager.addClient(clientSocket.getInetAddress().getHostAddress(),clientSocket,true,false);
                    NewSocket.clients_tcp_index++;
                }
                System.out.println("Client: " + clientSocket.getInetAddress() + " is connected by TCP"
                        + " & index: " + (NewSocket.clients_tcp_index - 1));
            }

            this.receiverTcp = new Server_Tcp(clientSocket,this ,receivedMessagesArea);
        }

        @Override
        public void run() {
        	
            try {
            	
                // TCP Ack 수신  시작
                receiverTcp.startReceiving();

                
                
            }catch (IOException e) {
                System.out.println("IOException in ClientHandler: " + clientSocket.getInetAddress().getHostAddress() );
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in ClientHandler: " + clientSocket.getInetAddress().getHostAddress());
                e.printStackTrace();
                
            } finally {
                // while문을 빠져나오면 Handler를 종료함
                
                stopTCPCheck();
                stopHandler();
            }
            
            
            
        }

        // ClientHandler 스레드를 종료하는 메소드 
        public void stopHandler() {
            
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
        public void stopTCPCheck() {

        	tcpCheck.stopChecking();
        }
    }

}
