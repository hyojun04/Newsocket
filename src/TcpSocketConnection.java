import java.io.*;
import java.net.Socket;

public class TcpSocketConnection {
    private static final int PORT = 1995;
    private Socket socket;
    private SenderViewModel sender; // SenderViewModel 인스턴스

    public void startClient(String serverIP) {
        try {
            socket = new Socket(serverIP, PORT);
            // SenderViewModel 인스턴스 생성
            sender = new SenderViewModel(socket);
            System.out.println("Client: " + serverIP + " is connected by TCP" + " & index: " + NewSocket.clients_tcp_index);

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

    // 소켓을 외부에서 접근할 수 있도록 getter 추가
    public Socket getSocket() {
        return socket;
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
