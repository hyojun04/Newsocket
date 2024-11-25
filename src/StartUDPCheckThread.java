import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class StartUDPCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;
    private JLabel imageLabel; // 이미지 표시용 JLabel


    public StartUDPCheckThread(ReceiverViewModelUdp receiver_udp, TcpSocketConnection tcpConnection, JLabel imageLabel) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
        this.imageLabel = imageLabel;
       
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!Arrays.equals(receiver_udp.checkNewMessage, receiver_udp.lastMessage)) {
                    // ACK 전송
                    tcpConnection.sendAckMessage(receiver_udp.checkNewMessage);
                    printByteArrayAsBinary(receiver_udp.checkNewMessage);

                    // 모든 패킷이 수신되었는지 확인
                    boolean allBitsOne = true;
                    for (byte b : receiver_udp.checkNewMessage) {
                        if (b != (byte) 0xFF) {
                            allBitsOne = false;
                            break;
                        }
                    }

                    // 모든 패킷이 수신된 경우
                    if (allBitsOne) {
                        System.out.println("All packets received successfully");

                        // 모든 행이 유효한지 확인하고 이미지 조립 시작
                        boolean allRowsValid = true;
                        for (int i = 0; i < receiver_udp.imageData.length; i++) {
                            if (receiver_udp.imageData[i] == null) {
                                allRowsValid = false;
                                System.err.println("Missing data for row: " + i);
                            }
                        }

                        // 모든 행이 유효할 때만 이미지 변환 시도
                        if (allRowsValid) {
                            convertAndDisplayImage(receiver_udp.imageData);
                        } else {
                            System.err.println("Cannot convert image: incomplete data");
                        }

                        // 배열 초기화
                        Arrays.fill(receiver_udp.checkNewMessage, (byte) 0);
                        for (int i = ReceiverViewModelUdp.array_index * 8 - ReceiverViewModelUdp.ignored_bits + 1; i <= ReceiverViewModelUdp.array_index * 8; i++) {
                            receiver_udp.SetNewMsgBit(i);
                        }

                        ReceiverViewModelUdp.receivedMessageNum++;
                    }

                    // 마지막 메시지 복사
                    receiver_udp.lastMessage = Arrays.copyOf(receiver_udp.checkNewMessage, receiver_udp.checkNewMessage.length);
                    receiver_udp.resetNewMessageFlag();
                }

                // 설정한 시간 동안 대기
                long interval = 100;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted");
                break;
            }
        }
    }

    public void convertAndDisplayImage(byte[][] imageData2D) {
        if (imageData2D == null || imageData2D.length == 0) {
            System.err.println("Invalid image data: no data to convert.");
            return;
        }

        int rows = imageData2D.length;
        final int FIXED_DATA_SIZE = 1014; // 송신 측에서 고정한 데이터 크기
        int totalLength = rows * FIXED_DATA_SIZE;
        byte[] imageData1D = new byte[totalLength];
        int index = 0;

        for (int i = 0; i < rows; i++) {
            if (imageData2D[i] != null) {
                // 행의 길이가 고정 크기보다 작을 수 있으므로 최소값 사용
                int lengthToCopy = Math.min(imageData2D[i].length, FIXED_DATA_SIZE);
                System.arraycopy(imageData2D[i], 0, imageData1D, index, lengthToCopy);
                index += FIXED_DATA_SIZE; // 고정 크기만큼 인덱스 증가
            } else {
                System.err.println("Row " + i + " is null. Filling with zeros.");
                Arrays.fill(imageData1D, index, index + FIXED_DATA_SIZE, (byte) 0);
                index += FIXED_DATA_SIZE;
            }
        }

        // 이미지 디스플레이
        displayImage(imageData1D);
    }


    public void displayImage(byte[] imageData) {
        try {
            // 이미지 데이터를 BufferedImage로 변환
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bais);

            if (image != null) {
                // 이미지를 JLabel에 설정
                imageLabel.setIcon(new ImageIcon(image));
            } else {
                System.err.println("Failed to decode image. The data may be corrupted.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printByteArrayAsBinary(byte[] byteArray) {
        for (byte b : byteArray) {
            // 각 바이트를 0과 1로 변환
            String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            //System.out.println(binaryString); // 변환된 이진수 출력
        }
    }
}