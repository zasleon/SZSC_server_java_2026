package test;

import java.util.List;

import com.healthmarketscience.jackcess.util.CaseInsensitiveColumnMatcher;

public class SZSC_game_effect {
	
	//根据卡片id，前去general表获取卡片具体信息
	//type（助攻卡、效果卡、武器卡、场景卡等等）condition、cost、effect
	//（满足发动条件在其他地方判断，不在本函数内判断）
	//获取cost_name,cost_value,effect_name,effect_value
	//查询effect表name对应效果信息
	//如果是元效果，则执行
	//如果是复合效果，则读取其所有内含效果并执行
	//如果是buff或其他则报错
	public static String launch_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,SZSC.Launch_Info launch_Info) {
		
		String result=SZSC_game_protocol.still_fight;
		int card_No=launch_Info.get_card_No();
		
		//获取该卡号具体信息
		SZSC.General_Info general_Info=SZSC_game_dictionary.get_card_info(card_No);
		
		//提取cost信息
		String cost_name=general_Info.get_string("cost_name");
		float cost_value=general_Info.get_float("cost_value");
		
		result=execute_effect(this_room,event_info, player_launcher, player_target, cost_name, cost_value,launch_Info);
		
		if(SZSC_game_judge.event_force_end(result))
			return result;
		
		//提取effect信息
		String effect_name=general_Info.get_string("effect_name");
		float effect_value=general_Info.get_float("effect_value");
		result=execute_effect(this_room,event_info, player_launcher, player_target, effect_name, effect_value,launch_Info);
		
		return result;
		
	}
	//执行卡片效果
	//获取effect表对应effect_name信息
	//提取type类型，如果是元效果，直接执行元效果，如果是复合效果，则读取其效果list，执行list中一个个效果
	public static String execute_effect(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,String effect_name,float effect_value,SZSC.Launch_Info launch_Info) {
		String result=SZSC_game_protocol.still_fight;
		
		if(effect_name.isBlank())//如果效果名为空说明该效果栏为空
			return result;
		show("执行 "+effect_name+"  "+effect_value);
		
		SZSC.General_Info effect_Info=SZSC_game_dictionary.EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH,"effect","name",effect_name);
		String type=effect_Info.get_string("type");
		
		switch (type) {
			case "元效果": //进行执行
				result=effect_Resolution(this_room,event_info,player_launcher,player_target,effect_name,effect_value,launch_Info);
				break;
			case "卡片效果"://获取复合效果
			{
				int pointer=1;
				while(true) {
					//如果没了
					if(!effect_Info.column_exist("效果"+pointer))
						break;
					String pointer_effect_name=effect_Info.get_string("效果"+pointer);
					//如果效果名为空说明没了
					if(pointer_effect_name.length()<=0)
						break;
					
					float pointer_effect_value=effect_Info.get_float("value"+pointer);
					show("执行效果"+pointer+"    "+pointer_effect_name+"  "+pointer_effect_value);
					result=execute_effect(this_room,event_info, player_launcher, player_target, pointer_effect_name, pointer_effect_value,launch_Info);
					//如果事件被强制结束，则不继续执行后续效果
					if(SZSC_game_judge.event_force_end(result))
						break;
					pointer++;
				}
			}
				break;
			case "buff":
				show("本不该执行的buff常态量  "+effect_name);
				break;
			
		}
		
		return result;
	}
	
	public static String effect_Resolution(SZSC_game this_room,SZSC.Event_info event_info,SZSC_player player_launcher,SZSC_player player_target,String effect_name,float effect_value,SZSC.Launch_Info launch_Info) {
		
		String result=SZSC_game_protocol.still_fight;
		
		String off_line_result=SZSC_game_protocol.force_end_event;
		if(this_room==null)
			return result;
		switch(effect_name) {
			case SZSC_game_protocol.Effect_get_buff_one_enemy://给敌人附上一个buff
				//根据effect_value给敌人加上general表内No为effect_value的buff信息
				
				{
					SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data((int)effect_value);
					SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_target,player_launcher);
				}
				break;
			case SZSC_game_protocol.Effect_get_buff_one_enemy_50percent:
				if(SZSC_game_judge.random_happen(50)){
					SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data((int)effect_value);
					SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_target,player_launcher);
				}
				break;
			case SZSC_game_protocol.Effect_get_buff_myself:
				{
					SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data((int)effect_value);
					SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_launcher,player_launcher);
				}
				break;
			case SZSC_game_protocol.Effect_refresh_card_all:
				{
					result=SZSC_game_deck.all_player_refresh_card(this_room,(int)effect_value, player_launcher);
				}
				break;
			case SZSC_game_protocol.Effect_i_point_one_enemy_get_effect_dmg_50percent:
				{
					int which_player=SZSC_game_player_choose.choose_enemy(this_room, player_launcher, "请选择一人伤 "+effect_value+" 血", true);
					if(player_launcher.offline()) {return off_line_result;}//玩家做出选择，如果此时断开通讯
					SZSC_player new_player_target=this_room.players.get(which_player);
					
					
					result=SZSC_game_attack.normal_effect_dmg(this_room,effect_value,50,new_player_target, player_launcher);
				}
				break;
			case SZSC_game_protocol.Effect_i_point_one_enemy_get_effect_dmg:
				{
					int which_player=SZSC_game_player_choose.choose_enemy(this_room, player_launcher, "请选择一人伤 "+effect_value+" 血", true);
					if(player_launcher.offline()) {return off_line_result;}//玩家做出选择，如果此时断开通讯
					SZSC_player new_player_target=this_room.players.get(which_player);
					
					result=SZSC_game_attack.normal_effect_dmg(this_room,effect_value,100,new_player_target, player_launcher);
				}
			
				break;
			case SZSC_game_protocol.Effect_i_let_enemy_effect_dmg:{
				result=SZSC_game_attack.normal_effect_dmg(this_room,effect_value,100,player_target, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_point_one_get_heal:{
				int which_player=SZSC_game_player_choose.choose_someone(this_room, player_launcher, "请选择一人回复 "+effect_value+" 血", true);
				if(player_launcher.offline()) {return off_line_result;}//玩家做出选择，如果此时断开通讯
				SZSC_player new_player_target=this_room.players.get(which_player);
				result=SZSC_game_attack.recover_blood(this_room, effect_value, new_player_target,player_launcher);
			}
				break;
			case SZSC_game_protocol.Effect_i_recover_to_limit:{
				float recover_value=player_launcher.bloodlimit-player_launcher.blood;
				result=SZSC_game_attack.recover_blood(this_room, recover_value, player_launcher,player_launcher);
			}
				break;
			
			case SZSC_game_protocol.Effect_i_recover:
				result=SZSC_game_attack.recover_blood(this_room, effect_value, player_launcher,player_launcher);
				break;
			case SZSC_game_protocol.Effect_SP_i_defend_attack_throne_succeed_sword:{
				//获取该武器信息
				int which_weapon=launch_Info.get_which_weapon();
				SZSC_Buff weaponBuff=SZSC_game_weapon.get_player_weapon_buff(player_launcher, which_weapon, 2);
				weaponBuff.reduce_times(1);//次数减少
				if(weaponBuff.use_time_no_remain()) {//如果该武器发动该效果次数用完则破坏该武器
					this_room.game_broadcast("次数用完，此武器被破坏!");
					SZSC_game_weapon.delete_weapon(this_room, player_launcher, which_weapon);
				}
				result=SZSC_game_protocol.i_failed_attack_be_defend;
				break;
				
			}
			case SZSC_game_protocol.Effect_i_defend_attack:
				result=SZSC_game_protocol.i_failed_attack_be_defend;
				break;
			case SZSC_game_protocol.Effect_i_claim_attack:
			{
				int which_player=SZSC_game_player_choose.choose_enemy(this_room, player_launcher, "请选择一人普攻", true);
				if(player_launcher.offline()) {return off_line_result;}//玩家做出选择，如果此时断开通讯
				SZSC_player new_player_target=this_room.players.get(which_player);
				
				result=SZSC_game_attack.general_attack(this_room,player_launcher,new_player_target);
			}
				
				break;
			case SZSC_game_protocol.Effect_i_attack_enemy:
			{
				result=SZSC_game_attack.general_attack(this_room,player_launcher,player_target);
			}
				break;
			case SZSC_game_protocol.Effect_i_attack_super:{
				result=SZSC_game_attack.lightspeed_attack(this_room, player_launcher, player_target, effect_value);
			}
				break;
			case SZSC_game_protocol.Effect_SP_burst_fight:{
				result=SZSC_game_attack.lightspeed_attack(this_room, player_launcher, player_target, effect_value);
				result=SZSC_game_protocol.force_end_turn;
			}
			case SZSC_game_protocol.Effect_i_get_card:{
				SZSC_game_deck.get_card(this_room, (int)effect_value, player_launcher, player_launcher);
			}
				break;
			case SZSC_game_protocol.Effect_SP_lightning_blaze:{
				//询问是否进行普攻
				//如果不进行普攻，则返回结果“自己无效了伤害”
				if(SZSC_game_player_choose.ask_whether_do(player_launcher, "是否进行普攻?"))
					result=SZSC_game_attack.general_attack(this_room,player_launcher,player_target);
				else
					result=SZSC_game_protocol.i_failed_attack;
				break;
			}
			case SZSC_game_protocol.Effect_i_escape_attack_and_can_fight_back:{
				//询问是否进行普攻
				//如果不进行普攻，则返回结果“自己闪避了攻击”
				if(SZSC_game_player_choose.ask_whether_do(player_launcher, "是否进行普攻?"))
					result=SZSC_game_attack.general_attack(this_room,player_launcher,player_target);
				else
					result=SZSC_game_protocol.i_failed_attack_be_escape;
				break;
			}
			case SZSC_game_protocol.Effect_i_can_attack_back_with_enforce:{
				if(SZSC_game_player_choose.ask_whether_do(player_launcher, "是否进行普攻?"))
				{
					//添加buff
					SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data((int)effect_value);
					SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_launcher,player_launcher);
					//进行普攻
					result=SZSC_game_attack.general_attack(this_room,player_launcher,player_target);
				}
			}
				break;
			case SZSC_game_protocol.Effect_i_let_enemy_get_effect_dmg_escape_available:
			{
				SZSC.Result_info result_info=SZSC_game_deck.choose_discard(this_room, 1, false, player_target, player_launcher);
				player_target.game_tips("可丢一卡躲避此次效果伤血");
				if(result_info.get_value()<1) {
					SZSC.Event_info new_event_info=SZSC.get_new_event_info(SZSC_game_protocol.TYPE_event_effect_dmg, effect_value, player_target, player_launcher);
					result=SZSC_game_attack.hit_damage(this_room, player_target, player_launcher, new_event_info);
				}
				
			}
				break;
			case SZSC_game_protocol.Effect_i_point_enemy_get_effect_dmg_escape_available:
			{
				int which_player=SZSC_game_player_choose.choose_enemy(this_room, player_launcher, "请选择一人伤"+effect_value+"血", true);
				if(player_launcher.offline()) {return off_line_result;}//玩家做出选择，如果此时断开通讯
				SZSC_player new_player_target=this_room.players.get(which_player);
				
				SZSC.Result_info result_info=SZSC_game_deck.choose_discard(this_room, 1, false, new_player_target, player_launcher);
				new_player_target.game_tips("可丢一卡躲避此次效果伤血");
				if(result_info.get_value()<1) {//如果没丢
					SZSC.Event_info new_event_info=SZSC.get_new_event_info(SZSC_game_protocol.TYPE_event_effect_dmg, effect_value, new_player_target, player_launcher);
					result=SZSC_game_attack.hit_damage(this_room, new_player_target, player_launcher, new_event_info);
				}
			}
				break;
			case SZSC_game_protocol.Effect_i_immune_effect_dmg:
				result=SZSC_game_protocol.i_failed_do_dmg_effect;
				break;
			case SZSC_game_protocol.Effect_equip_weapon:
			{
				result=SZSC_game_weapon.equip_weapon(this_room, player_launcher, (int)effect_value);
			}
				break;
			case SZSC_game_protocol.Effect_SP_ice_burst:{
				if(launch_Info.is_buff()){
					SZSC_Buff this_buff=launch_Info.get_buff();
					int token_value=this_buff.get_token_value()+1;
					if(token_value>=3)
						SZSC_game_weapon.delete_weapon_all(this_room, player_launcher);
					else
						this_buff.set_token_value(token_value);
				}
				else
					show("冰爆触发错误？不是buff？？？");
				break;
			}
			case SZSC_game_protocol.Effect_deck_top_card_to_graveyard:{
				int time=(int)effect_value;
				while(time>0) {
					this_room.deck.remove(0);
					SZSC_game_deck.add_one_random_new_card_to_deck(this_room);
					time--;
				}
				break;
			}
			case SZSC_game_protocol.Effect_destroy_enemy_weapon_random:{
				int weapon_number=player_target.get_weapon_number();//获取敌人武器数量
				int destroy_required_number=(int)effect_value;
				if(weapon_number<=destroy_required_number) {
					this_room.game_broadcast(player_target.get_room_name()+" 的武器被全部破坏!");
					SZSC_game_weapon.delete_weapon_all(this_room, player_target);
					break;
				}
				//执行到这里，必然玩家武器持有数量大于效果数量
				while(destroy_required_number>0) {
					int which_weapon=SZSC_game_general_function.getrandom(0, weapon_number-1);
					int weapon_ID=player_target.weapon.get(which_weapon).get_weapon_ID();
					this_room.game_broadcast(player_target.get_room_name()+" 的武器 "+SZSC_game_dictionary.search_ID_name(weapon_ID)+" !");
					SZSC_game_weapon.delete_weapon(this_room, player_target, which_weapon);
					weapon_number--;
					destroy_required_number--;
				}
				break;				
			}			
			
			case SZSC_game_protocol.Effect_i_let_enemy_blood_halve:{
				float blood_result=player_target.get_blood();
				player_target.set_blood(blood_result);
				break;
			}
			case SZSC_game_protocol.Effect_i_escape_attack:{
				result=SZSC_game_protocol.i_failed_attack_be_escape;
				break;
			}
			case SZSC_game_protocol.Effect_SP_break_out_godattack:{
				//发动者丢任意数量手卡
				int discard_card_number=SZSC_game_deck.player_discard_card_free(this_room, 0, false, player_launcher);
				result=SZSC_game_attack.normal_effect_dmg(this_room, discard_card_number, 100, player_target, player_launcher);
				if(SZSC_game_judge.event_force_end(result))
					break;
				result=SZSC_game_attack.general_attack(this_room, player_launcher, player_target);
				break;
			}
			
			case SZSC_game_protocol.Effect_equip_weapon_from_deck:{
				int pointer=0;
				int equip_remain_number=(int)effect_value;
				while(pointer<this_room.deck.size())
				{
					if(equip_remain_number==0)
						break;
					if(SZSC_game_judge.whether_weapon_full(player_launcher))
						break;
					int card_No=this_room.deck.get(pointer).get_card_No();
					if(SZSC_game_judge.judge_type_weapon(card_No)){//如果是武器，进行装备
						SZSC_game_weapon.equip_weapon(this_room, player_launcher, card_No);
						equip_remain_number--;
					}
					
					this_room.deck.remove(pointer);
					SZSC_game_deck.add_one_random_new_card_to_deck(this_room);
					
				}
				//执行到这里必然卡组查完
				if(equip_remain_number>0)//如果卡组查完还没装备完，则随机生成武器进行装备
					while(equip_remain_number>0) {
						if(SZSC_game_judge.whether_weapon_full(player_launcher))
							break;
						int card_No=SZSC_game_general_function.getrandom(0, 9);
						SZSC_game_weapon.equip_weapon(this_room, player_launcher, card_No);
						equip_remain_number--;
					}
				
				//执行到这里必然装备完
				SZSC_game_deck.deck_shuffle(this_room);
				break;
			}
			case SZSC_game_protocol.Effect_token_this_card_add:{
				SZSC_Buff buff=launch_Info.get_buff();
				if(launch_Info.is_buff()&&buff.is_token()) {
					int token_value=buff.get_token_value()+(int)effect_value;
					buff.set_token_value(token_value);
				}
				else {
					show("Effect_token_this_card_add 出错!不是token?");
				}
				break;
			}
			
			case SZSC_game_protocol.Effect_i_let_enemy_refresh_card_randomly:{
				result=SZSC_game_deck.player_refresh_card(this_room, (int)effect_value, player_target, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_let_enemy_random_discard_card:{
				SZSC_game_deck.random_discard(this_room, (int)effect_value, player_target, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_destroy_enemy_all_weapon:{
				if(SZSC_game_judge.random_happen((int)effect_value)) {
					this_room.game_broadcast("效果成功命中! "+player_target.get_room_name()+" 的装备被全部破坏!");
					SZSC_game_weapon.delete_weapon_all(this_room, player_target);
				}
				
				break;
			}
			case SZSC_game_protocol.Effect_i_reset_attack_chance:{
				SZSC_game_general_function.reset_rule_attack(player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_check_enemy_card_and_plunge:{
				result=SZSC_game_deck.plunder_ones_card(this_room, player_launcher, player_target, 1);
				break;
			}
			case SZSC_game_protocol.Effect_i_discard_card:{
				SZSC.Result_info result_info=SZSC_game_deck.choose_discard(this_room, (int)effect_value, true, player_launcher, player_launcher);
				result=result_info.get_type();
				break;
			}
			case SZSC_game_protocol.Effect_i_discard_card_all:{
				result=SZSC_game_deck.discard_all_card(this_room, player_launcher, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_get_effect_dmg_by_myself:{
				result=SZSC_game_attack.self_effect_dmg(this_room, effect_value, 100, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_i_destroy_my_weapon_this:{
				int which_weapon=launch_Info.get_which_weapon();
				if(!SZSC_game_judge.weapon_choice_valid(player_launcher, which_weapon)) {
					show(effect_name+"处理出错! get_which_weapon="+which_weapon);
					break;
				}
				//执行到这里必然获取了正确的要摧毁自己哪把武器
				SZSC_game_weapon.delete_weapon(this_room, player_launcher, which_weapon);
				
				break;
			}
			case SZSC_game_protocol.Effect_i_consume_token_my_weapon_this:{//只适合单个token标记的武器，如果多个则需额外特殊处理？
				if(!launch_Info.is_buff()) {
					show(effect_name+"+ 处理时 launch_Info 非buff判定!");
					break;
				}
				int which_weapon=launch_Info.get_which_weapon();
				int consume_value=-(int)effect_value;
				//开始遍历玩家buff，查询对应该武器的buff，且判断是否是token类型，如果是，则消耗token并结束效果处理
				for(SZSC_Buff buff:player_launcher.buff) {
					if(buff.check_source_type(SZSC_game_protocol.Buff_source_my_weapon_effect_fix)&&buff.get_source_which_item()==which_weapon)
						if(buff.is_token()) {
							buff.change_token_value(consume_value);
							break;
						}
				}
				
				break;
			}
			case SZSC_game_protocol.Effect_i_public_card_i_get:{
				//未来版本需要在eventinfo里添加自己本次获取所有手卡的信息，才在这里通过eventinfo内部访问来获取该内容，
				//以免如果抽到的卡被丢弃导致不该公开的已有手卡被公开
				
				//直接把自己获得的最后x张卡公开
				String card_name="";
				int number=(int)effect_value;
				int pointer=player_launcher.card.size()-1;
				while(number-->0) {
					if(pointer<0)
					{
						show(effect_name+" 错误! pointer小于0了还在公开! "+number+"    "+effect_value);
						break;
					}
					int card_No=player_launcher.card.get(pointer--).get_card_No();
					String tmp_name=SZSC_game_dictionary.search_ID_name(card_No)+"  ";
					card_name+=tmp_name;
				}
				this_room.game_broadcast(player_launcher.get_room_name()+" 公开抽到的卡: "+card_name+"!");
				
				break;
			}
			case SZSC_game_protocol.Effect_SP_scarecrow:{
				//先结算武器全部破坏，再返回普攻被闪避
				SZSC_game_weapon.delete_weapon_all(this_room, player_target);
				result=SZSC_game_protocol.i_failed_attack;
				break;
			}
			case SZSC_game_protocol.Effect_i_let_enemy_effect_dmg_my_weapon_quantity:{
				//统计自己武器数量
				int weapon_number=player_launcher.get_weapon_number();
				result=SZSC_game_attack.normal_effect_dmg(this_room, weapon_number, 100, player_target, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_SP_fire_splash:{
				SZSC_game_weapon.delete_all_enemy_weapon(this_room, player_launcher);
				result=SZSC_game_attack.normal_effect_dmg(this_room, effect_value, 100, player_target, player_launcher);
				break;
			}
			case SZSC_game_protocol.Effect_hatred:
			{
				//如果当前event为效果的发动，则让玩家主动选择，选项为（1）无效化当前行动并根据来源进行处理（2）主动
				//如果当前event不是效果的发动，则直接发动（2）选项
				//（1）根据event_info内容进行处理，比如如果event来源为人物效果则沉默该玩家并且返回当前发动无效化、武器发动无效并破坏
				//（2）如果event为空，则让玩家主动发动对玩家沉默/武器破坏/卡片发动无效化
				
				JSON_process msg=new JSON_process();
				msg.add("signal",SZSC_game_protocol.Signal_hatred_choice);
				if(event_info.launch_event()) 
					msg.add("launch_event",2);
				else
					msg.add("launch_event",1);
				player_launcher.send(msg);
				
				JSON_process reply_msg=player_launcher.game_listen();
				if(player_launcher.offline())
					break;
				int user_choice=reply_msg.getInt("hatred_choice");
				
				while(true) {
					switch(user_choice) {
						case SZSC_game_protocol.Signal_user_apply_hatred_launch_effect:{
							if(!event_info.launch_event()) {
								show("非发动情况使用无效化效果？");
								break;
							}
							//获取来源
							int source_type=event_info.get_source_type();
							switch(source_type) {
								case SZSC_game_protocol.TYPE_source_card:
									//卡片单纯无效化发动
									break;
								case SZSC_game_protocol.TYPE_source_selfeffct:{
									//人物效果额外进行沉默
									SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(14050);
									SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_target,player_launcher);
								}
									break;
								case SZSC_game_protocol.TYPE_source_weaponeffect:{
									//对该武器进行破坏
									int which_weapon=event_info.get_which_item();
									SZSC_game_weapon.destory_weapon(this_room, player_target, which_weapon);
									break;
								}
								default:
									show("event_info 奇怪source_type   "+source_type);
							}
							result=SZSC_game_protocol.i_failed_launch_effect;
							break;
						}
							
						case SZSC_game_protocol.Signal_user_apply_hatred_something:{
							int player_choose_type=reply_msg.getInt("player_choose_type");
							int which_one=reply_msg.getInt("which_one");
							
							if(!SZSC_game_judge.player_choice_valid_enemy(this_room, player_launcher, which_one)) {
								show("Signal_user_apply_hatred_something 憎恨目标 选择敌人错误"+which_one);
								break;
							}
							SZSC_player player_be_hatred=this_room.players.get(which_one);
							//玩家选择主动憎恨某个东西，并非用于无效化对手发动
							switch(player_choose_type) {
								case SZSC_game_protocol.TYPE_source_selfeffct:{
									this_room.game_broadcast(player_be_hatred+" 被 "+player_launcher.get_room_name()+" 沉默了!");
									//人物效果额外进行沉默
									SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(14050);
									SZSC_game_Buff_process.player_add_Buff(this_room,buff, player_be_hatred,player_launcher);
								}
									break;
									//第几把武器破坏
								case SZSC_game_protocol.TYPE_source_weaponeffect:{
									//对该武器进行破坏
									int which_weapon=reply_msg.getInt("which_weapon");
									int weapon_ID=player_be_hatred.weapon.get(which_weapon).get_weapon_ID();
									String weapon_name=SZSC_game_dictionary.get_name(weapon_ID);
									this_room.game_broadcast(player_be_hatred+" 的武器 "+weapon_name+" 被 "+player_launcher.get_room_name()+" 破坏了!");
									SZSC_game_weapon.destory_weapon(this_room, player_be_hatred, which_weapon);
									break;
								}
								default:
									show("player_choose_type 奇怪source_type   "+player_choose_type);
								
							}
						}
						
							break;
						default:
							show("hatred 奇怪user_choice   "+user_choice+"  内容"+reply_msg.getString("hatred_choice")+"\n 报文"+reply_msg.getString());
					}
					break;
				}
				
				//发送指令让玩家进行选择，先选择（1）或（2），如果（1）则直接进行处理，如果（2）则用户端继续选择要对哪项进行发动（例如选择哪个玩家，他的自身效果还是哪个武器）
				break;
			}
			
			case SZSC_game_protocol.Effect_check_deck_top_card:{
				
				String content="卡组最上方的卡为:";
				for(int pointer=0;pointer<effect_value;pointer++) {
					int card_No=this_room.deck.get(0).get_card_No();
					String card_name=SZSC_game_dictionary.get_name(card_No);
					content+=card_name;
				}
				SZSC_game_general_function.send_personal_msg(player_launcher, content);
				
				break;
			}
			case SZSC_game_protocol.Effect_state_change_attack:
				SZSC_game_transmit.attack_change(this_room, player_launcher, effect_value);
				break;
				
			case SZSC_game_protocol.Effect_state_change_blood:
				SZSC_game_transmit.blood_change(this_room, player_launcher, effect_value);
				break;
			
			case SZSC_game_protocol.Effect_check_one_enemy_card_random:{
				//随机获取对手
				List<SZSC_player>enemies_list=SZSC.getNewArrayList();
				for(SZSC_player player:this_room.players)
				{
					if(player.get_camp()!=player_launcher.get_camp())
						enemies_list.add(player);
				}
				int enemies_size=enemies_list.size();
				if(enemies_size==0)
				{
					show("错误获取敌方全体角色信息 0人");
					break;
				}
				int which_player=SZSC_game_general_function.getrandom(0, enemies_size);
				SZSC_player this_player=enemies_list.get(which_player);
				
				//如果对手不受公开效果，则效果直接结束
				SZSC.Event_info newEventInfo=SZSC.get_new_event_info(SZSC_game_protocol.i_will_be_effect_public_card, effect_value, this_player, player_launcher);
				if(SZSC_game_judge.whether_immune_this_effect(this_room, this_player, event_info)) {
					this_room.game_broadcast(this_player.get_room_name()+" 不受公开手卡效果影响!");
					break;
				}
				
				//获取被害者手卡总量
				int card_size=this_player.card.size();
				
				int left_count=(int)effect_value;//获取要偷看的卡的数量
				//如果被害者根本没有手卡
				if(card_size==0) {
					SZSC_game_general_function.send_personal_msg(player_launcher, this_player.get_room_name()+" 没有任何手卡");
					break;
				}
				String content="他拥有手卡: ";
				//如果被害者手卡数量少于等于要偷看的数量
				if(card_size<=left_count) {
					//告诉他所有手卡
					for(SZSC.card this_card:this_player.card) {
						int card_No=this_card.get_card_No();
						content+=SZSC_game_dictionary.get_name(card_No)+" ";
					}
					SZSC_game_general_function.send_personal_msg(player_launcher, content);
					
				}else {
					//进行随机获取
					List<Integer>cardList=SZSC.getNewArrayList();
					while(left_count>0) {
						int which_card=SZSC_game_general_function.getrandom(0, card_size);
						//如果是重复的，重新选取
						if(cardList.contains(which_card))
							continue;
						//如果不是重复的，进行添加
						cardList.add(which_card);
						left_count--;
					}
					for(int pointer:cardList) {
						int card_No=this_player.card.get(pointer).get_card_No();
						content+=SZSC_game_dictionary.get_name(card_No)+" ";
					}
					SZSC_game_general_function.send_personal_msg(player_launcher, content);
				}
				
				
				
				
				
			}
				
				
			//穆封灵
			case SZSC_game_protocol.Effect_get_extra_attack_power_by_my_each_weapon:{
				//获取自己武器数量
				int weapon_number=player_launcher.get_weapon_number();
				
				//生成buff
				SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(-100080);
				buff.set_effect_value(weapon_number*1);
				//添加buff
				SZSC_game_Buff_process.player_add_Buff(this_room, buff, player_launcher, player_launcher);
				
				
				break;
			}
			case SZSC_game_protocol.Effect_i_point_one_discard_card_randomly:{
				int which_one=SZSC_game_player_choose.choose_enemy(this_room, player_launcher, "请选择一名对手随机丢弃"+(int)effect_value+"张手卡", true);
				if(!SZSC_game_judge.player_choice_valid_enemy(this_room, player_launcher, which_one)) {
					show("Effect_i_point_one_discard_card_randomly 选取对手玩家错误 "+which_one);
					break;
				}
				SZSC_player which_player_to_discard_card=this_room.players.get(which_one);
				SZSC_game_deck.random_discard(this_room, (int)effect_value, which_player_to_discard_card, player_launcher);
				break;
			}
			default:
				show("未实装效果  "+effect_name);
		}
		return result;
	}
	
	
	
	
	

	private static void show(String msg)
	{
		SZSC_service.show(msg);
	}
}
