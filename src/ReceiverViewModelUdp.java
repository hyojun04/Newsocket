import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JTextArea;

public class ReceiverViewModelUdp {

    private static final int PORT = 1996;
    private static final int BUFFER_SIZE = 1024;
    private static final boolean MESSAGE_NUM = true;
    private static final boolean PACKET_NUM = false;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private static int receive_message_num = 0;
    private volatile boolean newMessageReceived_udp = false;
    public int receivedMessageNum; 
    public static int numberStr_message;
    public static int numberStr_packet;
    //private static int checkSerial; //받은 UDP 메시지가 몇 번째 메시지인지 저장
    
    //받은 UDP 메시지가 몇 번째 메시지인지 저장하고 있는 checkSerial 변수를 출력하는 메소드
    /*public int Print_checkSerial() {
    	return checkSerial;
    }*/
    
    // 전체 패킷 수와 수신 여부를 추적하는 배열
    private static final int TOTAL_PACKETS = 61; // 전체 패킷 수 (필요에 맞게 수정)
    private boolean[] checkSerial = new boolean[TOTAL_PACKETS + 1]; // 인덱스 0은 사용하지 않음
    private int receivedPacketCount = 0;
    
    // 생성자에서 JTextArea 전달 받음
    public ReceiverViewModelUdp(JTextArea receivedMessagesArea) {
        this.receivedMessagesArea = receivedMessagesArea;
    }
    
    public void reset_message_num() {
        receive_message_num = 0;
    }
    
    public boolean hasNewMessage() {
        return newMessageReceived_udp;
    }
    
    public void resetNewMessageFlag() {
        newMessageReceived_udp = false;
    }
    
    // getLeading을 true로 주면 "_"를 기준으로 앞의 숫자를, false로 주면 뒤의 숫자를 return함
    public static int extractNumberPart(String input, boolean getLeading) { 
        // 정규식을 사용하여 숫자와 `_`를 기준으로 문자열을 분리
        String[] parts = input.split("_");

        if (parts.length == 2) {
            String leadingNumber = parts[0].replaceAll("\\D", ""); // 앞부분 숫자만 추출
            String trailingNumber = parts[1].replaceAll("\\D", ""); // 뒷부분 숫자만 추출
            
            // getLeading이 true일 경우 앞부분 숫자 반환, false일 경우 뒷부분 숫자 반환
            String numberString = getLeading ? leadingNumber : trailingNumber;
            
            // 빈 문자열을 처리하여 int로 변환
            return numberString.isEmpty() ? 0 : Integer.parseInt(numberString);
        } else {
            // 형식이 맞지 않을 경우 0 반환
            return 0;
        }
    }

    public void startServer() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("UDP Server started on port " + PORT + ". Waiting for messages...");

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

                    // 송신자 IP 주소 가져오기
                    InetAddress senderAddress = receivePacket.getAddress();
                    String senderIP = senderAddress.getHostAddress();

                    // 현재 시간을 hh:mm:ss.SSS 형식으로 가져오기
                    String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

                    // 메시지의 앞부분 10글자만 잘라서 표시
                    String truncatedMessage = receivedMessage.length() > 10
                            ? receivedMessage.substring(0, 10)
                            : receivedMessage;

                    // 수신 메시지 GUI에 표시
                    receive_message_num++;
                    
                    
                    receivedMessagesArea.append("[" + receive_message_num + "] Received UDP message from " + senderIP + ": " + truncatedMessage + " [" + timeStamp + "]\n");                    
                    System.out.println("I got Message: " + truncatedMessage);

                    numberStr_packet = extractNumberPart(truncatedMessage, PACKET_NUM);
                    numberStr_message = extractNumberPart(truncatedMessage, MESSAGE_NUM);
                    
                    if (numberStr_packet != 0 ) {                      
                        //새로운 패킷이 왔을 때
                        if (numberStr_packet > 0 && numberStr_packet <= TOTAL_PACKETS && !checkSerial[numberStr_packet]) {
                        	checkSerial[numberStr_packet] = true; // 해당 패킷을 수신한 것으로 표시
                            System.out.println("packet["+numberStr_packet+"]가 ture로 바뀜");
                            receivedPacketCount++; // 새 패킷 수신 시 count 증가
                            System.out.println("receivedPacketCount:"+receivedPacketCount);
                            //패킷이 모두 도착했을 때
                            if (receivedPacketCount == TOTAL_PACKETS) {
                            	receivedMessageNum ++;
                            	receivedPacketCount = 0;
                            	Arrays.fill(checkSerial, false);
                            }
                            synchronized (this) {
                                newMessageReceived_udp = true; // 새로운 메시지를 받았을 경우
                                System.out.println("newMessageReceived_udp set to true");
                                notifyAll(); // 상태가 바뀌었으므로 대기 중인 스레드에게 알림
                            }
                        }
                         
                        //한 번 이상 받은 메시지번호를 받으면 에코메시지를 보내지 않음
                        else {
                        	 // newMessageReceived_udp 상태 업데이트 및 notifyAll() 호출
                            synchronized (this) {
                                newMessageReceived_udp = false; // 새로운 메시지를 받았을 경우
                                System.out.println("newMessageReceived_udp set to false because same serialNumber was coming");
                                
                             }
                         }
                        
                        
                    } else {
                        System.out.println("No leading numbers found.");
                    }

                   

                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format in received message: " + e.getMessage());
                } catch (SocketException e) {
                    System.out.println("Socket error occurred: " + e.getMessage());
                    break; // 소켓에 문제가 생기면 루프를 탈출하여 서버를 중단합니다.
                } catch (SecurityException e) {
                    System.out.println("Security exception: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Illegal argument: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error while receiving data: " + e.getMessage());
                    e.printStackTrace(); // 추가적인 오류 로그를 출력하여 문제를 더 정확히 파악할 수 있게 합니다.
                }
            }
        } catch (SocketException e) {
            System.out.println("Failed to bind UDP socket to port " + PORT + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("UDP Server socket closed.");
            }
        }
    }
    
    public String startConnect_to_tcp() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("UDP Server started on port " + PORT + ". Waiting for connect to tcp...");
            
            // 버퍼 생성
            byte[] buffer = new byte[BUFFER_SIZE];

            // 수신할 패킷 생성
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // 데이터 수신 (메시지가 올 때까지 대기)
            socket.receive(receivePacket);

            // 송신자 IP 주소 가져오기
            InetAddress senderAddress = receivePacket.getAddress();
            String senderIP = senderAddress.getHostAddress();

            // IP 주소가 유효하면 반환, 유효하지 않으면 null 반환
            if (senderIP != null && !senderIP.isEmpty()) {
                System.out.println("This is serverIP: " + senderIP);
                return senderIP;
            } else {
                System.out.println("No server IP");
                return null;
            }
        } catch (SocketException e) {
            System.out.println("Failed to bind UDP socket to port " + PORT + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 메시지를 수신한 후 소켓을 닫아 더 이상 메시지를 받지 않도록 함.
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("UDP Server socket closed.");
            }
        }
        return null; // 메시지를 수신하지 못했거나 오류 발생 시 null 반환
    }

}
