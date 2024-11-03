import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client_Tcp {
    
	private Socket socket;
	private PrintWriter out = null;
	
	// Socket을 파라미터로 받는 생성자
	public Client_Tcp(Socket socket) {
		this.socket = socket;
	}

	// TCP 메시지를 전송하는 메서드
	public void sendMessage_tcp(String message) {
		try {
			// PrintWriter를 사용하여 서버로 메시지를 전송
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

			// 에코 메시지 구현 
			

			// 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
			String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
			

			// 메시지 전송 + 타임스탬프 추가
			out.println("From Window " + "[" + timeStamp + "]" + message);
			System.out.println(message+ "Echo message gets sent");

		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
