package test;

import java.io.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;





public class network {
	
	public static final int limit_client_mount=1000;
	public static Thread[] listenerThread=new Thread[limit_client_mount];
	public static boolean[] sink_full=new boolean[limit_client_mount];
	
	public static int socket_pointer;
	
	static private void show(String msg) {
		core_main.show(msg);
	}
	


	public static String ip_address="";
	
	//初始化并开启服务
	
	public static void ini(int port) {
		for(int i=0;i<limit_client_mount;i++)
			sink_full[i]=false;
		socket_pointer=0;
		
		//服务器开启网络连接
		SYSTEM_SOCKET system_SOCKET=SYSTEM_SOCKET.get_new_socket(port);
		while(true) {
			
			// 接受客户端连接
        	Socket socket= system_SOCKET.accept();
        	if(socket==null)
        		return;
        	core_main.clientHandlers[socket_pointer]=new Client(socket,socket_pointer);
        	
        	listenerThread[socket_pointer]=new Thread(core_main.clientHandlers[socket_pointer]);
        	Thread this_thread=listenerThread[socket_pointer];
            this_thread.start();
            
            socket_pointer++;
            if(socket_pointer>=limit_client_mount)
            	socket_pointer=0;
            // 处理客户端连接（这里可以创建一个新的线程来处理，或者放入线程池）
		}
		
		
	}
	
	
	
	
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
