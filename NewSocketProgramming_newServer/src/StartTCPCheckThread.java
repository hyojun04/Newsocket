public class StartTCPCheckThread implements Runnable {
    private final Server_Tcp serverTcp;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    volatile boolean runningFlag = true;
    
    public StartTCPCheckThread(Server_Tcp serverTCP, TcpConnectionAccepter.ClientHandler handler) {
        this.serverTcp = serverTCP;
        this.handler = handler;
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
                System.out.println("TCP Ack message is got");
                
                //인덱스 번호에 해당하는 client클래스 배열 호출
                ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
                clientinfo.setNewMsg(true);
                //클라이언트로부터 받을 바이트 배열 출력
                System.out.println(Server_Tcp.checkNewMessage);
            	if(clientinfo.getNewMsg() == true)
                	System.out.println("Client Num: "+handler.permanent_id+" Changed index value TRUE");
                

                // 플래그 초기화
                serverTcp.resetNewEchoMessageFlag();
            }

            }
        }
    
    public void stopThread() {
    	//해당 인덱스를 true로 고정하여 상관없이 프로그램이 작동하도록함
    	
    	 //인덱스 번호에 해당하는 client클래스 배열 호출 후 연결상태 false, 새로운 메시지 true 고정
        ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
        System.out.println("Client : "+clientinfo.getIp()+" is disconnected");
        clientinfo.setNewMsg(true);
    	clientinfo.setConnected(false);
    	runningFlag = false;    	
    }
    
}
