

public class StartTCPCheck  {
    private final Server_Tcp serverTcp;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    
 	
    
    public StartTCPCheck(Server_Tcp serverTCP, TcpConnectionAccepter.ClientHandler handler) {
        this.serverTcp = serverTCP;
        this.handler = handler;
    }

	public void startChecking() {
		
		boolean allBitsOne = true;
	    // 모든 비트가 1인지 확인
		for (byte b : serverTcp.checkNewMessage) {
			if (b != (byte) 0xFF) { // 만약 한 바이트라도 0xFF가 아니라면
				allBitsOne = false;
				break;
			}
		}
		System.out.println("Ack checking");
		if (allBitsOne) {
			// 인덱스 번호에 해당하는 client 클래스 배열 호출
			ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
			clientinfo.setNewMsg(true);
			System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
		}

	}

	public void stopChecking() {
		// 해당 인덱스를 true로 고정하여 상관없이 프로그램이 작동하도록함

		// 인덱스 번호에 해당하는 client클래스 배열 호출 후 연결상태 false, 새로운 메시지 true 고정
		ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
		System.out.println("Client : " + clientinfo.getIp() + " is disconnected");
		clientinfo.setNewMsg(true);
		clientinfo.setConnected(false);

	}

}
