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

                if(!Arrays.equals(receiver_udp.checkNewMessage, receiver_udp.lastMessage)) { // checkNewMessage 배열에 변화가 생겼을 때만 ack 전송
                	
                	
                	//byte배열만 Ack메시지로 보내게된다. sendAckMessage는 재정의되어있음
                	tcpConnection.sendAckMessage(receiver_udp.checkNewMessage);
                	             
                	printByteArrayAsBinary(receiver_udp.checkNewMessage); // 보낸 ack내용 출력           	

                	// 배열의 모든 비트가 1인지 확인
                	boolean allBitsOne = true;
                	for (byte b : receiver_udp.checkNewMessage) {
                	    if (b != (byte) 0xFF) { // 만약 한 바이트라도 0xFF가 아니라면
                	        allBitsOne = false;
                	        break;
                	    }
                	}

                	if (allBitsOne) {
                	    //System.out.println("all packet received");
                	    Arrays.fill(receiver_udp.checkNewMessage, (byte) 0); // checkNewMessage 배열을 0으로 초기화
                	    //byte 배열 초기화(무시해야할 비트들을 모두 1로)
                        for(int i=ReceiverViewModelUdp.array_index*8 - ReceiverViewModelUdp.ignored_bits+1 ; i<= ReceiverViewModelUdp.array_index*8; i++){
                        	receiver_udp.SetNewMsgBit(i);     	
                        	} 
                        
                        receiver_udp.lastMessage = receiver_udp.checkNewMessage.clone();
                	    ReceiverViewModelUdp.receivedMessageNum++; // 다음 메시지를 받을 준비
                	    //System.out.println("receivedMessageNum: " + ReceiverViewModelUdp.receivedMessageNum);
                	}
                	
                	// 배열의 내용을 복사하여 lastMessage에 저장
                	receiver_udp.lastMessage = Arrays.copyOf(receiver_udp.checkNewMessage, receiver_udp.checkNewMessage.length);
                
                
                	// 플래그 초기화
                	receiver_udp.resetNewMessageFlag();
                	
                
                }
                
                // 설정한 시간 동안 대기
                long interval = 10;
                
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // 스레드가 중단되었을 때 예외 처리
                //System.out.println("Thread was interrupted");
                break;
            }
        }
    }
    public static void printByteArrayAsBinary(byte[] byteArray) {
        for (byte b : byteArray) {
            // 각 바이트를 0과 1로 변환
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            //System.out.println(binaryString); // 변환된 이진수 출력
        }
    }
}