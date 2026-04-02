package test;

import java.util.ArrayList;
import java.util.List;



public class SZSC_service {
	public static List<SZSC_room> szsc_rooms=new ArrayList<>();
	
	public static int system_original_state() {//获取系统初始状态
		return core_protocol.in_online;
	}
	
	public static void SZSC_tips(Client c1,String msg)
	{
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal",core_protocol.toast_tips);
		reply_msg.add("content",msg);
		send_msg(c1,reply_msg);
	}
	
	public static int get_client_unique_identity(Client client) {
		return client.get_client_unique_identity();
	}
	
	
	public static void sleep(int time) {
		core_main.sleep(time);
	}
	public static void show(String msg) {
		//core_main.show(msg);
		System.out.println(msg);
	}
	public static String getCurrentTime() {
		return core_main.getCurrentTime();
	}
	
	
	public static void provide_service(Client client,JSON_process json_msg)
	{
		String msgString;
		int signal=json_msg.getInt("signal");
		int user_ID=client.get_client_unique_identity();
		switch(signal)
		{
			case SZSC_protocol.SZSC_apply_create_room:{//创建房间
				
				if(client.getclientstate()==SZSC_protocol.SZSC_in_room)
				{SZSC_tips(client,"你已在房间内!无法继续创建!");break;}
				if(client.getclientstate()!=system_original_state())
				{SZSC_tips(client,"当前状态无法创建房间!");break;}
				if(!SZSC_room.check_empty_room())//检查是否有空房间
				{SZSC_tips(client,"当前房间已满，无法继续创建!");break;}
	
				//执行到这里必然可以创建
				
				SZSC_room this_room=new SZSC_room();
				SZSC_player this_player=new SZSC_player();
				this_player.set_client(client);
				this_player.set_host(true);
				this_room.game.players.add(this_player);
				
				SZSC_room.rooms.add(this_room);
				
				client.setclientstate(SZSC_protocol.SZSC_in_room);
				
				{
					JSON_process reply_msg=new JSON_process();//创建成功!
					reply_msg.add("signal",SZSC_protocol.SZSC_create_room_success);
					reply_msg.add("playernumber",SZSC_protocol.playernumber);
					reply_msg.add("master",client.get_client_name());
					send_msg(client, reply_msg);
				}
				
				{
					JSON_process reply_msg=new JSON_process();
					reply_msg.add("signal",SZSC_protocol.SZSC_room_tips);
					reply_msg.add("content","房间创建成功!");
					this_room.room_broadcast(reply_msg);
				}
				//show_room_state();
			}
				break;
			case SZSC_protocol.SZSC_apply_show_roomlist:{
				if(client.getclientstate()!=system_original_state())
				{SZSC_tips(client,"当前状态无法继续加入房间!");break;}
				if(!SZSC_room.SZSC_check_in_use_room())//检查是否有房间可加入
				{SZSC_tips(client,"当前没有使用正在使用的房间!");break;}
				
				JSON_process reply_msg=new JSON_process(SZSC_room.SZSC_show_all_room());
				
				send_msg(client,reply_msg);
			}
				break;
			case SZSC_protocol.SZSC_apply_enter_room://选择了加入房间!
				{
					int room_No=json_msg.getInt("room_ID");
					if(room_No<0||room_No>SZSC_room.rooms.size())
					{
						show("包内数据错误!room_ID="+room_No);
						break;
					}
					if(client.getclientstate()!=system_original_state())
					{SZSC_tips(client,"当前状态无法加入房间!");break;}
					SZSC_room this_room=SZSC_room.rooms.get(room_No);
					if(this_room.player_full()){SZSC_tips(client,"该房间内人满!");sleep(100);break;}
					if(this_room.current_state!=SZSC_protocol.SZSC_finish_state){SZSC_tips(client,"该房间正在游戏!");sleep(100);break;}
	
					//执行到这一步必然加入成功
					SZSC_player player=new SZSC_player();
					player.set_client(client);
					
					this_room.add_player(player);
					
					client.setclientstate(SZSC_protocol.SZSC_in_room);
					
					send_msg_signal(client, SZSC_protocol.enter_room_success);//加入成功!
					
					
					{//广播
						JSON_process reply_msg=new JSON_process();
						reply_msg.add("signal",SZSC_protocol.SZSC_room_tips);//告诉房间主人有人进来了
						reply_msg.add("content","玩家 "+client.get_client_name()+" 进来了");//告诉房间其他人进来者姓名
						this_room.room_broadcast(reply_msg);
					}
				
					//show_room_state();//服务器自己显示现在房间状态
				}
				break;
	
			case SZSC_protocol.SZSC_apply_exit_room://您选择了退出房间!
				{
					if(client.getclientstate()==SZSC_protocol.SZSC_in_game){SZSC_tips(client,"正在游戏中!无法退出房间!");break;}
					if(client.getclientstate()!=SZSC_protocol.SZSC_in_room){SZSC_tips(client,"当前状态无法退出房间!");break;}
		
					SZSC_room.exit_the_room(user_ID);//执行退出//确认退出者在哪个房间
					client.setclientstate(system_original_state());//设置退出房间用户状态为系统初始在线状态
					send_msg_signal(client,SZSC_protocol.SZSC_leave_room_success);//告诉他退出成功!
					//show_room_state();//服务器自己显示现在房间状态
				}
				break;
			case SZSC_protocol.SZSC_apply_start_game://选择了开始战斗!
				{
					if(client.getclientstate()==SZSC_protocol.SZSC_in_game)//此时必然是非房主用户进入游戏
					{
						SZSC_room this_room=SZSC_room.which_room(user_ID);
						SZSC_player p1=SZSC_room.which_player(user_ID);
						if(p1.whether_host())
							show("是房主？？？");
						this_room.game_process(p1);//执行完表示游戏结束
						break;
					}
		
					//if(c1.state==SZSC_in_game){SZSC_tips(c1,"已在游戏中!无法开始新游戏!");break;}
					if(client.getclientstate()!=SZSC_protocol.SZSC_in_room){SZSC_tips(client,"当前状态无法开始游戏!");break;}//你不在房间内!无法开始战斗!
					SZSC_room this_room=SZSC_room.which_room(user_ID);
					SZSC_player p1=SZSC_room.which_player(user_ID);
					if(this_room==null) {core_main.show("找寻房间失败!");break;}
					if(!p1.whether_host()){SZSC_tips(client,"你不是房主!无法开始游戏!");break;}//如果不是房间主人
					if(!this_room.player_full()){SZSC_tips(client,"房间内人数不足!无法开始战斗!");break;}//如果房间没有宾客或机器人
					
					//执行到这里，必然可以开始游戏
					show("开始游戏");
		
					//初始化房间游戏数据
					this_room.game_process(p1);//执行完表示游戏结束
					
					show("游戏结束");
				
				}
				break;
			case SZSC_protocol.SZSC_apply_add_robot://添加机器人
				{
					if(client.getclientstate()!=SZSC_protocol.SZSC_in_room)//如果不在房间内
					{SZSC_tips(client,"当前状态无法添加机器人!");break;}
					SZSC_room this_room=SZSC_room.which_room(user_ID);
					if(this_room==null) {core_main.show("找寻房间失败!");break;}
					SZSC_player p1=SZSC_room.which_player(user_ID);
					if(!p1.whether_host()){SZSC_tips(client,"只有房间主人才能添加机器人!");break;}	
					if(this_room.player_full()){SZSC_tips(client,"房间人数已满!无法继续添加!");break;}
					//执行到这里必然可以添加
					SZSC_player player=new SZSC_player();
					player.set_type(SZSC_protocol.SZSC_wood_robot);
					
					this_room.add_player(player);
					
					
					
					//广播更新房间所有人状态，提示机器人添加成功
					{
						JSON_process reply_msg=new JSON_process();
						reply_msg.add("signal",SZSC_protocol.SZSC_room_tips);
						reply_msg.add("content","房主添加了机器人");
						this_room.room_broadcast(reply_msg);
					}
				}
				break;
			case SZSC_protocol.SZSC_apply_remove_someone://移除某个位子上的东西
				{
					if(client.getclientstate()!=SZSC_protocol.SZSC_in_room){SZSC_tips(client,"你不在房间内!");break;}//如果不在房间内
					
					SZSC_room this_room=SZSC_room.which_room(client.get_client_unique_identity());
					SZSC_player p1=SZSC_room.which_player(client.get_client_unique_identity());
					if(!p1.whether_host())
					{SZSC_tips(client,"只有房间主人有权限修改机器人!");break;}
					int chair_number=json_msg.getInt("chair_number");
					if(chair_number<0||chair_number>=SZSC_protocol.playernumber)
					{core_main.show("错误chair_number"+chair_number);break;}
					
					SZSC_player player=this_room.game.players.get(chair_number);
					if(player.check_player(p1))
					{SZSC_tips(client,"不能移除自己!请按\"退出房间\"!");break;}
					
					if(player.human()) {//如果是活人，给他办理离房间手续
						player.send_signal(SZSC_protocol.SZSC_leave_room_success);//告诉他退出成功!
						SZSC_room.exit_the_room(player.get_server_ID());//执行退出
					}
					else//执行到这里必然是机器人，可以移除
					{
						this_room.game.players.remove(chair_number);//如果是机器人，直接删除即可
						
						//广播更新房间所有人状态，提示机器人添加成功
						JSON_process reply_msg=new JSON_process();
						reply_msg.add("signal",SZSC_protocol.SZSC_room_tips);
						reply_msg.add("content","房主移除了机器人");
						this_room.room_broadcast(reply_msg);
						
					}
				}
				break;
			default:
				if(!SZSC_asset_process.service(client, json_msg))
					show("获取了错误的signal="+signal);
				return;
		}

		return ;
	}
	
	public static void send_msg_signal(Client client,int signal)
	{
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal",signal);
		send_msg(client, reply_msg);
	}
	
	//关于SZSC的所有发送内容都需进过这里才能发送，不能直接通过client或者player发送
	public static void send_msg(Client client,JSON_process reply_msg) {
		core_main.sleep(300);
		
		reply_msg.add("service_kind", core_protocol.SZSC_service);
		client.send(reply_msg);
	}
}
