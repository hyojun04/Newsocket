public class StartUdpCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;

    public StartUdpCheckThread(ReceiverViewModelUdp receiver_udp, TcpSocketConnection tcpConnection) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
    }

    @Override
    public void run() {
        while (true) {
            if (receiver_udp.hasNewMessage()) {
                
            System.out.println("UDP broadcast message is sent");
            NewSocket.clients_tcp.set(tcpConnection.perminent_id, true); //메시지를 받았으면 수신여부 true로 변경
            if(NewSocket.clients_tcp.get(tcpConnection.perminent_id) == true)
            	System.out.println("Client Num: "+tcpConnection.perminent_id+" Changed index value TRUE");
                // 수신 상태 플래그 초기화
                receiver_udp.resetNewMessageFlag();
            }

            try {
                Thread.sleep(10); // 10ms마다 수신 여부를 확인
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
