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

                if(!Arrays.equals(receiver_udp.checkNewMessage, receiver_udp.lastMessage)) { // checkNewMessage �迭�� ��ȭ�� ������ ���� ack ����
                	
                	
                	//byte�迭�� Ack�޽����� �����Եȴ�. sendAckMessage�� �����ǵǾ�����
                	tcpConnection.sendAckMessage(receiver_udp.checkNewMessage);
                	             
                	printByteArrayAsBinary(receiver_udp.checkNewMessage); // ���� ack���� ���           	

                	// �迭�� ��� ��Ʈ�� 1���� Ȯ��
                	boolean allBitsOne = true;
                	for (byte b : receiver_udp.checkNewMessage) {
                	    if (b != (byte) 0xFF) { // ���� �� ����Ʈ�� 0xFF�� �ƴ϶��
                	        allBitsOne = false;
                	        break;
                	    }
                	}

                	if (allBitsOne) {
                	    //System.out.println("all packet received");
                	    Arrays.fill(receiver_udp.checkNewMessage, (byte) 0); // checkNewMessage �迭�� 0���� �ʱ�ȭ
                	    //byte �迭 �ʱ�ȭ(�����ؾ��� ��Ʈ���� ��� 1��)
                        for(int i=ReceiverViewModelUdp.array_index*8 - ReceiverViewModelUdp.ignored_bits+1 ; i<= ReceiverViewModelUdp.array_index*8; i++){
                        	receiver_udp.SetNewMsgBit(i);     	
                        	} 
                        
                        receiver_udp.lastMessage = receiver_udp.checkNewMessage.clone();
                	    ReceiverViewModelUdp.receivedMessageNum++; // ���� �޽����� ���� �غ�
                	    //System.out.println("receivedMessageNum: " + ReceiverViewModelUdp.receivedMessageNum);
                	}
                	
                	// �迭�� ������ �����Ͽ� lastMessage�� ����
                	receiver_udp.lastMessage = Arrays.copyOf(receiver_udp.checkNewMessage, receiver_udp.checkNewMessage.length);
                
                
                	// �÷��� �ʱ�ȭ
                	receiver_udp.resetNewMessageFlag();
                	
                
                }
                
                // ������ �ð� ���� ���
                long interval = 10;
                
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // �����尡 �ߴܵǾ��� �� ���� ó��
                //System.out.println("Thread was interrupted");
                break;
            }
        }
    }
    public static void printByteArrayAsBinary(byte[] byteArray) {
        for (byte b : byteArray) {
            // �� ����Ʈ�� 0�� 1�� ��ȯ
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            //System.out.println(binaryString); // ��ȯ�� ������ ���
        }
    }
}