

public class StartTCPCheck  {
    private final Server_Tcp serverTcp;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    
 	
    
    public StartTCPCheck(Server_Tcp serverTCP, TcpConnectionAccepter.ClientHandler handler) {
        this.serverTcp = serverTCP;
        this.handler = handler;
    }

	public void startChecking() {
		
		boolean allBitsOne = true;
	    // ��� ��Ʈ�� 1���� Ȯ��
		for (byte b : serverTcp.checkNewMessage) {
			if (b != (byte) 0xFF) { // ���� �� ����Ʈ�� 0xFF�� �ƴ϶��
				allBitsOne = false;
				break;
			}
		}
		System.out.println("Ack checking");
		if (allBitsOne) {
			// �ε��� ��ȣ�� �ش��ϴ� client Ŭ���� �迭 ȣ��
			ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
			clientinfo.setNewMsg(true);
			System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
		}

	}

	public void stopChecking() {
		// �ش� �ε����� true�� �����Ͽ� ������� ���α׷��� �۵��ϵ�����

		// �ε��� ��ȣ�� �ش��ϴ� clientŬ���� �迭 ȣ�� �� ������� false, ���ο� �޽��� true ����
		ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
		System.out.println("Client : " + clientinfo.getIp() + " is disconnected");
		clientinfo.setNewMsg(true);
		clientinfo.setConnected(false);

	}

}
