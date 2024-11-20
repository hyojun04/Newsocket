import java.net.*;
import java.util.Enumeration;

public class BroadcastAddressFinder {

    public static InetAddress getWiFiBroadcastAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Ensure interface is up, not loopback, and is Wi-Fi or wireless
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    String interfaceName = networkInterface.getDisplayName();
                    // Check for common Wi-Fi names on different platforms
                    if (interfaceName.contains("Wi-Fi") || interfaceName.equals("en0") || interfaceName.equals("wlan0")) {
                        System.out.println(interfaceName); 
                        
                        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = address.getBroadcast();
                            if (broadcast != null) {
                                return broadcast; // Return broadcast address
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Error retrieving network interfaces: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null; // No Wi-Fi broadcast address found
    }
}
