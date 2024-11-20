import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class StartUDPCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;
    private JLabel imageLabel; // �̹��� ǥ�ÿ� JLabel

    public StartUDPCheckThread(ReceiverViewModelUdp receiver_udp, TcpSocketConnection tcpConnection, JLabel imageLabel) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
        this.imageLabel = imageLabel;
       
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
                		convertAndDisplayImage(receiver_udp.imageData); // imageData�� ���ŵ� ��Ŷ�� ������ ����Ʈ �迭
                	    Arrays.fill(receiver_udp.checkNewMessage, (byte) 0); // checkNewMessage �迭�� 0���� �ʱ�ȭ
                	    //byte �迭 �ʱ�ȭ(�����ؾ��� ��Ʈ���� ��� 1��)
                        for(int i=ReceiverViewModelUdp.array_index*8 - ReceiverViewModelUdp.ignored_bits+1 ; i<= ReceiverViewModelUdp.array_index*8; i++){
                        	receiver_udp.SetNewMsgBit(i);     	
                        	}
                       
                        //receiver_udp.lastMessage = receiver_udp.checkNewMessage.clone();
                	    ReceiverViewModelUdp.receivedMessageNum++; // ���� �޽����� ���� �غ�
                	    //System.out.println("receivedMessageNum: " + ReceiverViewModelUdp.receivedMessageNum);
                	}
                	
                	// �迭�� ������ �����Ͽ� lastMessage�� ����
                	receiver_udp.lastMessage = Arrays.copyOf(receiver_udp.checkNewMessage, receiver_udp.checkNewMessage.length);
               
               
                	// �÷��� �ʱ�ȭ
                	receiver_udp.resetNewMessageFlag();
                	
               
                }
               
                // ������ �ð� ���� ���
                long interval = 100;
               
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // �����尡 �ߴܵǾ��� �� ���� ó��
                //System.out.println("Thread was interrupted");
                break;
            }
        }
    }
    public void convertAndDisplayImage(byte[][] imageData2D) {
        // ������ �迭�� 1���� �迭�� ��ȯ
        int rows = imageData2D.length;
        int cols = imageData2D[0].length;
        byte[] imageData1D = new byte[rows * cols];

        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                imageData1D[index++] = imageData2D[i][j];
            }
        }

        // ��ȯ�� 1���� �迭�� displayImage �޼��忡 ����
        displayImage(imageData1D);
    }
   
    public void displayImage(byte[] imageData) {
        try {
            // �̹��� �����͸� BufferedImage�� ��ȯ
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bais);

            // �̹����� JLabel�� ����
            imageLabel.setIcon(new ImageIcon(image));
        } catch (IOException e) {
            //�����߻�
            e.printStackTrace();
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