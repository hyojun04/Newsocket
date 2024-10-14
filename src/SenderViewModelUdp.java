import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderViewModelUdp {
    private static final int PORT = 1995;
    private static final int PACKET_SIZE = 1024; // UDP 패킷 크기 (1KB)

    public void startClient(String serverIP) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            System.out.println("서버에 연결되었습니다.");

            // 60KB의 연속된 "A" 문자 생성 , 임의로 30 bytes 설정
            StringBuilder messageBuilder = new StringBuilder(30);
            for (int i = 0; i < 30; i++) {
                messageBuilder.append('A');
            }
            String message = messageBuilder.toString();

            // 메시지를 분할하여 전송
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

            System.out.println("60KB의 연속된 'A' 메시지를 서버로 전송했습니다.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

}
