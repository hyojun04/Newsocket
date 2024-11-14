import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderViewModelUdp {
    private static final int PORT = 1996;
    private static final int PACKET_SIZE = 1024; // ���� ��Ŷ ũ�� (1KB)

    public void startSend(String serverIP, int messageNum, int messageSize) { // messageSize�� �������� �ϴ� �޽����� ũ��(60KB�� ����)
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            //System.out.println("UDP is connected.");

            // ������ ũ���� ���ӵ� "A" ���� ����
            StringBuilder messageBuilder = new StringBuilder(messageSize);
            for (int i = 0; i < messageSize; i++) {
                messageBuilder.append('A');
            }
            String fullMessage = messageBuilder.toString(); // ��ü �޽��� ����
            
            // �޽����� ����Ʈ �迭�� ��ȯ
            byte[] messageBytes = fullMessage.getBytes();
            int offset = 0;
            int packetCount = 0; // ��Ŷ ��ȣ ī���� �߰�

            // �޽����� 1024����Ʈ�� �ʰ��� ���, 1024����Ʈ ������ �����Ͽ� ����
            while (offset < messageBytes.length) { 
            	// �� ��Ŷ�� ���� 1024byte(-10�� ��� ����)�� (�޽��� ��ü ���� - offset)�� �ּڰ�
                int length = Math.min(PACKET_SIZE-10, messageBytes.length - offset); 
                byte[] buffer = new byte[length+10]; // ��Ŷ ��ȣ�� ������ ����(���) �߰�
                
                // ��Ŷ ��ȣ�� ����� ����
                String packetHeader = messageNum + "_" + (packetCount + 1); // 1���� �����ϴ� ��Ŷ ��ȣ
                byte[] headerBytes = packetHeader.getBytes();
                System.arraycopy(headerBytes, 0, buffer, 0, headerBytes.length); // ����� ���ۿ� ����
                
                // �迭 ����(������ ���� �迭, ���� �迭���� ���縦 ������ �ε���, ������ ��� �迭, ��� �迭���� �����͸� ������ ���� �ε���, ������ ������ ����)
                System.arraycopy(messageBytes, offset, buffer, headerBytes.length, length); 
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);
                socket.send(packet);

                offset += length;
                packetCount++; // ��Ŷ ��ȣ ����
                //System.out.println(packetCount + "��° ��Ŷ ���� �Ϸ�"); // ��Ŷ ���� �Ϸ� �޽��� ���
            }

            //System.out.println("�޽����� ���۵Ǿ����ϴ�.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
