package test;

import java.util.List;

public class SZSC_room {
	
	public static List<SZSC_room> rooms=SZSC_service.szsc_rooms;

	public int character_choose_ready_number;//角色选择完成人数
	public int master_number;//房主座位号
	public int current_state;//当前游戏阶段(游戏结束、预备开始阶段、进行中阶段)
	public SZSC_game game;
	
	public int get_player_number() {
		return game.players.size();
	}
	
	public void refresh_player_No() {
		int player_No=0;
		for(SZSC_player player:game.players)
			player.set_player_No(player_No++);
	}
	public void add_player(SZSC_player player) {
		game.players.add(player);
		refresh_player_No();
	}
	
	public void game_process(SZSC_player p1) {
		
		//房主初始化房间各种游戏数据（例如生命值、卡组、攻击力等）
		p1.set_host(p1.whether_host());
		if(p1.whether_host())
		{
			character_choose_ready_number=0;//已选人数清零
			room_broadcast(SZSC_protocol.SZSC_show_character_choice);
			set_state(SZSC_protocol.SZSC_choose_character_state);//游戏初始化完成，房间处于游戏准备阶段
			
		}
		else//当房间指令没切到角色选择，进行等待
			while(check_state(SZSC_protocol.SZSC_finish_state))
				if(game_over())
					return;
				else
					sleep(300);
		
		//执行到此应该进入选角界面
		if(!check_state(SZSC_protocol.SZSC_choose_character_state))
		{
			show("本该进入选角界面! "+get_state());
			return;
		}
		//开始选角
		show("开始选角");
		
		SZSC_game_selfeffect_process.choose_character(this,p1);
			
		if(p1.offline()||game_over())return;
		
		//选角完成，进入
		if(p1.whether_host())
		{
			set_state(SZSC_protocol.SZSC_prepare_state);
		}
		game.start(p1);
			
		
		
		finish_fight(p1);
	}
	
	private static void show(String msg) {
		SZSC_service.show(msg);
	}
	public void set_state(int value) {
		if(current_state==value)
			show("切换游戏全局状态出错! 状态value="+value);
		current_state=value;
			
	}
	public boolean check_state(int value) {
		if(current_state!=value)
			return false;
		else 
			return true;
	}
	public int get_state() {
		return current_state;
	}
	
	
	
	
	
	//所有人选择角色是否完成
	public boolean choose_complete() {
		if(character_choose_ready_number<SZSC_protocol.playernumber)
			return false;
		return true;
	}
	
	
	
	public boolean game_over() {
		if(game.game_over_offline())
			return true;
		return false;
	}
	
	
	
	
	public void finish_fight(SZSC_player p1) {
		if(!p1.whether_host())
			return;
		
		
		//如果是房主，但游戏还能正常进行，则房主掉线退出切换房主，不做结算
		if(!this.game.game_over_normal())
			return;
		
		//如果是房主，而且游戏结束了，则进行结算
		//确认哪个阵营最后存活
		JSON_process msg=new JSON_process();
		for(SZSC_player player:game.players)
			if(player.is_alive()) {
				msg.add("winner",player.get_name());
				msg.add("player_No",player.get_player_No());
				break;
			}
		
		this.game.game_broadcast(msg);
		
		this.game.set_state(SZSC_protocol.SZSC_finish_state);//变更所有人状态到处于房间,房间状态为游戏结束状态
		current_state=SZSC_protocol.SZSC_finish_state;
		
		for(SZSC_player player:this.game.players) {
			player.set_client_state(SZSC_protocol.SZSC_in_room);//状态从“游戏中”转变为“房间中”
			player.send_signal(SZSC_game_protocol.Signal_game_end);
		}
			
		
		
	}
	
	
	
	public SZSC_room() {//构造struct
		
		ini();
		
	}
	public void ini() {
		game=new SZSC_game();
		
		
		master_number=0;//房主
		current_state=SZSC_protocol.SZSC_finish_state;//游戏处于结束阶段		
		character_choose_ready_number=0;//角色选择完成人数
		
		//Robot[count].member_No=SZSC.robot_symbol;
		//Robot[count].p1=&Robot[count].body;
		//addtext(Robot[count].client_name,"系统机器人");
	}
	
			
	public static void SZSC_room_ini()//房间全部初始化
	{
		rooms.clear();
		
	}
	
	
	
	
	
	
	
	
	//判断房间是否人满
	public boolean player_full() {
		if(get_player_number()==SZSC_protocol.playernumber)
			return true;
		return false;
	}

	

	





	//房间信息广播
	public void room_broadcast(JSON_process msg)
	{
		//添加房间所有人数据
		load_room_member_info(msg);

		//发送数据给该房间所有活人用户
		for(SZSC_player player:this.game.players)
			player.send(msg);
	}
	public void room_broadcast(int signal)
	{
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal", signal);
		//添加房间所有人数据
		load_room_member_info(reply_msg);

		//发送数据给该房间所有活人用户
		for(SZSC_player player:this.game.players)
			player.send(reply_msg);
		
		
	}
	public void room_broadcast(String msg)
	{
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal", SZSC_protocol.SZSC_room_broadcast);
		reply_msg.add("content", msg);
		
		//添加房间所有人数据
		load_room_member_info(reply_msg);

		//发送数据给该房间所有活人用户
		for(SZSC_player player:this.game.players)
			player.send(reply_msg);
		
		
	}
	
	public void load_room_member_info(JSON_process reply_msg) {
		reply_msg.add("playernumber",SZSC_protocol.playernumber);
		for(SZSC_player player:this.game.players)
		{
			switch(player.get_type())
			{
				case SZSC_protocol.SZSC_none_player:
					reply_msg.addToArray("member_name","空");
					reply_msg.addToArray("member_ID",SZSC_protocol.code_none);
					reply_msg.addToArray("player_No",player.get_player_No());
					break;
				case SZSC_protocol.SZSC_real_player:
					reply_msg.addToArray("member_name",player.get_user_name());
					reply_msg.addToArray("member_ID",player.member_No);
					reply_msg.addToArray("player_No",player.get_player_No());
					break;
				case SZSC_protocol.SZSC_wood_robot:
					reply_msg.addToArray("member_name","木桩");
					reply_msg.addToArray("member_ID",SZSC_protocol.code_none);
					reply_msg.addToArray("player_No",player.get_player_No());
					break;
				default:
					reply_msg.addToArray("member_name","???");
					reply_msg.addToArray("member_ID",SZSC_protocol.code_none);
					reply_msg.addToArray("player_No",player.get_player_No());
					break;
			}
		}
		//添加房主信息
		reply_msg.add("master",master_number);
	}
	
	
	static public int which_room_No(int user_ID)
	{
		int room_No=0;
		for(SZSC_room room:rooms)//找到在哪个房间
			for(SZSC_player player:room.game.players)//找到该房间对应哪个玩家
				if(player.get_server_ID()==user_ID)
				{
					return room_No;
				}
		return SZSC_protocol.code_none;
	}
	static public int which_player_No(int user_ID)
	{
		
		for(SZSC_room room:rooms)//找到在哪个房间
		{
			int player_No=0;
			for(SZSC_player player:room.game.players){//找到该房间对应哪个玩家
				if(player.get_server_ID()==user_ID)
				{
					return player_No;
				}
				player_No++;
			}
		}
		return SZSC_protocol.code_none;
	}
	
	static public SZSC_player which_player(int user_ID)
	{
		
		for(SZSC_room room:rooms)//找到在哪个房间
			for(SZSC_player player:room.game.players)//找到该房间对应哪个玩家
				if(player.get_server_ID()==user_ID)
				{
					return player;
				}
				
		return null;
	}
	static public SZSC_room which_room(int user_ID) {
		for(SZSC_room room:rooms)//找到在哪个房间
			for(SZSC_player player:room.game.players)//找到该房间对应哪个玩家
				if(player.get_server_ID()==user_ID)
				{
					return room;
				}
		return null;
	}
	

	public static void exit_the_room(int user_ID) {

		//show("开始执行退出房间");
		for(SZSC_room room:rooms)//找到在哪个房间
			for(SZSC_player player:room.game.players)//找到该房间对应哪个玩家
				if(player.get_server_ID()==user_ID) {
					room.exit_the_room(player);
					return;
				}
		
		/*
		int i=which_room_No(user_ID);
		int j=which_player_No(user_ID);
		if(i<0||i>=SZSC_protocol.roomlimit)
		{
			show("退房房间号出错"+i);return;
		}
		if(j<0||j>=SZSC_protocol.playernumber)
		{
			show("退房玩家位置出错"+j);return;
		}*/
		
		//如果执行到这里
		SZSC_service.show("执行退出房间时没找到该玩家！");
	}

	private void exit_the_room(SZSC_player p1)//用户退出房间
	{

		game.turn_end_player_number++;
		character_choose_ready_number=0;
		
		int room_No=which_room_No(p1.get_server_ID());
		boolean host_player_leave=p1.whether_host();//如果是以主人身份退出
		
		int player_pointer=0;
		for(SZSC_player player:game.players) {
			if(player.check_player(p1)) {
				game.players.remove(player_pointer);//离去者 置空player
				refresh_player_No();
				//show(player_pointer+"号位置置空");	
				break;
				
			}
			player_pointer++;
		}
		
		if(host_player_leave)//如果是以主人身份退出
		{
			int real_player_number=0;
			for(SZSC_player player:game.players)//确认总活人人数
			{
				if(player.human())
					real_player_number++;
			}

			if(real_player_number==0)//如果房主离开后没活人了
			{
				//show("离开后没活人了");
				rooms.remove(room_No);
				return;
			}
			else//如果有多个活人，交接房主权限
			{
				master_number=0;
				for(SZSC_player player:game.players)
				{
					
					if(player.human())//如果是活人
					{
						player.set_host(true);
						break;
					}
					master_number++;
				}
			}
				
		}
		else//如果不是以房主身份离去，必然房间中有其他活人
		{
			//show("非主人离去");
		}

		//广播事件
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_protocol.SZSC_room_tips);//有人从房间离开了
		msg.add("content","玩家 "+p1.get_user_name()+" 离开了房间!");//离开者姓名
		//广播退房消息
		switch(current_state)
		{
			case SZSC_protocol.SZSC_finish_state://如果是在房间中
				room_broadcast(msg);
				break;
				
			default://如果是在游戏中
				show("奇怪state="+current_state);
					
		
		}
		
		
	}

	public static boolean check_empty_room()//检查有没有空房间，有的话返回true，没有则返回false
	{
		if(rooms.size()>=SZSC_protocol.roomlimit)
			return false;
		return true;
	}
	
	public static boolean SZSC_check_in_use_room()//检查有没有人在用房间
	{
		return (rooms.size()>0);
	}
	public static String SZSC_show_all_room()//显示被使用的房间
	{
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal",SZSC_protocol.SZSC_show_roomlist);//表明可以选择房间了
		
		int total_mount=rooms.size();
		
		int room_No=0;
		for(SZSC_room room:rooms)
		{
			
			
			//房间号
			reply_msg.addToArray("room_number",room_No);
			//添加房主名称
			int master=room.master_number;
			reply_msg.addToArray("master",room.game.players.get(master).get_user_name());
			//该房间内人数
			reply_msg.addToArray("room_member_number",room.game.players.size());
			room_No++;
		}
		reply_msg.add("total_mount", total_mount);
		return reply_msg.getString();
	}
	
	public static void sleep(int time) {
		SZSC_service.sleep(time);
	}
	public static void tips(SZSC_player p1,String msg) {
		p1.game_tips(msg);
	}
	
	
	
}
