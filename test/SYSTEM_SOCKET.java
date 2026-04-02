package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SYSTEM_SOCKET {
	public static void show(String content) {
		core_main.show(content);
	}

	
	public static SYSTEM_SOCKET get_new_socket(int port) {
		SYSTEM_SOCKET new_one=new SYSTEM_SOCKET(port);
		
		return new_one;
	}
	
	public int port;
	public String ip_address="";
	public String deviceName="";
	public InetAddress inetAddress;
	public ServerSocket serverSocket=null;
	
	public SYSTEM_SOCKET(int port) {
		this.port=port;
		try {
			InetAddress localhost = InetAddress.getLocalHost();
            this.deviceName = localhost.getHostName();
            this.inetAddress = InetAddress.getByName(deviceName);
            
        } catch (IOException e) {
            show(e.getMessage());
        }
		
		try {
			serverSocket = new ServerSocket(port, 0, inetAddress);
        	show("服务器启动!Server started and listening\n设备名称devicename: " + deviceName +"\n服务器ip:"+inetAddress.getHostAddress()+"     端口port:" + port);
        	
        	this.ip_address=inetAddress.getHostAddress();
        	
        } catch (IOException e) {
            show(e.getMessage());
        }
		
		//服务端广播服务器信息
		broadcast(inetAddress, port);
		
	}
	
	public Socket accept() {
		
		try {
			Socket socket= serverSocket.accept();
	    	show("接收客户端连接！ip地址: " + socket.getInetAddress());
	    	return socket;
    	}catch (IOException e) {
    		show(e.getMessage());
        }
		return null;
	}
	
	
	//服务端广播服务器信息
	public static void broadcast(InetAddress inetAddress,int port) {
		
		new Thread(() -> {
		    List<DatagramSocket> sockets = new ArrayList<>();
		    
		    try {
		        // 获取所有网络接口
		        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		        
		        // 为每个可用接口创建socket
		        while (interfaces.hasMoreElements()) {
		            NetworkInterface networkInterface = interfaces.nextElement();
		            
		            // 跳过回环、未启用、虚拟接口
		            if (networkInterface.isLoopback() || !networkInterface.isUp() 
		                || networkInterface.isVirtual()) {
		                continue;
		            }
		            
		            try {
		                // 获取接口的IP地址
		                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
		                InetAddress localAddress = null;
		                
		                while (addresses.hasMoreElements()) {
		                    InetAddress addr = addresses.nextElement();
		                    if (addr instanceof Inet4Address) {
		                        localAddress = addr;
		                        break;
		                    }
		                }
		                
		                if (localAddress == null) continue;
		                
		                // 创建绑定到特定IP的socket
		                DatagramSocket socket = new DatagramSocket();
		                socket.setBroadcast(true);
		                sockets.add(socket);
		                
		                System.out.println("在接口上启用广播: " + networkInterface.getDisplayName() + 
		                    " IP: " + localAddress.getHostAddress());
		                
		            } catch (Exception e) {
		                System.err.println("无法在接口 " + networkInterface.getName() + 
		                    " 上创建socket: " + e.getMessage());
		            }
		        }
		        
		        if (sockets.isEmpty()) {
		            // 回退方案：创建普通广播socket
		            try {
		                DatagramSocket socket = new DatagramSocket();
		                socket.setBroadcast(true);
		                sockets.add(socket);
		                System.out.println("使用默认广播socket");
		            } catch (Exception e) {
		                System.err.println("无法创建广播socket: " + e.getMessage());
		                return;
		            }
		        }
		        
		        // 主广播循环
		        while (true) {
		            try {
		                String message = "SERVER:" + inetAddress.getHostAddress() + ":" + port;
		                byte[] buffer = message.getBytes("UTF-8");
		                
		                // 在255.255.255.255上发送广播
		                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
		                int broadcastPort = 8888;
		                
		                // 在每个socket上发送广播
		                for (DatagramSocket socket : sockets) {
		                    try {
		                        DatagramPacket packet = new DatagramPacket(
		                            buffer, buffer.length, broadcastAddress, broadcastPort
		                        );
		                        socket.send(packet);
		                        //System.out.println("广播发送成功: " + message);
		                        
		                    } catch (Exception e) {
		                        System.err.println("socket广播失败: " + e.getMessage());
		                    }
		                }
		                
		                Thread.sleep(3000); // 每3秒广播一次
		                
		            } catch (InterruptedException e) {
		                System.out.println("广播线程被中断");
		                Thread.currentThread().interrupt();
		                break;
		            } catch (Exception e) {
		                System.err.println("广播失败: " + e.getMessage());
		                Thread.sleep(10000);
		            }
		        }
		        
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        // 清理所有socket
		        for (DatagramSocket socket : sockets) {
		            if (socket != null && !socket.isClosed()) {
		                socket.close();
		            }
		        }
		    }
		}, "Broadcast-Thread").start();
	}
	
	
}
