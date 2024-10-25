import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;

public class ReceiverViewModelUdp {

    private static final int PORT = 1996;
    private static final int BUFFER_SIZE = 1024;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private static int receive_message_num = 0;
    private volatile boolean newMessageReceived_udp = false;
    public int receivedMessageNum; 
    private static int checkSerial; //받은 UDP 메시지가 몇 번째 메시지인지 저장
    
    //받은 UDP 메시지가 몇 번째 메시지인지 저장하고 있는 checkSerial 변수를 출력하는 메소드
    public int Print_checkSerial() {
    	return checkSerial;
    }
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
    public static String extractLeadingNumbers(String input) {
        // 정규식을 사용해 문자열의 앞부분에서 숫자만 추출
        StringBuilder numberStr = new StringBuilder();
        
        // 문자열을 하나씩 검사해서 숫자인 경우만 numberStr에 추가
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                numberStr.append(c);
            } else {
                // 숫자가 아닌 문자를 만나면 반복을 중지하고 숫자 부분만 반환
                break;
            }
        }
        
        // 숫자가 있으면 문자열 형태로 반환하고, 없으면 null 반환
        return numberStr.length() > 0 ? numberStr.toString() : null;
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

                    // 송신자 IP 주소 가져오기
                    InetAddress senderAddress = receivePacket.getAddress();
                    String senderIP = senderAddress.getHostAddress();

                    // 로컬 IP에서 보낸 메시지인지 검사
                    if (isMessageFromLocalAddress(senderAddress)) {
                        System.out.println("Ignored message from local IP: " + senderIP);
                        continue; // 메시지를 무시하고 다음 메시지로 넘어감
                    }

                    // 수신된 데이터 처리
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

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

                    // 문자열을 정수로 변환 및 처리 (기존 로직 유지)
                    String numberStr = extractLeadingNumbers(truncatedMessage);
                    if (numberStr != null) {
                        int number = Integer.parseInt(numberStr);
                        System.out.println("Extracted number: " + number);
                        System.out.println("CheckSerial number: " + checkSerial);

                        if (checkSerial == number) {
                            synchronized (this) {
                                newMessageReceived_udp = false;
                                System.out.println("newMessageReceived_udp set to false because same serialNumber was coming");
                            }
                        } else if (checkSerial != number) {
                            receivedMessageNum = number;
                            checkSerial++;
                            synchronized (this) {
                                newMessageReceived_udp = true;
                                System.out.println("newMessageReceived_udp set to true");
                                notifyAll();
                            }
                        }
                    } else {
                        System.out.println("No leading numbers found.");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format in received message: " + e.getMessage());
                } catch (SocketException e) {
                    System.out.println("Socket error occurred: " + e.getMessage());
                    break;
                } catch (SecurityException e) {
                    System.out.println("Security exception: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Illegal argument: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error while receiving data: " + e.getMessage());
                    e.printStackTrace();
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

 // 메소드: 로컬 IP 주소와 일치하는지 확인
    private boolean isMessageFromLocalAddress(InetAddress senderAddress) {
        try {
            // 로컬 호스트의 IP 주소 가져오기
            InetAddress localAddress = InetAddress.getLocalHost();
            
            // 송신자의 IP 주소와 로컬 호스트의 IP 주소가 동일한지 비교
            return senderAddress.equals(localAddress);
        } catch (Exception e) {
            System.out.println("Error checking local address: " + e.getMessage());
            return false;
        }
    }
}

