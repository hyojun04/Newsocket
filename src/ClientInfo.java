import java.net.Socket;
import java.util.ArrayList;

// Ŭ���̾�Ʈ�� ������ ���� Ŭ����
public class ClientInfo {
    String ip;
    Socket mysocket;
    Boolean connected;
    Boolean newMsg;
    
    // ������
    public ClientInfo(String ip,Socket mysocket, Boolean connected, Boolean newMsg) {
        this.ip = ip;
        this.mysocket = mysocket;
        this.connected = connected;
        this.newMsg = newMsg;        
    }

    // ���Ǽ��� ���� get�� set �޼ҵ带 �����س���
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }

    public Socket getSocket() {
        return mysocket;
    }
   
    public Boolean getNewMsg() {
        return newMsg;
    }

    public void setNewMsg(Boolean newMsg) {
        this.newMsg = newMsg;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }
}

class TcpConnectionManager {
    // ClienttInfo ArrayList�� ����
    public static ArrayList<ClientInfo> clients_tcp = new ArrayList<>();

    // Method to add a client to the list
    public static void addClient(String ip,Socket mysocket ,Boolean connected ,Boolean newMsg) {
        clients_tcp.add(new ClientInfo(ip, mysocket ,connected, newMsg));
        
    }
 // Method to add a client to the list
    public static void setClient(int index,ClientInfo client) {
        clients_tcp.set(index,client);
        
    }

    // Method to retrieve a client's information by index
    public static ClientInfo getClient(int index) {
        return clients_tcp.get(index);
    }
    
    public boolean checkAllClientsNewMessage() {
        for (ClientInfo client : clients_tcp) {
            if (!client.getNewMsg()) {  // ������� ���� Ŭ���̾�Ʈ�� ������ false ��ȯ
                return false;
            }
        }
        System.out.println("Every client is connected.");
        return true;
    }
    
    public void AllClientsSetFalse() {
    	for (ClientInfo client: clients_tcp) {
    		if(client.getConnected()) //����Ǿ� �ִ� Ŭ���̾�Ʈ�� ���Ͽ� ���ο�޽��������� boolean false�� �ʱ�ȭ
    		client.setNewMsg(false);
    	}
    }

}
