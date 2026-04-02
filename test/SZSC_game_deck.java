package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SZSC_game_deck {
	//手卡变动：抽卡，丢卡，使用卡
	//丢卡、使用卡后refresh
	//抽卡、refresh后card_change信息传输
	//card_change传输时更新隐效果

	public static int random_card(int i)//根据送入的随机数赋予卡片序号，部分卡片抽到的权重不同，可以适当调整
	{
	switch(i)
		{
		case 0:;
	    case 1:return 60;
		case 2:
		case 3:;
		case 4:return 30;
		case 5:;
		case 6:;
		case 7:return 61;
		case 8:
		case 9:;
		case 10:return 0;
		case 11:;
		case 12:;
		case 13:return 62;
		case 14:;
		case 15:;
		case 16:return 31;
		case 17:;
		case 18:
		case 19:return 64;
		case 20:
		case 21:;
		case 22:return 1; 
		case 23:
		case 24:;
		case 25:return 63;
		case 26:;
		case 27:
		case 28:return 32;
		case 29:;
		case 30:return 65;
		case 31:
		case 32:;
		case 33:return 2;
		case 34:
		case 35:
		case 36:return 66;
		case 37:
		case 38:
		case 39:return 33;
		case 40:
		case 41:return 3;
		case 42:;
		case 43:
		case 44:return 77;
		case 45:;
		case 46:
		case 47:return 34;
		case 48:;
		case 49:;
		case 50:return 4;
		case 51:;
		case 52:;
		case 53:return 70;
		case 54:;
		case 55:;
		case 56:return 35;
		case 57:;
		case 58:;
		case 59:return 5;
		case 60:
		case 61:;
		case 62:return 71;
		case 63:;
		case 64:return 72;
		case 65:;
		case 66:
		case 67:return 36;
		case 68:
		case 69:;
		case 70:return 6;
		case 71:;
		case 72:;
		case 73:return 73;
		case 74:;
		case 75:return 37;
		case 76:;
		case 77:;
		case 78:return 75;
		case 79:;
		case 80:
	    case 81:return 7;
		case 82:;
		case 83:;
		case 84:return 76;
		case 85:;
		case 86:;
		case 87:return 38;
		case 88:;
		case 89:;
		case 90:return 8;
	    case 91:;
		case 92:;
		case 93:return 74;
		case 94:;
		case 95:return 39;
		case 96:;
		case 97:;
		case 98:return 40;
		case 99:;
		case 100:return 9;
		default:return 0;


		}
	}



	


	public static int player_discard_all(SZSC_game this_room,SZSC_player p1)//丢光手卡
	{
		int card_number=0;
		
		
		card_number=p1.card.size();//查看手卡数量，进行数值计算
		p1.card.clear();
		refresh_card_data(this_room);
		
		return card_number;//有时候要统计自己丢了几张手卡
	}
	
	//p2导致p1丢光手卡
	public static String discard_all_card(SZSC_game this_room,SZSC_player player_target,SZSC_player player_launcher) {
		String result=SZSC_game_protocol.still_fight;
		String situation;
		float situation_value=get_player_card_quantity(player_target);
		
		if(SZSC_game_judge.judge_effect_positive(player_target,player_launcher))
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_positive;
		else {
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_passive;
		}
		
		SZSC.Event_info new_event_info=new SZSC.Event_info(situation, situation_value, player_target, player_launcher);
		result=SZSC_game_attack.fight_back(this_room, player_target, new_event_info);
		
		int real_discard_card=0;
		if(!SZSC_game_judge.event_force_end(result))
			real_discard_card=player_discard_all(this_room, player_target);
		
		if(real_discard_card>0)
		{
			if(SZSC_game_judge.judge_effect_positive(player_target, player_launcher))
				situation=SZSC_game_protocol.i_be_discard_card_positive;
			else
				situation=SZSC_game_protocol.i_be_discard_card_passive;
			result=SZSC_game_attack.fight_back(this_room, player_target, new_event_info);
			
		}
		
		return result;
		
	}

	
	



	public static int get_player_card_quantity(SZSC_player p1)//获取手卡数量
	{
		return p1.card.size();
	}

	
	public static void add_one_random_new_card_to_deck(SZSC_game this_room) {
		int card_No=random_card(SZSC_game_general_function.getrandom(0, 100));
		this_room.deck.add(new SZSC.card(card_No));//添加入一张随机生成的卡
	}

	public static void ini_deck(SZSC_game this_room)																					//全部重新洗牌
	{
		for(int i=0;i<SZSC_protocol.decklimit;i++)
		{
			add_one_random_new_card_to_deck(this_room);
			
		}
	}
	
	




	//所有正规从卡组获取卡片都要从这里经过
	public static String get_card(SZSC_game this_room,int number,SZSC_player player_target,SZSC_player player_launcher)	//人物从deck卡组抽卡
	{	
		String result=SZSC_game_protocol.still_fight;
		if(number<1){
			show("抽卡异常，卡片数为："+number);
			return result;
		}
		
		//广播c1抽取x卡
		/*
		this_room.game_broadcast(player_target.get_room_name()+"即将抽取"+number+"卡!");
		//fight_back(SZSC_game this_room,SZSC_player p1,SZSC_player p2,SZSC.Event_info event_info)//p1反击p2
		for(SZSC_player player:this_room.players)
			if(!player_target.check_player(player)) {
				SZSC.Event_info event_info=SZSC.get_new_event_info(SZSC_game_protocol.someone_will_get_card, number, player, player_target);
				result=SZSC_game_attack.fight_back(this_room, player, player_target,event_info);
			}
			*/
		
		
		int current_card_number=get_player_card_quantity(player_target);
		
		if(player_target.get_card_limit()<current_card_number+number)
		{
			this_room.game_broadcast(player_target.get_room_name()+" 本次抽卡将超出手卡上限!\n丢弃"+(current_card_number+number-player_target.get_card_limit())+"手卡才能获取本次所有抽卡!");
		
			if(SZSC_game_judge.whether_moveable(player_target))//如果可以行动，自主丢卡，如果不能，直接跳过抽卡环节
			{
				player_target.game_tips("你手卡已达上限!可以丢弃手卡来继续抽卡!");
				if(player_discard_card_free(this_room,0,false,player_target)<1)
				{
					this_room.game_broadcast("他选择不丢卡!放弃本次抽卡机会!");
					return result;
				}
			}
		}
		
		int real_get_card_number=player_get_card(this_room, number,player_target);
		
		
		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_success_get_card, real_get_card_number, player_target, null);//我抽到了x张卡
		result=SZSC_game_Buff_process.activate_Buff(this_room,player_target,null,event_info);
		SZSC_game_Buff_process.event_end(this_room, SZSC_game_protocol.TYPE_Duration_this_time_get_card);
		
		return result;
	}
	
	
	public static int player_get_card(SZSC_game this_room,int number,SZSC_player player_target) {
		//进行抽卡
		int real_get_card_number=0;//实际获得的手卡数量
		for(;number>0;number--) {
			if(get_player_card_quantity(player_target)>=player_target.get_card_limit())//满了就停
				break;
			SZSC.card this_card=this_room.deck.remove(0);
			player_target.card.add(this_card);//抽一张
			add_one_random_new_card_to_deck(this_room);//卡组新生成一张
			real_get_card_number++;
		}//卡抽好了
		
		this_room.game_broadcast(player_target.get_room_name()+"抽取"+real_get_card_number+"卡!");
		SZSC_game_transmit.card_change(this_room);//刷新手卡数据
		return real_get_card_number;
	}
	

	public static String rule_get_card(SZSC_game this_room,SZSC_player p1)//规则性抽卡
	{
		String result=SZSC_game_protocol.still_fight;
		int card_number=2;//要抽的卡
		
		if(p1.card.size()<2) {//主回合的玩家不满2张手卡的情况下补满到4张，其余情况获取2张手卡
			card_number=4-p1.card.size();
		}
		
		result=get_card(this_room, card_number, p1,null);
		
		return result;
		
	}

	public static void get_specific_card(SZSC_game this_room,int card_No,SZSC_player p1)													//获取指定卡片
	{
		if(p1.card.size()>SZSC_protocol.cardlimit) {
			show("该玩家手卡数量已达上限！无法继续抽卡");
			return;
		}
		
		p1.card.add(new SZSC.card(card_No));//将指定卡片号为number的卡，给玩家p
		SZSC_game_transmit.card_change(this_room);//刷新手卡数据
		
	}
	
	public static void refresh_card_data(SZSC_game this_room)//如果有卡使用后，需要马上使用这个函数,消耗手卡后重新整理手卡
	{
		SZSC_game_transmit.card_change(this_room);
	}
	



	public static String use_card(SZSC_game this_room,SZSC_player player_launcher,SZSC_player player_target,int which_card,int card_No)//消耗了这一卡槽的卡
	{
		String result=SZSC_game_protocol.still_fight;
		
		refresh_card_data(this_room);//刷新手卡
		
		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_used_card, 1, player_launcher, player_target);
		result=SZSC_game_Buff_process.activate_Buff(this_room,player_launcher,player_launcher,event_info);//我自己使用了手卡
		
		event_info=new SZSC.Event_info(SZSC_game_protocol.other_use_card, 1, player_target, player_launcher);
		result=SZSC_game_attack.fight_back(this_room,player_target,event_info);
		
		return result;
	}

	public static void choose_discard_weapon(SZSC_game this_room,SZSC_player p1)//必须选择一张武器卡丢弃
	{
		
		//如果是机器人，根据优先级选择一张装备卡丢弃
		//if(check_robot(*p1))return 0;

		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_be_effect_discard_card_positive, 0, p1, null);
		if(SZSC_game_judge.whether_immune_this_effect(this_room,p1,event_info))//是否免疫丢卡效果
			return;//免疫丢卡效果，直接算走完丢卡流程

		p1.set_interface_state(SZSC_game_protocol.player_interface_state_discard_one_card);//图形界面变化
		p1.game_tips("请丢弃1张装备卡!");
		
			while(true)
			{
				JSON_process msg=p1.game_listen();
				
				if(this_room.game_over_offline()){return;}//玩家做出选择，如果此时断开通讯
				
				int which_card=msg.getInt("card_number");

				if(!SZSC_game_judge.card_choice_valid(p1, which_card))//检测选择有效性
				{continue;}	
				
				SZSC.card this_card=p1.card.get(which_card);
				int card_No=this_card.get_card_No(); 
				
				if(SZSC_game_judge.judge_type_weapon(card_No))//输入该卡号，判断该手卡是否为装备卡
				{				
					player_discard(p1, which_card);//丢掉该手卡		
					break;
				}
				else//该卡不是装备卡!请重新选择!
				{
					p1.game_tips("该卡不是装备卡!");
				}
			}
			//图形界面按钮还原
			p1.set_interface_state(SZSC_game_protocol.player_interface_state_return_normal);
			p1.game_tips("丢弃成功！");
			return;
	}




	//返回card_discard丢卡成功，返回few_card丢卡失败，返回i_cancel_effect主动取消
	//为了满足一定效果的cost条件，让玩家丢number数量的卡
	public static SZSC.Result_info choose_discard(SZSC_game this_room,int number,Boolean must,SZSC_player player_target,SZSC_player player_launcher)
	{
		int player_card_number=get_player_card_quantity(player_target);//确认手卡总数
		
		String result_type=SZSC_game_protocol.still_fight;
		SZSC.Result_info result_info=SZSC.get_new_Result_info();
		result_info.set_value(0);
		
		String situation;
		if(SZSC_game_judge.judge_effect_positive(player_target, player_launcher))
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_positive;
		else
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_passive;
		
		SZSC.Event_info event_info=new SZSC.Event_info(situation,number,player_target,player_launcher);
		if(SZSC_game_judge.whether_immune_this_effect(this_room,player_target,event_info)){//是否免疫这个效果
			result_info.set_value(number);
			if(number==0)
				result_info.set_value(player_card_number);
			return result_info;//免疫丢卡效果，直接算走完丢卡流程
		}
		
		if(player_target.bot()) {
			if(must){//如果必须丢
				if(player_card_number<number) {//如果手卡数量少于要求数量，全部丢弃
					result_type=discard_all_card(this_room, player_target, player_launcher);
					result_info.set_type(result_type);
					result_info.set_value(player_card_number);
				}
				else{//如果手卡数量多于要求数量，则将最前面几张丢弃
					while(number>0){
						player_discard(player_target, 0);//丢第一张
						number--;
					}
					result_info.set_value(number);
				}
			}
			else//如果不是必须丢
				result_info.set_value(0);//如果是机器人，默认不会丢卡
			return result_info;
		}
		
		//执行到这里必然活人
		if(player_card_number<number)
		{
			if(must){//如果是必须要玩家选择这数量卡丢弃，但数量不够，则全部丢完
				discard_all_card(this_room, player_target, player_launcher);
				result_info.set_value(player_card_number);
			}
		}
		
		//执行到这里，让用户选择卡进行丢弃

		int real_discard_number=player_discard_card_free(this_room,number,must,player_target);
		
		result_info.set_value(real_discard_number);

		return result_info;//表示丢卡流程成功走完
	}
	
	
	public static int player_discard_card_free(SZSC_game this_room,int required_card_number,boolean must,SZSC_player player_target) {
		int result=0;
		while(true)
		{
			if(required_card_number==1)//如果丢1张或丢n张，展现不同界面
				player_target.set_interface_state(SZSC_game_protocol.player_interface_state_discard_one_card);
			else
				player_target.set_interface_state(SZSC_game_protocol.player_interface_state_discard_muti_card);
		
			
			
			JSON_process reply_msg=new JSON_process();
			reply_msg.add("signal",SZSC_game_protocol.Signal_pls_discard_card);
			if(must)
				reply_msg.add("must",1);//是否必须丢
			else
				reply_msg.add("must",-1);

			reply_msg.add("number",required_card_number);//丢卡数量
			player_target.send(reply_msg);
			
			SZSC_service.sleep(500);
			
			String request_content;//请丢x张卡!
			if(must)
				request_content="请丢"+required_card_number+"张手卡!";
			else {
				if(required_card_number<1)
					request_content="可以选择丢任意数量手卡或不丢!";
				else
					request_content="可以选择丢 "+required_card_number+" 张手卡或不丢!";
			}
			player_target.game_tips(request_content);

			player_target.set_interface_state(SZSC_game_protocol.player_interface_state_return_normal);//图形界面按钮还原
		
			JSON_process msg=player_target.game_listen();//玩家做出选择，如果此时断开通讯
			if(player_target.offline())return 0;
			if(this_room.game_over_offline())return 0;
		
			{
				if(msg.getInt("signal")==SZSC_game_protocol.Signal_not_do_card_choice) {
					if(must) {
						player_target.game_tips("本次丢卡为必须丢");
						continue;
					}else {
						this_room.game_broadcast(player_target.get_room_name()+" 选择不丢卡!");
						return 0;
					}
				}
				
				
				List<Integer> list = new ArrayList<>();
				
				List<Integer> choices=msg.getIntList("card_number");
				int discard_number=choices.size();//获取选择总个数
				if(must)//如果必须丢
				{
					if(discard_number<required_card_number)
					{
						show("丢卡数量不匹配指定数量！"+discard_number+"   "+required_card_number);
						continue;				
					}
				}
					
				for(int choice:choices)
				{
					if(!SZSC_game_judge.card_choice_valid(player_target, choice))//检查choice是否越位
					{
						continue;
					}
					if(!list.contains(choice))//如果没有重复的值时进行添加
						list.add(choice);
				}
				
				Collections.sort(list, Collections.reverseOrder());//从大到小排列，防止删除手卡时出现偏差
				if(list.size()!=required_card_number&&(required_card_number!=0))//进行核对，如果不正确
				{
					show("丢卡数量与要求不符"+required_card_number+"   "+list.size());
					if(must)//如果数量不符，且必须丢则重新选择，否则直接结算
						continue;
				}
				//执行到这里必然核对完成，正确
				for(int which_card:list) {
					
					player_discard(player_target, which_card);
				}
				result=list.size();
				this_room.game_broadcast(player_target.get_room_name()+" 丢了"+result+"张卡!");
				break;//破除丢卡死循环
			}
		}
		refresh_card_data(this_room);//刷新手卡
		return result;
	}

	



	public static int random_choose_card(int card_total_mount)//随机选择卡,输入参数为手卡总数
	{
		int result=SZSC_game_general_function.getrandom(0, card_total_mount-1);
		
		return result;
	}

	public static String all_player_random_discard(SZSC_game this_room,SZSC_player player_launcher,int number)//所有人丢number张卡
	{
		String result=SZSC_game_protocol.still_fight;
		for(SZSC_player player:this_room.players)
			if(player.not_none())//如果该位子上非空
				random_discard(this_room,number,player,player_launcher);
		
		return result;
	}

	public static String all_player_get_card(SZSC_game this_room,int number,SZSC_player player_launcher)//所有人获得number张卡
	{
		String result=SZSC_game_protocol.still_fight;
		String final_result=SZSC_game_protocol.still_fight;
		
		for(SZSC_player player:this_room.players) {
			result=get_card(this_room,number,player,player_launcher);
			if(SZSC_game_judge.event_force_end(result))
				final_result=result;
		}
		
		return final_result;
	}
	public static String all_player_refresh_card(SZSC_game this_room,int number,SZSC_player player_launcher) {
		String result=SZSC_game_protocol.still_fight;
		String final_result=SZSC_game_protocol.still_fight;
		for(SZSC_player player:this_room.players) {
			result=player_refresh_card(this_room, number, player,player_launcher);
			if(SZSC_game_judge.event_force_end(result))
				final_result=result;
		}
		
		return final_result;
	}
	public static String player_refresh_card(SZSC_game this_room,int number,SZSC_player player_target,SZSC_player player_launcher) {
		String result=SZSC_game_protocol.still_fight;
		
		
		SZSC.Result_info result_info=random_discard(this_room,number, player_target,player_launcher);
		int real_discard_number=(int)result_info.get_value();
		if(real_discard_number>0)
			result=get_card(this_room, real_discard_number, player_target,player_launcher);
		return result;
	}
	
	


	public static SZSC.Result_info random_discard(SZSC_game this_room,int number,SZSC_player player_target,SZSC_player player_launcher)
	{
		SZSC.Result_info result_info=SZSC.get_new_Result_info();
		
		
		String situation;
		if(SZSC_game_judge.judge_effect_positive(player_target, player_launcher))
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_positive;
		else
			situation=SZSC_game_protocol.i_will_be_effect_discard_card_passive;
		
		SZSC.Event_info event_info=new SZSC.Event_info(situation, number, player_target, player_launcher);
		if(SZSC_game_judge.whether_immune_this_effect(this_room,player_target,event_info)){//是否免疫丢卡效果
			result_info.set_value(0);
			return result_info;//有则直接返回
		}
	    int player_card_number=get_player_card_quantity(player_target);//确认手卡总数
	    
	    //执行到这里必然要丢卡不能免疫效果
		
		if(player_card_number<=number)//手卡不足，直接清空手卡即可
		{
			result_info.set_value(player_discard_all(this_room, player_launcher));//实际丢弃数量
			return result_info;
		}

		while(number!=0)
		{
			int choice=random_choose_card(player_card_number-1);
			player_discard(player_target,choice);
			player_card_number--;//手卡总量-1
			number--;//剩余丢卡数量-1
		}
		
		refresh_card_data(this_room);
		
		result_info.set_value(number);
		return result_info;//表示丢卡流程成功走完
	}
	
	public static void player_discard(SZSC_player p1,int which_card) {
		p1.card.remove(which_card);
	}
	


	public static void show_aims_card(SZSC_player p1,SZSC_player p2,String message)//向p1展示p2所有手卡,并让他选择一张并抢夺
	{
		{
			JSON_process reply_msg=new JSON_process();
			reply_msg.add("signal",SZSC_game_protocol.Signal_show_rivals_card_P);
			reply_msg.add("goal",message);
			for(SZSC.card this_card:p2.card) {
				int card_No=this_card.get_card_No();
				reply_msg.addToArray("card",card_No);//具体卡片编号
			}
			
			reply_msg.add("card_number",p2.card.size());//统计卡片总数
			
			p1.send(reply_msg);
		}

	}

	public static String plunder_ones_card(SZSC_game this_room,SZSC_player p1,SZSC_player p2,int number)//number为要抢他多少张卡，p1抢p2
	{
		String result=SZSC_game_protocol.still_fight;
		
		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_be_effect_public_card,0, p1, p2);
		if(SZSC_game_judge.whether_immune_this_effect(this_room,p1,event_info))//看被普攻者能否防御公开手卡效果，如果能则抢夺失败
		{
			this_room.game_broadcast("抢夺手卡失败!");
			return result;
		}


		if(!SZSC_game_judge.whether_got_card(p2)){this_room.game_broadcast("被普攻玩家无手卡，无法抢夺! ");return result;}


		
		if(SZSC_game_judge.whether_card_full(p1))
		{
			p1.game_tips("你手卡已满，如果不丢弃一张，无法继续抢夺!");
			//if(discard_free_choose(this_room,p1,true)==0)//如果自己不丢手卡
			return result;//直接返回
		}
		int which_card=SZSC_protocol.code_none;
		while(true)
		{
			show_aims_card(p1,p2,"请选择要夺取的手卡");//向p1展示p2手卡,并选择一张并夺取

			JSON_process msg=p1.game_listen();
			if(this_room.game_over_offline()){return SZSC_game_protocol.force_offline;}//玩家做出选择，如果此时断开通讯
			which_card=msg.getInt("which_card");
			if(!SZSC_game_judge.card_choice_valid(p2, which_card))
			{
				
				continue;
			}
			break;
		}

		//公布抢夺了哪张
		int p2_card_No=p2.card.get(which_card).get_card_No();
		this_room.game_broadcast(p1.get_room_name()+"抢夺了"+p2.get_room_name()+"的手卡: "+SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name,p2_card_No));
		
		//判断是否是装备卡
		if(!SZSC_game_judge.judge_type_weapon(p2_card_No))//如果不是，则只抢夺手卡
		{
			p1.card.add(p2.card.remove(which_card));//手卡的给予
		}
		else//如果是装备卡
		{
			SZSC.card this_card=p2.card.remove(which_card);//p2先丢弃此卡
			//判断装备栏是否已满
			if(SZSC_game_judge.whether_weapon_full(p1))//判断装备栏是否已满
			{	
				this_room.game_broadcast("武器装备已满!抢夺的装备卡不会直接装备，而是加入手卡!");
				p1.card.add(this_card);//p1获取该卡
				
			}
			else//如果没满，进行装备
			{
				
				SZSC_game_weapon.equip_weapon(this_room,p1,p2_card_No);

				//公布抢夺了哪个武器
				this_room.game_broadcast(p1.get_room_name()+"抢夺了"+p2.get_room_name()+" 的武器 "+SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name,p2_card_No));
				
			}
		}
			
			
		refresh_card_data(this_room);

		return result;
	}
	
	public static void deck_shuffle(SZSC_game this_room) {
		this_room.deck.clear();
		int remain_number=SZSC_protocol.decklimit;
		while(remain_number-->0) {
			add_one_random_new_card_to_deck(this_room);
		}
	}


	public static void show(String msg) {
		SZSC_service.show(msg);
	}
}
