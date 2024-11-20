import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderViewModelUdp {
    private static final int PORT = 1996;
    private static final int PACKET_SIZE = 1024; // 단위 패킷 크기 (1KB)
    
    public void startSend(String serverIP, int messageNum, String imagePath) { // messageSize는 보내고자 하는 메시지의 크기(60KB로 설정)
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            //System.out.println("UDP is connected.");

            /*// 지정된 크기의 연속된 "A" 문자 생성
            StringBuilder messageBuilder = new StringBuilder(messageSize);
            for (int i = 0; i < messageSize; i++) {
                messageBuilder.append('A');
            }
            String fullMessage = messageBuilder.toString(); // 전체 메시지 생성
            
            // 메시지를 바이트 배열로 변환
            byte[] messageBytes = fullMessage.getBytes();
            int offset = 0;
            int packetCount = 0; // 패킷 번호 카운터 추가*/
            
            int offset = 0;
            int packetCount = 0; // 패킷 번호 카운터
            
            // 이미지 파일 읽기
            File file = new File(imagePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            byte[] imageBytes = new byte[(int) file.length()];
            bufferedInputStream.read(imageBytes); // 이미지 파일을 바이트 배열로 읽기
            bufferedInputStream.close();
            

            // 메시지가 1024바이트를 초과할 경우, 1024바이트 단위로 분할하여 전송
            while (offset < imageBytes.length) { 
            	// 한 패킷의 길이 1024byte(-10은 헤더 길이)와 (메시지 전체 길이 - offset)의 최솟값
                int length = Math.min(PACKET_SIZE-10, imageBytes.length - offset); 
                byte[] buffer = new byte[length+10]; // 패킷 번호를 저장할 공간(헤더) 추가
                
                // 패킷 번호를 헤더에 삽입
                String packetHeader = messageNum + "_" + (packetCount + 1)+"_"; // 1부터 시작하는 패킷 번호
                byte[] headerBytes = packetHeader.getBytes();
                System.arraycopy(headerBytes, 0, buffer, 0, headerBytes.length); // 헤더를 버퍼에 복사
                
                // 배열 복사(복사할 원본 배열, 원본 배열에서 복사를 시작할 인덱스, 복사할 대사 배열, 대상 배열에서 데이터를 복사할 시작 인덱스, 복사할 데이터 길이)
                System.arraycopy(imageBytes, offset, buffer, headerBytes.length, length); 
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);
                socket.send(packet);

                offset += length;
                packetCount++; // 패킷 번호 증가
                //System.out.println(packetCount + "번째 패킷 전송 완료"); // 패킷 전송 완료 메시지 출력
            }

            //System.out.println("메시지가 전송되었습니다.");

        }  catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    
    
}
