
public interface MessageListener {
	void onUdpMessageReceived(String clientIP,String msg);
	void onTcpMessageReceived();
}
