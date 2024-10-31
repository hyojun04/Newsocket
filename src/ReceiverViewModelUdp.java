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
    private JTextArea receivedMessagesArea;  // GUI�� receive message â
    private static int receive_message_num = 0;
    private volatile boolean newMessageReceived_udp = false;
    public int receivedMessageNum; 
    public static int numberStr_message;
    public static int numberStr_packet;
    //private static int checkSerial; //���� UDP �޽����� �� ��° �޽������� ����
    
    //���� UDP �޽����� �� ��° �޽������� �����ϰ� �ִ� checkSerial ������ ����ϴ� �޼ҵ�
    /*public int Print_checkSerial() {
    	return checkSerial;
    }*/
    
    // ��ü ��Ŷ ���� ���� ���θ� �����ϴ� �迭
    private static final int TOTAL_PACKETS = 61; // ��ü ��Ŷ �� (�ʿ信 �°� ����)
    private boolean[] checkSerial = new boolean[TOTAL_PACKETS + 1]; // �ε��� 0�� ������� ����
    private int receivedPacketCount = 0;
    
    // �����ڿ��� JTextArea ���� ����
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
    
    // getLeading�� true�� �ָ� "_"�� �������� ���� ���ڸ�, false�� �ָ� ���� ���ڸ� return��
    public static int extractNumberPart(String input, boolean getLeading) { 
        // ���Խ��� ����Ͽ� ���ڿ� `_`�� �������� ���ڿ��� �и�
        String[] parts = input.split("_");

        if (parts.length == 2) {
            String leadingNumber = parts[0].replaceAll("\\D", ""); // �պκ� ���ڸ� ����
            String trailingNumber = parts[1].replaceAll("\\D", ""); // �޺κ� ���ڸ� ����
            
            // getLeading�� true�� ��� �պκ� ���� ��ȯ, false�� ��� �޺κ� ���� ��ȯ
            String numberString = getLeading ? leadingNumber : trailingNumber;
            
            // �� ���ڿ��� ó���Ͽ� int�� ��ȯ
            return numberString.isEmpty() ? 0 : Integer.parseInt(numberString);
        } else {
            // ������ ���� ���� ��� 0 ��ȯ
            return 0;
        }
    }

    public void startServer() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("UDP Server started on port " + PORT + ". Waiting for messages...");

            // ���� ������ �޽��� ��� ����
            while (true) {
                try {
                    // ���� ����
                    byte[] buffer = new byte[BUFFER_SIZE];

                    // ������ ��Ŷ ����
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                    // ������ ����
                    socket.receive(receivePacket);

                    // ���ŵ� ������ ó��
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    // �۽��� IP �ּ� ��������
                    InetAddress senderAddress = receivePacket.getAddress();
                    String senderIP = senderAddress.getHostAddress();

                    // ���� �ð��� hh:mm:ss.SSS �������� ��������
                    String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

                    // �޽����� �պκ� 10���ڸ� �߶� ǥ��
                    String truncatedMessage = receivedMessage.length() > 10
                            ? receivedMessage.substring(0, 10)
                            : receivedMessage;

                    // ���� �޽��� GUI�� ǥ��
                    receive_message_num++;
                    
                    
                    receivedMessagesArea.append("[" + receive_message_num + "] Received UDP message from " + senderIP + ": " + truncatedMessage + " [" + timeStamp + "]\n");                    
                    System.out.println("I got Message: " + truncatedMessage);

                    numberStr_packet = extractNumberPart(truncatedMessage, PACKET_NUM);
                    numberStr_message = extractNumberPart(truncatedMessage, MESSAGE_NUM);
                    
                    if (numberStr_packet != 0 ) {                      
                        //���ο� ��Ŷ�� ���� ��
                        if (numberStr_packet > 0 && numberStr_packet <= TOTAL_PACKETS && !checkSerial[numberStr_packet]) {
                        	checkSerial[numberStr_packet] = true; // �ش� ��Ŷ�� ������ ������ ǥ��
                            System.out.println("packet["+numberStr_packet+"]�� ture�� �ٲ�");
                            receivedPacketCount++; // �� ��Ŷ ���� �� count ����
                            System.out.println("receivedPacketCount:"+receivedPacketCount);
                            //��Ŷ�� ��� �������� ��
                            if (receivedPacketCount == TOTAL_PACKETS) {
                            	receivedMessageNum ++;
                            	receivedPacketCount = 0;
                            	Arrays.fill(checkSerial, false);
                            }
                            synchronized (this) {
                                newMessageReceived_udp = true; // ���ο� �޽����� �޾��� ���
                                System.out.println("newMessageReceived_udp set to true");
                                notifyAll(); // ���°� �ٲ�����Ƿ� ��� ���� �����忡�� �˸�
                            }
                        }
                         
                        //�� �� �̻� ���� �޽�����ȣ�� ������ ���ڸ޽����� ������ ����
                        else {
                        	 // newMessageReceived_udp ���� ������Ʈ �� notifyAll() ȣ��
                            synchronized (this) {
                                newMessageReceived_udp = false; // ���ο� �޽����� �޾��� ���
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
                    break; // ���Ͽ� ������ ����� ������ Ż���Ͽ� ������ �ߴ��մϴ�.
                } catch (SecurityException e) {
                    System.out.println("Security exception: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Illegal argument: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error while receiving data: " + e.getMessage());
                    e.printStackTrace(); // �߰����� ���� �α׸� ����Ͽ� ������ �� ��Ȯ�� �ľ��� �� �ְ� �մϴ�.
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
            
            // ���� ����
            byte[] buffer = new byte[BUFFER_SIZE];

            // ������ ��Ŷ ����
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // ������ ���� (�޽����� �� ������ ���)
            socket.receive(receivePacket);

            // �۽��� IP �ּ� ��������
            InetAddress senderAddress = receivePacket.getAddress();
            String senderIP = senderAddress.getHostAddress();

            // IP �ּҰ� ��ȿ�ϸ� ��ȯ, ��ȿ���� ������ null ��ȯ
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
            // �޽����� ������ �� ������ �ݾ� �� �̻� �޽����� ���� �ʵ��� ��.
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("UDP Server socket closed.");
            }
        }
        return null; // �޽����� �������� ���߰ų� ���� �߻� �� null ��ȯ
    }

}
