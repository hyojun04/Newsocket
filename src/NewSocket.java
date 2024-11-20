import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;


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
    private JButton stopSetup_Button;
    private JButton resetProgram;
    
    private ReceiverViewModelUdp receiver_udp;
    private TcpSocketConnection tcp_connection;
    private SenderViewModelUdp sender_udp;
    private TcpConnectionAccepter tcp_accepter;
    
    private JTextField inputIp;
    private JTextField inputIp_udpBroad;
    
    private int sentMessageCount = 0;       // �޽����� ��ȣ 
    private int sentMessageCount_actual = 0; //���� ���� �޽��� ī��
    
    private Timer udpTimer_IP; //SETUP ������ ���� UDP broadcast�� ���� Ÿ�̸�
    private Timer udpTimer;// UDP ������ ���� Ÿ�̸�
    private Thread accepterThread;
    
    
    public static TcpConnectionManager tcpconnectionmanager;
    public static int clients_tcp_index = 0; // ���ڸ޽����� �迭�� �ε���
    
    
    public NewSocket() {
    	
    	//Ŭ���̾�Ʈ Ŭ���� �迭 ����
    	tcpconnectionmanager = new TcpConnectionManager();
    	
        // GUI �⺻ ����
        setTitle("P2P UCP Broadcast - newServer_v4_241114 Fetching");
        setSize(1300, 800); // ũ�⸦ ���� �� �÷���
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ���� �޽��� ���� (�� ���� ��ġ)
        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);
        

        JScrollPane receivedScrollPane = new JScrollPane(receivedMessagesArea);
        receivedScrollPane.setBorder(BorderFactory.createTitledBorder("Received Messages"));

        // Clear ��ư �߰� (���� �޽���)
        clearReceiveButton = new JButton("Clear Received Messages");
        clearReceiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedMessagesArea.setText(""); // ���� �޽��� â�� �ؽ�Ʈ �ʱ�ȭ
                if (receiver_udp != null) {
                    receiver_udp.reset_message_num(); // ���� �޽��� ī���� �ʱ�ȭ
                }
            }
        });

        // ���� �޽��� �Է� ���� (�߰��� ��ġ)
        sendMessageArea = new JTextArea();
        sendMessageArea.setLineWrap(true);
        sendMessageArea.setWrapStyleWord(true);
        JScrollPane sendScrollPane = new JScrollPane(sendMessageArea);
        sendScrollPane.setBorder(BorderFactory.createTitledBorder("Send Message"));

        // Clear ��ư �߰� (���� �޽���)
        clearSendButton = new JButton("Clear Send Messages");
        clearSendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageArea.setText(""); // ���� �޽��� â�� �ؽ�Ʈ �ʱ�ȭ
                sentMessageCount = 0;        // ���� �޽��� ī���� �ʱ�ȭ
            }
        });

        // �ܼ� ���� (�� �Ʒ��� ��ġ)
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
        consoleScrollPane.setPreferredSize(new Dimension(1000, 150)); // ���� �г� ũ�� ���̱�

        // ��ư ����
        connection_Button = new JButton("Connection");
        sendButton_UDP = new JButton("Send UDP Message");
        connectionSetup_Button = new JButton("Connection Setup");
        receiveButton_UDP = new JButton("Wait for UDP");
        sendStopButton_UDP = new JButton("Stop UDP Msg");
        stopSetup_Button = new JButton("Stop Connection Setup");
        resetProgram = new JButton("Reset Program");
        
        // TCP ������ IP �Է� �ʵ�
        inputIp = new JTextField("192.168.0.228", 15);
        //�ڵ����� WifiBroadAddress�� ã�� Ŭ���� ����
      		InetAddress BroadcastAddress = BroadcastAddressFinder.getWiFiBroadcastAddress();
      		if (BroadcastAddress != null) {
      			String BroadIP = BroadcastAddress.getHostAddress();
      			System.out.println("Found IP: "+ BroadIP);
      			inputIp_udpBroad = new JTextField(BroadIP,15);//���α׷��� ������ڸ��� IP�� �о�鿩 Broadcast �ּҸ� �Է��صд�.
      			
      		}
       
        
        
        // ��ư�� �ؽ�Ʈ �ʵ带 ���� �г�
        JPanel buttonPanel_main = new JPanel(new FlowLayout());
        JPanel buttonPanel_1 = new JPanel(new FlowLayout());
        JPanel buttonPanel_2 = new JPanel(new FlowLayout());
        JPanel buttonPanel_3 = new JPanel(new FlowLayout());
        JPanel buttonPanel_4 = new JPanel(new BorderLayout());
        JPanel buttonPanel_5 = new JPanel(new BorderLayout());
        JPanel buttonPanel_6 = new JPanel(new BorderLayout());
        //���ο� ��ư�� ���� �г� ����
        JPanel buttonSmallPanel = new JPanel(new BorderLayout());
        
        buttonPanel_1.add(new JLabel("Client1 IP:"));
        buttonPanel_1.add(inputIp);
        buttonSmallPanel.add(buttonPanel_1,BorderLayout.NORTH);       
        buttonPanel_main.add(buttonSmallPanel);
        
        buttonPanel_2.add(new JLabel("Broad IP:"));
        buttonPanel_2.add(inputIp_udpBroad);
        buttonPanel_main.add(buttonPanel_2);
          
        //buttonPanel_3.add(connection_Button);        
        buttonPanel_main.add(buttonPanel_3);
        
        buttonPanel_4.add(connectionSetup_Button,BorderLayout.NORTH);
        buttonPanel_4.add(stopSetup_Button,BorderLayout.SOUTH);
        buttonPanel_main.add(buttonPanel_4);
        buttonPanel_5.add(sendButton_UDP,BorderLayout.NORTH);
        buttonPanel_5.add(sendStopButton_UDP,BorderLayout.SOUTH);
        //buttonPanel_5.add(receiveButton_UDP,BorderLayout.SOUTH);
        buttonPanel_main.add(buttonPanel_5);
               
        
        buttonPanel_6.add(resetProgram);	
        buttonPanel_main.add(buttonPanel_6);
        

        // ���� ���̾ƿ� ����
        setLayout(new BorderLayout());

        // ���� �޽��� + Clear ��ư�� ���� �г�
        JPanel receivedPanel = new JPanel(new BorderLayout());
        receivedPanel.add(receivedScrollPane, BorderLayout.CENTER);
        receivedPanel.add(clearReceiveButton, BorderLayout.SOUTH);
        receivedPanel.setPreferredSize(new Dimension(1000, 400)); // ���� �г� ũ�� �ø���
        
        // ���� �޽��� + Clear ��ư�� ���� �г�
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.add(sendScrollPane, BorderLayout.CENTER);
        sendPanel.add(clearSendButton, BorderLayout.SOUTH);
        sendPanel.setPreferredSize(new Dimension(1000, 150)); // ���� �г� ũ�� ���̱�
        
        // Center Panel: ��� â�� ������ ũ��� �����ϱ� ���� GridLayout
        JPanel centerPanel = new JPanel(new BorderLayout(3, 1));  // 3 rows, 1 column
        centerPanel.add(receivedPanel,BorderLayout.NORTH);  // ���� �޽��� â + Clear ��ư        
        centerPanel.add(sendPanel,BorderLayout.CENTER);      // ���� �޽��� â + Clear ��ư
        centerPanel.add(consoleScrollPane,BorderLayout.SOUTH);   // �ܼ� â

        add(centerPanel, BorderLayout.CENTER);      // �߾ӿ� 3���� â�� ���� ũ��� ��ġ
        add(buttonPanel_main, BorderLayout.SOUTH);        // �ϴܿ� ��ư �г� ��ġ
        
        
        
        // ���� ��ư �̺�Ʈ ó��
        connection_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tcp_connection = new TcpSocketConnection();
                String serverIP = inputIp.getText();
                tcp_connection.startClient(serverIP);
                consoleArea.append("Client: "+serverIP+"�� TCP ���ϰ� ����Ǿ����ϴ�. \n");
                
            }
        });
        //�ش� ��ư�� ������ �����ư�� ���� ���Ͽ����� ������ 
        
     // TCP ���Ͽ��� ��ư �̺�Ʈ ó��
        connectionSetup_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            	//������ ����: ���Ľ�����ó���� ���������� socket.accept�ϴ� �κп��� ���߰� �ȴ�.
            	if (tcp_accepter == null) {
            		tcp_accepter = new TcpConnectionAccepter(receivedMessagesArea,consoleArea);
            		accepterThread = new Thread(tcp_accepter);
           		 	accepterThread.start();
                    consoleArea.append("TCP Connection Accepter thread started.\n");
            	}
            	else {
            		consoleArea.append("TCP Connection Accepter thread already running.\n");
            	}
                
                String broadIP = inputIp_udpBroad.getText();
                
                //TCP ������ ����, UDP Broad ����, stopUDPsend ��ư ������ �۽� ���� -> ���� ����Ǹ� ���ߵ����ϴ� ��Ŀ�������� ����
                SenderIPUdp sender_udp_ip = new SenderIPUdp();
                udpTimer_IP = new Timer();
                udpTimer_IP.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                    	sender_udp_ip.startSend(broadIP);
                    }
                }, 0, 500); // 500ms �������� ����

                
                consoleArea.append("Connection Setup Ready \n");
                
                
            }
        });
        //SETUP ���� ��ư
        stopSetup_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (udpTimer_IP != null) {
                    udpTimer_IP.cancel();  // Ÿ�̸� ����
                    udpTimer_IP = null;    // Ÿ�̸� ��ü�� null�� �����Ͽ� ���� �ʱ�ȭ
                    consoleArea.append("SETUP�� �����Ǿ����ϴ�.\n");
                }
            }
        });

        sendButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	//receiver_tcp = tcp_connection.receiverViewModel_tcp(); //ReceiverViewModel�� �ν��Ͻ��� �޾ƿ�
                if (udpTimer != null) {
                    udpTimer.cancel();  // Ÿ�̸� ���� (������ ���� ���̾��ٸ�)
                }
                
                sender_udp = new SenderViewModelUdp();
                String serverIP = inputIp_udpBroad.getText();

                udpTimer = new Timer();
                udpTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                    	// �ֱ������� Ŭ���̾�Ʈ ���� üũ
                    	
                        if (tcpconnectionmanager.checkAllClientsNewMessage()) {
                        	
                            consoleArea.append("��� Ŭ���̾�Ʈ�κ��� "+"[" + sentMessageCount + "]�� ���� �޽����� �޾����Ƿ� ��ε�ĳ��Ʈ ����\n");                            
                            tcpconnectionmanager.AllClientsSetFalse(); //���ڸ޽��� ���ſ��� �ʱ�ȭ 
                            sentMessageCount++; // ���� �޽��� ī��Ʈ ����
                            return; // ���� ���� �� ����
                        }
                       if (sentMessageCount == 0) sentMessageCount++; // ù �޽��� �߼۶��� ī��Ʈ ���� 
                       
                       
                        sender_udp.startSend(serverIP,sentMessageCount,"C:\\bird.jpg");   // 50ms���� UDP �޽��� ����
                        
                        // sendMessageArea�� ������ �޽��� �߰�
                        sentMessageCount_actual++;
                        sendMessageArea.append("[" + sentMessageCount_actual +"][" +sentMessageCount + "] message via UDP: 'A' * 61440 bytes\n");
                        
                        //consoleArea.append("UDP�� �޽����� ���۵Ǿ����ϴ�.\n");
                    }
                }, 0, 50); // 50ms �������� ����
            }
        });
        // UDP ���� ���� ��ư
        sendStopButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (udpTimer != null) {
                    udpTimer.cancel();  // Ÿ�̸� ����
                    udpTimer = null;    // Ÿ�̸� ��ü�� null�� �����Ͽ� ���� �ʱ�ȭ
                    consoleArea.append("UDP �޽��� ������ �����Ǿ����ϴ�.\n");
                }
            }
        });
        receiveButton_UDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiver_udp = new ReceiverViewModelUdp(receivedMessagesArea);  // receivedMessagesArea ����
                new Thread(() -> receiver_udp.startServer()).start();
                consoleArea.append("UDP ���� ��� ��...\n");
                
            }
        });
        
        
        resetProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	//Client List���� ���  Client ��ü ����
            	//System.out.println("Reset Program completed 1");
            	if (tcp_accepter != null) {
            		tcp_accepter.closeTcpSocket();
                    accepterThread.interrupt();
                  //tcp_accepter null�� �ʱ�ȭ �� ��� �޼��� â �ʱ�ȭ
                    tcp_accepter = null;
            	}
            	System.out.println("TcpConnectionAccepter sets null");
                
                consoleArea.setText("Reset Program");
                receivedMessagesArea.setText("");
                sendMessageArea.setText("");
                //Client �ʱ�ȭ
                tcpconnectionmanager.AllClientsReset();
                clients_tcp_index = 0;
                
                //Udp sender �ʱ�ȭ
                if(udpTimer != null&& udpTimer_IP != null) {
                	udpTimer = null;
                    udpTimer_IP = null;
                }
                // message number �ʱ�ȭ
                sentMessageCount = 0;
                
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
