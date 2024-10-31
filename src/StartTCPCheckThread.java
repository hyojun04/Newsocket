import java.util.Arrays;

public class StartTCPCheckThread implements Runnable {
    private final Server_Tcp serverTcp;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    volatile boolean runningFlag = true;
    
    // 전체 패킷 수와 수신 여부를 추적하는 배열
    private static final int TOTAL_PACKETS = 61; // 전체 패킷 수 (필요에 맞게 수정)
    private boolean[] packetReceived = new boolean[TOTAL_PACKETS + 1]; // 인덱스 0은 사용하지 않음
    private int receivedPacketCount = 0;
    
    public StartTCPCheckThread(Server_Tcp serverTCP, TcpConnectionAccepter.ClientHandler handler) {
        this.serverTcp = serverTCP;
        this.handler = handler;
    }
    
    public void checkAck() {
    	String receivedMessage = serverTcp.getReceivedMessage();
        System.out.println("Received ack Message: " + receivedMessage); //받은 ack 출력
        
    	// "_"를 기준으로 split하여 packetNum 추출
        String[] parts = receivedMessage.split("_");
        String packetString1 = parts[1].substring(0, 2);
        String packetString = packetString1.replaceAll("\\D","");
        
        
        System.out.println("part0:"+parts[0]+",parts1:"+packetString);
        if (parts.length == 2) {
            int packetNum = Integer.parseInt(packetString);
            
            // 유효한 packetNum인지 확인하고 새 패킷이라면 처리
            if (packetNum > 0 && packetNum <= TOTAL_PACKETS && !packetReceived[packetNum]) {
                packetReceived[packetNum] = true; // 해당 패킷을 수신한 것으로 표시
                receivedPacketCount++; // 새 패킷 수신 시 count 증가
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
                        System.out.println("StartTCPCheckThread에서 wait() 중...");
                        serverTcp.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        
                checkAck();
   			 
                // 플래그 초기화
                serverTcp.resetNewEchoMessageFlag();
            }

            }
        }
    
    public void stopThread() {
    	runningFlag = false;
    }
    
}
