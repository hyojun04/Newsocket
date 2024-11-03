import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import javax.swing.JTextArea;

public class Server_Tcp {
    /* 메시지를 받기만 하는 기능 구현 */
    private Socket socket;
    private static final int PORT = 8189;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private volatile boolean newAckReceived_tcp = false; // 에코 메시지 수신 여부
    private int receive_message_num = 0;

    public static byte[] checkNewMessage;
    // 생성자에서 JTextArea 전달 받음
    public Server_Tcp(Socket socket, JTextArea receivedMessagesArea) {
        this.socket = socket;
        this.receivedMessagesArea = receivedMessagesArea;
    }
    
    public void reset_message_num() {
        receive_message_num = 0;
    }
    
    public boolean hasNewEchoMessage() {
        return newAckReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newAckReceived_tcp = false;
    }

    public void startReceiving() throws IOException { //예외 throw하여 ClientHandler에서 처리하도록함
        BufferedReader in = null;
        PrintWriter out = null;
        // 데이터 수신을 위한 DataInputStream 사용
        DataInputStream dataInputStream = null;
        try {
            // BufferedReader를 사용하여 데이터를 송수신
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            dataInputStream =new DataInputStream(socket.getInputStream());
            
            // byte 배열 수신
            int byteArrayLength = dataInputStream.readInt(); // 먼저 수신할 배열의 길이를 읽음
            byte[] receivedData = new byte[byteArrayLength];
            dataInputStream.readFully(receivedData); // byte 배열 수신
            
            
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("Server_TCP is open");

            // 클라이언트와의 연결을 유지하면서 메시지를 지속적으로 수신
            String receivedMessage = dataInputStream.readUTF();
            while (!socket.isClosed() && (receivedMessage) != null) {
                // 수신된 메시지 처리
                receive_message_num++;
                receivedMessagesArea.append("[" + receive_message_num + "] 수신된 메시지 from " + clientIP + ": " + receivedMessage + "\n");
             // 수신된 데이터 출력
                System.out.println("Received byte array: " + Arrays.toString(receivedData));                
                System.out.println("수신된 메시지 from " + clientIP + ": " + receivedMessage);
                
                synchronized (this) {
                	checkNewMessage = receivedData;
                    newAckReceived_tcp = true; // 에코 메시지를 받았을 경우
                    System.out.println("newAckMessage was coming");
                    notifyAll();
                }
            }
        } finally {
            // Exception을 throw하여 외부에서 처리하도록 함.
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("TCP 소켓이 닫혔습니다.");
        }
    }
}
