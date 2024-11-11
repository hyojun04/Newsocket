import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;

import javax.swing.JTextArea;

public class TcpConnectionAccepter implements Runnable {
    private static final int PORT = 8189; // ������ ��Ʈ
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

            // Ŭ���̾�Ʈ ������ ����ϸ鼭, �� ���ῡ ���� ���ο� �����带 ����            
            
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Ŭ���̾�Ʈ ���� ����
                
                /*UDP broad�� Server�� IP �����ϴ� ��Ŀ���� �߰�*/
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientIP+"\n");
                consoleArea.append("Client connected: " + clientIP+"\n");
              
                // �� Ŭ���̾�Ʈ�� ���� ���ο� �ڵ鷯 �����带 ����
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, receivedMessagesArea);
                new Thread(clientHandler).start();
            }
            //consoleArea.append("TCP ���� ����Ϸ� \n");
            //System.out.println("All TCP Sockets are connnected");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable {
        private final Server_Tcp receiverTcp;
        public int permanent_id;
        private Socket clientSocket;
        private boolean running = true; // Socket�� �����ִ��� ��Ÿ���� ����
        
        private StartTCPCheckThread tcpCheckThread;
        private Thread checkThread;
        
        public ClientHandler(Socket clientSocket, TcpConnectionAccepter tcpAccepter, JTextArea receivedMessagesArea) {
            this.clientSocket = clientSocket;

            // �ε����� �����ϰ�, ����� Ŭ���̾�Ʈ�� ó���� �� �ֵ��� ����
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
                // TCP Ack �޽����� �����Ͽ����� üũ�ϴ� ������ ����
            	tcpCheckThread = new StartTCPCheckThread(receiverTcp, this);
                checkThread = new Thread(tcpCheckThread);
                checkThread.start();
                
                // TCP Ack ����  ����
                receiverTcp.startReceiving();

                // Check for socket status in a loop
                while (running) {
                    if (clientSocket.isClosed() || !clientSocket.isConnected()) {
                        System.out.println("Client socket is closed or disconnected. Stopping handler for client: " + clientSocket.getInetAddress());
                        break; // Exit the loop if the socket is closed or disconnected
                    }

                    // ������ ���⵵�� ���߱����� 1�ʸ��� �˻�
                    Thread.sleep(1000);
                }
                
            }catch (IOException e) {
                System.out.println("IOException in ClientHandler: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in ClientHandler: " + e.getMessage());
                e.printStackTrace();
                
            } finally {
                // while���� ���������� Handler�� ������
                
                stopTCPCheckThread();
                stopHandler();
            }
        }

        // ClientHandler �����带 �����ϴ� �޼ҵ� 
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
        	//TCPCheckThread�� �ߴܽ�Ŵ
        	tcpCheckThread.stopThread();
        }
    }

}
