import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderViewModelUdp {
    private static final int PORT = 1996;
    private static final int PACKET_SIZE = 1024; // UDP ��Ŷ ũ�� (1KB)

    public void startClient(String serverIP) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            System.out.println("������ ����Ǿ����ϴ�.");

            // 60KB�� ���ӵ� "A" ���� ���� , ���Ƿ� 30 bytes ����
            StringBuilder messageBuilder = new StringBuilder(30);
            for (int i = 0; i < 30; i++) {
                messageBuilder.append('A');
            }
            String message = messageBuilder.toString();

            // �޽����� �����Ͽ� ����
            byte[] messageBytes = message.getBytes();
            int offset = 0;
            while (offset < messageBytes.length) {
                int length = Math.min(PACKET_SIZE, messageBytes.length - offset);
                byte[] buffer = new byte[length];
                System.arraycopy(messageBytes, offset, buffer, 0, length);

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);
                socket.send(packet);

                offset += length;
            }

            System.out.println("60KB�� ���ӵ� 'A' �޽����� ������ �����߽��ϴ�.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

}
