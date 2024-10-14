import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ReceiverViewModel {
    private Socket socket;
    private static final int PORT = 1997;
   
    private volatile boolean newEchoReceived_tcp = false; // 에코 메시지 수신 여부

    public ReceiverViewModel(Socket socket) {
        this.socket = socket;
    }

    public boolean hasNewEchoMessage() {
        return newEchoReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newEchoReceived_tcp = false;
    }

    public void startReceiving() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            String clientIP = socket.getInetAddress().getHostAddress();

            // 클라이언트와의 연결을 유지하면서 메시지를 지속적으로 수신
            while (!socket.isClosed()) {
                try {
                    Object receivedObject = in.readObject();
                    if (receivedObject instanceof String) {
                        String receivedMessage = (String) receivedObject;

                        // 수신된 메시지 처리
                        System.out.println("수신된 메시지 from " + clientIP + ": " + receivedMessage);
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

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
