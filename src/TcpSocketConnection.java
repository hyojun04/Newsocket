import java.io.*;
import java.net.Socket;

public class TcpSocketConnection {
    private static final int PORT = 8189;
    private Socket socket;
    private Client_Tcp sender; // Client_Tcp 인스턴스
    
    public int permanent_id; // 에코 메시지 배열 인덱스와 연결하기 위한 고유 번호 
    
    public void startClient(String serverIP) {
        try {
            socket = new Socket(serverIP, PORT);
            sender = new Client_Tcp(socket);
            //receiver = new Server_Tcp(socket); // Server_Tcp 인스턴스 생성
            /* 여러 클라이언트를 받으려면 for루프를 통해서 소켓을 생성 */
            permanent_id = NewSocket.clients_tcp_index;
            System.out.println("Client: " + serverIP + " is connected by TCP" + " & index: " + NewSocket.clients_tcp_index);
            if(NewSocket.clients_tcp_index == 0)
			{	
				NewSocket.clients_tcp.set(NewSocket.clients_tcp_index, false);  // 초기 인덱스는 false로 초기
				permanent_id = NewSocket.clients_tcp_index;
				System.out.println("connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++;
			}
			else { //index가 0이 아니면 배열을 늘림 
				NewSocket.clients_tcp.add(NewSocket.clients_tcp_index, false);  // 다음 인덱스는 false로 초기
				permanent_id = NewSocket.clients_tcp_index;
				System.out.println("connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++; // index값 = client수 + 1 
				
				
			}
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    

    // TCP 에코 메시지를 전송하는 메서드
    public void sendEchoMessage(String message) {
        if (sender != null) {
            sender.sendMessage_tcp(message); // SenderViewModel을 사용하여 메시지 전송
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
