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

                    // 문자열을 정수로 변환
                    String numberStr = extractLeadingNumbers(truncatedMessage);
                    
                    if (numberStr != null) {
                        int number = Integer.parseInt(numberStr);
                        
                        
                        System.out.println("Extracted number: " + number);
                        System.out.println("CheckSerial number: " + checkSerial);
                        
                        //한 번 이상 받은 메시지번호를 받으면 에코메시지를 보내지 않음
                        if(checkSerial == number) {
                        	 // newMessageReceived_udp 상태 업데이트 및 notifyAll() 호출
                            synchronized (this) {
                                newMessageReceived_udp = false; // 새로운 메시지를 받았을 경우
                                System.out.println("newMessageReceived_udp set to false because same serialNumber was coming");
                                
                            }
                        }
                        // 다음 번호의 메시지가 올 때
                        else if(checkSerial != number) {
                        	receivedMessageNum = number;
                        	checkSerial++; //ex)처음 0으로 초기화 되어있는 checkSerial을 에코메시지를 보내고 난 다음 1번째의 메시지를 받았다는 의미
                        	 // newMessageReceived_udp 상태 업데이트 및 notifyAll() 호출
                            synchronized (this) {
                                newMessageReceived_udp = true; // 새로운 메시지를 받았을 경우
                                System.out.println("newMessageReceived_udp set to true");
                                notifyAll(); // 상태가 바뀌었으므로 대기 중인 스레드에게 알림
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
