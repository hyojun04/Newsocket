import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;

public class ReceiverViewModelUdp {

    private static final int PORT = 1995;
    private static final int BUFFER_SIZE = 1024;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private static int receive_massage_num = 0;
    private volatile boolean newMessageReceived = false;
    
    // 생성자에서 JTextArea 전달 받음
    public ReceiverViewModelUdp(JTextArea receivedMessagesArea) {
        this.receivedMessagesArea = receivedMessagesArea;
    }
    
    public void reset_message_num() { // 창을 clear 하면 meaage
    	receive_massage_num = 0;
    }
    
    public boolean hasNewMessage() {
        return newMessageReceived;
    }
    public void resetNewMessageFlag() {
    	newMessageReceived = false;
    }
    
    public void startServer() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("UDP Server " + " get started in Port."+PORT +" Waiting for Messages...");

            // 무한 루프로 메시지 계속 수신
            while (true) {
                try {
                    // 버퍼 생성
                    byte[] buffer = new byte[BUFFER_SIZE];

                    // 수신할 패킷 생성
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                    // 데이터 수신
                    socket.receive(receivePacket);

                    // 수신된 데이터 처리
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    // 클라이언트 IP 주소 가져오기
                    InetAddress clientAddress = receivePacket.getAddress();
                    String clientIP = clientAddress.getHostAddress();

                    // 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
                    String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

                    // 메시지의 앞부분 20글자만 잘라서 표시
                    String truncatedMessage = receivedMessage.length() > 20
                            ? receivedMessage.substring(0, 20) + "..."
                            : receivedMessage;

                    // 수신 메시지 GUI에 표시
                    receive_massage_num ++;
                    receivedMessagesArea.append("["+receive_massage_num+"]수신된 메시지 from " + clientIP + ": " + truncatedMessage + " [" + timeStamp + "]\n");                    
                    System.out.println("I got Message : " + truncatedMessage );
                    
                    if(receivePacket != null) newMessageReceived = true; //메시지 수 받았을 경우
                    
                    
                } catch (Exception e) {
                    System.out.println("데이터 수신 중 오류 발생: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    
    
}
