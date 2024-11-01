import java.util.ArrayList;

// 클라이언트의 정보를 담은 클래스
public class ClientInfo {
    String ip;
    Boolean newMsg;
    Boolean connected;

    // 구성자
    public ClientInfo(String ip, Boolean connected, Boolean newMsg) {
        this.ip = ip;
        this.connected = connected;
        this.newMsg = newMsg;        
    }

    // 편의성을 위해 get과 set 메소드를 생성해놓음
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
    // ClienttInfo ArrayList를 생성
    public static ArrayList<ClientInfo> clients_tcp = new ArrayList<>();

    // Method to add a client to the list
    public static void addClient(String ip, Boolean connected ,Boolean newMsg) {
        clients_tcp.add(new ClientInfo(ip, connected, newMsg));
        
    }
 // Method to add a client to the list
    public static void setClient(int index,ClientInfo client) {
        clients_tcp.set(index,client);
        
    }

    // Method to retrieve a client's information by index
    public static ClientInfo getClient(int index) {
        return clients_tcp.get(index);
    }
    
    public boolean checkAllClientsConnected() {
        for (ClientInfo client : clients_tcp) {
            if (!client.getNewMsg()) {  // 연결되지 않은 클라이언트가 있으면 false 반환
                return false;
            }
        }
        System.out.println("Every client is connected.");
        return true;
    }
    
    public void AllClientsSetFalse() {
    	for (ClientInfo client: clients_tcp) {
    		if(client.getConnected()) //연결되어 있는 클라이언트만 한하여 새로운메시지에대한 boolean false로 변경
    		client.setNewMsg(false);
    	}
    }

}
