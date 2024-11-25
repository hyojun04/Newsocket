import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class NewSocket extends JFrame {

    private JTextArea receivedMessagesArea;
    private JTextArea sendMessageArea;
    private JTextArea consoleArea;
    private JButton connection_Button;
    private JButton sendButton_UDP;
    private JButton sendStopButton_UDP;
    private JButton connectionSetup_Button;
    private JButton receiveButton_UDP;
    private JButton clearReceiveButton;
    private JButton clearSendButton;
    private JLabel imageLabel; // 이미지 표시용 JLabel
   
   
    public static ReceiverViewModelUdp receiver_udp;
    private TcpSocketConnection tcp_connection;
   
    private JTextField inputIp;
    private JTextField inputIp_udpBroad;
   
    private Timer udpTimer;                 // UDP 전송을 위한 타이머
   
   
   
    public NewSocket() {
    	
        // GUI 기본 설정
        setTitle("P2P UCP Broadcast - Client_v4");
        setSize(1300, 600); // 크기를 조금 더 늘려줌
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 수신 메시지 영역 (맨 위에 위치)
        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);
        JScrollPane receivedScrollPane = new JScrollPane(receivedMessagesArea);
        receivedScrollPane.setBorder(BorderFactory.createTitledBorder("Received Messages"));

        // Clear 버튼 추가 (수신 메시지)
        clearReceiveButton = new JButton("Clear Received Messages");
        clearReceiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedMessagesArea.setText(""); // 수신 메시지 창의 텍스트 초기화
                if (receiver_udp != null) {
                    receiver_udp.reset_message_num(); // 수신 메시지 카운터 초기화
                }
            }
        });

        // 전송 메시지 입력 영역 (중간에 위치)
        sendMessageArea = new JTextArea();
        sendMessageArea.setLineWrap(true);
        sendMessageArea.setWrapStyleWord(true);
        JScrollPane sendScrollPane = new JScrollPane(sendMessageArea);
        sendScrollPane.setBorder(BorderFactory.createTitledBorder("Send Message"));

        // Clear 버튼 추가 (전송 메시지)
        clearSendButton = new JButton("Clear Send Messages");
        clearSendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageArea.setText(""); // 전송 메시지 창의 텍스트 초기화
               
            }
        });

        // 콘솔 영역 (맨 아래에 위치)
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
       
        // 이미지를 표시할 JLabel 초기화
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER); // 이미지 가운데 정렬
        imageLabel.setBorder(BorderFactory.createTitledBorder("Received Image"));

        // 버튼 생성
        connection_Button = new JButton("Connection");
        sendButton_UDP = new JButton("Send UDP Message");
        connectionSetup_Button = new JButton("Connection Setup");
        receiveButton_UDP = new JButton("Wait for UDP");
        sendStopButton_UDP = new JButton("Stop UDP Msg");
        // IP 입력 필드
        inputIp = new JTextField("192.167.11.36", 15); //192.168.0.228
        inputIp_udpBroad = new JTextField("192.168.195.255",15);
        // 버튼과 텍스트 필드를 담을 패널
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(new JLabel("Client IP:"));
        buttonPanel.add(inputIp);
        buttonPanel.add(new JLabel("Broad IP:"));
        buttonPanel.add(inputIp_udpBroad);
        buttonPanel.add(connection_Button);
        //buttonPanel.add(connectionSetup_Button);
        //buttonPanel.add(sendButton_UDP);
        buttonPanel.add(receiveButton_UDP);
       // buttonPanel.add(sendStopButton_UDP);

        // 메인 레이아웃 설정
        setLayout(new BorderLayout());

        // 수신 메시지 + Clear 버튼을 위한 패널
        JPanel receivedPanel = new JPanel(new BorderLayout());
        receivedPanel.add(receivedScrollPane, BorderLayout.CENTER);
        receivedPanel.add(clearReceiveButton, BorderLayout.SOUTH);      

        // 이미지 표시용 패널 추가
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // 전송 메시지 + Clear 버튼을 위한 패널
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.add(sendScrollPane, BorderLayout.CENTER);
        sendPanel.add(clearSendButton, BorderLayout.SOUTH);

        // Center Panel: 모든 창을 동일한 크기로 설정하기 위한 GridLayout
        JPanel centerPanel = new JPanel(new GridLayout(4, 1));  // 4 rows, 1 column
        centerPanel.add(receivedPanel);  // 수신 메시지 창 + Clear 버튼
        centerPanel.add(imagePanel);        // 이미지 영역 추가
        centerPanel.add(sendPanel);      // 전송 메시지 창 + Clear 버튼
        centerPanel.add(consoleScrollPane);   // 콘솔 창

        add(centerPanel, BorderLayout.CENTER);      // 중앙에 3개의 창을 같은 크기로 배치
        add(buttonPanel, BorderLayout.SOUTH);        // 하단에 버튼 패널 배치
       
       
       
        // 연결 버튼 이벤트 처리
        connection_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tcp_connection = new TcpSocketConnection();
                receiver_udp = new ReceiverViewModelUdp(receivedMessagesArea);
                String serverIP = receiver_udp.startConnect_to_tcp();
                tcp_connection.startClient(serverIP);
                consoleArea.append("Client: "+serverIP+"가 TCP 소켓과 연결되었습니다. \n");
               
            }
        });
        //해당 버튼을 눌러야 연결버튼을 통한 소켓연결이 가능함
     // TCP 수신 버튼 이벤트 처리
        connectionSetup_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //server_tcp = tcp_connection.receiverViewModel_tcp(); //ReceiverViewModel의 인스턴스를 받아옴
                TcpConnectionAccepter tcp_accepter = new TcpConnectionAccepter();
                tcp_accepter.startServer();
                consoleArea.append("TCP 소켓 연결 완료\n");
                //System.out.println("Waiting for TCP");
            }
        });

        sendButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (udpTimer != null) {
                    udpTimer.cancel();  // 타이머 중지 (이전에 동작 중이었다면)
                }
               
               
               

                udpTimer = new Timer();
                udpTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                    	
                    }
                }, 0, 2000); // 2s 간격으로 실행
            }
        });
        // UDP 전송 중지 버튼
        sendStopButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (udpTimer != null) {
                    udpTimer.cancel();  // 타이머 중지
                    udpTimer = null;    // 타이머 객체를 null로 설정하여 상태 초기화
                    consoleArea.append("UDP 메시지 전송이 중지되었습니다.\n");
                }
            }
        });
        receiveButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiver_udp = new ReceiverViewModelUdp(receivedMessagesArea);  // receivedMessagesArea 전달
                new Thread(() -> receiver_udp.startServer()).start();
                consoleArea.append("UDP 수신 대기 중...\n");
                //UDP Broad메시지를 수신하였지 체크하는 스레드 생성
                StartUDPCheckThread udpCheckThread = new StartUDPCheckThread(receiver_udp,tcp_connection,imageLabel);
                Thread udpCheck = new Thread(udpCheckThread);
                udpCheck.start();
               
            }
        });
    }
   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NewSocket().setVisible(true);
            }
        });
    }
}