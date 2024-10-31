public class StartUDPCheckThread implements Runnable {
    private final ReceiverViewModelUdp receiver_udp;
    private final TcpSocketConnection tcpConnection;
    private final Server_Tcp receiver_tcp;

    public StartUDPCheckThread(ReceiverViewModelUdp receiver_udp, Server_Tcp receiver_tcp, TcpSocketConnection tcpConnection) {
        this.receiver_udp = receiver_udp;
        this.tcpConnection = tcpConnection;
        this.receiver_tcp = receiver_tcp;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (receiver_udp) { // receiver_udp�� ���� ����ȭ : ����ȭ ���� ���� �� tcp �۽Ű� udp ������ ������ �߻�����
                while (!receiver_udp.hasNewMessage()) { // �޽����� ���� ��� ���
                    try {
                        receiver_udp.wait(); // ���
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // ���ο� �޽����� ���ŵ� ���
                System.out.println("UDP broadcast message is sent");
                tcpConnection.sendAckMessage(receiver_udp.numberStr_message + "_" + receiver_udp.numberStr_packet); // ack �޽����� message num_packet num�� �����ֵ��� ����
                System.out.println("(1) UDP Message state: " + receiver_udp.hasNewMessage());

                // �÷��� �ʱ�ȭ
                receiver_udp.resetNewMessageFlag();
                System.out.println("(2) UDP Message state: " + receiver_udp.hasNewMessage());
            }
        }
    }
}
