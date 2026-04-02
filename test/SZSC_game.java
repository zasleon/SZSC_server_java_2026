package test;

import java.util.ArrayList;
import java.util.List;

public class SZSC_game {
	public int turn_end_player_number;//选择回合结束人数
	public int ready_number2;//回合结算是否完成
	public int current_state;//当前游戏阶段(选角、预备开始阶段、战斗阶段、回合结算、游戏结束阶段)

	public List<SZSC_player> players=new ArrayList<>();
	

	public int listener_ID;
	public int locktime;//当前连锁次数
	

	//HANDLE	AI_thread[UNO_member_limit];//AI思考线程，在游戏结束或房间空出时需结束
	public boolean	whether_lock;//是否已经锁死其他人线程

	public List<SZSC.card> deck= new ArrayList<>();//decklimit
	//卡组，Q：为什么要预设卡组？不能直接随机发放？A：有些角色有“预测未来(即预测即将抽到的卡)”的能力，因此需要预设卡。
	
	public int passturns;//总轮回数，最初为1
	public boolean turn_force_end;//该回合是否被强制结束？	
	public int playernumber;
	public int turn_master;//谁的回合，初始为0，即先选完的人先开始回合
	
	
	public List<SZSC.card> garbage=new ArrayList<>();//墓地，废卡区，有待设计
		
	public SZSC_game() {
		system_ini();
	}
	
	public void game_ini() {
		turn_end_player_number=0;
		locktime=-1;
		listener_ID=SZSC_protocol.code_none;
		SZSC_game_deck.ini_deck(this);//卡组初始化洗牌
		passturns=1;//轮回数置1
		int camp_No=0;
		for(SZSC_player p1:players) {
			p1.set_camp(camp_No++);
			p1.set_turn_end(false);//此回合自己还没选择回合结束
			p1.set_attack_success_time(0);//此回合自己普攻成功次数置0
			p1.fight_chance=0;//可普攻的次数置0
			
			p1.set_client_state(SZSC_protocol.SZSC_in_game);
			p1.set_game_state(SZSC_protocol.SZSC_normal_online);
		}
		
	}
	
	
	public void system_ini() {
		playernumber=SZSC_protocol.playernumber;
		players.clear();
		
		game_ini();
	} 
	
	public void set_whose_turn(int now_whose_turn) {
		turn_master=now_whose_turn;
		if(now_whose_turn<0||now_whose_turn>playernumber)
			return;
		
		SZSC_player p1=players.get(now_whose_turn);
		
		game_broadcast("当前是 "+p1.get_room_name()+" 的回合!");

		SZSC_game_deck.rule_get_card(this,p1);//主回合者进行规则性抽卡
		p1.fight_chance=1;//获得一次规则提供的普攻机会
		turn_end_player_number=0;//宣布回合结束者数量置为0
		turn_force_end=false;//清空“强制结束该回合”标志

		for(SZSC_player p2:players) {
			p2.set_whether_my_turn(false);//其他所有人状态变为非自己回合
			if(p2.bot())turn_end_player_number++;//如果是机器人，选择回合结束者+1
		}
		p1.set_whether_my_turn(true);//状态变为确实为自己回合
		
	}
	public int get_whose_turn() {
		return turn_master;
	}
	
	
	
	
	
	public void start(SZSC_player p1)//开启战斗
	{
		
		
		sleep(100);
		//让客户端选取角色(房间主人才发动以下内容)

		//房主广播用户进入选择角色界面
		
		
		
		//房主进行游戏内容初始化，不是房主直接等待开始
		if(p1.whether_host())
		{
			show("开局阶段");
			game_ini();
			passturns=1;//轮回数置1
			//p1.set_host(true);//
			
			boolean first_state_transmit=false;
			for(SZSC_player player:players) {
				player.send_signal(SZSC_protocol.game_start_interface);
				if(!first_state_transmit) {
					SZSC_game_transmit.refresh_character_state_F(this);
					first_state_transmit=true;
				}
				SZSC_game_deck.player_get_card(this, 4, player);//所有玩家初始获取4张手卡
			}
			//传输所有人信息状态
			
			
			//获取指定卡
			//SZSC_game_deck.get_specific_card(4, p1);
			SZSC_game_deck.get_specific_card(this,9, p1);
			//随机确认某个人是第一回合
			set_whose_turn(-1);

			
		}
		
		
		while(true)//一回合死循环开始
		{
			if(p1.whether_host())//房主进行回合开始宣告
			{
				show("回合初阶段");
				set_state(SZSC_protocol.SZSC_prepare_state);//游戏准备阶段
				int now_whose_turn=get_whose_turn()+1;
				if(now_whose_turn+1>playernumber)
					now_whose_turn=0;
				set_whose_turn(now_whose_turn);

				for(SZSC_player player:this.players)
					if(player.bot())
						turn_end_player_number++;
				
				set_state(SZSC_protocol.SZSC_fighting_state);//游戏准备完成，进入战斗阶段

				SZSC_game_weapon.equip_weapon(this, players.get(1), 0);
			}
			else
			{
				while(!check_state(SZSC_protocol.SZSC_fighting_state))//非房主者等待房主回合准备宣告完毕，等待战斗阶段的开始
				{
					sleep(50);
					if(game_over_normal())
					{return;}//如果游戏结束，退出
				}
			}
			
			show("回合开始");
			
			String result=SZSC_game_protocol.still_fight;

			while(true)//角色选择行动死循环
			{
				
				if(!SZSC_game_judge.whether_moveable(p1))//不能行动则强制结束回合
				{
					game_broadcast(p1.get_room_name()+"由于无法行动而被迫强制结束回合!");
					break;
				}
				
				JSON_process msg=p1.main_listen();
				
				if(p1.offline()){show("玩家  "+p1.get_user_name()+"断线离开游戏");return;}//如果此时断开通讯，离开游戏
				if(game_over_normal()){show("游戏正常结束");return;}//如果游戏结束
				
				if(!SZSC_game_judge.whether_moveable(p1))continue;//不能行动则强制结束回合
				if(turn_force_end==true)break;//强制结束该回合

				int choice=msg.getInt("signal");
				String situation="";
				float situation_value=0;
				SZSC.Event_info event_info=new SZSC.Event_info(situation, situation_value, p1, null);
				switch(choice)//确认是否输入正确,正确则进入行动函数
				{
					case SZSC_protocol.SZSC_player_apply_general_attack://进行普攻
						{
							int player_No=msg.getInt("player_ID");
							result = SZSC_game_player_choose.player_attack(this,p1,player_No);
						}
						break;
					case SZSC_protocol.SZSC_player_apply_use_self_effect://发动角色自身效果
						{
							int which_effect=msg.getInt("which_effect");
							result = SZSC_game_player_choose.player_self_effect(this,event_info,p1,null,which_effect);
							
						}
						break;
					case SZSC_protocol.SZSC_player_apply_use_card://使用手卡
						{
							int which_card=msg.getInt("which_card");
							boolean hide_effect;
							if(msg.getInt("hide_effect")==0)
								hide_effect=false;
							else
								hide_effect=true;

							
							result = SZSC_game_player_choose.player_use_card(this,event_info,p1,null,which_card,hide_effect);
							break;
						}
					case SZSC_protocol.SZSC_player_apply_delete_weapon://丢弃已装备的武器
						{
							int which_weapon=msg.getInt("which_weapon");
							SZSC_game_player_choose.player_delete_weapon(this,p1,which_weapon);break;
						}
					case SZSC_protocol.SZSC_player_apply_use_weapon_effect://发动已装备武器的效果
						{
							int which_weapon=msg.getInt("which_weapon");
							int which_effect=msg.getInt("which_effect");
							result = SZSC_game_player_choose.player_weapon_effect(this,event_info,p1,null,which_weapon,which_effect);
							
						}
						break;
					case SZSC_protocol.SZSC_player_apply_end_turn://结束回合
						SZSC_game_player_choose.player_end_turn(this,p1);
						break;
					case SZSC_protocol.SZSC_player_apply_use_buff://发动自带buff效果
						{
							int which_buff=msg.getInt("which_buff");
							result=SZSC_game_player_choose.player_buff_effect(this,event_info,p1,null,which_buff);break;
						}
					default:
						show(p1.get_room_name()+" 主游戏进程输入无效请求："+choice);
						continue;
							
				}//一般default为发来错误请求，直接continue处理，否则就是正常处理命令后来到这里
		
				//如果玩家之前执行了无法发动的决定，则让他重新决定，例如该卡不满足发动条件，则重新选择想发动的卡，可能是误触其他卡片等原因造成的
				if(SZSC_game_judge.player_do_useless_choice(result))
					continue;
				
				if(game_over_normal()){return;}//如果游戏结束
				
				if(result==SZSC_game_protocol.force_end_turn){
					turn_force_end=true;
					game_broadcast("此回合被强制结束!");
					break;
				}
				if(turn_force_end)//强制结束该回合
					break;
				if(p1.choose_turn_end())//自主选择回合结束
					break;
				
				

			}//角色选择行动死循环结束

			turn_end_player_number++;//选择回合结束者+1
			while(turn_end_player_number<playernumber)//自己结束回合后，等待所有人结束回合
			{
				if(game_over_normal()){return;}
				sleep(300);
			}


			//房主进行结算函数，其他人等待结算结束
			if(p1.whether_host())
			{
				set_state(SZSC_protocol.SZSC_end_turn_state);//开始回合结束结算
				
				turn_end_player_number=0;//宣布回合结束者置0
				
				natural_set_playerturn();//进行回合切换、结算，切换到下一个人的回合

			}
			else
				sleep(1000);//防止回合结算标志没从战斗阶段切到回合结算阶段



//一回合死循环结束
		}

		
	}
	
	
	
	
	
	
	public void natural_set_playerturn() {
		turn_force_end=false;//清空“强制结束该回合”标志
		

		SZSC_game_Buff_process.event_end(this, SZSC_game_protocol.TYPE_Duration_turn);
		//一般回合结算，对所有人物状态结算
		boolean change_turn_master=false;
		int whose_turn=0;
		for(SZSC_player player:players) {
			SZSC_game_general_function.turnsettle(player);//每个人回合结算，特效清除结算
			if(!change_turn_master)//如果还没确定下回合是谁的回合
				if(player.whether_my_turn())
				{
					//广播，进入回合结算!……
					game_broadcast("本回合结束!");
					
					
					player.set_whether_my_turn(false);//不再是他的主回合
					
					//查询下一个回合玩家
					whose_turn++;
					if(whose_turn==players.size()) {
						whose_turn=0;
					}
					SZSC_player next_master_player=players.get(whose_turn);
					next_master_player.set_whether_my_turn(true);//后一个人作为主回合拥有者
					//因为该玩家又一次轮到他的主回合，因此他的轮回效果过期1轮回
					SZSC_game_Buff_process.event_end(this, SZSC_game_protocol.TYPE_Duration_cycle, next_master_player);
					//确认完成回合切换
					change_turn_master=true;
					continue;
				}
			whose_turn++;
		}
		
		
		

	}
	
	
		
	//事件结束、有人死后、接收玩家消息使用，判断场上是否只剩一个阵营，如果是，游戏结束该阵营获胜
	public boolean game_over_normal() {
		game_over_offline();
		//如果场上只剩一方势力
		int first_camp=SZSC_protocol.code_none;//存储第一个阵营
		
		for(SZSC_player player:players)
		{
			if(player.not_none()&&!player.offline()) {
				first_camp=player.get_camp();
				break;
			}
		}
		

		if(first_camp==SZSC_protocol.code_none)
			show("阵营获取错误???");
		
		for(SZSC_player player:players)
			if(player.not_none()&&!player.offline())
				if(player.is_alive()&&first_camp!=player.get_camp())//如果存在其他阵营的活人
					return false;//返回游戏没结束

		set_state(SZSC_protocol.SZSC_end_state);
		return true;

	}
	public void set_state(int value) {//设置房间状态（游戏结束状态、回合结束结算状态、回合初状态）
		current_state=value;
	}
	public boolean check_state(int value) {//确认当前房间状态是否为对应值
		if(current_state==value)
			return true;
		return false;
	}
	public int get_state() {
		return current_state;
	}

	//断线式游戏结束
	public boolean game_over_offline() {
		boolean result=false;
		for(SZSC_player player:players)
			if(player.offline()) {
				game_broadcast(player.get_room_name()+"断线!");
				player.set_game_state(SZSC_protocol.SZSC_force_offline);
				turn_end_player_number++;//选择回合结束者+1
			}
		
		 
		return result;
	}
	
	
	
	

	
	
	
	
	
	
	
	public static void sleep(int time) {
		SZSC_service.sleep(time);
	}
	
	private static void show(String msg)
	{
		SZSC_service.show(msg);
	}
	public void game_broadcast(JSON_process msg)//房间内广播信号
	{
		for(SZSC_player p1:players)
		
			if(p1.human())
			{
				//统一广播发送这句话
				p1.send(msg);
			}
	}

	public void game_broadcast(String message)//房间内广播信息
	{
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.Signal_game_broadcast);
		msg.add("content",message);
		for(SZSC_player player:players)//统一广播发送这句话
			player.send(msg);
		
			
	}
	
}
