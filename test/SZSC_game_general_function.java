package test;

import java.io.*;

public class SZSC_game_general_function {
	public static void show(String msg) {
		SZSC_service.show(msg);
	}
	public static void sleep(int time) {
		SZSC_service.sleep(time);
	}
	public static SYSTEM_EXCEL get_system_asset(String excel_path,String sheet_name){
		
		InputStream is = SZSC_game_general_function.class.getClassLoader().getResourceAsStream(excel_path);
		/*
		InputStream is=null;
		try {
			is = new FileInputStream(excel_path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return new SYSTEM_EXCEL(is, sheet_name);
		
    }
	

	
	//回合结束、有人死后、接收玩家消息使用，判断场上是否只剩一个阵营，如果是，游戏结束该阵营获胜
	public static boolean whether_game_over(SZSC_game this_room)
	{
		//如果场上只剩一方势力
		int first_camp=SZSC_protocol.code_none;//存储第一个阵营
		for(SZSC_player player:this_room.players)
			if(player.not_none()&&!player.offline())
			{
				first_camp=player.get_camp();
				break;
			}
		

		if(first_camp==SZSC_protocol.code_none)
		{
			SZSC_service.show("阵营获取错误???");
		}
		
		for(SZSC_player player:this_room.players)
			if(player.not_none()&&!player.offline())
				if(player.is_alive()&&first_camp!=player.get_camp())
					return false;//如果存在其他阵营的活人,返回游戏没结束
		
		this_room.current_state=SZSC_protocol.SZSC_end_state;
		return true;
	}

	public static void SZSC_get_client_message(SZSC_player p1,int time_length)//获取客户端发送的信息，限时操作，时长time_length,,普通打牌时和选择颜色时用到，在使用之后对选择进行超时判定强制默认选择
	{
		time_length=999000;
		if(!p1.human())return;//不是活人，c1为空指针
		
		SZSC_service.sleep(10);
		//cout<<"游戏服务等待用户输入\n";
		int wait_time=0;
		/*
		while(strlen(c1.message)==0)
		{
			if(c1.F_offline)return;
			Sleep(100);
			
			if(current_state==SZSC_end_state) return;//如果游戏结束，退出监听
			
			if(whether_your_turn(p1))//如果当前是自己回合,限时等待
			{
				p1.think_time+=100;
				//if(p1.think_time==2000||p1.think_time%10000==0)//反复每隔一段时间刷新，防止客户端没收到消息
					//UNO_update_all_info_to_himself(&uno_room[p1.c1.room_No],p1);
				if(p1.think_time>=time_length)//如果等待超时
				{
					//cout<<"用户输入超时!\n";
					return;
				}
			}
			else
				p1.think_time=0;
		}//进行等待

		//执行到这里必然收到数据
		memset(p1.message,0,sizeof(p1.message));
		addtext(p1.message,p1.c1.message);
		c1.not_be_read=false;
		Sleep(5);
		cout<<"SZSC游戏收到数据:"<<p1.message<<endl;*/
		return;
	}
	
	
	
	
    
    
    
    
    
    
    
	


	
	


	
	//void AI_think(fight_room* this_room){}



	//↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓对回合的处理↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
		

	public static void turnsettle(SZSC_player p1)														//回合结束时所有玩家状态结算
	{
		p1.set_turn_end(false);//此回合自己还没选择回合结束
		p1.set_attack_success_time(0);//此回合自己普攻成功次数置0
		p1.fight_chance=0;//可普攻的次数置0
	}

	public static void reset_rule_attack(SZSC_player p1)//重置规则性普攻
	{
		p1.fight_chance=1;//自己这回合的规则性普攻次数被重置为1
	}


	

	//↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑对回合的处理↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


	//↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓处理玩家各种信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	public static void show_character(SZSC_player p1,boolean i)//显示人物状态
	{
		/*
		HANDLE handle = GetStdHandle(STD_OUTPUT_HANDLE);//设置字体颜色（只在显示对手时改变）
		cout<<"-------------------------------------------------------\n";
		cout<<"----------------------";
		if(i)
			cout<<"主人";
		else 
		{
			SetConsoleTextAttribute(handle,FOREGROUND_INTENSITY | FOREGROUND_RED );//对手用亮红表示
			cout<<"宾客";//根据传入的boolean i判别是自己还是对手
		}

		cout<<"的状态:";
		SetConsoleTextAttribute(handle,FOREGROUND_RED | FOREGROUND_GREEN | FOREGROUND_BLUE);////恢复默认白
		cout<<"----------------------\n";
		cout<<"-------------------------------------------------------\n";
	    cout<<" "<<search_character(p1.character_number)<<endl;
		cout<<" 血量："<<p1.blood;

		int count,count2;
		float p_attack_all=	(p1.attack)	+	(p1.exattack_turn)	+	(p1.exattack_time);
		for(count=0;count<weaponlimit;count++)//检查装备卡是否有加攻击力
			if(p1.w[count]==true)
				p_attack_all=	p_attack_all	+	(p1.exattack_weapon[count])+p1.exattack_weapon_turn[count];
		cout<<"    总攻："<<p_attack_all<<"	";

		if(p1.overdeath>1)cout<<p1.overdeath<<"轮回不死之身!";
		if(p1.overdeath==1)cout<<"仅本轮回不死之身!";

		cout<<endl<<endl;

		for(count=0;count<abilitylimit;count++)
			if(p1.a[count]==true)//进行角色自身效果显示，如果有效果则显示
				cout<<"角色自身效果"<<count+1<<"："<<search_skill(p1.ability[count])<<endl;
				cout<<endl;

		boolean whether_enter=false;
		for(count=0;count<weaponlimit;count++)
			if(p1.w[count]==true)
				{cout<<"武器"<<count+1<<": "<<search_card(p_name,p1.weapon[count]);
			     if(p1.weapon[count]==6)
					 cout<<"(充能+"<<p1.weaponeffect06[count]<<")"<<"  ";
				 else
					 cout<<"(攻+"<<p1.exattack_weapon[count]+p1.exattack_weapon_turn[count]<<")"<<"  ";
				 whether_enter=true;}
		if(whether_enter)cout<<endl;
		for(count=0;count<weaponlimit;count++)
			{
			if(p1.w[count]==true)
			{
				cout<<"武器"<<count+1;
				if(p1.weapon[count]==10){cout<<"\t 无特殊效果\n";continue;}
				for(count2=0;count2<weaponeffectlimit;count2++)
					{
					if(p1.w_e[count][count2]==true)//进行角色装备武器的效果显示，如果有效果则显示
						{cout<<"\t"<<" 效果"<<count2+1<<"："<<search_skill(p1.weapon_effect[count][count2])<<endl;}
					else 
						break;
					}
			}
			}

		if(!i)//统计对手有多少手卡
		{
			for(count=0;count<cardlimit;count++)
				if(p1.c[count]!=true)
					break;
			cout<<"他目前有"<<count<<"张手卡\n";
		}*/

	}
	public static void player_launch_broadcast(SZSC_game this_room,SZSC_player p1,SZSC.Launch_Info situation_Info) {
		int card_No=situation_Info.get_card_No();
		switch(situation_Info.get_launch_source_type()) {
			case SZSC_game_protocol.TYPE_launch_assist:
			case SZSC_game_protocol.TYPE_launch_hide:
			case SZSC_game_protocol.TYPE_launch_weapon:
			case SZSC_game_protocol.TYPE_launch_effect:
				this_room.game_broadcast(p1.get_room_name()+"发动手卡:  " +SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name, card_No));
				break;
			case SZSC_game_protocol.TYPE_buff:
				this_room.game_broadcast(p1.get_room_name()+"发动自身加附效果:  " +SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name, card_No));
				break;
			case SZSC_game_protocol.TYPE_weapon_effect:{
				int which_weapon=situation_Info.get_which_weapon();
				int which_effect=situation_Info.get_which_effect();
				int weapon_ID=p1.weapon.get(which_weapon).get_weapon_ID();
				int weapon_effect_ID=card_No;
				String weapon_name=SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name, weapon_ID);
				String effect_description=SZSC_game_dictionary.search_card(SZSC_game_protocol.p_details, weapon_effect_ID);
				this_room.game_broadcast(p1.get_room_name()+"发动自己第 "+which_weapon+" 把武器:  " +weapon_name+" 的第 "+(which_effect+1)+" 个效果: "+effect_description);
			}
				break;
			case SZSC_game_protocol.TYPE_self_effect:
			{
				int which_effect=situation_Info.get_which_effect();
				int effect_ID=p1.ability.get(which_effect).get_effect_ID();
				String effect_name=SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name, effect_ID);
				String effect_description=SZSC_game_dictionary.search_card(SZSC_game_protocol.p_details, effect_ID);
				this_room.game_broadcast(p1.get_room_name()+"发动自己第 "+which_effect+" 个自身效果【"+effect_name+"】"+effect_description);
			}
				break;
		}
	}
	
	public static void send_personal_msg(SZSC_player player,String content) {
		JSON_process msg=new JSON_process();
		msg.add("signal", SZSC_game_protocol.Signal_player_record_information);
		msg.add("content", content);
		player.send(msg);
	}


	
	public static int getrandom(int bottomlimit,int uplimit) {
		return core_main.getrandom(bottomlimit, uplimit);
	}
	
	
}
