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
                System.out.println("TCP Echo message is got");
                
                NewSocket.clients_tcp.set(handler.permanent_id,true);
            	if(NewSocket.clients_tcp.get(handler.permanent_id) == true)
                	System.out.println("Client Num: "+handler.permanent_id+" Changed index value TRUE");
                

                // 플래그 초기화
                serverTcp.resetNewEchoMessageFlag();
            }

            }
        }
    
    public void stopThread() {
    	runningFlag = false;
    }
    
}
