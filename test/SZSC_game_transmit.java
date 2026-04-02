package test;

public class SZSC_game_transmit {

	public static void blood_change(SZSC_game this_room,SZSC_player p1,float number)//血量发生变化,进行状态更新
	{
		//SZSC_blood_character_info
		p1.blood+=number;
		if(p1.blood<0)
			p1.blood=0;
		refresh_character_basic_info(this_room);
	}
	public static void attack_change(SZSC_game this_room,SZSC_player p1,float number)//攻击力发生变化,进行状态更新
	{
		p1.attack+=number;
		if(p1.attack<0)
			p1.attack=0;
		refresh_character_basic_info(this_room);
	}
	
	public static void refresh_card_info(SZSC_game this_room) {
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.SZSC_card_character_info);
		
		for(SZSC_player player:this_room.players) {
			int card_number=SZSC_game_deck.get_player_card_quantity(player);//手卡数量
			msg.addToArray("player_card_number",card_number);
		}
		
		for(SZSC_player player:this_room.players) {
			if(player.human()){
				int card_number=SZSC_game_deck.get_player_card_quantity(player);//手卡数量
				JSON_process msg1=new JSON_process(msg.getString());
				msg1.add("my_card_number", card_number);
				for(SZSC.card this_card:player.card)
				{
					int card_No=this_card.get_card_No();
					msg1.addToArray("my_card",card_No);
				}
				player.send(msg1);
			}	
		}
		
		
			
		
	}

	public static void card_change(SZSC_game this_room)//有人手卡变动
	{
		refresh_card_info(this_room);
	}
	
	
	public static void weapon_change(SZSC_game this_room,SZSC_player p1)//该玩家的武器信息有了变化
	{
		refresh_character_basic_info(this_room);
		Buff_change(this_room, p1);
		/*
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_protocol.SZSC_weapon_character_info);

		msg.add("player_No",p1.get_player_No());//谁的武器更新了

		float value=SZSC_game_attack.calculate_all_attack(p1);
		msg.add("player_attack",String.valueOf(value));

		int weapon_number=0;
		for(int i=0;i<p1.get_weapon_limit();i++)
			if(p1.weapon[i].is_used())
			{
				weapon_number++;
				msg.add_array("weapon_ID",p1.weapon[i].get_weapon_ID(),"i");
			}
		msg.add("weapon_number",weapon_number);

		
		int current_weapon_number=0;
		
		for(;current_weapon_number<weapon_number;current_weapon_number++)
		{
			int effect_number=0;//该武器有多少效果
			int token_number=0;//具有的充能数量
			float ex_attack=0;//提供的总额外攻击力

			for(int i=0;i<SZSC_protocol.SZSC_Buff_limit;i++)
				if(p1.Buff[i].not_none())
					if(p1.Buff[i].check_source_type(SZSC_protocol.SZSC_source_my_weapon_effect)&&
					p1.Buff[i].get_weapon_number()==current_weapon_number)//如果该buff来源为武器且是该武器
					{
						if(p1.Buff[i].check_effect_type(SZSC_protocol.SZSC_Buff_extra_attack))
						{
							ex_attack+=p1.Buff[i].get_effect_value()[0];
							continue;//如果是加攻击力的，不算作效果
						}
						if(p1.Buff[i].is_token())//如果一个效果里带有充能
							token_number+=p1.Buff[i].get_use_time_reamin();
						

						msg.add_array("w_"+current_weapon_number,p1.Buff[i].get_ID(),"i");
						effect_number++;
					}
			msg.add_array("weapon_effect",effect_number,"i");
			msg.add_array("weapon_token",token_number,"i");

			msg.add_array("weapon_attack","i",String.valueOf(ex_attack));


			//检测该武器是否有增加额外攻击力或充能

		}
		this_room.game_broadcast(msg);
*/
	}
	public static void Buff_change(SZSC_game this_room, SZSC_player p1)
	{
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.SZSC_Buff_character_info);
		msg.add("player_No",p1.get_player_No());//谁的buff更新了

		
		String attack_value=String.valueOf(SZSC_game_attack.calculate_all_attack(p1));
		msg.add("player_attack",attack_value);

		for(SZSC_Buff current_buff:p1.buff) {
			if(current_buff.check_source_type(SZSC_game_protocol.Buff_source_card_effect))
			{
				msg.addToArray("Buff_ID",current_buff.get_effect_ID());
			}
		}
		
		msg.add("Buff_number",p1.buff.size());//谁的buff更新了
		this_room.game_broadcast(msg);

	}
	public static void blood_effect_change(SZSC_game this_room,SZSC_player p1,float rate)//血量因为效果而变动，不算伤血效果，只算效果
	{
		p1.blood*=rate;
		SZSC_game_transmit.blood_change(this_room,p1,0);
	}
	
	//名称、玩家类型、阵营、自身效果
	public static void load_player_info(SZSC_game this_room,JSON_process msg) {
		msg.add("playernumber",this_room.playernumber);
		int player_pointer=0;
		for(SZSC_player player:this_room.players)//统计所有人数据
		{
			msg.addToArray("player_type",player.get_type());
			msg.addToArray("player_camp",player.get_camp());
			msg.addToArray("player_name",player.get_name());
			String bloodlimit=String.valueOf(player.bloodlimit);
			String origin_attack=String.valueOf(player.attack);
			msg.addToArray("player_bloodlimit",bloodlimit);
			msg.addToArray("player_attackorigin",origin_attack);
			for(SZSC.ability current_ability:player.ability) {
				msg.addToArray(player_pointer+"_player_ability",current_ability.get_effect_ID());//自身效果详细数据
			}
			player_pointer++;
		}
		
	}
	//武器
	public static void load_character_weapon_info(SZSC_game this_room,JSON_process msg) {
		int player_pointer=0;
		for(SZSC_player player:this_room.players) {//统计所有人数据
			for(SZSC.Weapon this_weapon:player.weapon)
			{
				int weapon_ID=this_weapon.get_weapon_ID();
				
				msg.addToArray(player_pointer+"_player_weapon",weapon_ID);
				//只传输武器编号，根据武器编号、静态表来确认武器效果
			}
			
			player_pointer++;
		}
			
		
		
	}
	
	//血量、攻击
	public static void load_character_basic_info(SZSC_game this_room,JSON_process msg) {
		msg.add("playernumber",this_room.playernumber);
		for(SZSC_player player:this_room.players) {//统计所有人数据

			String blood=String.valueOf(player.blood);
			
			String attack=String.valueOf(SZSC_game_attack.calculate_all_attack(player));
			
			msg.addToArray("player_blood",blood);
			msg.addToArray("player_attack",attack);
		}
		
		
	}
	
	//之后反复刷新的人物状态，血量、攻
	public static void refresh_character_basic_info(SZSC_game this_room) {
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.SZSC_basic_character_info);
		load_character_basic_info(this_room, msg);
		load_character_weapon_info(this_room, msg);

		this_room.game_broadcast(msg);
	}
	
	//首次传输人物全部状态，包括阵营、个人效果、名字
	public static void refresh_character_state_F(SZSC_game this_room)
	{
		JSON_process msg=new JSON_process();
		msg.add("signal",SZSC_game_protocol.SZSC_first_basic_character_info);
		//load_character_basic_info(this_room, msg);
		//load_character_weapon_info(this_room, msg);
		load_player_info(this_room, msg);
		this_room.game_broadcast(msg);
		
		refresh_character_basic_info(this_room);
		/*
		for(int i=0;i<this_room.playernumber;i++)//统计所有人数据
		{

			msg.add_array("player_type",this_room.players[i].get_type(),"i");
			if(!this_room.players[i].not_none())
			{
				msg.add_array("player_blood",0,"i");
				msg.add_array("player_attack",0,"i");
				msg.add_array("player_camp",-1,"i");
				msg.add_array("player_name",0,"i");
				msg.add_array("player_ability",0,"i");
				continue;
			}
			SZSC_player p1=this_room.players[i];

			String blood=String.valueOf(p1.blood);
			String attack=String.valueOf(SZSC_game_attack.calculate_all_attack(p1));
			
			msg.add_array("player_blood",blood,"i");
			msg.add_array("player_attack",attack,"i");
			msg.add_array("player_camp",p1.get_camp(),"i");
			msg.add_array("player_name",p1.get_name(),"i");
			
			
			for(int j=0;j<SZSC_protocol.abilitylimit;j++)
				msg.add_array(i+"_player_ability",p1.ability[j].get_effect_ID(),"i");//自身效果详细数据
		}
		
		
		this_room.game_broadcast(msg);

		
		for(int i=0;i<this_room.playernumber;i++)
			if(this_room.players[i].human())
			{
				msg.add("player_No",i);//用于确认哪个是自己
				this_room.players[i].send(msg);
			}*/

		
	}

}
