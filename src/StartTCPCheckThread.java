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
                        System.out.println("StartTCPCheckThread에서 wait() 중...");
                        serverTcp.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //System.out.println("TCP Ack message is got");
                
                
                
                int totalBits = Server_Tcp.array_index * 8 - Server_Tcp.ignored_bits; // 체크할 총 비트 수
            	int totalBytes = (totalBits + 7) / 8; // 총 바이트 수, 8로 나눈 나머지가 있으면 +1

            	// 새로운 배열을 만들어서 checkNewMessage의 각 바이트를 왼쪽으로 shift
            	byte[] shiftedMessage = new byte[totalBytes];
            	for (int i = 0; i < totalBytes; i++) {
            	    // 해당 바이트를 왼쪽으로 ignored_bits 만큼 쉬프트
            	    int index = i < Server_Tcp.array_index ? i : Server_Tcp.array_index - 1; // 마지막 바이트는 인덱스를 조정
            	    // 왼쪽으로 쉬프트 후 오른쪽 비트는 1로 채움
            	    shiftedMessage[i] = (byte) ((Server_Tcp.checkNewMessage[index] << Server_Tcp.ignored_bits) |
            	        ((1 << Server_Tcp.ignored_bits) - 1)); // 오른쪽 비트를 모두 1로 채우기
            	}

            	// 모든 비트가 1인지 확인
            	boolean allBitsOne = true;
            	for (byte b : shiftedMessage) {
            	    if (b != (byte) 0xFF) { // 만약 한 바이트라도 0xFF가 아니라면
            	        allBitsOne = false;
            	        break;
            	    }
            	}

            	if (allBitsOne) {
            	    // 인덱스 번호에 해당하는 client 클래스 배열 호출
            	    ClientInfo clientinfo = TcpConnectionManager.getClient(handler.permanent_id);
            	    // 기존 true set에서 임의로 false set 지정
            	    clientinfo.setNewMsg(true);
            	    System.out.println("Client Num: " + handler.permanent_id + " Changed index value TRUE");
            	}
            	
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
