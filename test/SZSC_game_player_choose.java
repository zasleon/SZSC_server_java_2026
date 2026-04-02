package test;


public class SZSC_game_player_choose{
	
	
	//普攻、发动卡片效果、删除武器时，入锁
	public static void lock(SZSC_game this_room,SZSC_player p1) {//封锁p1之外所有人
		
		
		if(this_room.locktime==-1){
			while(this_room.listener_ID!=SZSC_protocol.code_none)
			{SZSC_service.sleep(500);}
			this_room.listener_ID=p1.get_server_ID();
			for(SZSC_player player:this_room.players)
				if(!player.check_player(p1))
				{
					player.lock_player_listen();
				}
		}
		this_room.locktime++;
				
	}
	public static void unlock(SZSC_game this_room,SZSC_player p1) {//解除p1之外所有人
		for(SZSC_player player:this_room.players)
			SZSC_game_Buff_process.chain_level_erase(this_room, player);
		
		this_room.locktime--;
		if(this_room.locktime==-1) {
			for(SZSC_player player:this_room.players)
				if(!player.check_player(p1))
				{
					player.unlock_player_listen();
				}
			this_room.game_broadcast("本次事件彻底结束!");
			this_room.listener_ID=SZSC_protocol.code_none;
		}
		
					
	               
	}
	
	
	
	public static String player_use_card(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,int which_card,boolean hide_effect){
		String result=SZSC_game_protocol.still_fight;
		//判断which_card有效性
		if(!SZSC_game_judge.card_choice_valid(player_launcher, which_card)){
			return SZSC_game_protocol.CHOICE_i_do_useless_choice;
		}

		int card_No=player_launcher.card.get(which_card).get_card_No();
		if(hide_effect)
			card_No=player_launcher.card.get(which_card).get_hide_effect();
		
		SZSC.Launch_Info situation_Info=new SZSC.Launch_Info(card_No);
		situation_Info.set_which_card(which_card);
		
		result=player_launch_effect(this_room, event_info,player_launcher, player_target, situation_Info);
		
		return result;
	}
	
	
	public static String player_attack(SZSC_game this_room,SZSC_player p1,int player_No)//玩家行动选择普攻,player_No为目标
	{
		String result=SZSC_game_protocol.still_fight;
		
		
		//验证选择目标是否是敌方玩家
		if(!SZSC_game_judge.player_choice_valid_enemy(this_room, p1, player_No))
		{show("普攻选取错误目标！"+player_No);return SZSC_game_protocol.CHOICE_i_do_useless_choice; }

		SZSC_player p2=this_room.players.get(player_No);//获取目标玩家
		
		if(p1.fight_chance==0)//确认是该玩家回合，检查该玩家在该回合剩余的普攻次数
		{p1.game_tips("该回合你的普攻次数已消耗完!");return SZSC_game_protocol.still_fight;}
		//执行到这里必然可以发动普攻
		p1.fight_chance--;//如果是某人的回合，他主动发起普攻,消耗一次普攻机会

		lock(this_room,p1);
		//this_room.locktime=0;//连锁次数重置为0
		result=SZSC_game_attack.general_attack(this_room,p1,p2);//普攻宣言发动，进入通用普攻事件
		
		//this_room.locktime=0;//连锁次数重置为0
		unlock(this_room,p1);

		return result;
	}
	public static void player_delete_weapon(SZSC_game this_room,SZSC_player p1,int which_weapon)
	{
		//判断是否是该玩家主回合
		if(!p1.whether_my_turn())
			return;

		if(!SZSC_game_judge.weapon_choice_valid(p1, which_weapon))
			return;
		
		//执行到这里必然能够丢弃选中的武器
		lock(this_room,p1);//事件开始
		//广播
		String weapon_name=SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name,p1.weapon.get(which_weapon).get_weapon_ID());
		this_room.game_broadcast(p1.get_room_name()+" 将自己的武器 "+weapon_name+" 丢弃!");
		
		//丢弃武器
		SZSC_game_weapon.delete_weapon(this_room,p1,which_weapon);

		unlock(this_room,p1);//事件结束,解放其他人行动
	}
	//读取人物属性栏weapon_effect中的号码，显示其效果，玩家选择效果发动。进入发动函数，判断是否符合发动条件，不符合的返回无法发动，符合的进行发动
	public static String player_weapon_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,int which_weapon,int which_effect)//发动武器效果，第几个武器的第几个效果
	{
		//show_him_his_weapon_effect(c1);//显示武器总览

		
		String result=SZSC_game_protocol.still_fight;
		if(!SZSC_game_judge.weapon_effect_choice_valid(p1, which_weapon,which_effect))
			return SZSC_game_protocol.CHOICE_i_do_useless_choice; 

		
		int weapon_effect_ID=SZSC_game_weapon.get_weapon_effect_ID(p1, which_weapon, which_effect);
		if(weapon_effect_ID==SZSC_protocol.code_none) {
			show("weapon_effect_ID为空?");
			return SZSC_game_protocol.CHOICE_i_do_useless_choice; 
		}
		
		SZSC.Launch_Info launch_Info=SZSC.get_new_Launch_Info(weapon_effect_ID);
		launch_Info.set_weapon_info(which_weapon, which_effect);//设置发动来源
		if(SZSC_game_judge.card_No_is_buff(weapon_effect_ID)) {
			SZSC_Buff buff=SZSC_game_weapon.get_weapon_effect(p1,which_weapon,which_effect);
			if(buff==null){
				show("发动武器效果失败！"+which_weapon+" ??  "+which_effect);
				return SZSC_game_protocol.CHOICE_i_do_useless_choice; 
			}
			launch_Info=new SZSC.Launch_Info(buff);
		}
		
		result=player_launch_effect(this_room, event_info, p1, p2, launch_Info);
		
		return result;

	}

	
	

	public static String player_self_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,int which_effect)//发动人物自身效果
	{
		String result=SZSC_game_protocol.still_fight;

		//如果个人效果检查
		if(!SZSC_game_judge.selfeffect_choice_valid(p1, which_effect))
			return SZSC_game_protocol.CHOICE_i_do_useless_choice;
		//如果有
		int self_effect_ID=p1.ability.get(which_effect).get_effect_ID();
		
		//查询表格,如果该效果id是buff，则从个人buff栏提取buff，如果不是，则直接执行效果
		//一般但凡带有次数限制、token的都会是以buff方式呈现以确保次数能够计算被用尽
		SZSC.Launch_Info situation_Info=new SZSC.Launch_Info(self_effect_ID);
		
		if(SZSC_game_judge.card_No_is_buff(self_effect_ID)){
			//如果是buff，则获取玩家自身该效果buff数据
			//this_buff=SZSC_game_Buff_process.get_buff_data(self_effect_ID);
			SZSC_Buff this_buff=p1.get_selfeffect(which_effect);
			if(this_buff==null) {
				show("个人效果 号码出错 查询结果为空(本该有的)    self_effect_ID="+self_effect_ID);
				return SZSC_game_protocol.CHOICE_i_do_useless_choice;
			}
			situation_Info=new SZSC.Launch_Info(this_buff);
		}
		situation_Info.set_which_effect(which_effect);
		result=player_launch_effect(this_room, event_info, p1, p2, situation_Info);
		
		
		return result;
	}
	public static void player_end_turn(SZSC_game this_room,SZSC_player p1)//该玩家宣布回合结束
	{
		p1.set_turn_end(true);
		//广播他主动结束回合
		this_room.game_broadcast(p1.get_room_name()+" 结束此回合行动!");
		
	}


	public static String player_buff_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,int which_buff)//人物发动附加效果，指定buff栏
	{
		String result=SZSC_game_protocol.still_fight;
		//show_him_his_buff_effect(c1);//展示他所有的buff效果

		//检查buff是否正常值
		if(!SZSC_game_judge.buff_choice_valid(p1, which_buff))
			return result;
		
		SZSC_Buff this_buff=p1.buff.get(which_buff);
		SZSC.Launch_Info situation_Info=new SZSC.Launch_Info(this_buff);//此处一般是增益减益效果，因此无需填写任何额外信息
		
		result=player_launch_effect(this_room, event_info, p1, p2, situation_Info);
		
		

		return result;
	}
	
	
	//玩家要发动的所有效果必须进过本函数
	//先判断是否满足发动条件，满足后进行反击之类的操作，之后效果结算，之后返回
	public static String player_launch_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,SZSC.Launch_Info launch_info) {
		
		
		String result=SZSC_game_protocol.still_fight;
		int card_No=launch_info.get_card_No();//获取发动效果id
		
		//如果是buff
		SZSC_Buff buff=launch_info.get_buff();
		if(launch_info.is_buff()){//如果是buff类
			if(!SZSC_game_judge.whether_activate_buff(this_room,player_launcher,event_info,buff,true))
				return SZSC_game_protocol.CHOICE_i_do_useless_choice;
		}
		
		if(!SZSC_game_judge.judge_launch_condition(this_room,event_info, player_launcher,player_target,launch_info))
		{	
			return SZSC_game_protocol.CHOICE_i_do_useless_choice;
		}
		//执行到这里必然可以发动
		lock(this_room,player_launcher);
		
		//广播发动事件
		SZSC_game_general_function.player_launch_broadcast(this_room, player_launcher, launch_info);
		
		if(launch_info.is_buff()) {
			//进行buff次数消耗
			buff.reduce_times(1);
		}else if(launch_info.is_card()) {//一般发动手卡效果时，手卡本身并非buff类效果（可能效果给角色添加buff），只有个人自身、武器效果为buff
			int which_card=launch_info.get_which_card();
			result=SZSC_game_deck.use_card(this_room,player_launcher,player_target,which_card,card_No);//使用了该槽手卡
		}
		
		SZSC.Event_info new_event_info=new SZSC.Event_info(SZSC_game_protocol.someone_launch_effect, 0, player_target, player_launcher);
		
		String launch_source_type=launch_info.get_launch_source_type();
		switch(launch_source_type) {
			case SZSC_game_protocol.TYPE_launch_effect:
			case SZSC_game_protocol.TYPE_launch_weapon:
			case SZSC_game_protocol.TYPE_launch_assist:
			case SZSC_game_protocol.TYPE_launch_buff:
			case SZSC_game_protocol.TYPE_launch_hide:
				new_event_info.set_source(SZSC_game_protocol.TYPE_source_card, SZSC_protocol.code_none,  SZSC_protocol.code_none);
				break;
			case SZSC_game_protocol.TYPE_launch_weapon_effect:{
				int which_weapon=launch_info.get_which_weapon();
				int which_effect=launch_info.get_which_effect();
				new_event_info.set_source(SZSC_game_protocol.TYPE_source_weaponeffect, which_weapon, which_effect);
			}
				break;
			case SZSC_game_protocol.TYPE_launch_self_effect:{
				new_event_info.set_source(SZSC_game_protocol.TYPE_source_selfeffct, SZSC_protocol.code_none,  SZSC_protocol.code_none);
			}
				break;
				
		}
		
		result=SZSC_game_attack.fight_back(this_room, player_target, new_event_info);//宣布发动，对手进行反击
		
		if(SZSC_game_judge.event_force_end(result))//如果有事件中断或逾期
			return result;
		if(result!=SZSC_game_protocol.i_failed_launch_effect)//如果效果没被无效化，发动效果
		{
			
			switch(launch_source_type) {
				case SZSC_game_protocol.TYPE_launch_self_effect://如果是发动角色效果而因为某些原因角色被沉默
					if(SZSC_game_judge.character_is_silent(player_launcher)) {
						this_room.game_broadcast(player_launcher.get_room_name()+" 的自身效果因被沉默所以发动失败!");
						break;
					}
						
				default:
					this_room.game_broadcast(player_launcher.get_room_name()+" 的效果发动成功!");
					result=SZSC_game_effect.launch_effect(this_room,event_info,player_launcher,player_target,launch_info);//发动效果，返回一个结果
			
			}
		}
		else {
			this_room.game_broadcast(player_launcher.get_room_name()+" 的效果发动失败!");
		}
		unlock(this_room,player_launcher);
		return result;
	}
	
	
	public static int choose_enemy(SZSC_game this_room,SZSC_player p1,String message,boolean must) {
		return choose_someone(this_room,p1,SZSC_game_protocol.Signal_show_enemy_list,message,must);
	}
	public static int choose_someone(SZSC_game this_room,SZSC_player p1,String message,boolean must) {
		return choose_someone(this_room,p1,SZSC_game_protocol.Signal_show_alive_list,message,must);
	}
	
	//选择指定的对象(该函数为通用函数u)，包括自己
	private static int choose_someone(SZSC_game this_room,SZSC_player p1,int signal,String message,boolean must)
	{
			JSON_process reply_msg=new JSON_process();
			reply_msg.add("signal",signal);
			reply_msg.add("ask",message);//询问内容，比如指定一人回复4血之类的
			if(must)//如果是必须选择一个对象，如果不是必须，则可以取消操作
				reply_msg.add("must",1);
			else
				reply_msg.add("must",0);
			
			int which_one=0;
			while(true)
			{
				p1.send(reply_msg);
			
				JSON_process msg=p1.main_listen();
				if(p1.offline()) {return SZSC_protocol.code_none;}//玩家做出选择，如果此时断开通讯
				
				which_one=msg.getInt("player_ID");
				int get_signal=msg.getInt("signal");
				
				if(get_signal==SZSC_protocol.SZSC_i_cancel_do_choice)
					if(must) {
						SZSC_service.show("在必选对象环节选择了取消选择!  "+which_one);
						continue;
					}
					else {//如果取消选择对象，直接返回
						p1.game_tips("你取消了选择!");return SZSC_protocol.SZSC_i_cancel_do_choice;
					}
				
				
				switch(signal) {
					case SZSC_game_protocol.Signal_show_enemy_list:
						if(!SZSC_game_judge.player_choice_valid_enemy(this_room, p1, which_one))
						{
							SZSC_service.show("选取了错误敌人! "+which_one);
							continue;
						}
						break;
					case SZSC_game_protocol.Signal_show_friend_list:
						if(!SZSC_game_judge.player_choice_valid_teammate(this_room, p1, which_one))
						{
							SZSC_service.show("选取了错误友军! "+which_one);
							continue;
						}
						break;
					default:
						if(!SZSC_game_judge.player_choice_valid(this_room, p1, which_one))
						{
							SZSC_service.show("选取了错误玩家目标! "+which_one);
							continue;
						}
				}
				//执行到这里必然选择正确对象
				break;
			}

			
		return which_one;
	}
	//指定一人发动效果伤血,一定概率成功
	public static String choose_one_get_effect_damage(SZSC_game this_room,SZSC_player player_launcher,float damage_value,int player_No,float probability)
	{
		String result=SZSC_game_protocol.still_fight;
		//内部检查指定对象正确性，如果指定对象错误，直接返回still_fight
		if(!SZSC_game_judge.player_choice_valid_enemy(this_room, player_launcher, player_No))
			result=SZSC_game_protocol.CHOICE_i_do_useless_choice;
		else {
			SZSC_player player_target=this_room.players.get(player_No);
			SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.TYPE_event_effect_dmg, damage_value, player_target,player_launcher);
			event_info.set_probability(probability);
			result=SZSC_game_attack.hit_damage(this_room,player_target,player_launcher,event_info);//扣血
		}
		
		return result;
	}
	
	public static boolean ask_whether_do(SZSC_player p1,String message)//询问是否要这样做
	{
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.Signal_chooseYN);
		msg.add("ask_content",message);

		p1.send(msg);
		JSON_process reply_msg=p1.game_listen();

		if(p1.offline())return false;
		if(reply_msg.getInt("answer")==1)
			return true;
		return false;
	}
	

	//指定一人发动效果回血
	public static String choose_one_get_effect_recover(SZSC_game this_room,SZSC_player player_launcher,float recover_value,int player_No)
	{
		//内部检查指定对象正确性，如果指定对象错误，直接返回still_fight
		String result=SZSC_game_protocol.still_fight;
		SZSC_player player_target=this_room.players.get(player_No);
		SZSC_game_attack.recover_blood(this_room,recover_value,player_target,player_launcher);//血量增加3
		return result;
	}
	private static void show(String msg)
	{
		SZSC_service.show(msg);
	}
}
