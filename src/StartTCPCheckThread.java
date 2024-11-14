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

            	// ��� ��Ʈ�� 1���� Ȯ��
            	boolean allBitsOne = true;
            	for (byte b : serverTcp.checkNewMessage) {
            	    if (b != (byte) 0xFF) { // ���� �� ����Ʈ�� 0xFF�� �ƴ϶��
            	        allBitsOne = false;
            	        break;
            	    }
            	}
            	
            	if (allBitsOne) {
            	    // �ε��� ��ȣ�� �ش��ϴ� client Ŭ���� �迭 ȣ��
            		ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
            		clientinfo.setNewMsg(true);
            	    System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
            		/*
            		if(!clientinfo.getNewMsg()) {
            			// ���� true set���� ���Ƿ� false set ����
                	    clientinfo.setNewMsg(true);
                	    System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
            		}*/
            	    
            	}
            	
            	// �÷��� �ʱ�ȭ
                serverTcp.resetNewEchoMessageFlag();
                /*
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/                         
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
