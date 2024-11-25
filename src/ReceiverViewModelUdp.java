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
    private static final int BUFFER_SIZE = 1024; // 여유롭게 설정
    private static final int TOTAL_PACKETS = 63; // 전체 패킷 수 (필요에 맞게 수정)
    private static final boolean MESSAGE_NUM = true;
    private static final boolean PACKET_NUM = false;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private static int receive_message_num = 0;
    private volatile boolean newMessageReceived_udp = false;
    public static int receivedMessageNum = 1; //현재 받고 있는 메시지 번호
    public static int array_index= 0;
    public static int ignored_bits = 0;
    private static int checkSerial; //받은 UDP 메시지가 몇 번째 메시지인지 저장
   
    private DatagramSocket socket;
   
    public byte[] checkNewMessage; // 받은 패킷을 체크하는 배열
    public byte[] lastMessage; // 이전 배열(배열에 변화가 생겼을 때만 ack 전송)
    public byte[][] imageData; // 패킷 데이터를 저장할 배열
   
    //받은 UDP 메시지가 몇 번째 메시지인지 저장하고 있는 checkSerial 변수를 출력하는 메소드
    public int Print_checkSerial() {
    	return checkSerial;
    }
    // 생성자에서 JTextArea 전달 받음
    public ReceiverViewModelUdp(JTextArea receivedMessagesArea) {
        this.receivedMessagesArea = receivedMessagesArea;
        initializePacketTracking();
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

     
            String leadingNumber = parts[0].replaceAll("\\D", ""); // 앞부분 숫자만 추출
           
            String trailingNumber = parts[1].replaceAll("\\D", ""); // 뒷부분 숫자만 추출
           
            // getLeading이 true일 경우 앞부분 숫자 반환, false일 경우 뒷부분 숫자 반환
            String numberString = getLeading ? leadingNumber : trailingNumber;
           
            // 빈 문자열을 처리하여 int로 변환
            return numberString.isEmpty() ? 0 : Integer.parseInt(numberString);
            
        
    }
   
    private void initializePacketTracking() {
        array_index = (TOTAL_PACKETS + 7) / 8;
        ignored_bits = 8 - (TOTAL_PACKETS % 8);
        checkNewMessage = new byte[array_index];
        imageData = new byte[TOTAL_PACKETS][]; // 이미지 데이터 저장

        // 무시할 비트 초기화
        for (int i = array_index * 8 - ignored_bits + 1; i <= array_index * 8; i++) {
            SetNewMsgBit(i);
        }
        lastMessage = checkNewMessage.clone();
    }


    public void startServer() {
        try {
            socket = new DatagramSocket(PORT);

            // 무한 루프로 메시지 계속 수신
            while (true) {
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                    // 데이터 수신
                    socket.receive(receivePacket);

                    // 수신된 데이터 처리
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    InetAddress senderAddress = receivePacket.getAddress();
                    String senderIP = senderAddress.getHostAddress();
                    String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
                    String truncatedMessage = receivedMessage.length() > 10
                            ? receivedMessage.substring(0, 10)
                            : receivedMessage;

                    receive_message_num++;
                    receivedMessagesArea.append("[" + receive_message_num + "] Received UDP message from " + senderIP + ": " + truncatedMessage + " [" + timeStamp + "]\n");

                    // 메시지 번호와 패킷 번호 추출
                    int message_num = extractNumberPart(truncatedMessage, MESSAGE_NUM);
                    int packet_num = extractNumberPart(truncatedMessage, PACKET_NUM);

                    // 유효한 패킷 번호인지 확인
                    if (packet_num > 0 && packet_num <= TOTAL_PACKETS) {
                        int headerLength = Integer.toString(message_num).length() + Integer.toString(packet_num).length() + 2; // "_" 문자 2개 포함
                        byte[] imagePacketData = Arrays.copyOfRange(receivePacket.getData(), headerLength, receivePacket.getLength());
                        
                        // 패킷 번호가 10 이상이면 크기 조정
                        if (packet_num >= 10) {
                            byte[] extendedPacketData = new byte[imagePacketData.length + 1]; // 기존 크기 + 1
                            System.arraycopy(imagePacketData, 0, extendedPacketData, 0, imagePacketData.length); // 기존 데이터 복사
                            extendedPacketData[extendedPacketData.length - 1] = 0; // 마지막에 빈 공간(0) 추가
                            imagePacketData = extendedPacketData; // 크기 조정된 배열로 업데이트
                        }
                        if (receivedMessageNum == message_num) {
                            SetNewMsgBit(packet_num);
                            imageData[packet_num - 1] = imagePacketData; // 이미지 데이터 저장
                        }
                    } else {
                        System.err.println("Invalid packet number: " + packet_num);
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format in received message: " + e.getMessage());
                } catch (SocketException e) {
                    System.err.println("Socket error occurred: " + e.getMessage());
                    break;
                } catch (SecurityException e) {
                    System.err.println("Security exception: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Illegal argument: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            System.err.println("Failed to bind UDP socket to port " + PORT + ": " + e.getMessage());
        } catch (Exception e) {
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
            //System.out.println("UDP Server started on port " + PORT + ". Waiting for connect to tcp...");
           
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
                //System.out.println("This is serverIP: " + senderIP);
                return senderIP;
            } else {
                //System.out.println("No server IP");
                return null;
            }
        } catch (SocketException e) {
            //System.out.println("Failed to bind UDP socket to port " + PORT + ": " + e.getMessage());
        } catch (Exception e) {
            //System.out.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 메시지를 수신한 후 소켓을 닫아 더 이상 메시지를 받지 않도록 함.
            if (socket != null && !socket.isClosed()) {
                socket.close();
                //System.out.println("UDP Server socket closed.");
            }
        }
        return null; // 메시지를 수신하지 못했거나 오류 발생 시 null 반환
    }
   
    // 수신한 패킷 번호에 맞는 bit를 set 시킴
    public void SetNewMsgBit(int packet_num) {
            // packet_num의 위치에 해당하는 비트를 설정(0번째 비트부터 채움)
            int byteIndex = (packet_num-1) / 8;   // 해당 비트가 속한 바이트 인덱스
            int bitIndex = (packet_num-1) % 8;    // 해당 바이트 내의 비트 위치

            // 해당 바이트 내에서 bitIndex 위치의 비트가 0인지 1인지 확인
            if ((checkNewMessage[byteIndex] & (1 << bitIndex)) == 0) {
                // 비트가 0이라면 1로 설정
            	checkNewMessage[byteIndex] |= (1 << bitIndex);
                //System.out.println("Set checkNewMessage[" + packet_num + "]:");
                StartUDPCheckThread.printByteArrayAsBinary(checkNewMessage); //배열 출력    
            } else {
                // 이미 비트가 1인 경우
                //System.out.println("checkNewMessage[" + packet_num + "] is already set to 1.");
              }
           }
   
    //Server에서 Reset 요청이 올 때
    public void resetUDPreceiving() {
    	socket.close();
    }
  }            