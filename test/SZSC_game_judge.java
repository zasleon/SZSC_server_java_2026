package test;

public class SZSC_game_judge {
	private static void show(String msg)
	{
		
		SZSC_service.show(msg);
	}
	
	
	//当前契机场景：situation，玩家p1想发动card_No这个效果，判断是否满足发动条件
	//p2为当前场景发动造成者
	public static boolean judge_launch_condition(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,SZSC.Launch_Info launch_Info) {
		
		boolean result=true;
		//根据general表获取card_No对应的 具体信息 （buff、卡片、武器技能、人物技能）
		int card_No=launch_Info.get_card_No();
		//根据 卡片信息 获取 发动条件
		//如果满足发动条件则再判断cost发动前置条件是否满足，如果不满足发动条件则直接返回不能发动
		result=fulfill_the_activation_conditions(this_room,event_info,player_launcher,player_target,launch_Info);
		
		
		if(result) {
			//执行到这里，必然满足发动卡片信息上要求的发动条件
			//查看是否有次数限制、次数条件限制
			if(launch_Info.is_buff()) {
				SZSC_Buff buff=launch_Info.get_buff();
				String limit_type=buff.get_limit_type();
				if(!limit_type.isBlank()) {//如果存在使用次数限制
					//判断是否有剩余次数
					if(buff.use_time_no_remain()) {
						player_launcher.game_tips("次数已经用完!");
						return false;
					}
					//特定次数限制带有自己回合字样，需要同时满足是在自己回合
					switch(limit_type) {
						case "自己回合":
							if(!player_launcher.whether_host()) {
								player_launcher.game_tips("当前非自己回合!");
								return false;
							}
							break;
							
					}
				}
			}
			
			//查看是否满足特定种类卡发动条件
			String launch_source_type=launch_Info.get_launch_source_type();
			switch(launch_source_type) {
				case SZSC_game_protocol.TYPE_launch_assist:{
					launch_Info.set_condition_name(SZSC_game_protocol.i_will_attack);
					result=fulfill_the_activation_conditions(this_room,event_info,player_launcher,player_target,launch_Info);
					if(!result)
						player_launcher.game_tips("助攻卡需在自己发动普攻后才能发动!");
					break;
				}
				case SZSC_game_protocol.TYPE_launch_weapon:{
					launch_Info.set_condition_name(SZSC_game_protocol.STATE_my_turn_positive_weapon_not_full);
					result=fulfill_the_activation_conditions(this_room,event_info,player_launcher,player_target,launch_Info);
					if(!result)
						player_launcher.game_tips("想要装备武器，需要在自己回合、自己未处于事件、自己武器槽未满时才能发动!");
					break;
				}
				//case SZSC_game_protocol.TYPE_launch_assist:
			}
		}
		else {
			if(launch_Info.get_buff()!=null)
				if(launch_Info.get_buff().is_positive())//只有主动发动效果需要提示，被动buff不满足发动条件时无需提示
					player_launcher.game_tips("不满足发动条件!无法发动!");
			if(launch_Info.get_buff()==null)
				player_launcher.game_tips("不满足发动条件!无法发动!");
		}
		
		return result;
	}
	public static boolean fulfill_the_activation_conditions(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,SZSC.Launch_Info launcher_info) {
		
		boolean result=true;
		//如果为“是”则一定要满足,否则直接判定为无法发动
		//如果为"可以"则不一定要满足，但在“状态”或者“场合”里至少要满足一项才能发动
		//如果为""（空）则表示非该类，不用进行项该判断
		String condition_name=launcher_info.get_condition_name();
		if(condition_name.isBlank())
			return true;
		
		SZSC.General_Info condition_Info=SZSC_game_dictionary.EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH,"event", "name", condition_name);
		//根据 具体信息 提取 是否是“状态”，如果是状态，则检查环境、人物效果是否满足发动条件
		boolean condition_fulfill=true;
		String condition_state=condition_Info.get_string("状态");
		//如果是buff被动，而条件内容为空，则意味着不可触发型被动
		if(condition_state.isBlank()&&launcher_info.is_buff()&&!launcher_info.get_buff().is_positive())
			return false;
		if(!condition_state.isBlank()) {
			condition_fulfill=STATE_judge(this_room,event_info,p1,p2,launcher_info);
			//如果是“是”而不满足，则直接返回“否”
			if(condition_state.equals("是")&&(!condition_fulfill))
				return false;
		}
		
		
		
		//根据 具体信息 提取 是否是“场合”，如果是场合，则判断是否和当前场合相符，或者是否包含当前场合
		boolean cost_condition_fulfill=true;
		String condition_situation=condition_Info.get_string("场合");
		
		//如果是“是”而不满足，则直接返回“否”
		if(!condition_situation.isBlank()) {
			cost_condition_fulfill=condition_judge(this_room,event_info,p1,p2,launcher_info);
			if(condition_situation.equals("是")&&(!cost_condition_fulfill))
				return false;
		}
		
		
		result=(condition_fulfill||cost_condition_fulfill);
		
		return result;
	}
	public static boolean STATE_judge(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,SZSC.Launch_Info launch_Info) {
		
		String condition_name=launch_Info.get_condition_name();
		if(condition_name.equals(""))
			return true;
		boolean result=false;
		//直接根据condition_name判断需要满足的当前各种状态
		switch(condition_name) {
			case SZSC_game_protocol.i_will_die:
				result=p1.soon_die();
				break;
			case SZSC_game_protocol.i_success_attack_over_3_times:
				result=(p1.get_attack_success_time()>3);
				break;
			case SZSC_game_protocol.i_success_attack_or_my_turn_positive:
				result=p1.whether_my_turn();
				break;
			case SZSC_game_protocol.i_success_use_card_in_my_turn:
				result=p1.whether_my_turn();
				break;
			case SZSC_game_protocol.STATE_my_turn_enough_weapon_token:
				result=p1.whether_my_turn();
				if(result)
				{
					float token_request_value=launch_Info.get_condition_value();
					//查找对应buff
					int which_weapon=launch_Info.get_which_weapon();
					int which_effect=launch_Info.get_which_effect();
					int token_value=SZSC_game_Buff_process.get_weapon_token_value(p1, which_weapon,which_effect);
					result=(token_value>=token_request_value);
				}
				break;
			case SZSC_game_protocol.STATE_my_turn_positive:
				result=p1.whether_my_turn()&&!p1.in_attack_event();
				break;
			case SZSC_game_protocol.STATE_my_turn_positive_weapon_not_full:
				result=(p1.get_weapon_number()<p1.get_weapon_limit())&&p1.whether_my_turn()&&!p1.in_attack_event();
				break;
			case SZSC_game_protocol.STATE_team_turn_i_positive:
				for(SZSC_player player:this_room.players) {
					if(player.whether_my_turn())
						result=(player.get_camp()==p1.get_camp());
						break;
					}
				break;
			case SZSC_game_protocol.STATE_i_weapon_not_full:
				result=(p1.get_weapon_number()<p1.get_weapon_limit());
				break;
			case SZSC_game_protocol.STATE_i_have_weapon_equipped:
				result=(p1.get_weapon_number()>0);
				break;
			case SZSC_game_protocol.STATE_i_involve_event_over_4_chains:
				result=(this_room.locktime>=4)&&p1.in_attack_event();
				break;

			default:
				show("未能识别 condition_name:   "+condition_name+"   直接判定为不满足发动条件!");
				result=false;

		}
		
		
		return result;
	}
	public static boolean condition_judge(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player p1,SZSC_player p2,SZSC.Launch_Info launch_info) {
		String situation=event_info.get_type();
		float situation_value=event_info.get_value();
		
		String condition_name=launch_info.get_condition_name();
		if(condition_name.equals(""))
			return true;
		boolean result=false;
		
		//如果condition和situation重合则直接返回符合当前场合
		if(situation.equals(condition_name))
			result=true;
		else {//如果condition和situation不一样，则查询 event表 判断condition是否包含situation
			//获取event表对应condition_name的行
			SZSC.General_Info general_Info=SZSC_game_dictionary.EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "event", "condition_name", condition_name);
			int pointer=1;
			while(true) {//获取兼容1-x，比对，如果存在空则break因为之后肯定也没有，如果比对存在相同，则表示满足 发动场合
				String other_condition=general_Info.get_string("兼容"+pointer);
				if(other_condition.isEmpty())//如果找到最后都没找到相同的场景，说明不兼容
					break;
				if(situation.equals(other_condition)) {
					result=true;//发现满足条件的，直接破出循环
					break;
				}
				pointer++;
				
			}
			
		}
		return result;
	}
	
	
	
	

	//检查choice卡号是否是助攻卡，是则返回true，否则返回false
	public static boolean judge_type_weapon(int card_No) {
		
		String target_type=SZSC_game_dictionary.get_card_info(card_No).get_string("type");
		return judge_type(SZSC_game_protocol.TYPE_launch_weapon, target_type);
	}
	public static boolean judge_type_assist(int card_No) {
		String target_type=SZSC_game_dictionary.get_card_info(card_No).get_string("type");
		return judge_type(SZSC_game_protocol.TYPE_launch_assist, target_type);
	}
	public static boolean judge_type(String want_type,String target_type) {
		if(want_type.equals(target_type))
			return true;
		return false;
	}
	
	
	public static boolean whether_card_full(SZSC_player p1)//他手卡是否满了
	{
		if(p1.card.size()==p1.get_card_limit())
			return true;
		return false;
	}

	
	
	

	public static boolean whether_blood_full(SZSC_player p1)//判断是否满血
	{
		if(p1.blood<p1.bloodlimit)
			return false;
		
		if(p1.blood>p1.bloodlimit)
		{
			show(p1.get_room_name()+" 的血量大于上限？当前"+p1.blood+"  上限:"+p1.bloodlimit);
		}
		
		return true;
	}

	
	public static boolean whether_weapon_full(SZSC_player p1)//他装备栏是否满了
	{
		if(p1.get_weapon_limit()==p1.weapon.size())
			return true;
		return false;
	}

	public static boolean whether_his_turn_active(SZSC_game this_room,SZSC_player p1)//是否当前是他的主回合且当前没处于事件状态
	{
		if(!p1.whether_my_turn())
		{p1.game_tips("当前不是你的回合!");return false;}
		if(whether_in_fight(p1))
		{p1.game_tips("自己正处于战斗中!");return false;}
		return true;
	}
	public static boolean whether_deathly_attack(SZSC_player p1)//判断是否是致命的一次攻击
	{
		if(p1.soon_die())
			return true;
		return false;
	}

	public static boolean whether_got_attack_times(SZSC_player p1)//检查p1是否有普攻机会
	{
		if(p1.fight_chance>0)
			return true;
		return false;
	}
	

	public static boolean whether_in_fight(SZSC_player p1)//检查p1是否处于战斗状态
	{
		if(p1.in_attack_event())
			return true;
		return false;
	}
	

	public static boolean whether_got_card(SZSC_player p1)//检查p1是否有手卡
	{
		if(p1.card.size()==0)
			return false;
		
		return true;
		
	}
	
	public static boolean whether_moveable(SZSC_player p1) {
		if(whether_got_buff(p1,SZSC_game_protocol.Buff_unmovable))
			return false;
		return true;
	}
	public static boolean whether_penetrate(SZSC_player p1) {
		if(whether_got_buff(p1,SZSC_game_protocol.Buff_attack_penetrating))
			return true;
		return false;
	}
	
	private static boolean whether_got_buff(SZSC_player p1,String Buff_ID)//检查是否有该效果的buff
	{
		
		for(SZSC_Buff current_buff:p1.buff)
		{
			if(current_buff.get_effect_ID().equals(Buff_ID)) {
				
				return true;
			}
		}
		
		return false;
		
	}
	
	
	//effect_ID是否会满足激活角色已有的buff的发动条件
	//whether_active指玩家选择主动发动，只有标记为positive的buff才能被主动发动，其他都是被动
	//此函数为所有发动buff效果前置判断，判断完还需继续用其他函数判断buff的condition是否满足event_info对应场景
	public static boolean whether_activate_buff(SZSC_game this_room,SZSC_player p1,SZSC.Event_info event_info,SZSC_Buff buff,Boolean whether_active)
	{
		if(buff==null) {
			show("buff为空？？？");
			return false;
		}
		
		if(buff.got_chain_level_limit())//如果限制连锁层，则只在某些个瞬间才需要触发
			if(buff.got_chain_level()!=this_room.locktime)
				return false;
		
		if(whether_active)//如果是主动触发
		{
			if(!buff.is_positive())
			{
				p1.game_tips("该效果为被动效果，无法主动触发!");
				return false;
			}
		}
		else
			if(buff.is_positive())//如果是被动触发但这个是主动效果，不会触发
				return false;
		//判断是否有剩余次数
		if(buff.use_time_no_remain()) {
			if(whether_active)
				p1.game_tips("剩余可使用次数不足!");
			return false;
		}
		
		return true;
		

		//检查是否足够发动成本，或可以发动效果
		/*
		int effect=buff.get_effect_ID();
		switch(effect)
		{
			case SZSC_protocol.SZSC_i_defend_attack:
				if(!whether_can_escape_or_defend(p1))
				{
					if(whether_active)
						p1.game_tips("你无法躲避或格挡普攻!");
						
					return false;
				}
				break;
		}

		
		if(buff.check_condition_type(situation))
		{
			return true;
		}
		int condition_type=buff.get_condition_type();

		switch(situation){
			case SZSC_game_protocol.SZSC_Buff_unmovable:
				switch(condition_type){
					case SZSC_protocol.SZSC_i_will_get_unmovable_Buff:
						
						return true;
				}
				break;
			case SZSC_protocol.SZSC_i_will_discard:
			case SZSC_protocol.SZSC_i_discard_card:
				switch(condition_type){
					case SZSC_protocol.SZSC_i_will_discard:
						//game_broadcast("此次丢卡效果被免疫！");
						return true;
				}
				break;
			case SZSC_protocol.SZSC_Buff_i_public_card_i_get://公开此次抽到的手卡
				switch(condition_type){
					case SZSC_protocol.SZSC_i_will_public_card:
						//game_broadcast("此次公开手卡效果被免疫！");
						return true;
				}
				break;
			case SZSC_protocol.SZSC_i_will_public_card://公开自己有的手卡
				switch(condition_type){
					case SZSC_protocol.SZSC_i_will_public_card:
						//game_broadcast("此次公开手卡效果被免疫！");
						return true;
				}
				break;
			case SZSC_protocol.SZSC_i_get_effect_A:
				switch(condition_type){
					case SZSC_protocol.SZSC_i_will_be_effect_A:
						//game_broadcast("此次自主伤血效果被免疫！");
						return true;
				}
				break;
		}
		if(whether_active)//如果人为主动触发，提示不满足发动条件
			p1.game_tips("不满足发动条件!");
			*/
	}

	public static boolean whether_immune_this_effect(SZSC_game this_room,SZSC_player p1,SZSC.Event_info event_info)//是否免疫这个效果
	{
		for(SZSC_Buff current_buff:p1.buff) {
			if(whether_activate_buff(this_room,p1, event_info, current_buff, false)) {//如果该效果会激活该被动
				if(current_buff.check_effect_type(SZSC_game_protocol.Buff_immune))//如果该被动效果为免疫该效果，返回自己能免疫该效果
				{
					this_room.game_broadcast("此效果被免疫！");
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean judge_effect_positive(SZSC_player player_target,SZSC_player player_launcher) {
		if(player_launcher==null||player_target.check_player(player_launcher))
			return true;
		return false;
	}

	

	

	

	public static boolean whether_got_card(SZSC_player p1,int number)//检查p1是否有足够数量的手卡
	{
		if(p1.card.size()<number)
			return false;
		return true;
	}
	public static boolean whether_got_weapon_card(SZSC_player p1)//是否拥有武器类手卡
	{
		for(SZSC.card current_one:p1.card) {
			if(judge_type_weapon(current_one.get_card_No()))
				return true;
		}
		
		return false;
	}

	public static int library_get_Buff_ID(int card_ID,int which_effect)//根据卡片ID card_ID和该卡片第几个效果which_effect 获取对应Buff
	{
		//一般卡片都只有1个效果，所以which_effect为1，如果是武器，则可以有多个
		//如果找不到该卡或该卡的第which_effect个效果提供Buff_ID，提示报错
		return 0;
	}

	
	
	public static boolean card_choice_valid(SZSC_player p1,int which_one) {
		if(p1.card.size()<=which_one||which_one<0) {
			show("选择错误卡片范围   "+which_one);
			return false;
		}
		
		return true;
	}
	
	public static boolean weapon_effect_choice_valid(SZSC_player p1,int which_weapon,int which_effect) {
		if(!weapon_choice_valid(p1, which_weapon))
			return false;
		int weapon_ID=SZSC_game_weapon.get_weapon_ID(p1, which_weapon);
		int effect_ID=SZSC_game_weapon.get_weapon_effect_ID(weapon_ID, which_effect);
		//如果该武器号码的第x个效果为空
		if(effect_ID==SZSC_protocol.code_none)
			return false;
		//执行到这里，必然which_weapon正确、which_effect存在
		return true;
	}
	
	public static boolean weapon_choice_valid(SZSC_player p1,int which_one) {
		if(p1.weapon.size()<=which_one||which_one<0) {
			show("选择错误武器范围   "+which_one);
			return false;
		}
		
		return true;
	}
	public static boolean selfeffect_choice_valid(SZSC_player p1,int which_one) {
		if(p1.ability.size()<=which_one||which_one<0) {
			show("选择错误个人效果范围   "+which_one);
			return false;
		}
		
		return true;
	}
	public static boolean buff_choice_valid(SZSC_player p1,int which_one) {
		if(p1.buff.size()<=which_one||which_one<0) {
			show("选择错误个人buff范围   "+which_one);
			return false;
		}
		
		return true;
	}
	public static boolean player_choice_valid(SZSC_game this_room,SZSC_player p1,int which_one) {
		
		if(which_one<0||which_one>=this_room.players.size())
			return false;
		
		if(!this_room.players.get(which_one).not_none())
			return false;
		
		return true;
	}
public static boolean player_choice_valid_teammate(SZSC_game this_room,SZSC_player p1,int which_one) {
		
		if(which_one<0||which_one>=this_room.players.size())
			return false;

		SZSC_player player=this_room.players.get(which_one);
		
		if(!player.not_none())
			return false;
		if(player.get_camp()!=p1.get_camp())//如果不相同阵营
			return false;
		
		return true;
	}

	public static boolean player_choice_valid_enemy(SZSC_game this_room,SZSC_player p1,int which_one) {
		
		if(which_one<0||which_one>=this_room.players.size())
			return false;
		SZSC_player player=this_room.players.get(which_one);
		
		if(!player.not_none())
			return false;
		if(player.get_camp()==p1.get_camp())//如果相同阵营
			return false;
		
		return true;
	}
	
	public static boolean player_do_useless_choice(String result) {
		if(result.equals(SZSC_game_protocol.CHOICE_i_do_useless_choice))
			return true;
		return false;
	}
	//角色是否被沉默
	public static boolean character_is_silent(SZSC_player player) {
		return whether_got_buff(player, SZSC_game_protocol.Buff_myself_silent);
	}

	public static boolean card_No_is_buff(int card_No) {
		SZSC.General_Info general_Info=SZSC_game_dictionary.get_card_info(card_No);
		if(general_Info.get_string(SZSC_game_protocol.is_buff_column).equals(SZSC_game_protocol.is_buff))
			return true;
		return false;
	}
	public static boolean weapon_effect_is_buff(int weapon_ID,int which_effect) {
		
		int effect_ID=SZSC_game_weapon.get_weapon_effect_ID(weapon_ID, which_effect);
		
		return card_No_is_buff(effect_ID);
	}

	/*
	public static boolean check_weapon_Buff_times(SZSC_player p1,int which_weapon,int which_effect)//检查第几个的武器某效果是否有剩余使用次数,which_weapon第几个武器，which_effect第几个效果，如果有，返回true，否则false
	{

		int Buff_ID=library_get_Buff_ID(p1.weapon.get(which_weapon).get_weapon_ID(),which_effect);//根据武器id和第几项效果获取对应Buff_ID
		
		SZSC_Buff this_buff=SZSC_game_Buff_process.got_this_Buff_place(p1,Buff_ID,SZSC_game_protocol.Source_my_weapon_effect,which_weapon);
		if(this_buff==null)
			return false;
		
		if(this_buff.get_use_time_reamin()>=this_buff.get_consume_per_time())
			return true;
		return false;
	}*/
	/*
	bool judge_weapon_effect(SZSC_player* p1,SZSC_Buff buff,int situation)//判断是否可以发动该武器效果,主动使用才会用到
	{
		if(Buff_ID==SZSC_Buff_none)
		{
			cout<<"错误buff\n";
			return false;
		}

		if(!Buff_positive)
		{
			game_tips(p1,"此为被动效果,无法主动发动!");
			return false;
		}

		//判断是否满足发动条件，一般被动触发都只用这个而不用外面套个judge_weapon_effect
		if(!whether_activate_buff(p1,situation,buff,true))
			return false;

		//检查是否有足够发动成本

		//判断是否有剩余次数
		if(!check_Buff_times(buff))
		{
			game_tips(p1,"发动次数已用完!");
			return false;//检查是否有剩余使用次数，如果有，返回true，否则false
		}

		return true;
	}

	bool judge_weapon_effect(SZSC_player* p1,int card_No,int situation,int whichweapon)
	{
		switch(card_No)
		{
		case 1002://1回合限1次，自己可丢1手卡，抽1卡（承皇剑1）
				if(whether_in_fight(p1)){game_tips(p1,"目前处于战斗中!\n不能发动!");return false;}

				if(check_weapon_Buff_times(p1,whichweapon,1))//如果该回合效果还有剩余使用次数
				{
					if(!whether_got_card(p1))//如果没有手卡
					{game_tips(p1,"手卡数量过少!");return false;}
					
					return true;	
			   }
			   else
					{game_tips(p1,"次数已用完!");return false;}
		case 1003://自己可挡4次普攻，抵挡第4次普攻后此卡被破坏（承皇剑2）
				if(situation!=SZSC_i_will_be_attacked){game_tips(p1,"自己没受到普攻!");return false;}
				if(!whether_can_escape_or_defend(p1)){game_tips(p1,"自己无法躲避和格挡!");return false;}
				//理论上发动完次数后就会被破坏了，检查发动次数，在发动效果里实现，这里就不检查了
				return true;
	 
		case 1004://【自己回合可发动】自己主动将此卡破坏，指定1人伤2血（承皇剑3）
			if(whether_his_turn_active(p1))return true;
			return false;
				
		case 1012:  //"1回合限2次，自己可丢1手卡，自己此回合攻+1";疾风刃1
				if(check_weapon_Buff_times(p1,whichweapon,1))
				//if(p1.exattack_weapon_turn[whichweapon]>=2)//如果说该位置的疾风刃叠满该效果，发动失败
					{game_tips(p1,"次数已用完!");return false;}
				return true;
					
		case 1013:  //"【自己回合可发动】自己主动将此卡破坏，指定1人伤1血";疾风刃2
				if(whether_his_turn_active(p1))return true;
				return false;
			
		case 1022:  ;//"自己每使1人伤1血，自己+1血";长者之镰1
				{game_tips(p1,"该效果为被动效果!");return false;}

		case 1023:  //【自己回合可发动】自己主动将此卡破坏，指定1人+3血";长者之镰2
				if(whether_his_turn_active(p1))return true;
				return false;
		case 1032:  //"自己每次与人搏斗过后限1次，随机破坏 与自己搏斗者 装备的1张装备卡";//噬剑之剑1
				{game_tips(p1,"该效果为被动效果!");return false;}

		case 1033:  ;//"【自己回合可发动】自己主动将此卡破坏，指定1人伤2血";//噬剑之剑2
				if(whether_his_turn_active(p1))return true;
				return false;

		case 1043:  //"此次游戏限1次可发动，发动回合过后，自己持续不死2轮回";//黑曜剑1
				return true;//该效果可主动触发，当自己血量归0时自动触发

		case 1044: ;//"【自己回合可发动】自己主动将此卡破坏，指定1人50%伤3血";//黑曜剑2
				if(whether_his_turn_active(p1))return true;
				return false;
		case 1051: //"自己每抽1卡，自己血+1";//食腐再生装置1
				{game_tips(p1,"该效果为被动效果!");return false;}

		case 1052: //1回合限1次，自己可丢1手卡，血+1;//食腐再生装置2
				if(!whether_his_turn_active(p1))
					return false;
				if(!check_weapon_Buff_times(p1,whichweapon,2))//如果该回合效果还有剩余使用次数
				{game_tips(p1,"次数已用完!");return false;}
				//if(c1.p1.weaponeffect051[whichweapon]<=0){confirm_send_success(c1,"该回合的此武器效果次数已用完!\n");return false;}	
				return true;

		case 1053: //"自己回合限1次，自己可额外抽1卡";//食腐再生装置3
				if(!whether_his_turn_active(p1))
					return false;
				if(!check_weapon_Buff_times(p1,whichweapon,3))//如果该回合效果还有剩余使用次数
				{game_tips(p1,"次数已用完!");return false;}
				//if(c1.p1.weaponeffect052[whichweapon]<=0){confirm_send_success(c1,"次数已用完!发动失败!\n");return false;}
				return true;

		case 1061: ;//"此卡被装备上时给此卡放置4魔力指示物";//恩空法棒1
				{game_tips(p1,"该效果为被动效果!");return false;}

		case 1062: ;//"己方每发动1手卡，给此卡放置1魔力指示物";//恩空法棒2
				{game_tips(p1,"该效果为被动效果!");return false;}

		case 1063: ;//"【自己回合可发动】自己消耗2魔力指示物使1人伤1血";//恩空法棒3
				if(!whether_his_turn_active(p1))
					return false;
				if(!check_weapon_Buff_times(p1,whichweapon,3))//如果该回合效果还有剩余使用次数
				{game_tips(p1,"次数已用完!");return false;}
				//if(c1.p1.weaponeffect06[whichweapon]<2){confirm_send_success(c1,"魔力指示物过少!发动失败!\n");return false;}
				return true;

		case 1071: //"自己不受任何丢卡和手卡公开的效果影响";//骷髅诡面1
			{game_tips(p1,"该效果为被动效果!");return false;}
		case 1072: //"【场上除己外1人死亡时】自己血量回满";//骷髅诡面2
			{game_tips(p1,"该效果为被动效果!");return false;}
		case 1073: //"自己不受任何自主伤血效果影响";//骷髅诡面3
			{game_tips(p1,"该效果为被动效果!");return false;}
		case 1081: //"自己可装备无限数量装备卡"; //破败披风1
			{game_tips(p1,"该效果为被动效果!");return false;}
		case 1082: //"【自己受到普攻时可发动】自己主动将此卡破坏，自己抵挡此次普攻";//破败披风2
			if(!whether_can_escape_or_defend(p1)){game_tips(p1,"自己无法躲避和格挡!");return false;}
			return true;

		case 1092: //"【自己每普攻1次】所有人丢3手卡，抽3卡，卡堆最上方3张卡送入废卡区";//冠阳剜月斧1
			{game_tips(p1,"该效果为被动效果!");return false;}

		case 1093: ;//"【自己回合可发动】自己主动将此卡破坏，指定1人伤2血,场上所有人丢5手卡，抽5卡";//冠阳剜月斧2
				if(whether_his_turn_active(p1))return true;
				return false;
		default:
			cout<<"错误武器号!card_No="<<card_No;
			return false;

		}
			cout<<"判定发动条件错误!"<<card_No<<endl;
		return false;
	}
	*/
	
	
	
	
	/*
	public static boolean judge_card(SZSC_game this_room,SZSC_player p1,int choice,String situation)//situation发动条件场景，如果符合发动条件直接使用
	{
		if(judge_type_weapon(choice))//如果是武器
		{
			if(!p1.whether_my_turn())//检查是否为该玩家回合
			{
				p1.game_tips("现在不是你的回合!无法装备武器!");
				return false;
			}
			if(whether_weapon_full(p1))//检查武器数量，若装备武器已达上限则直接返回
			{
				p1.game_tips("装备武器已达上限!无法继续装备!");
				return false;
			}
			if(whether_in_fight(p1))
			{
				p1.game_tips("当前你处于战斗中!无法装备!");
				return false;
			}
			return true;
		}

		if(choice>=30&&choice<=59)//如果是助攻卡
		{
			if(situation.equals(SZSC_game_protocol.i_will_attack))//如果条件为“我要进攻了”，可以发动助攻卡
			{
				if(choice==33&&p1.get_weapon_number()<1)//霸地·挥刃【自己装备武器时才可发动】
				{p1.game_tips("你没装备武器!");return false;}
				return true;//其他助攻卡应该都能发动
			}
			else
				p1.game_tips("该卡为助攻卡\n在自己发动普攻宣言时才可发动!");
			return false;
			
		}

		
		switch(choice)
		{
	//--------------------------------------------------------隐效果----------------------------------------
		case 400://连攻，【自己普攻成功时或自己回合自己未处于搏斗状态可发动】自己发动一次普攻
			if(whether_his_turn_active(this_room,p1))return true;
			switch(situation)
			{
				case SZSC_protocol.SZSC_i_attack_success:
				case SZSC_protocol.SZSC_i_attack_fail_E:
				case SZSC_protocol.SZSC_i_attack_fail_D:
				case SZSC_protocol.SZSC_i_attack_fail:
				case SZSC_protocol.SZSC_i_damage_fail:
					return true;
			}
			p1.game_tips("自己普攻成功时\n或\n自己回合自己未处于搏斗状态时\n此卡才可发动!");
			return false;
		case 401://闪
			if(situation==SZSC_protocol.SZSC_i_will_be_attacked)
				return true;
			p1.game_tips("自己即将被普攻时可发动!");
			return false;
		case 402://+4血
			return true;
		case 403://暗枪
			if(whether_in_fight(p1))
				return true;
			p1.game_tips("自己处于搏斗中才可发动!");
			return false;
		case 404://稻草人
			
			if(situation==SZSC_protocol.SZSC_i_will_be_attacked)
				return true;
			p1.game_tips("自己即将受到普攻时才可使用!");
			return false;
		case 405://憎恨
			p1.game_tips("暂未实现!");
			return false;
		case 406://极天束
			if(situation==SZSC_protocol.SZSC_i_attack_success)
				return true;
			p1.game_tips("自己普攻成功时才可使用!");
			return false;
		case 407://蓝爆冰晶
			if(whether_his_turn_active(this_room,p1))
				return true;
			else
				return false;
			
	//--------------------------------------------------------隐效果----------------------------------------
	//------------------------------------------------------一般效果卡--------------------------------------
		case 60://神起·焰射\n【自己普攻被躲避时可发动】
			switch(situation)
			{
				case SZSC_protocol.SZSC_i_attack_fail_E:
				case SZSC_protocol.SZSC_i_attack_fail_D:
				case SZSC_protocol.SZSC_i_attack_fail:
					return true;
			}
			p1.game_tips("自己普攻失败时才可发动!");
			return false;
		case 61://近击·搏杀\n【自己参与的1次事件内双方发动过大于3次手卡时 可发动】
			if(whether_in_fight(p1))
			{
				if(this_room.locktime>=4)
					return true;
				else
				{
					p1.game_tips("连锁次数不够!\n当前连锁次数: %d"+this_room.locktime);
					return false;
				}
			}
			p1.game_tips("当前你没处于连锁中!");
			return false;
		case 62://"[效果卡]阴势·突进\n【自己回合时可发动】自己指定1人-1血且此回合不能行动（被攻者可丢1手卡躲避此次伤血），接下来的此回合内自己只能对被指定者或自己发动效果卡、搏斗\n\n";
			if(whether_his_turn_active(this_room,p1))
				return true;
			return false;
		case 63:// "[效果卡]神空·闪降\n【自己回合时可发动】自己抽1卡，将抽到的该卡公开，自己这回合的规则性普攻次数被重置为1\n\n";
			if(whether_his_turn_active(this_room,p1))
				return true;
			return false;
		case 64:// "[效果卡]空遁·闪回\n【自己受到普攻时可发动】自己抽1卡且躲避此次搏斗，自己此回合可对 发动该次普攻者 普攻1次\n\n";
			
			if(situation==SZSC_protocol.SZSC_i_will_be_attacked)
				return true;
			p1.game_tips("自己即将被普攻时才可发动!");
			return false;

		case 65:// "[效果卡]光斩·绝杀\n【自己受到致命伤血时可发动】此次伤血无效化且此回合自己基础攻击力×2，自己可进行1次普攻\n\n";
			if(p1.soon_die())return true;//如果接到这招我血量就要归零了
			p1.game_tips("自己即将受到致命伤血时才可发动!");
			return false;

		case 66:// "[效果卡]天华绝伦\n【自己普攻成功且该回合内自己成功普攻超过2次时可发动】自己指定1人，自己此回合可 超速普攻 其4次，每次超速普攻对其造成的普攻伤血强制变为1且不会造成反伤\n\n";
			if(situation!=SZSC_protocol.SZSC_i_attack_success){p1.game_tips("自己普攻成功时才可发动!");return false;}
			int attack_success_time=p1.get_attack_success_time();
			if(attack_success_time<3)
			{
				p1.game_tips("当前回合自己普攻成功次数为: "+attack_success_time+"\n没达到3次!");
				return false;
			}
			return true;

		case 67:// "[效果卡]神尚激光\n【己方回合时可发动】自己丢光手卡，此回合之后的3轮回内自己若受到致命伤血，使自己受到致命伤血者 将受到之前自己丢光手卡数值×2的伤血\n\n";
			if(!whether_got_card(p1))
			{p1.game_tips("你当前没有手卡!");return false;}
			if(whether_his_turn_active(this_room,p1))
				return true;
			return false;
			

		case 68:// "[效果卡]轰炸星球\n【自己回合时可发动】破坏场上所有场景卡和结界卡，每破坏1张，使 放置该结界或场景者 伤3血\n\n";
			if(whether_his_turn_active(this_room,p1))
				return true;
			return false;
		case 69:// "[效果卡]最后的指望\n【自己处于濒死状态时可发动】自己指定1人，将自己全部手卡、自己装备的武器全部加入他手卡\n\n";
			p1.game_tips("仅限多人对战时使用!");
			return false;
				
		case 70:// "[效果卡]双臂剑\n自己血-3，将卡堆最上方的2张武器卡装备上\n\n";
			if(whether_his_turn_active(this_room,p1))
				return true;
			return false;
		
		case 71:// "[效果卡]离心续\n【自己普攻成功时可发动】自己再进行1次普攻，此次普攻造成的普攻伤血翻倍\n\n";
			if(situation==SZSC_protocol.SZSC_i_attack_success)
				return true;
			p1.game_tips("自己普攻成功时才可发动!");
			return false;

		case 72:// "[效果卡]虚混·太清\n【自己受到普攻时可发动】自己躲避本次普攻，自己丢光手卡，发动该次普攻者 的血量减半\n\n";
			
			if(situation==SZSC_protocol.SZSC_i_will_be_attacked)
				return true;
			p1.game_tips("自己即将被普攻时才可发动!");
			return false;

		case 73:// "[效果卡]幻影剑\n将此卡作为 攻+2 的武器给自己装备上，在装备上此卡的回合结束时，此卡自动被破坏\n\n";
			if(whether_weapon_full(p1)){p1.game_tips("装备栏已满!");return false;}
			return true;	
		case 74:// "[效果卡]冷现·巨剑\n【自己受到效果伤血时可发动】自己丢1手卡中的装备卡，指定1人伤3血\n\n";
			switch(situation)
			{
				case SZSC_protocol.SZSC_i_be_effect_B:
				case SZSC_protocol.SZSC_i_be_effect_A:
					if(!whether_got_weapon_card(p1))
					{
						p1.game_tips("你没有武器类手卡!");
						return false;
					}
				return true;
			}
			p1.game_tips("自己受到效果伤血时才可发动!");
			return false;
		case 75:// "[效果卡]隐天盾\n此卡发动的回合以及发动后的2轮回内，自己可丢1手卡使 1次对己的效果伤血 无效化或抵挡1次普攻\n\n";
			if(situation==SZSC_protocol.SZSC_i_will_be_attacked)
				
			return true;//可以瞬防
		
		case 76:// "[效果卡]破局·神击\n【自己普攻失败时可发动】自己普攻 使自己普攻失败者 1次，且此次普攻内 被攻者 无法行动。自己可丢1次不超过4张数量的手卡，使 被攻者 受到丢卡数量一半的伤血\n\n";
			switch(situation)
			{
				case SZSC_protocol.SZSC_i_attack_fail_E:
				case SZSC_protocol.SZSC_i_attack_fail_D:
				case SZSC_protocol.SZSC_i_attack_fail:
					return true;
			}
			p1.game_tips("自己普攻失败时才可发动!");
			return false;
			
		case 77:;// 旋地·回击\n【自己受到普攻时可发动】自己抽1卡，自己可普攻 发动普攻者 1次，本次攻击自己攻+1";
			if(situation==SZSC_protocol.SZSC_i_be_attacked)
				return true;
			p1.game_tips("自己被普攻时才可发动!");
			return false;
		}
		show("出现莫名其妙卡号故障!!卡号为:"+choice);
		return false;
	}*/


	/*
	public static boolean judge_character_effect(SZSC_player p1,int effect_ID,int situation)//判断人物效果是否可主动发动
	{
		switch(effect_ID)
		{
			case 10000:
				return true;
			case 10001:p1.game_tips("该效果为被动效果!无法主动发动!");return false;
				
			case 10002:p1.game_tips("该效果为被动效果!无法主动发动!");return false;
			default:
				show("出现了莫名其妙的人物效果编号!编号为:"+effect_ID);
				
		}
		
		return false;
	}*/

	
	
	

	
	public static boolean judge_one_die(SZSC_game this_room,SZSC_player p1,SZSC_player p2)//判断他死没有
	{
		SZSC_player player=p1;
		if(player.blood<=0)
		{
			player.blood=0;
			if(!SZSC_game_judge.whether_got_buff(player,SZSC_game_protocol.Buff_immortal))//双重判定，过不了第二次判定则是死
			{
				SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_die, 0, player, null);
				SZSC_game_Buff_process.activate_Buff(this_room,player,null,event_info);
				//自动触发不死效果

				//if(p1.blood==0&&p1.overdeath==0)
				if(!SZSC_game_judge.whether_got_buff(player,SZSC_game_protocol.Buff_immortal))//如果还没激发不死状态则死亡
				{
					player.set_die();return true;
				}
			}
		}
		player=p2;
		if(player.blood<=0)
		{
			player.blood=0;
			if(!SZSC_game_judge.whether_got_buff(player,SZSC_game_protocol.Buff_immortal))//双重判定，过不了第二次判定则是死
			{
				SZSC.Event_info event_info=new SZSC.Event_info(SZSC_game_protocol.i_will_die, 0, player, null);
				SZSC_game_Buff_process.activate_Buff(this_room,player,null,event_info);
				//自动触发不死效果

				//if(p1.blood==0&&p1.overdeath==0)
				if(!SZSC_game_judge.whether_got_buff(player,SZSC_game_protocol.Buff_immortal))//如果还没激发不死状态则死亡
				{
					player.set_die();return true;
				}
			}
		}
		return false;//还没死
	}
	
	
	//随机事件是否命中，如果输入参数为100则100%命中
	public static boolean random_happen(int probability) {
		int result=core_main.getrandom(0, 100);
		if(probability>=result)
			return true;
		return false;
	}
	public static boolean event_force_end(String signal) {
		boolean result=false;
		switch (signal) {
			case SZSC_game_protocol.force_end_event: 
			case SZSC_game_protocol.force_end_turn:
			case SZSC_game_protocol.force_offline:
			case SZSC_game_protocol.game_end:
				result=true;
			
		}
		return result;
	}
}
