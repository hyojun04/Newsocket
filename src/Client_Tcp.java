import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client_Tcp {
    
    private Socket socket;
    private PrintWriter out = null;
    private DataOutputStream dataOutputStream = null;
    private BufferedReader in = null; // 수신용 BufferedReader 추가
    private DataInputStream dataInputStream = null; // 바이트 수신용 DataInputStream 추가
    
    // Socket을 파라미터로 받는 생성자
    public Client_Tcp(Socket socket) {
        this.socket = socket;
        try {
            // PrintWriter 초기화
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            // byteArray를 위해 추가함
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            
            /*Reset을 위한 tcp 수신*/
            // 수신 스트림 초기화
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataInputStream = new DataInputStream(socket.getInputStream());
            // 수신 스레드 시작
            startResetReceiving();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TCP 메시지를 전송하는 메서드
    public void sendMessage_tcp(String message) {
        try {
            // 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
            String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

            // 메시지 전송 + 타임스탬프 추가
            out.println(message + " From Window " + "[" + timeStamp + "]");
            //System.out.println(message + " Ack message gets sent");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // byte배열의 TCP check 메시지를 전송하는 메서드
    public void sendMessage_tcp(byte[] byteArray) {
        try {
            // 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
            String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

            // 메시지 전송 + 타임스탬프 추가
            // byte 배열의 길이를 먼저 전송
            dataOutputStream.writeInt(byteArray.length);
            // byte 배열 전송
            dataOutputStream.write(byteArray);
            // String 메시지 전송
            dataOutputStream.writeUTF(" From Window " + "[" + timeStamp + "]");
            
            //System.out.println(byteArray + " Ack message gets sent");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 소켓 종료 메서드
    public void closeSocket() {
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                //System.out.println("TCP socket is closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 // 메시지 수신을 위한 스레드 시작 메서드
    private void startResetReceiving() {
        Thread receiveResetThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) { // EOF를 확인하며 수신
                    //System.out.println("Received message: " + message);
                }
                //System.out.println("Server closed the connection.");
                //Message num 초기화
                ReceiverViewModelUdp.receivedMessageNum =1;
                NewSocket.receiver_udp.resetUDPreceiving();
                NewSocket.receiver_udp = null;
            } catch (IOException e) {
                //System.out.println("Connection closed.");
            }
        });
        receiveResetThread.start();
    }
}
