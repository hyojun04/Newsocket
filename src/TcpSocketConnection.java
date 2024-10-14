import java.io.*;
import java.net.Socket;


public class TcpSocketConnection {
	private static final int PORT = 1995;
	public int perminent_id; //에코메시지 배열 인덱스와 연결하기 위한 고유 번호 
	
	public void startClient(String serverIP) {

		Socket socket = null;
		
		
		try {
			socket = new Socket(serverIP, PORT);
			
			if(NewSocket.clients_tcp_index == 0)
			{	
				NewSocket.clients_tcp.set(NewSocket.clients_tcp_index, false);  // 초기 인덱스는 false로 초기
				perminent_id = NewSocket.clients_tcp_index;
				System.out.println("Client: " + serverIP + "is connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++;
			}
			else {
				NewSocket.clients_tcp.add(NewSocket.clients_tcp_index, false);  // 다음 인덱스는 false로 초기
				perminent_id = NewSocket.clients_tcp_index;
				System.out.println("Client: " + serverIP + "is connected by TCP"+ " & index: " + NewSocket.clients_tcp_index);
				NewSocket.clients_tcp_index++; // index값 = client수 + 1 
				
				
			}
			
	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				if (socket != null)
					socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
