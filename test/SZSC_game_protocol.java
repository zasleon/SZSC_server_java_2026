package test;


public class SZSC_game_protocol {

	private static void show(String msg) {
		SZSC_service.show(msg);
	}
	//transmit
	public final static int SZSC_first_basic_character_info	=108200;//个人基本信息，攻击力、生命、玩家姓名
	public final static int SZSC_basic_character_info		=108201;//个人攻击力、武器信息
	public final static int SZSC_card_character_info		=108202;//个人卡片信息
	public final static int SZSC_Buff_character_info		=108203;//个人附加效果buff发生变化，因此攻击力变化统一编入buff变化
	/*
	public final static int refresh_first_state			=7100;//首次传输人物数据
	public final static int refresh_state_CM			=7101;//传输自己人物手卡数据
	public final static int refresh_state_CH			=7102;//传输对手手卡数据
	public final static int refresh_state_blood_M		=7103;//更新血量信息
	public final static int refresh_state_blood_H		=7104;//更新对手血量信息
	public final static int refresh_state_Buff_M		=7105;//更新自己buff信息
	public final static int refresh_state_Buff_H		=7106;//更新对手buff信息
	public final static int refresh_state_attack_M		=7107;//更新自己攻击力信息
	public final static int refresh_state_attack_H		=7108;//更新对手攻击力信息
	public final static int refresh_state_weapon_M		=7109;//更新自己武器信息
	public final static int refresh_state_weapon_H		=7110;//更新对手武器信息*/
	
	//选项展示类型
	public final static int Interface_show_type_enemies        =1;//进行对对手的选择
    public final static int Interface_show_type_teammates      =2;//进行对队友的选择
    public final static int Interface_show_type_all_members    =3;//进行对全员的选择
	
    
  //launch_info发动效果类型
    public static final String TYPE_launch_effect="效果卡";
    public static final String TYPE_launch_weapon="装备";
    public static final String TYPE_launch_assist="助攻卡";
    public static final String TYPE_launch_hide="隐藏效果";
    public static final String TYPE_launch_buff="增益效果";
    public static final String TYPE_launch_weapon_effect="装备卡效果";
    public static final String TYPE_launch_self_effect="人物效果";
    
    public static final String TYPE_event_dmg_attack="普攻类伤血";
    public static final String TYPE_event_effect_dmg="效果类伤血";
    public static final String TYPE_event_effect="效果类";
    
	public static final String TYPE_weapon_effect="装备卡效果";
	public static final String TYPE_self_effect="人物效果";
	
	public static final String TYPE_buff="增益效果";
	public static final String TYPE_debuff="减益效果";
	
	//buff data
	public static final String Buff_duration_type_column="持续时间单位";
	public static final String Buff_duration_value_column="持续时长";
	public static final String Buff_add_type_column="添置类型";
	
	
	//buff 添加类型
	public static final String Buff_add_type_independent="独立";
	public final static String Buff_add_type_reset="覆盖";//
	public final static String Buff_add_type_overlay="叠加";//
	
	
	;//buff	持续时间、限制次数时域 时间单位
	public final static String TYPE_Duration_this_time_get_card				="此次抽卡";//
	public final static String TYPE_DURATION_this_effect_dmg				="此次效果伤血";//
	public final static String TYPE_Duration_this_attack					="此次普攻";//
	public final static String TYPE_Duration_this_launch					="此次效果结算";//
	public final static String TYPE_Duration_this_event						="此次事件";//此次行动，指某人主动发动事件后到该连锁全部结束为止

	public final static String TYPE_Duration_turn							="回合";//
	public final static String TYPE_Duration_cycle							="轮回";//轮回

	public final static String TYPE_Duration_permanent						="常驻";//永久，此次游戏
	public final static String TYPE_Duration_permanent_selfeffect			="自身效果";//永久，此次游戏
	public final static String TYPE_Duration_permanent_weaponeffect			="武器常驻";//永久，此次游戏
    
	public final static int p_name		=1;//查询卡片名字
	public final static int p_details	=2;//查询卡片详细信息
    
	
	public final static int Signal_event_happen				=20002;//告诉客户端，发生事件了,包含事件类型
	public final static int Signal_player_record_information=21000;//告诉客户端，单独对该玩家进行记录说明信息，不对其他玩家通知相同内容
	public final static int Signal_show_rivals_card_P		=107308;//显示对手所有手卡并抢夺
	public final static int Signal_game_end					=107317;//游戏结束
	
	public final static int Signal_chooseYN				=107306;//【图形界面客户端】做“是否”选择，一般后面都跟一句问话
	public final static int Signal_do_chooseYN			=107307;//【图形界面客户端】做“是否”选择，一般后面都跟一句问话
	public final static int Signal_do_card_choice		=107308;//客户端发来请求为做卡片选择
	public final static int Signal_not_do_card_choice	=110000;//客户端发来请求为不做卡片选择
	
	public final static int Signal_show_enemy_list		=107320;//显示敌人目录
	public final static int Signal_show_friend_list		=107322;//显示友方目录
	public final static int Signal_show_alive_list		=107324;//显示存活者目录
	public final static int Signal_pls_discard_card		=108120;//因为某原因而丢卡，包含bool值表明是否必须丢
	public final static int Signal_game_tips			=108032;//游戏中对个人的消息提示，例如不能发动的原因，或对其行为作出,以toast方式提示
	public final static int Signal_game_broadcast		=108031;//房间内广播消息
	
	public final static int Signal_hatred_choice		=107309;//做憎恨目标选择
	
	public final static int Signal_user_apply_hatred_launch_effect	=1;
	public final static int Signal_user_apply_hatred_something		=2;
	
	
	;//interface_state
	public final static int player_interface_state_discard_one_card			=107400;//【图形界面客户端】进入“丢弃单张手卡“的操作状态
	public final static int player_interface_state_discard_muti_card		=107401;//【图形界面客户端】进入”丢弃多张手卡”的操作状态
	public final static int player_interface_state_return_normal			=107402;//【图形界面客户端】回到原始“使用手卡”的操作状态
	public final static int player_interface_state_discard_free_card		=107403;//【图形界面客户端】进入“选择任意张手卡“的操作状态
	public final static int player_interface_state_choose_one_card			=107404;//【图形界面客户端】进入“发动单张手卡”的操作状态
	public final static int player_interface_state_fight_back				=107405;//【图形界面客户端】进入“发动反击”的操作状态
	
	
	//发动来源
	public final static int TYPE_source_card			=1000;
	public final static int TYPE_source_selfeffct		=1001;
	public final static int TYPE_source_weaponeffect	=1002;
	
	
	
	//读取预设表格获取字符串对应的数值作为特征号
	
	//public static final SYSTEM_protocol_result PROTOCOL;
	
	public static final String List_add_type="添置类型（独立、叠加、覆盖）";
	public static final String List_first_remain="初始量";
	public static final String List_token="是否token";
	public static final String List_buff="是否buff";
	public static final String List_positive_or_negative="主被动";
	public static final String List_trigger_type="发动类型"; 
	public static final String List_duration_type="持续时间单位"; 
	public static final String List_duration_first_remain_value="持续时长"; 
	
	
	public static final String force_end_event="force_end_event";
	public static final String force_end_turn="force_end_turn";
	public static final String force_offline="force_offline";
	public static final String game_end="game_end";
	
	public static final String is_buff_column="是否buff";
	public static final String is_buff="是";
	public static final String is_token="是";
	public static final String is_positive="主动";
	public static final String trigger_type_column="发动类型";
	public static final String is_must_activate="必发";
	public static final String is_choose_activate="选发";
	
	//Buff source
	;//buff source 添加buff时在添加时识别来源
	public final static int Buff_source_my_character_effect				=1;//人物自身效果
	public final static int Buff_source_card_effect						=2;//卡片效果
	public final static int Buff_source_my_weapon_effect_fix			=3;//自己武器固定效果
	public final static int Buff_source_my_weapon_effect_bring			=4;//自己武器效果
	public final static int Buff_source_enemy_weapon_effect				=5;//敌人武器
	public final static int Buff_source_field_effect					=6;//场景效果
	public final static int Buff_source_friend_characeter_effect		=7;//友方自身效果
	public final static int Buff_source_friend_weapon_effect			=8;//友方武器效果
	
	
	public static final String still_fight="still_fight";
	public static final String CHOICE_i_cancel_action = "CHOICE_i_cancel_action";
	public static final String CHOICE_i_do_useless_choice = "CHOICE_i_do_useless_choice";
	public static final String event_happen = "event_happen";
	public static final String Time_turn_end = "Time_turn_end";
	public static final String Time_attack_end = "Time_attack_end";
	public static final String STATE_my_turn = "STATE_my_turn";
	public static final String STATE_my_turn_enough_weapon_token = "STATE_my_turn_enough_weapon_token";
	public static final String STATE_my_turn_positive = "STATE_my_turn_positive";
	public static final String STATE_my_turn_positive_weapon_not_full = "STATE_my_turn_positive_weapon_not_full";
	public static final String STATE_team_turn_i_positive = "STATE_team_turn_i_positive";
	public static final String STATE_i_weapon_not_full = "STATE_i_weapon_not_full";
	public static final String STATE_i_have_weapon_equipped = "STATE_i_have_weapon_equipped";
	public static final String STATE_i_involve_event_over_4_chains = "STATE_i_involve_event_over_4_chains";
	public static final String someone_launch_effect="someone_launch_effect";

	public static final String someone_will_get_card = "someone_will_get_card";
	public static final String someone_dead = "someone_dead";
	public static final String someone_dead_not_me = "someone_dead_not_me";
	public static final String other_use_card = "other_use_card";
	public static final String other_dead = "other_dead";
	public static final String Result_force_end_event = "Result_force_end_event";
	public static final String Result_force_end_turn = "Result_force_end_turn";
	public static final String i_used_card = "i_used_card";
	public static final String i_success_use_card = "i_success_use_card";
	public static final String i_success_use_card_in_my_turn = "i_success_use_card_in_my_turn";
	public static final String i_success_get_one_card = "i_success_get_one_card";
	public static final String i_success_get_card = "i_success_get_card";
	public static final String i_success_do_fight = "i_success_do_fight";
	public static final String i_success_do_dmg = "i_success_do_dmg";
	public static final String i_success_do_dmg_effect = "i_success_do_dmg_effect";
	public static final String i_success_attack = "i_success_attack";
	public static final String i_success_attack_or_my_turn_positive = "i_success_attack_or_my_turn_positive";
	public static final String i_success_attack_over_3_times = "i_success_attack_over_3_times";
	public static final String i_failed_launch_effect = "i_failed_launch_effect";
	public static final String i_failed_do_dmg = "i_failed_do_dmg";
	public static final String i_failed_do_dmg_effect = "i_failed_do_dmg_effect";
	public static final String i_failed_attack = "i_failed_attack";
	public static final String i_failed_attack_be_defend = "i_failed_attack_be_defend";
	public static final String i_failed_attack_be_escape = "i_failed_attack_be_escape";
	public static final String i_will_attack = "i_will_attack";
	public static final String i_will_discard_card = "i_will_discard_card";
	public static final String i_will_die = "i_will_die";
	public static final String i_will_be_fatal_dmg = "i_will_be_fatal_dmg";
	public static final String i_will_be_effect = "i_will_be_effect";
	public static final String i_will_be_effect_unmovable = "i_will_be_effect_unmovable";
	public static final String i_will_be_effect_public_card = "i_will_be_effect_public_card";
	public static final String i_will_be_effect_positive = "i_will_be_effect_positive";
	public static final String i_will_be_effect_dmg = "i_will_be_effect_dmg";
	public static final String i_will_be_effect_discard_card_positive = "i_will_be_effect_discard_card_positive";
	public static final String i_will_be_effect_discard_card_passive = "i_will_be_effect_discard_card_passive";
	public static final String i_will_be_dmg = "i_will_be_dmg";
	public static final String i_will_be_dmg_effect_passive = "i_will_be_dmg_effect_passive";
	public static final String i_will_be_dmg_effect_positive = "i_will_be_dmg_effect_positive";
	public static final String i_will_be_attacked = "i_will_be_attacked";
	public static final String i_be_fatal_dmg = "i_be_fatal_dmg";
	public static final String i_be_dmg_effect = "i_be_dmg_effect";
	public static final String i_be_dmg_effect_passive = "i_be_dmg_effect_passive";
	public static final String i_be_dmg_effect_positive = "i_be_dmg_effect_positive";
	public static final String i_be_discard_card = "i_be_discard_card";
	public static final String i_be_discard_card_positive = "i_be_discard_card_positive";
	public static final String i_be_discard_card_passive = "i_be_discard_card_passive";
	public static final String i_be_attacked = "i_be_attacked";
	public static final String i_immune_effect = "i_immune_effect";
	public static final String Launch_i_take_dmg_attack="Launch_i_take_dmg_attack";
	public static final String Launch_i_take_dmg_effect_positive="Launch_i_take_dmg_effect_positive";
	public static final String Launch_i_take_dmg_effect_passive="Launch_i_take_dmg_effect_passive";

	
	//元效果
	public static final String Effect_get_buff_one_enemy = "Effect_get_buff_one_enemy";
	public static final String Effect_get_buff_one_enemy_50percent = "Effect_get_buff_one_enemy_50percent";
	public static final String Effect_get_buff_myself = "Effect_get_buff_myself";
	public static final String Effect_refresh_card_all = "Effect_refresh_card_all";
	public static final String Effect_i_point_one_enemy_get_effect_dmg = "Effect_i_point_one_enemy_get_effect_dmg";
	public static final String Effect_i_point_one_get_heal = "Effect_i_point_one_get_heal";
	public static final String Effect_i_point_one_enemy_get_effect_dmg_50percent = "Effect_i_point_one_enemy_get_effect_dmg_50percent";
	public static final String Effect_i_give_all_my_card_to_teammates = "Effect_i_give_all_my_card_to_teammates";
	public static final String Effect_i_recover_to_limit = "Effect_i_recover_to_limit";
	public static final String Effect_hatred = "Effect_hatred";
	public static final String Effect_i_recover = "Effect_i_recover";
	public static final String Effect_SP_i_defend_attack_throne_succeed_sword = "Effect_SP_i_defend_attack_throne_succeed_sword";
	public static final String Effect_i_defend_attack = "Effect_i_defend_attack";
	public static final String Effect_i_claim_attack = "Effect_i_claim_attack";
	public static final String Effect_i_attack_enemy = "Effect_i_attack_enemy";
	public static final String Effect_i_attack_super = "Effect_i_attack_super";
	public static final String Effect_SP_burst_fight = "Effect_SP_burst_fight";
	public static final String Effect_i_get_card = "Effect_i_get_card";
	public static final String Effect_i_escape_attack_and_can_fight_back = "Effect_i_escape_attack_and_can_fight_back";
	public static final String Effect_SP_lightning_blaze="Effect_SP_lightning_blaze";
	public static final String Effect_i_can_attack_back_with_enforce = "Effect_i_can_attack_back_with_enforce";
	public static final String Effect_i_point_enemy_get_effect_dmg_escape_available="Effect_i_point_enemy_get_effect_dmg_escape_available";
	public static final String Effect_i_let_enemy_get_effect_dmg_escape_available = "Effect_i_let_enemy_get_effect_dmg_escape_available";
	public static final String Effect_i_immune_effect_dmg = "Effect_i_immune_effect_dmg";
	public static final String Effect_equip_weapon = "Effect_equip_weapon";
	public static final String Effect_SP_ice_burst = "Effect_SP_ice_burst";
	public static final String Effect_deck_top_card_to_graveyard = "Effect_deck_top_card_to_graveyard";
	public static final String Effect_destroy_enemy_weapon_random = "Effect_destroy_enemy_weapon_random";
	public static final String Effect_i_let_enemy_effect_dmg = "Effect_i_let_enemy_effect_dmg";
	public static final String Effect_i_let_enemy_blood_halve = "Effect_i_let_enemy_blood_halve";
	public static final String Effect_i_escape_attack = "Effect_i_escape_attack";
	public static final String Effect_SP_break_out_godattack = "Effect_SP_break_out_godattack";
	public static final String Effect_SP_destory_planet = "Effect_SP_destory_planet";
	public static final String Effect_equip_weapon_from_deck = "Effect_equip_weapon_from_deck";
	public static final String Effect_token_this_card_add = "Effect_token_this_card_add";
	public static final String Effect_SP_scarecrow = "Effect_SP_scarecrow";
	public static final String Effect_i_let_enemy_effect_dmg_my_weapon_quantity = "Effect_i_let_enemy_effect_dmg_my_weapon_quantity";
	public static final String Effect_SP_fire_splash = "Effect_SP_fire_splash";
	public static final String Effect_SP_divine_laser_effect = "Effect_SP_divine_laser_effect";
	public static final String Effect_lightning_blaze = "Effect_lightning_blaze";
	public static final String Effect_i_let_enemy_refresh_card_randomly = "Effect_i_let_enemy_refresh_card_randomly";
	public static final String Effect_i_let_enemy_random_discard_card = "Effect_i_let_enemy_random_discard_card";
	public static final String Effect_i_destroy_enemy_all_weapon = "Effect_i_destroy_enemy_all_weapon";
	public static final String Effect_i_reset_attack_chance = "Effect_i_reset_attack_chance";
	public static final String Effect_i_check_enemy_card_and_plunge = "Effect_i_check_enemy_card_and_plunge";
	public static final String Effect_i_discard_card = "Effect_i_discard_card";
	public static final String Effect_i_discard_card_all = "Effect_i_discard_card_all";
	public static final String Effect_i_get_effect_dmg_by_myself = "Effect_i_get_effect_dmg_by_myself";
	public static final String Effect_i_destroy_my_weapon_this = "Effect_i_destroy_my_weapon_this";
	public static final String Effect_i_consume_token_my_weapon_this = "Effect_i_consume_token_my_weapon_this";
	public static final String Effect_i_public_card_i_get = "Effect_i_public_card_i_get";

	// 复合效果常量（已修正原名称中的多余空格）
	public static final String compound_earth_splitting = "compound_earth_splitting";
	public static final String compound_plunder_cleave = "compound_plunder_cleave";
	public static final String compound_weapon_out_and_back = "compound_weapon_out_and_back";
	public static final String compound_savage_wield = "compound_savage_wield";
	public static final String compound_blessing_frozen = "compound_blessing_frozen";
	public static final String compound_blessing_thunder = "compound_blessing_thunder";
	public static final String compound_blessing_blazing = "compound_blessing_blazing";
	public static final String compound_blessing_torrent = "compound_blessing_torrent";
	public static final String compound_blessing_sacredness = "compound_blessing_sacredness";
	public static final String compound_blessing_netherdark = "compound_blessing_netherdark";
	public static final String compound_divine_void_slash = "compound_divine_void_slash";
	public static final String compound_dark_rush = "compound_dark_rush"; // 修正：移除了名称中的多余空格
	public static final String compound_skyfall_dazzling_entrance = "compound_skyfall_dazzling_entrance";
	public static final String compound_escape_to_sky_fight_back = "compound_escape_to_sky_fight_back";
	public static final String compound_divine_laser = "compound_divine_laser";
	public static final String compound_centrifugal_combo = "compound_centrifugal_combo"; // 修正：空格替换为下划线（符合Java命名规范）
	public static final String compound_chaos_bubble = "compound_chaos_bubble";
	public static final String compound_aetherveil_aegis = "compound_aetherveil_aegis"; // 修正：移除了名称中的多余空格
	public static final String compound_circling_fight_back = "compound_circling_fight_back";
	public static final String compound_zephyr_shortblade_1 = "compound_zephyr_shortblade_1";
	public static final String compound_obsidian_sword_1 = "compound_obsidian_sword_1";
	public static final String compound_universe_splitting_axe_1 = "compound_universe_splitting_axe_1";
	public static final String compound_universe_splitting_axe_3 = "compound_universe_splitting_axe_3";

	// Buff常量
	public static final String Buff_extra_attack_power = "Buff_extra_attack_power";
	public static final String Buff_normal_attack_dmg_mutipler = "Buff_normal_attack_dmg_mutipler";
	public static final String Buff_unmovable = "Buff_unmovable";
	public static final String Buff_immortal = "Buff_immortal";
	public static final String Buff_immune = "Buff_immune";
	public static final String Buff_my_attack_cannot_escape_defend = "Buff_my_attack_cannot_escape_defend";
	public static final String Buff_my_weapon_all_invalid = "Buff_my_weapon_all_invalid";
	public static final String Buff_attack_penetrating = "Buff_attack_penetrating";
	public static final String Buff_base_attack_mutipler = "Buff_base_attack_mutipler";
	public static final String Buff_all_attack_mutipler = "Buff_all_attack_mutipler";
	
	
	
	
	public static final String Buff_myself_silent = "Buff_myself_silent";
	public static final String Buff_only_wood = "Buff_only_wood";
	public static final String Effect_check_deck_top_card = "Effect_check_deck_top_card";
	public static final String compound_SP_qijiefeng = "compound_SP_qijiefeng";
	public static final String Effect_state_change_attack = "Effect_state_change_attack";
	public static final String Effect_state_change_blood = "Effect_state_change_blood";
	public static final String compound_SP_qijiefeng_back = "compound_SP_qijiefeng_back";
	public static final String Effect_check_one_enemy_card_random = "Effect_check_one_enemy_card_random";
	public static final String Effect_get_extra_attack_power_by_my_each_weapon = "Effect_get_extra_attack_power_by_my_each_weapon";
	public static final String Effect_SP_pretrap = "Effect_SP_pretrap";
	public static final String Effect_SP_fire_god_rage = "Effect_SP_fire_god_rage";
	public static final String Effect_SP_attack_without_weapon = "Effect_SP_attack_without_weapon";
	public static final String Effect_SP_stance_of_king = "Effect_SP_stance_of_king";
	public static final String compound_SP_one_fierce_attack = "compound_SP_one_fierce_attack";
	public static final String compound_get_extra_1_attack_this_turn = "compound_get_extra_1_attack_this_turn";
	public static final String Effect_i_point_one_discard_card_randomly = "Effect_i_point_one_discard_card_randomly";
	
	
	
	/*
	static {
		
		
		SYSTEM_protocol_result protocol_Result=new SYSTEM_protocol_result("SZSC_game_protocol");
		protocol_Result.load_protocol_parameter(SZSC_protocol.Game_card_EXCEL_PATH, "effect");
		PROTOCOL=protocol_Result;
		
    }*/
	
}
