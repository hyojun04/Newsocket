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

                // ���ο� �޽����� ���ŵ� ���
                System.out.println("StartUDPCheckThread is awake");
                
                if(!Arrays.equals(ReceiverViewModelUdp.checkNewMessage, ReceiverViewModelUdp.lastMessage)) { // checkNewMessage �迭�� ��ȭ�� ������ ���� ack ����
                	
                	System.out.println( "Array changed");
                	//byte�迭�� Ack�޽����� �����Եȴ�. sendAckMessage�� �����ǵǾ�����
                	tcpConnection.sendAckMessage(ReceiverViewModelUdp.checkNewMessage);
                	System.out.println( "SendAck:");               
                	printByteArrayAsBinary(ReceiverViewModelUdp.checkNewMessage); // ���� ack���� ���
                	
                	
                	
                	//���� ��� ��Ŷ�� �޾Ҵ��� Ȯ��
                	int totalBits = ReceiverViewModelUdp.array_index * 8 - ReceiverViewModelUdp.ignored_bits; // üũ�� �� ��Ʈ ��
                	int totalBytes = (totalBits + 7) / 8; // üũ�� �� ����Ʈ ��, 8�� ���� �������� ������ +1

                	// ��Ŷ ���� 8�� ����� �ƴ� ��� �����ؾ� �ϴ� bit(ignore_bit) �ڸ��� 1�� ä��
                	byte[] shiftedMessage = new byte[totalBytes];
                	for (int i = 0; i < totalBytes; i++) {
                	    // �ش� ����Ʈ�� �������� ignored_bits ��ŭ ����Ʈ
                	    int index = i < ReceiverViewModelUdp.array_index ? i : ReceiverViewModelUdp.array_index - 1; // ������ ����Ʈ�� �ε����� ����
                	    // �������� ����Ʈ �� ������ ��Ʈ�� 1�� ä��
                	    shiftedMessage[i] = (byte) ((ReceiverViewModelUdp.checkNewMessage[index] << ReceiverViewModelUdp.ignored_bits) | 
                	        ((1 << ReceiverViewModelUdp.ignored_bits) - 1)); // ������ ��Ʈ�� ��� 1�� ä���
                	}

                	// �迭�� ��� ��Ʈ�� 1���� Ȯ��
                	boolean allBitsOne = true;
                	for (byte b : shiftedMessage) {
                	    if (b != (byte) 0xFF) { // ���� �� ����Ʈ�� 0xFF�� �ƴ϶��
                	        allBitsOne = false;
                	        break;
                	    }
                	}

                	if (allBitsOne) {
                	    System.out.println("all packet received");
                	    Arrays.fill(ReceiverViewModelUdp.checkNewMessage, (byte) 0); // checkNewMessage �迭�� 0���� �ʱ�ȭ
                	    ReceiverViewModelUdp.receivedMessageNum++; // ���� �޽����� ���� �غ�
                	    System.out.println("receivedMessageNum: " + ReceiverViewModelUdp.receivedMessageNum);
                	}
                	
                	// �迭�� ������ �����Ͽ� lastMessage�� ����
                    ReceiverViewModelUdp.lastMessage = Arrays.copyOf(ReceiverViewModelUdp.checkNewMessage, ReceiverViewModelUdp.checkNewMessage.length);
                
                	System.out.println( " is transmitted");
                	System.out.println("(1) UDP Message state: " + receiver_udp.hasNewMessage());

                	// �÷��� �ʱ�ȭ
                	receiver_udp.resetNewMessageFlag();
                	System.out.println("(2) UDP Message state: " + receiver_udp.hasNewMessage());
                
                }
                else
                	System.out.println( "Array unchanged");
                // ������ �ð� ���� ���
                long interval = 50;
                
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // �����尡 �ߴܵǾ��� �� ���� ó��
                System.out.println("Thread was interrupted");
                break;
            }
        }
    }
    public static void printByteArrayAsBinary(byte[] byteArray) {
        for (byte b : byteArray) {
            // �� ����Ʈ�� 0�� 1�� ��ȯ
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.println(binaryString); // ��ȯ�� ������ ���
        }
    }
}