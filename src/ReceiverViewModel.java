import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiverViewModel {
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private static final int PORT = 1995;
	public int perminent_id; //에코메시지 배열 인덱스와 연결하기 위한 고유 번호 
    private volatile boolean newEchoReceived_tcp = false; // 에코 메시지 수신 여부

    public boolean hasNewEchoMessage() {
        return newEchoReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newEchoReceived_tcp = false;
    }

    public void startServer() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
        	
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for connection...");
            socket = serverSocket.accept();
            System.out.println("Connected completely.");
            
            if(NewSocket.clients_tcp_index == 0)
			{	
				NewSocket.clients_tcp.set(NewSocket.clients_tcp_index, false);  // 초기 인덱스는 false로 초기
				perminent_id = NewSocket.clients_tcp_index;
				System.out.println("connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++;
			}
			else { //index가 0이 아니면 배열을 늘림 
				NewSocket.clients_tcp.add(NewSocket.clients_tcp_index, false);  // 다음 인덱스는 false로 초기
				perminent_id = NewSocket.clients_tcp_index;
				System.out.println("connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++; // index값 = client수 + 1 
				
				
			}
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            String clientIP = socket.getInetAddress().getHostAddress();
            String hostIP = InetAddress.getLocalHost().getHostAddress();
            
            
            
            // 클라이언트와의 연결을 유지하면서 메시지를 지속적으로 수신
            while (!socket.isClosed()) {
                try {
                    Object receivedObject = in.readObject();
                    if (receivedObject instanceof String) {
                        String receivedMessage = (String) receivedObject;

                        // 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
                        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

                        System.out.println("수신된 메시지 from " + clientIP + ": " + receivedMessage + " [" + timeStamp + "]");

                        // 새로운 에코 메시지가 수신되었음을 표시
                        newEchoReceived_tcp = true;

                        out.flush(); 
                    }
                } catch (EOFException e) {
                    System.out.println("Client Connection is disconnected.");
                    break; // 클라이언트가 연결을 종료했을 때 while 루프를 탈출
                } catch (SocketException e) {
                    System.out.println("Connection gets reset: " + e.getMessage());
                    break; // 연결이 리셋되었을 때 while 루프를 탈출
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break; // 기타 오류가 발생했을 때 while 루프를 탈출
                }
            }
            
            
            
            
        } catch (BindException e) {
            System.out.println("The port is already used: " + e.getMessage());
            try {
                Thread.sleep(1000); // 1초 대기
                serverSocket = new ServerSocket(PORT); // 다시 시도
            } catch (InterruptedException | IOException ie) {
                ie.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
