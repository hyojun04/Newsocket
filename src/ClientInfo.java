import java.net.Socket;
import java.util.ArrayList;

// 클라이언트의 정보를 담은 클래스
public class ClientInfo {
    String ip;
    Socket mysocket;
    Boolean connected;
    Boolean newMsg;
    
    // 구성자
    public ClientInfo(String ip,Socket mysocket, Boolean connected, Boolean newMsg) {
        this.ip = ip;
        this.mysocket = mysocket;
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
    // ClienttInfo ArrayList를 생성
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
        if (index >=0 && index < clients_tcp.size()) {
        	return clients_tcp.get(index);
        }else {
        	//인덱스의 범위를 벗어날 경우 예외처리 또는 null 반환
        	throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }
 // Method to retrieve the index of a specific client in the list
    public static int getIndex(ClientInfo client) {
        int index = clients_tcp.indexOf(client); // ArrayList의 indexOf 메소드를 사용
        if (index != -1) {
            return index; // 유효한 인덱스를 반환
        } else {
            throw new IllegalArgumentException("Client not found in the list.");
        }
    }

    
    public boolean checkAllClientsNewMessage() {
    	
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
    		if(client.getConnected()) //연결되어 있는 클라이언트만 한하여 새로운메시지에대한 boolean false로 초기화
    		client.setNewMsg(false);
    	}
    }
    
    public void AllClientsReset() {
    	clients_tcp.clear();
    }

}
