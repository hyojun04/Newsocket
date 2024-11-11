import java.util.Arrays;
import javax.swing.*;
import java.awt.*;

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
                        System.out.println("StartTCPCheckThread���� wait() ��...");
                        serverTcp.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //System.out.println("TCP Ack message is got");
                
                
                
                int totalBits = Server_Tcp.array_index * 8 - Server_Tcp.ignored_bits; // üũ�� �� ��Ʈ ��
            	int totalBytes = (totalBits + 7) / 8; // �� ����Ʈ ��, 8�� ���� �������� ������ +1

            	// ���ο� �迭�� ���� checkNewMessage�� �� ����Ʈ�� �������� shift
            	byte[] shiftedMessage = new byte[totalBytes];
            	for (int i = 0; i < totalBytes; i++) {
            	    // �ش� ����Ʈ�� �������� ignored_bits ��ŭ ����Ʈ
            	    int index = i < Server_Tcp.array_index ? i : Server_Tcp.array_index - 1; // ������ ����Ʈ�� �ε����� ����
            	    // �������� ����Ʈ �� ������ ��Ʈ�� 1�� ä��
            	    shiftedMessage[i] = (byte) ((Server_Tcp.checkNewMessage[index] << Server_Tcp.ignored_bits) |
            	        ((1 << Server_Tcp.ignored_bits) - 1)); // ������ ��Ʈ�� ��� 1�� ä���
            	}

            	// ��� ��Ʈ�� 1���� Ȯ��
            	boolean allBitsOne = true;
            	for (byte b : shiftedMessage) {
            	    if (b != (byte) 0xFF) { // ���� �� ����Ʈ�� 0xFF�� �ƴ϶��
            	        allBitsOne = false;
            	        break;
            	    }
            	}

            	if (allBitsOne) {
            	    // �ε��� ��ȣ�� �ش��ϴ� client Ŭ���� �迭 ȣ��
            	    ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
            	    // ���� true set���� ���Ƿ� false set ����
            	    clientinfo.setNewMsg(true);
            	    System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
            	}
            	
            	// �÷��� �ʱ�ȭ
                serverTcp.resetNewEchoMessageFlag();
                         
            }
           }
        }
    
    public void stopThread() {
    	//�ش� �ε����� true�� �����Ͽ� ������� ���α׷��� �۵��ϵ�����
    	
    	 //�ε��� ��ȣ�� �ش��ϴ� clientŬ���� �迭 ȣ�� �� ������� false, ���ο� �޽��� true ����
        ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
        System.out.println("Client : "+clientinfo.getIp()+" is disconnected");
        clientinfo.setNewMsg(true);
    	clientinfo.setConnected(false);
    	runningFlag = false;    	
    }
    
    
}
