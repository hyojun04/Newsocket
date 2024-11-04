public class StartUDPCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;
    

    public StartUDPCheckThread(ReceiverViewModelUdp receiver_udp, TcpSocketConnection tcpConnection) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
        
    }

    @Override
    public void run() {
        while (true) {
            
        	try {

                // 새로운 메시지가 수신된 경우
                System.out.println("StartUDPCheckThread is awake");
                
                //byte배열만 Ack메시지로 보내게된다. sendAckMessage는 재정의되어있음
                tcpConnection.sendAckMessage(ReceiverViewModelUdp.checkNewMessage);
                System.out.println(ReceiverViewModelUdp.checkNewMessage +" is transmitted");
                System.out.println("(1) UDP Message state: " + receiver_udp.hasNewMessage());

                // 플래그 초기화
                receiver_udp.resetNewMessageFlag();
                System.out.println("(2) UDP Message state: " + receiver_udp.hasNewMessage());
                
                
                // 설정한 시간 동안 대기
                long interval = 50;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // 스레드가 중단되었을 때 예외 처리
                System.out.println("Thread was interrupted");
                break;
            }
        }
    }
}