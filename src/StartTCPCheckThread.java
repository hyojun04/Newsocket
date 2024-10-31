import java.util.Arrays;

public class StartTCPCheckThread implements Runnable {
    private final Server_Tcp serverTcp;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    volatile boolean runningFlag = true;
    
    // ��ü ��Ŷ ���� ���� ���θ� �����ϴ� �迭
    private static final int TOTAL_PACKETS = 61; // ��ü ��Ŷ �� (�ʿ信 �°� ����)
    private boolean[] packetReceived = new boolean[TOTAL_PACKETS + 1]; // �ε��� 0�� ������� ����
    private int receivedPacketCount = 0;
    
    public StartTCPCheckThread(Server_Tcp serverTCP, TcpConnectionAccepter.ClientHandler handler) {
        this.serverTcp = serverTCP;
        this.handler = handler;
    }
    
    public void checkAck() {
    	String receivedMessage = serverTcp.getReceivedMessage();
        System.out.println("Received ack Message: " + receivedMessage); //���� ack ���
        
    	// "_"�� �������� split�Ͽ� packetNum ����
        String[] parts = receivedMessage.split("_");
        String packetString1 = parts[1].substring(0, 2);
        String packetString = packetString1.replaceAll("\\D","");
        
        
        System.out.println("part0:"+parts[0]+",parts1:"+packetString);
        if (parts.length == 2) {
            int packetNum = Integer.parseInt(packetString);
            
            // ��ȿ�� packetNum���� Ȯ���ϰ� �� ��Ŷ�̶�� ó��
            if (packetNum > 0 && packetNum <= TOTAL_PACKETS && !packetReceived[packetNum]) {
                packetReceived[packetNum] = true; // �ش� ��Ŷ�� ������ ������ ǥ��
                receivedPacketCount++; // �� ��Ŷ ���� �� count ����
            }
        }
        
        
        if (receivedPacketCount == TOTAL_PACKETS) {
           receivedPacketCount = 0;
           Arrays.fill(packetReceived, false);
           NewSocket.clients_tcp.set(handler.permanent_id,true); 
           if(NewSocket.clients_tcp.get(handler.permanent_id) == true)
              System.out.println("Client Num: "+handler.permanent_id+" Changed index value TRUE");
        }
    }

    @Override
    public void run() {
        while (runningFlag) {
            synchronized (serverTcp) {
                while (!serverTcp.hasNewEchoMessage()) {
                    try {
                        System.out.println("StartTCPCheckThread���� wait() ��...");
                        serverTcp.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        
                checkAck();
   			 
                // �÷��� �ʱ�ȭ
                serverTcp.resetNewEchoMessageFlag();
            }

            }
        }
    
    public void stopThread() {
    	runningFlag = false;
    }
    
}
