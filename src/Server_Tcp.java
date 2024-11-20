import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import javax.swing.JTextArea;

public class Server_Tcp {
    /* 메시지를 받기만 하는 기능 구현 */
    private Socket socket;
    private final TcpConnectionAccepter.ClientHandler handler;
    
    private static final int TOTAL_PACKETS = 61;
    private JTextArea receivedMessagesArea;  // GUI의 receive message 창
    private volatile boolean newAckReceived_tcp = false; // 에코 메시지 수신 여부
    private int receive_message_num = 0;
    
    public int array_index =0;
    public int ignored_bits =0;

    public byte[] checkNewMessage;
    // 생성자에서 JTextArea 전달 받음
    
    public static int calculateBits(int total_packets, int mode) {
    	if (mode == 0) {
    		//mode가 0이면 byte 배열 인덱스 계산
    		return (total_packets+7)/8;
    	}else if(mode ==1) {
    		return 8-(total_packets % 8);
    	}else {
    		throw new IllegalArgumentException("Invalid mode: mode should be 0 or 1");
    	}
    }
    
    
    
    public Server_Tcp(Socket socket,TcpConnectionAccepter.ClientHandler clientHandler ,JTextArea receivedMessagesArea) {
        this.socket = socket;
        handler = clientHandler;
        this.receivedMessagesArea = receivedMessagesArea;
    }
    
    
    
    
    public void reset_message_num() {
        receive_message_num = 0;
    }
    
    public boolean hasNewEchoMessage() {
        return newAckReceived_tcp;
    }

    public void resetNewEchoMessageFlag() {
        newAckReceived_tcp = false;
    }

    public void startReceiving() throws IOException { //예외 throw하여 ClientHandler에서 처리하도록함
    	
    	StartTCPCheck startCheck = new StartTCPCheck(this, handler);
    	
    	
        BufferedReader in = null;
        PrintWriter out = null;
        // 데이터 수신을 위한 DataInputStream 사용
        DataInputStream dataInputStream = null;
        array_index = calculateBits(TOTAL_PACKETS,0);
        ignored_bits = calculateBits(TOTAL_PACKETS,1);
        checkNewMessage = new byte[array_index];
        try {
            // BufferedReader를 사용하여 데이터를 송수신
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            dataInputStream =new DataInputStream(socket.getInputStream());
            
            
            
            
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("Server_TCP is open");

            
            
            while (!socket.isClosed()) {//클라이언트와의 연결을 유지하면서 메시지를 지속적으로 수신
                // 수신된 메시지 처리
            	// byte 배열 수신
                int byteArrayLength = dataInputStream.readInt(); // 먼저 수신할 배열의 길이를 읽음
                byte[] receivedData = new byte[byteArrayLength];
                dataInputStream.readFully(receivedData); // byte 배열 수신
                String receivedMessage = dataInputStream.readUTF();
              
                synchronized (this) {
                	checkNewMessage = receivedData;
                	//클라이언트로부터 받은 바이트배열을 append 
                	StringBuilder temp = printByteArrayAsBinary(checkNewMessage);
                	receive_message_num++;
                    receivedMessagesArea.append("[" + receive_message_num + "] 수신된 메시지 from " + clientIP + ": " +temp +receivedMessage +"\n" );
                    //newAckReceived_tcp = true; // 에코 메시지를 받았을 경우
                    System.out.println("newAckMessage was coming");
                    
                    //notify() 대신 checking메소드 호출
                    startCheck.startChecking();
                }
            }
        } finally {
            // Exception을 throw하여 외부에서 처리하도록 함.
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("TCP 소켓이 닫혔습니다.");
        }
    }
    
    private StringBuilder printByteArrayAsBinary(byte[] byteArray) {
    	StringBuilder binaryStringBuilder = new StringBuilder();
        for (byte b : byteArray) {
            // 각 바이트를 0과 1로 변환
        	// 16진수 1111 1111과 &연산
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.println(binaryString); // 변환된 이진수 출력
            binaryStringBuilder.append(binaryString).append(" ");
            
        }
        return binaryStringBuilder;
    }
    
    
   

}
