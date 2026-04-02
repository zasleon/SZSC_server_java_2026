package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;


public class Client implements Runnable {
	private int member_No;//座位号，本次接受服务所在槽内
	private boolean member_in_use;//客户端该位置是否被占用？false为空，true为占用
	private String client_name;//用户名

	private int device;//手机或者电脑
	private int state;//客户状态：房间/在线/战斗中
	//int room_No;//自己所在房间号
	private boolean guest;//在房间中是否是宾客？true为宾客，false可能为主人或不在房间内


	private int user_ID;

	//boolean be_locked; //是否被锁（不听该客户端发来的内容）

	String message;
	boolean not_be_read;//客户端发来的message是否还没被系统读（分析），true为没被读，false为读了
	boolean listen;//是否监听中
	//HANDLE listen_this;//指向“监听客户端发来内容”的线程，在accept客户端后创建该线程并附给这个指针
	String p_message;//系统将客户端发来的信息先存储在message，要处理时放入p_message处理

	public Client(Socket socket,int pointer) {
		ini(socket,pointer);
        new Thread(new listen_client()).start();
        show("已开启监听线程");
		
	}
	
	private void ini(Socket socket,int pointer) {
		member_in_use=false;
		state=core_protocol.in_online;
		member_No=pointer;
		
		
    	network.sink_full[member_No]=true;
        clientSocket = socket;
        //jsonObject = new JSON_process();
        whether_connect=true;
        msgString="";
	}
	
	public int getclientstate() {
		return state;
	}
	public void setclientstate(int signal) {
		state=signal;
	}
	
	
	
	
	//private JSON_process jsonObject;//用于发送的json格式数据
    private Socket clientSocket;
    private Boolean whether_connect;

    private String msgString;
    
    private void disconnect() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}
    
    class listen_client implements Runnable
    {
		
		
    	@Override
        public void run() {
    		
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"gbk"))) {
				
                // 读取客户端发送的消息
                while (true) {
                	
                	msgString= in.readLine();
                	//jsonObject = new JSON_process();
                	if(network_error())
                		return;
                	
                }
            } catch (IOException e) {
            	
                show("收听错误"+e.getMessage());
                return ;
            } 
    	}
    }
    
    boolean main_listen=true;
    
    public void lock_listen() {
    	main_listen=false;
    }
    public void unlock_listen() {
    	main_listen=true;
    }
    public String listen(boolean whether_main) {
    	
    	show("开始收听");
    	while(msgString.isBlank())
    	{
    		sleep(300);
    		if(whether_main)
    			if(!main_listen)//如果不让主线程监听
    				continue;
    		
    		if(msgString==null)//如果socket连接断开，则msg接收字符串信息为null
        	{
        		whether_connect=false;
        		return "";
        	}
    	}
    	show("收听结束");
    	if(msgString==null)//如果socket连接断开，则msg接收字符串信息为null
    	{
    		whether_connect=false;
    		return "";
    	}
    	
    	show("收到客户端发来的信息 [长度"+msgString.length()+"]:\n" +msgString);
    	String receive_msg=msgString;
    	msgString="";
    	return receive_msg;
	}
    
    
    
    public void send(JSON_process reply_msg) {

    	sleep(500);
    	//在开头加字符是为了让客户端能准确检索到第一个“{”
    	//客户端收到报文开头可能有乱码，通过重新检索第一个“{”并将之前部分全部删除后，获取json格式数据
    	reply_msg.add("state", getclientstate());
    	String msg= "aaa"+reply_msg.getString()+"\n"; 
    	if(whether_connect)
	    	try{
	    		ObjectOutputStream oos=new ObjectOutputStream(clientSocket.getOutputStream());
	    		byte[] messageBytes = msg.getBytes(Charset.forName("gbk"));
	    		//oos.writeBytes(kkkString);
	    		oos.write(messageBytes);
	    		show("发送信息(长度"+msg.length()+")\n"+msg); 
	    		oos.flush();
			}catch (java.rmi.UnknownHostException e) {
				show("rmi发送错误"+e.getMessage());// TODO: handle exception
			}catch (IOException e1){
				show("发送错误"+e1.getMessage());
				// TODO: handle exception
			}
    	reply_msg=new JSON_process();//清空原有内容
    	
    	
	}
    
    public int get_socket_pointer() {
		return member_No;
	}
    
    
    

   
    
    private void client_offline() {
    	msgString=null;
    	network.sink_full[member_No]=false;
    	show(member_No+"号用户下线!");
    	disconnect();
    	if(!(network.listenerThread[member_No]==null))
    		network.listenerThread[member_No].interrupt();
    	else {
			show("listenerThread  "+member_No+"  为空");
		}
	}

    @Override
    public void run() {
   	 	show("开启该客户端服务");
   	 	sleep(500);
   	 
   	 	start_service();
        client_offline();
        
   	
   }
    
	public void start_service() {
		JSON_process reply_msg=new JSON_process();
	   	 
		reply_msg.add("signal", core_protocol.start_link);
		reply_msg.add("state", core_protocol.in_online);
	       
		send(reply_msg);
	    reply_msg=new JSON_process(listen(true));
	       
	    int device_type=reply_msg.getInt("device_type");
	    switch(device_type){
	       case core_protocol.android_phone:device=core_protocol.android_phone;show("安卓端设备!");break;
	       case core_protocol.win_console:device=core_protocol.win_console;show("win_console端设备!");break;
	       	default:show("未能识别该设备!强制断开连接!");client_offline();return;
	    }
	    //用户名
	    client_name=reply_msg.getString("username");
	    if(client_name==null)
	    {show("用户名获取错误");return;}
	    int username_lenth_limit=10;
	    if(client_name.length()>10)
	    {show("用户名过长(不可大于"+username_lenth_limit+")"+client_name);return;}
	    if(!SZSC_asset_command.checkDiamondUserExists(client_name)) {
	    	show("本次登陆为【首次登陆】的新用户!进行数据库录入并且进行一次十连……");
	    	//首次登陆的新用户进行SZSC服务数据库记录创建
	    	SZSC_asset_command.user_register(client_name);
	    	int Asset_user_ID=SZSC_asset_command.get_user_ID(client_name);
	    	//先执行一次十连
	    	SZSC_asset_process.purchase(Asset_user_ID,10);
	    }
       
	    //发送登录成功
	    reply_msg=new JSON_process();
	    reply_msg.add("signal", core_protocol.login_success);
	    reply_msg.add("state", core_protocol.in_online);
	    send(reply_msg);
	       
	    while(true){
	       	
	    	if(network_error())
	    		break;
	       	
	    	String msg_content=listen(true);
	    	if(network_error())
	       		break;
	       	JSON_process get_msg = new JSON_process(msg_content);
	       	
	       	switch (get_msg.getInt("service_kind")) {
	  			case core_protocol.VV_service: {
	  				reply_msg=new JSON_process(VV_DB.provide_service(get_msg));
	  				reply_msg.add("state", core_protocol.in_online);
	  	            send(reply_msg);
	  	            break;
	  	             
	  			}
				case core_protocol.SZSC_service:{
					SZSC_service.provide_service(this,get_msg);
					break;
				}
				default:
					show("Unexpected service_kind value: " + get_msg.getInt("service_kind"));
	       	}
	    }//执行到这里必然连接断开
	    switch(getclientstate()){
	       	case SZSC_protocol.SZSC_in_game:
	       	case SZSC_protocol.SZSC_in_room:
	       		SZSC_room.exit_the_room(get_client_unique_identity());
	       		
	       		break;
	    }
	  
   }
    
   
   public boolean network_error() {
	   if(msgString==null)
	   {
	   		
	   		return true;
	   	}
	   if(!whether_connect)
		{
			show("第1项退出");
			return true;
		}
		if(!clientSocket.isConnected())
		{
			show("第2项退出");
			return true;
		}
		return false;
	
    }
    public int get_client_unique_identity(){
    	return member_No;
	}
    public String get_client_name() {
		return client_name;
	}
    
    
    static private void show(String msg) {
		//core_main.show(msg);
		System.out.println(msg);
	}
	static private void sleep(int number) {
		core_main.sleep(number);
	}
	
	
	
}
