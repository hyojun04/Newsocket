import java.util.Arrays;
public class StartUDPCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;

    public StartUDPCheckThread(ReceiverViewModelUdp receiver_udp, TcpSocketConnection tcpConnection) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
        
    }

    @Override
    public void run() {
        while (true) {
            
        	try {

                // 새로운 메시지가 수신된 경우
                System.out.println("StartUDPCheckThread is awake");
                
                if(!Arrays.equals(ReceiverViewModelUdp.checkNewMessage, ReceiverViewModelUdp.lastMessage)) { // checkNewMessage 배열에 변화가 생겼을 때만 ack 전송
                	
                	System.out.println( "Array changed");
                	//byte배열만 Ack메시지로 보내게된다. sendAckMessage는 재정의되어있음
                	tcpConnection.sendAckMessage(ReceiverViewModelUdp.checkNewMessage);
                	System.out.println( "SendAck:");               
                	printByteArrayAsBinary(ReceiverViewModelUdp.checkNewMessage); // 보낸 ack내용 출력
                	
                	
                	
                	//현재 모든 패킷을 받았는지 확인
                	int totalBits = ReceiverViewModelUdp.array_index * 8 - ReceiverViewModelUdp.ignored_bits; // 체크할 총 비트 수
                	int totalBytes = (totalBits + 7) / 8; // 체크할 총 바이트 수, 8로 나눈 나머지가 있으면 +1

                	// 패킷 수가 8의 배수가 아닌 경우 무시해야 하는 bit(ignore_bit) 자리를 1로 채움
                	byte[] shiftedMessage = new byte[totalBytes];
                	for (int i = 0; i < totalBytes; i++) {
                	    // 해당 바이트를 왼쪽으로 ignored_bits 만큼 쉬프트
                	    int index = i < ReceiverViewModelUdp.array_index ? i : ReceiverViewModelUdp.array_index - 1; // 마지막 바이트는 인덱스를 조정
                	    // 왼쪽으로 쉬프트 후 오른쪽 비트는 1로 채움
                	    shiftedMessage[i] = (byte) ((ReceiverViewModelUdp.checkNewMessage[index] << ReceiverViewModelUdp.ignored_bits) | 
                	        ((1 << ReceiverViewModelUdp.ignored_bits) - 1)); // 오른쪽 비트를 모두 1로 채우기
                	}

                	// 배열의 모든 비트가 1인지 확인
                	boolean allBitsOne = true;
                	for (byte b : shiftedMessage) {
                	    if (b != (byte) 0xFF) { // 만약 한 바이트라도 0xFF가 아니라면
                	        allBitsOne = false;
                	        break;
                	    }
                	}

                	if (allBitsOne) {
                	    System.out.println("all packet received");
                	    Arrays.fill(ReceiverViewModelUdp.checkNewMessage, (byte) 0); // checkNewMessage 배열을 0으로 초기화
                	    ReceiverViewModelUdp.receivedMessageNum++; // 다음 메시지를 받을 준비
                	    System.out.println("receivedMessageNum: " + ReceiverViewModelUdp.receivedMessageNum);
                	}
                	
                	// 배열의 내용을 복사하여 lastMessage에 저장
                    ReceiverViewModelUdp.lastMessage = Arrays.copyOf(ReceiverViewModelUdp.checkNewMessage, ReceiverViewModelUdp.checkNewMessage.length);
                
                	System.out.println( " is transmitted");
                	System.out.println("(1) UDP Message state: " + receiver_udp.hasNewMessage());

                	// 플래그 초기화
                	receiver_udp.resetNewMessageFlag();
                	System.out.println("(2) UDP Message state: " + receiver_udp.hasNewMessage());
                
                }
                else
                	System.out.println( "Array unchanged");
                // 설정한 시간 동안 대기
                long interval = 50;
                
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // 스레드가 중단되었을 때 예외 처리
                System.out.println("Thread was interrupted");
                break;
            }
        }
    }
    public static void printByteArrayAsBinary(byte[] byteArray) {
        for (byte b : byteArray) {
            // 각 바이트를 0과 1로 변환
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.println(binaryString); // 변환된 이진수 출력
        }
    }
}