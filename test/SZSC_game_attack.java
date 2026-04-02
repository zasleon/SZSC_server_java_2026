package test;

public class SZSC_game_attack {
	public static String fight_back(SZSC_game this_room,SZSC_player p1,SZSC.Event_info event_info)//所有人反击p1作出的行为(憎恨)
	{
		String result=SZSC_game_protocol.still_fight;
		SZSC_player player_launcher=event_info.get_launcher();
		
		//统计一共有多少另外的活人
		int alive_number=0;
		for(SZSC_player player:this_room.players)
			if(!player.check_player(player_launcher)&&player.is_alive())
				alive_number++;
		
		//SZSC_service.show("event  "+ event_info.get_type()+"   其他活人个数:"+alive_number);
		
		if(alive_number<=1)//如果只有一个,也可能场上只有一个机器人了
		{
			for(SZSC_player player:this_room.players) {
				if(player.human()&&!player.check_player(player_launcher)&&player.is_alive())
				{
					result=fight_back(this_room,player,player_launcher,event_info);//只让该玩家进行反击
					break;
				}
				if(player.bot())
				{
					SZSC_service.show("bot暂时不会作任何反击");
				}
					
			}
			
			
		}

		return result;
		//后面是多人模式下对某个人作出行动的反击，需要重写

		//开启多线程监听，谁先反击
		//当某个人作出反击后，查看反击是否可以发动，如果可以发动，其他人监听反击函数结束，

		/*
		for(SZSC_player player:this_room.players)
			if(player.human()&&player.check_player(p1)&&player.is_alive())
				if(SZSC_game_judge.whether_moveable(player))
				{
					String event_name="???";
					
					
					switch(situation)
					{
						case SZSC_game_protocol.SZSC_other_use_card:
							event_name="他发动了一张卡";break;
						default:SZSC_service.show("----------------------------意外situation="+situation);
					}
					//if(player[i].i_soon_die)
						//addtext(event_name,"(此为致命性伤血!)");

					JSON_process reply_msg=new JSON_process();
					reply_msg.add("signal",SZSC_game_protocol.event_happen);
					reply_msg.add("event_name",event_name);
				}
		

		return result;*/

	}



	public static String fight_back(SZSC_game this_room,SZSC_player p1,SZSC_player p2,SZSC.Event_info event_info)//p1反击p2
	{
		String event_name=event_info.get_type();
		float event_value=event_info.get_value();
		
		SZSC_service.show("反击事件 "+event_name+"    "+event_value);
		
		String result=SZSC_game_protocol.still_fight;
		if(p1==null)
		{
			SZSC_service.show("反击对象失效!");
			return result;
		}

		if(!SZSC_game_judge.whether_moveable(p1))
		{
			String content=p1.get_room_name()+"当前处于无法行动状态!";
			
			
			
			this_room.game_broadcast(content);
			return SZSC_game_protocol.still_fight;
		}
		if(!p1.human())
		{
			SZSC_service.show("bot暂时不会做任何反击行动");
			return result;
		}

		if(p2!=null)p2.game_tips("等待对面思考……");

		/*
		switch(situation)
		{
			case SZSC_protocol.SZSC_i_will_be_effect_A:		event_name="你即将受到自主效果伤血";break;
			case SZSC_protocol.SZSC_i_will_attack:			event_name="你即将普攻";break;
			case SZSC_protocol.SZSC_i_will_be_attacked:		event_name="你即将被普攻";break;
			case SZSC_protocol.SZSC_i_attack_success:		event_name="你普攻成功";break;
			case SZSC_protocol.SZSC_i_be_attacked:			event_name="你被普攻了";break;
			case SZSC_protocol.SZSC_i_attack_fail_E:		event_name="你普攻被躲避了";break;
			case SZSC_protocol.SZSC_i_attack_fail_D:		event_name="你普攻被格挡了";break;
			case SZSC_protocol.SZSC_i_be_effect_A:			event_name="你受到了自主伤血效果";break;
			case SZSC_protocol.SZSC_i_be_effect_B:			event_name="你受到了伤血效果";break;
			case SZSC_protocol.SZSC_i_will_be_effect_B:		event_name="你即将受到伤血效果";break;
			case SZSC_protocol.SZSC_other_use_card:			event_name="发动了一张卡";break;
			default:SZSC_service.show("----------------------------意外situation="+situation);
		}
		if(p1.soon_die())
			event_name=event_name+"(此为致命性伤血!)";*/

		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal",SZSC_game_protocol.Signal_event_happen);
		reply_msg.add("event_name",event_name);
		reply_msg.add("whether_die", p1.soon_die()?"(此为致命性伤血!)":"");
		reply_msg.add("event_value",String.valueOf(event_value));
		

		while(true)//做选择死循环
		{
			p1.set_interface_state(SZSC_game_protocol.player_interface_state_fight_back);//用户界面改成反击界面
			
			p1.send(reply_msg);
			JSON_process msg=p1.game_listen();

			if(p1.offline()){return SZSC_game_protocol.force_offline;}//玩家做出选择，如果此时断开通讯
			
			int choice=msg.getInt("signal");
			p1.set_interface_state(SZSC_game_protocol.player_interface_state_return_normal);
			//state_return_normal
			

			//if(StrBuf[0]=='0'&&strlen(StrBuf)==1){confirm_send_success(c1,"你选择不行动!\n");confirm_send_success(c2,"对面选择不行动!\n");return still_fight;}
			//if(strlen(StrBuf)>1||atoi(StrBuf)>4||atoi(StrBuf)<1){confirm_send_success(c1,"输入了无效字符!请重新选择!\n");continue;}
			
			switch(choice){
				case SZSC_protocol.SZSC_player_apply_use_card:{//发动手卡开始
					//获取发动第几张手卡
					int which_card=msg.getInt("which_card");
					//是否发动卡片隐效果,1为发动，0为不发动
					boolean hide_effect=(msg.getInt("hide_effect")==1)?true:false;
					//检查选择合规性
					result=SZSC_game_player_choose.player_use_card(this_room, event_info, p1, p2, which_card, hide_effect);
					
					}//发动手卡结束
					break;
				case SZSC_protocol.SZSC_player_apply_use_self_effect:{//发动角色效果开始
				
					int which_effect=msg.getInt("which_effect");
					result=SZSC_game_player_choose.player_self_effect(this_room, event_info, p1, p2, which_effect);
					
					
					}//发动角色效果结束
					break;
				case SZSC_protocol.SZSC_player_apply_use_weapon_effect:{//发动武器效果开始
				
					int which_weapon=msg.getInt("which_weapon");
					int which_effect=msg.getInt("which_effect");
					result=SZSC_game_player_choose.player_weapon_effect(this_room, event_info, p1, p2, which_weapon, which_effect);
					
					break;
				}
			case SZSC_protocol.SZSC_player_apply_use_buff://发动加附效果开始
				{
					int which_buff=msg.getInt("which_buff");
					
					result=SZSC_game_player_choose.player_buff_effect(this_room, event_info, p1, p2, which_buff);
					
				}//发动加附效果结束
				break;
			case SZSC_protocol.SZSC_player_apply_give_up_action:
				{
					p1.game_tips("你选择不行动!");
					result=SZSC_game_protocol.CHOICE_i_cancel_action;
				}
				break;
			default:
				{SZSC_service.show("错误反击请求:"+choice);continue;}
			}//所有的发动结局统一到这里收尾
			if(result.equals(SZSC_game_protocol.CHOICE_i_do_useless_choice))
				continue;
			break;

		}//被攻者做选择死循环结束
		p1.set_interface_state(SZSC_game_protocol.player_interface_state_return_normal);
		return result;
	}



	//统计p1总攻击力
	public static float calculate_all_attack(SZSC_player p1)
	{
		//额外攻击力
		float ex_attack=0;
		for(SZSC_Buff current_buff:p1.buff)//获取自身原本攻击力，为基础攻击力
			if(current_buff.check_effect_type(SZSC_game_protocol.Buff_extra_attack_power))
				ex_attack+=current_buff.get_effect_value();
		
		//SZSC_service.show("额外攻击力:  " +ex_attack);
		float base_attack=p1.attack;
		for(SZSC_Buff current_buff:p1.buff)//基础攻击力倍率
			if(current_buff.check_effect_type(SZSC_game_protocol.Buff_base_attack_mutipler))
				base_attack*=current_buff.get_effect_value();
		

		//总攻击力=基础攻击力*基础攻击力倍率+额外攻击力
		float attack_all=base_attack+ex_attack;
		for(SZSC_Buff current_buff:p1.buff)//总攻击力倍率
			if(current_buff.check_effect_type(SZSC_game_protocol.Buff_all_attack_mutipler))
				attack_all*=current_buff.get_effect_value();
		
		
		return attack_all;
	}


	public static float calculate_general_attack_damage(SZSC_player p1,SZSC_player p2)//计算普攻伤害，p1打p2
	{
		float attacker_value=calculate_all_attack(p1);
		float defender_value=calculate_all_attack(p2);
		float damage=0;//计算普攻伤害数值

		//检索p1 buff，是否有穿攻
		if(SZSC_game_judge.whether_penetrate(p1))
		{
			damage	=	attacker_value;//如果是穿攻，伤害值为全额攻击方攻击力


			//检查双方有无增加/减少伤害倍率的效果buff
			for(SZSC_Buff current_buff:p1.buff)
			{
				if(current_buff.check_effect_type(SZSC_game_protocol.Buff_normal_attack_dmg_mutipler))
					damage*=current_buff.get_effect_value();
			}
			for(SZSC_Buff current_buff:p2.buff)
			{
				if(current_buff.check_effect_type(SZSC_game_protocol.Buff_normal_attack_dmg_mutipler))
					damage*=current_buff.get_effect_value();
			}

			//addtext(StrBuf,p1.name);addtext(StrBuf," 对 ");
			//addtext(StrBuf,p1.name);addtext(StrBuf," 造成了穿透性攻击!!!");
			//addtext(StrBuf,"并对他造成了");addtext(StrBuf,damage);addtext(StrBuf,"点伤害!\n");
		}
		else
		{
			damage	=( attacker_value - defender_value );//普通类型攻击，计算两者攻击力差额
			//检查双方有无增加/减少伤害倍率的效果buff
			for(SZSC_Buff current_buff:p1.buff)
			{
				if(current_buff.check_effect_type(SZSC_game_protocol.Buff_normal_attack_dmg_mutipler))
					damage*=current_buff.get_effect_value();
			}
			for(SZSC_Buff current_buff:p2.buff)
			{
				if(current_buff.check_effect_type(SZSC_game_protocol.Buff_normal_attack_dmg_mutipler))
					damage*=current_buff.get_effect_value();
			}



			if(damage>0)
			{
				//addtext(StrBuf,p1.name);addtext(StrBuf,"的攻击成功命中 ");
				//addtext(StrBuf,p1.name);addtext(StrBuf,"!");
				//addtext(StrBuf,"并对他造成了 ");addtext(StrBuf,damage);addtext(StrBuf,"点伤害!\n");
			}
			else if(damage<0)
			{
				//addtext(StrBuf,p1.name);addtext(StrBuf,"的攻击力小于被攻击者 ");
				//addtext(StrBuf,p1.name);addtext(StrBuf,"的攻击力!对自己造成了反伤!");
				//addtext(StrBuf,"并对自己造成了");addtext(StrBuf,-damage);addtext(StrBuf,"点伤害!\n");
			}
			//else
				//addtext(StrBuf,"两者攻击力相同!本次普攻不对双方任何人造成伤害!\n");
		}
				
			
		
		return damage;
	}



	

	public static void attack_event_end(SZSC_player p1,SZSC_player p2)//普攻事件结束时的结算
	{
		//清除此次普攻特效
		//fight_data_clean(p1,false);
		//fight_data_clean(p2,false);
		
		
		//两者参与事件-1
		p1.attack_event(false);
		p2.attack_event(false);
		
		return;
	}


	//通用攻击函数，p1攻击p2，situtation判断是否是p1主场主动攻击，若是则消耗规则普攻次数，不是则此项无关
	public static String general_attack(SZSC_game this_room,SZSC_player p1,SZSC_player p2)
	{
		String result=SZSC_game_protocol.still_fight;//初始默认为普攻成功
		
		p1.attack_event(true);p2.attack_event(true);//两者进入搏斗状态

		//广播
		this_room.game_broadcast(p1.get_room_name()+" 对 "+p2.get_room_name()+"发动了普攻!");
		
		//SZSC_service.show("事件：“我要普攻了\n";
		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_attack,0,p1,null);
		//被动生效
		result=SZSC_game_Buff_process.activate_Buff(this_room,p1,p2,event_info);
		if(SZSC_game_judge.event_force_end(result))//如果事件中断或逾期
		{attack_event_end(p1,p2);return result;}

		//SZSC_service.show("事件：“我要被普攻了\n";
		event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_be_attacked,0,p2,p1);
		//被动生效
		result=SZSC_game_Buff_process.activate_Buff(this_room,p2,p1,event_info);
		if(SZSC_game_judge.event_force_end(result))//如果事件中断或逾期
		{attack_event_end(p1,p2);return result;}

		//普攻玩家可以在此时发动助攻卡
		event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_attack,0,p1,p2);
		//主动
		result=fight_back(this_room,p1,p2,event_info);
		//如果在普攻前摇过程中游戏结束，或回合结束了，返回
		if(SZSC_game_judge.event_force_end(result))//如果有普攻事件中断或逾期
		{attack_event_end(p1,p2);return result;}
		

		float damage=calculate_general_attack_damage(p1,p2);
		//SZSC_service.show("预估攻击伤害----  "<<damage<<endl;

		//即将进行伤害计算，询问被普攻者是否进行反击，包括 躲避 格挡 反击 等
		//在伤害判定后的“伤害成功”与“伤害失败”事件中对普攻特效清除
		event_info=new SZSC.Event_info(SZSC_game_protocol.TYPE_event_dmg_attack,damage,p1,p2);
		result=hit_damage(this_room,p2,p1,event_info);//反击之类的都在该函数内实现
		
		attack_event_end(p1,p2);//两者退出搏斗状态
		if(!SZSC_game_judge.event_force_end(result))
			result=SZSC_game_protocol.force_end_event;
		return result;

		
	}


	public static String normal_effect_dmg(SZSC_game this_room,float dmg_value,float probability,SZSC_player player_target,SZSC_player player_launcher) {
		
		SZSC.Event_info new_event_info=SZSC.get_new_event_info(SZSC_game_protocol.TYPE_event_effect_dmg, dmg_value, player_target, player_launcher);
		new_event_info.set_probability(probability);
		return SZSC_game_attack.hit_damage(this_room, player_target, player_launcher, new_event_info);
		
	}
	
	public static String self_effect_dmg(SZSC_game this_room,float dmg_value,float probability,SZSC_player player_launcher) {
		
		SZSC.Event_info new_event_info=SZSC.get_new_event_info(SZSC_game_protocol.TYPE_event_effect_dmg, dmg_value, player_launcher, player_launcher);
		new_event_info.set_probability(probability);
		return SZSC_game_attack.hit_damage(this_room, player_launcher, player_launcher, new_event_info);
		
	}


	//damage 受到多少伤害 p1受伤害者 p2施加伤害者 this_room这个房间 locktime连锁次数 situation什么伤害类型（自主伤血，效果伤血，普攻伤血）
	public static String hit_damage(SZSC_game this_room,SZSC_player player_target,SZSC_player player_launcher,SZSC.Event_info event_info)//c2对c1造成damage伤血
	{
		String result=SZSC_game_protocol.still_fight;
		float damage_value=event_info.get_value();

		if(player_target.blood-damage_value<0)//如果是致命伤血型反击
			player_target.set_soon_die();//自己即将死亡

		String event_type=event_info.get_type();
		String take_dmg_player_new_event_type="";
		String do_dmg_player_new_event_type="";
		boolean dmg_effect_positive=false;
		
		switch(event_type) {
			case SZSC_game_protocol.TYPE_event_effect_dmg:
				dmg_effect_positive=(player_launcher==null||player_launcher==player_target);
				if(dmg_effect_positive)
					take_dmg_player_new_event_type=SZSC_game_protocol.i_will_be_dmg_effect_positive;
				else
					take_dmg_player_new_event_type=SZSC_game_protocol.i_will_be_dmg_effect_passive;
				break;
			case SZSC_game_protocol.TYPE_event_dmg_attack:
				take_dmg_player_new_event_type=SZSC_game_protocol.i_will_be_attacked;
				break;
			default:
				SZSC_service.show("hit_damage 获取错误event_type :"+event_type);
				return result;
		}
		
		//询问即将受到伤害的玩家是否反击
		SZSC.Event_info new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type,damage_value,player_target,player_launcher);
		result=fight_back(this_room,player_target,player_launcher,new_event_info);//自己反击

		player_target.cancel_soon_die();//消除即将死亡标志

		if(SZSC_game_judge.event_force_end(result))return result;//如果事件中断或逾期

		switch(result)
		{
			case SZSC_game_protocol.i_failed_attack_be_defend:
			case SZSC_game_protocol.i_failed_attack_be_escape:
			case SZSC_game_protocol.i_failed_attack:
			{
				//本次普攻结束
				SZSC_game_Buff_process.event_end(this_room, SZSC_game_protocol.TYPE_Duration_this_attack);
				
				//被动
				new_event_info=new SZSC.Event_info(result, damage_value,player_launcher,player_target);
				result=SZSC_game_Buff_process.activate_Buff(this_room,player_launcher,player_target,new_event_info);
				
				//询问失败后的反击
				result=fight_back(this_room,player_target,player_launcher,new_event_info);
				
				if(SZSC_game_judge.event_force_end(result))
					return result;
				else
					return SZSC_game_protocol.force_end_event;
			}
				
			case SZSC_game_protocol.i_failed_do_dmg_effect:
			case SZSC_game_protocol.i_failed_do_dmg:
			{
				//本次伤害结束
				SZSC_game_Buff_process.event_end(this_room, SZSC_game_protocol.TYPE_DURATION_this_effect_dmg);
				
				//被动
				new_event_info=new SZSC.Event_info(result, damage_value,player_launcher,player_target);
				result=SZSC_game_Buff_process.activate_Buff(this_room,player_launcher,player_target,new_event_info);
				
				//询问失败后的反击
				new_event_info=new SZSC.Event_info(result, damage_value,player_launcher,player_target);
				result=fight_back(this_room,player_target,player_launcher,new_event_info);
				
				if(SZSC_game_judge.event_force_end(result))
					return result;
				else
					return SZSC_game_protocol.force_end_event;
			}
				
		}
		//执行到这里必然结算伤害
		
		if(SZSC_game_judge.event_force_end(result))return result;//如果事件中断或逾期
		
		
		//是否免疫即将到来的伤害
		boolean do_dmg=true;
		if(SZSC_game_judge.random_happen((int)event_info.get_probability())) {
			switch(event_type) {
				case SZSC_game_protocol.TYPE_event_effect_dmg:
					if(dmg_effect_positive)//如果是自伤
						take_dmg_player_new_event_type=SZSC_game_protocol.Launch_i_take_dmg_effect_positive;
					else//如果是他人给予的伤害
						take_dmg_player_new_event_type=SZSC_game_protocol.Launch_i_take_dmg_effect_passive;
					new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type, damage_value,player_target,player_launcher);
					if(SZSC_game_judge.whether_immune_this_effect(this_room,player_target,new_event_info))
						do_dmg=false;
					break;
				case SZSC_game_protocol.TYPE_event_dmg_attack:
					damage_value=calculate_general_attack_damage(player_launcher,player_target);
					take_dmg_player_new_event_type=SZSC_game_protocol.Launch_i_take_dmg_attack;
					new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type, damage_value,player_target,player_launcher);
					if(SZSC_game_judge.whether_immune_this_effect(this_room,player_target,new_event_info))
						do_dmg=false;
			}
		}
		else {
			do_dmg=false;
		}
		
		if(do_dmg) {
			//game_broadcast(StrBuf);//伤害成功命中目标，广播装逼效果句
			//伤害结算
			if(damage_value>0)//成功受伤
			{
				SZSC_game_transmit.blood_change(this_room,player_target,-damage_value);//扣血
			}
			else//反伤
			{
				if(!event_type.equals(SZSC_game_protocol.TYPE_event_dmg_attack))
					SZSC_service.show("非普攻反伤？？？   "+event_type+"   "+damage_value);
				SZSC_game_transmit.blood_change(this_room,player_launcher,damage_value);//反伤
			}
			this_room.game_broadcast(player_launcher.get_room_name()+" 对 "+player_target.get_room_name()+" 造成了"+damage_value+"伤害!");
		}
		//执行到这里必然结算完成
		if(SZSC_game_judge.judge_one_die(this_room,player_target,player_launcher))
		{
			return SZSC_game_protocol.force_end_event;
		}

		boolean ensure_dmg_type=false;
		
		//场景转换
		if(do_dmg)
		switch (event_type) {
			case SZSC_game_protocol.TYPE_event_effect_dmg://效果伤血
				if(dmg_effect_positive) {//自主伤血效果
					take_dmg_player_new_event_type=SZSC_game_protocol.i_be_dmg_effect_positive;
					new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type, damage_value,player_target,player_launcher);
					result=SZSC_game_Buff_process.activate_Buff(this_room,player_target,player_launcher,new_event_info);//成功承受伤害，被动
					
					//本次伤害结束
					SZSC_game_Buff_process.event_end(this_room, SZSC_game_protocol.TYPE_DURATION_this_effect_dmg);
					
					//反击
					result=fight_back(this_room,player_target,player_launcher,new_event_info);
					break;
				}
				else {//他人造成的伤血效果
					take_dmg_player_new_event_type=SZSC_game_protocol.i_be_dmg_effect_passive;
					do_dmg_player_new_event_type=SZSC_game_protocol.i_success_do_dmg_effect;
					ensure_dmg_type=true;
				}
					
			case SZSC_game_protocol.TYPE_event_dmg_attack:
				//如果是普攻伤害
				if(!ensure_dmg_type) {
					take_dmg_player_new_event_type=SZSC_game_protocol.i_be_attacked;
					do_dmg_player_new_event_type=SZSC_game_protocol.i_success_attack;
				}
				//成功承受伤害，被动
				new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type, damage_value,player_target,player_launcher);
				result=SZSC_game_Buff_process.activate_Buff(this_room,player_target,player_launcher,new_event_info);
				if(SZSC_game_judge.event_force_end(result))return result;//如果事件中断或逾期
				//成功造成了伤害，触发长者之镰或其他效果的被动
				new_event_info=new SZSC.Event_info(do_dmg_player_new_event_type, damage_value,player_launcher,player_target);
				result=SZSC_game_Buff_process.activate_Buff(this_room,player_launcher,player_target,new_event_info);
				if(SZSC_game_judge.event_force_end(result))return result;//如果事件中断或逾期
				
				//本次普攻结束
				SZSC_game_Buff_process.event_end(this_room, SZSC_game_protocol.TYPE_Duration_this_attack);
				
				//成功承受伤害，反击
				new_event_info=new SZSC.Event_info(take_dmg_player_new_event_type, damage_value,player_target,player_launcher);
				result=fight_back(this_room,player_target,player_launcher,new_event_info);
				if(SZSC_game_judge.event_force_end(result))return result;//如果事件中断或逾期
				//成功造成伤害，追击
				new_event_info=new SZSC.Event_info(do_dmg_player_new_event_type, damage_value,player_launcher,player_target);
				result=fight_back(this_room,player_launcher,player_target,new_event_info);
				break;
			default:SZSC_service.show("奇怪的伤害伤害场景  event_type="+event_type+"     "+damage_value);
		}


		return result;
	}

	public static String hit_myself_with_effect_damage(SZSC_game this_room,SZSC_player p1,float damage_value)
	{
		String result=SZSC_game_protocol.still_fight;
		
		
		SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_be_dmg_effect_positive, damage_value, p1, null);

		result=hit_damage(this_room,p1,null,event_info);//扣血

		return result;

	}

	

	


	

	//对方所有人受到效果伤血
	public static String all_enemy_get_effect_damage(SZSC_game this_room,SZSC_player p1,int damage_value)
	{
		String result=SZSC_game_protocol.still_fight;
		//如果不是和p1一个势力的都会受到效果伤血
		return result;
	}



	public static String lightspeed_attack(SZSC_game this_room,SZSC_player p1,SZSC_player p2,float damage)//超速普攻,p1打p2,暂定为直接修改血量,如果damage=0说明根据两者原本攻击力差额计算伤害，否则为固定伤害
	{

		if(damage==0)//如果damage为0，则计算两者攻击力
		{
		
			damage=calculate_general_attack_damage(p1,p2);
			if(damage!=0)
			{
				if(damage>0)
				{
					SZSC_game_transmit.blood_change(this_room,p2,-damage);
				}
				else//反伤
				{
					SZSC_game_transmit.blood_change(this_room,p1,damage);
				}
			}
			
		}
		else//一般都是特殊值的超速普攻
		{
			SZSC_game_transmit.blood_change(this_room,p2,-damage);
		}

		p1.attack_success();//此回合普攻成功次数+1

		/*
		SZSC_game_Buff_process.activate_Buff(this_room,SZSC_game_protocol.i_success_do_dmg,damage,p2,p1);//对某人成功造成了伤害，触发长者之镰或其他效果的被动	
		SZSC_game_Buff_process.activate_Buff(this_room,SZSC_game_protocol.i_success_attack,0,p2,p1);
		SZSC_game_Buff_process.activate_Buff(this_room,SZSC_game_protocol.i_success_do_fight,0,p1,p2);//两人进行了一次搏斗，触发一些被动（例如 噬剑之剑）
		*/
		
		SZSC_game_judge.judge_one_die(this_room,p1,p2);
		

		return SZSC_game_protocol.force_end_event;

	}


	public static String recover_blood(SZSC_game this_room,float value,SZSC_player player_target,SZSC_player player_launcher)//p1血量回复number
	{
		String result=SZSC_game_protocol.still_fight;
		if(value<0)
		{
			SZSC_service.show("恢复数值异常!为负!  "+value);
			return result;
		}
		if(SZSC_game_judge.whether_blood_full(player_target))
		{
			this_room.game_broadcast(player_target.get_room_name()+" 的血量为满，无法继续增加!");
			return result;
		}
		float real_value=value;
		if(player_target.get_blood()+value>player_target.bloodlimit)
			real_value=player_target.bloodlimit-player_target.get_blood();
		
		this_room.game_broadcast(player_target.get_room_name()+" 回复了"+real_value+" 血!");
		SZSC_game_transmit.blood_change(this_room,player_target,value);
		return result;
	}
	
	


}
