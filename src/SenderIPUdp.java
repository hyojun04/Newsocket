import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderIPUdp {
	private static final int PORT = 1996;
	
	public void startSend(String broadIP) {
    	DatagramSocket socket = null;
    	try {
    		socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(broadIP);
            String message = "Connect here";
            byte[] messageBytes = message.getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, serverAddress, PORT);
            socket.send(packet);
            System.out.println("UDP serverIp is sending");
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
    		if (socket != null && !socket.isClosed()) {
    			socket.close();
    		}
    	}
    }
}
