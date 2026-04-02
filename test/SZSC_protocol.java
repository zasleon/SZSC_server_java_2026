package test;




public class SZSC_protocol {
	public final static boolean client_mode=false;
	
	public static final String DEFAULT_PATH_STRING="";
	public static String DB_path="SZSC.db";
	// 资产总览表Excel文件路径
	
    public static final String Asset_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC.xlsx";
    public static final String Character_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC_character_configuration.xlsx";
    public static final String Character_default_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC_character_default.xlsx";
    public static final String Character_bot_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC_character_bot.xlsx";
    

    public static final String Game_card_hide_effect_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC_card_hide_effect.xlsx";
    public static final String Game_card_EXCEL_PATH = DEFAULT_PATH_STRING+"SZSC_card.xlsx";
    

    
    
	
	public final static int code_none					=-999;

	public final static int playernumber		=2;//目前只建立两个角色
	public final static int weaponlimit			=2;//武器上限为2
	public final static int cardlimit			=10;//手卡上限为10
	public final static int decklimit			=8;//预设卡组数量为8张
	public final static int abilitylimit		=5;//自身能力最多四个
	public final static int weaponeffectlimit	=5;//携带武器效果最多15个
	public final static int garbagelimit		=1000;//废卡区大小
	public final static int roomlimit			=250;//服务器战斗房间上限
	
	public final static int SZSC_Buff_limit				=50;//个人所持有最大buff数量
	public final static int SZSC_message_length			=300;//单次接受报文最大长度
	public final static int SZSC_history_log_limit		=10;//保留前n条历史记录
	
	;//state
	public final static int SZSC_in_game				=6801;//处于游戏状态
	public final static int SZSC_in_room				=6802;//处于在房间状态
	public final static int SZSC_in_roomlist			=6803;//处于在房间状态
	
	public final static int SZSC_wood_robot		=-123;//木桩
	public final static int SZSC_none_player	=1;//位置上为空
	public final static int SZSC_real_player	=2;//活人






	
	public final static int do_attack					=11111;//是否需要进行普攻？（做选择时使用）
	public final static int make_shield					=11112;//发动隐藏天盾
	public final static int do_choice					=21111;//确认选择
	public final static int cancel_choice				=31111;//选择拒绝
	public final static int robot_symbol				=41111;//机器人识别码


	;//玩家游戏状态self_game_state
    public final static int SZSC_force_offline							=40000;//断线了
    public final static int SZSC_normal_online							=40001;//正常在线
    /*
    public final static int SZSC_i_die									=30180;//我死了
    public final static int SZSC_other_one_death						=30140;//场上有一个别人死了
*/
	//public final static int battle_choice				=6900;//让客户端显示战斗选项一览
	public final static int SZSC_show_character_choice	=6901;//让客户端显示角色选取界面

/*
	public final static int event_happen				=7001;//某事件发生了,强制锁定别人的自由行动输入输出
	public final static int event_end					=7002;//事件结束，解除别人的自由行动输入输出
	public final static int event_happen_N				=7003;//事件发生，不思考
	public final static int your_event					=7004;//你的事件
	*/
	//public final static int SZSC_event_happen			=20002;//告诉客户端，发生事件了,包含事件类型
	public final static int SZSC_i_cancel_do_choice						=-888;//自己放弃选择

	public final static int SZSC_apply_create_room		=7010;
	public final static int SZSC_apply_enter_room		=7011;
	public final static int SZSC_apply_exit_room		=7012;
	public final static int SZSC_apply_add_robot		=7013;
	public final static int SZSC_apply_remove_someone	=7014;
	public final static int SZSC_apply_start_game		=7015;
	public final static int SZSC_apply_show_roomlist	=7016;
	public final static int SZSC_apply_choose_character =7017;
	public final static int SZSC_apply_choose_character_change_page =7018;
	public final static int SZSC_apply_choose_character_default		=7019;




	public final static int SZSC_create_room_success	=7200;//你进入房间了
	public final static int SZSC_leave_room_success		=7201;//你退出房间了
	public final static int someone_get_in				=7202;//有人进入房间了
	public final static int you_are_guest				=7203;//你是房间宾客
	public final static int you_are_host				=7204;//你是房间主人
	public final static int SZSC_show_roomlist			=7205;//显示房间列表信息
	public final static int stop_show_room				=7206;//停止显示可选择的房间
	public final static int enter_room_success			=7207;//你进入房间成功了
	public final static int SZSC_someone_leave			=7208;//有人离开房间了
	public final static int someone_offline				=7209;//有人掉线了
	public final static int game_start_interface			=7210;//角色全选完了,进入游戏界面开始游戏
	public final static int you_win						=7211;//游戏结束，你赢了
	public final static int you_lose					=7212;//游戏结束，你输了
	public final static int game_standoff				=7213;//游戏结束，平局
	public final static int you_not_in_room				=7214;//你不在房间中
	public final static int SZSC_room_tips				=7218;//房间信息
	
	
	
	public final static int SZSC_purchase_result_success		=-900;
	public final static int SZSC_purchase_result_failed			=-999;
	public final static int SZSC_purchase_result_lack_money		=-1000;
	public final static int SZSC_purchase_result_order_wrong	=-1100;
	
	
	
	//public final static int start_turn_settle			=7300;//开始回合结算
	//public final static int start_another_turn			=7301;//本回合开始
	//public final static int show_weapon_list			=7302;//【图形界面客户端】显示武器栏武器和效果
	//public final static int get_c_number				=7303;//【图形界面客户端】想要装备武器，让他发送来想要装备第几张手卡
	//public final static int get_we_number				=7304;//【图形界面客户端】获取发动第几个效果
	//public final static int show_Buff_list				=7305;//【图形界面客户端】显示buff栏效果
	//public final static int SZSC_chooseYN				=7306;//【图形界面客户端】做“是否”选择，一般后面都跟一句问话
	//public final static int whether_fight_back			=7307;//是否反击？你是否行动？（0:不行动；1:发动手卡；2:发动个人效果；3:发动武器效果；4.加附效果）\n请选择：
	//public final static int SZSC_show_rivals_card_P		=7308;//显示对手所有手卡并抢夺
	//public final static int SZSC_hatred_choice			=7309;//做憎恨目标选择
	
	//public final static int now_is_your_turn			=7311;//确认是你的回合
	//public final static int whether_launch_hideeffect	=7312;//是否发动卡片隐效果？
	//public final static int SZSC_card_launch			=7313;//发动了卡片
	//public final static int delete_which_weapon			=7314;//【图形界面客户端】丢弃哪一个武器？
	//public final static int which_to_hatred				=7315;//对什么发动憎恨？？？
	//public final static int get_w_number				=7316;//选择了哪个一武器？
	//public final static int SZSC_game_end				=7317;//游戏结束
	
	
	//public final static int SZSC_game_show_enemy_list		=7320;//显示敌人目录
	//public final static int SZSC_game_show_friend_list		=7322;//显示友方目录
	//public final static int SZSC_game_show_alive_list		=7324;//显示存活者目录
	
	
	
	;//当前游戏阶段current_state
	public final static int SZSC_prepare_state	=0;//游戏准备阶段
	public final static int SZSC_fighting_state	=1;//游戏进行阶段
	public final static int SZSC_end_turn_state	=2;//游戏回合结算阶段
	public final static int SZSC_end_state		=3;//游戏结束阶段，在游戏结束和完成两个阶段之间，由房主执行游戏结算，宣布游戏中仅剩的一方胜利
	public final static int SZSC_finish_state	=4;//游戏完成阶段
	
	public final static int SZSC_choose_character_state=5;//选择角色阶段

	

	;//系统发向客户端信号
	public final static int SZSC_lock_character_choice	=8000;//选择完成，封锁角色选择

	public final static int SZSC_room_broadcast			=8030;//房间内广播消息
	//public final static int SZSC_game_broadcast			=8031;//游戏内广播消息
	//public final static int SZSC_game_tips				=8032;//游戏中对个人的消息提示，例如不能发动的原因，或对其行为作出,以toast方式提示
	//public final static int SZSC_game_log				=8033;//游戏中对个人的消息提示，以log方式显示

	//public final static int SZSC_new_event				=8050;//告诉客户端修改背景颜色

	//public final static int SZSC_pls_choose_enemy		=8100;//请指定一个敌人
	//public final static int SZSC_pls_choose_someone		=8101;//请指定场上一个对象

	//public final static int SZSC_pls_discard			=8120;//因为某原因而丢卡，包含bool值表明是否必须丢

	
	

	;//客户端发来行动请求
	public final static int SZSC_player_apply_general_attack	=1;//发动普攻
	public final static int SZSC_player_apply_use_self_effect	=2;//发动自身效果
	public final static int SZSC_player_apply_use_card			=3;//发动手卡
	public final static int SZSC_player_apply_delete_weapon		=4;//丢弃武器
	public final static int SZSC_player_apply_use_weapon_effect	=5;//发动武器效果
	public final static int SZSC_player_apply_end_turn			=7;//宣布回合结束
	public final static int SZSC_player_apply_use_buff			=8;//发动buff
	public final static int SZSC_player_apply_give_up_action	=9;//不做任何行动

	public final static int SZSC_player_apply_dicard_this_card	=11;//丢这张卡
	public final static int SZSC_player_apply_dicard_these_card	=12;//丢这些卡
	
	
	
	
	
	//资产
	public final static int SZSC_page_asset_limit					=10;//单次显示个数
	public final static int SZSC_page_character_limit				=5;//单次显示个数
	
	public final static int SZSC_apply_check_asset					=9220;//查看个人资产
	public final static int SZSC_apply_create_character				=9221;//创建角色
	public final static int SZSC_apply_update_character				=9222;//修改角色
	public final static int SZSC_apply_delete_character				=9223;//删除角色
	public final static int SZSC_apply_get_character				=9224;//展示角色
	public final static int SZSC_apply_go_lottery					=9235;//抽奖页面
	public final static int SZSC_apply_do_lottery					=9236;//进行抽奖
	
	public final static int SZSC_apply_refresh_asset_asset			=9237;//刷新显示个人资产 ，带页码
	public final static int SZSC_apply_refresh_asset_character		=9238;//刷新显示个人资产 ，带页码
	public final static int SZSC_apply_refresh_character			=9239;//刷新显示个人角色详细信息时旁边的个人资产 ，带页码
	public final static int SZSC_apply_refresh_character_asset		=9240;//刷新显示角色页面的个人资产 ，带页码
	
	public final static int SZSC_show_lottery_plate					=9250;//展示抽奖页面
	public final static int SZSC_show_own_asset						=9251;//展示个人资产
	public final static int SZSC_refresh_own_asset_asset			=9252;//刷新个人资产词条
	public final static int SZSC_refresh_own_asset_character		=9253;//刷新个人资产角色
	public final static int SZSC_show_own_character					=9260;//展示个人角色	
	public final static int SZSC_refresh_own_character				=9261;//刷新个人角色	
	public final static int SZSC_refresh_own_character_asset		=9262;//刷新个人角色	
	public final static int SZSC_refresh_default_character          =9263;//刷新默认角色
	public final static int SZSC_show_lottery_result				=9270;//刷新抽奖结果
	public final static int SZSC_delete_character_success			=9280;//告知角色删除成功，退出角色页面
	
	public static final int SZSC_normal_order_10=10;//普通开箱订单
    public static final int SZSC_normal_order_1=1;
	
	
	public final static int SZSC_character_update_name				=9300;//修改角色称呼
	public final static int SZSC_character_update_insert_effect		=9310;//新增角色词条
	public final static int SZSC_character_update_drop_effect		=9320;//删除角色词条
	
	public final static int SZSC_result_delete_character_not_empty	=9000;//删除角色失败，原因：角色词条没有全部清空
	public final static int SZSC_result_delete_character_success	=9001;//删除角色成功
	public final static int SZSC_result_delete_character_not_owner	=9002;//删除角色失败，原因：非角色持有者
	public final static int SZSC_result_delete_character_wrong_DB	=9003;//删除角色失败，原因：数据库查询错误
	
	
	//页面展示
    public final static int SZSC_show_up                =1;//往最上方记录开始显示
    public final static int SZSC_show_bottom            =2;//往最下方记录开始显示
    public final static int SZSC_show_left              =3;//往最左方记录开始显示
    public final static int SZSC_show_right             =4;//往最右方记录开始显示

    public final static int SZSC_choice_type_character  			=1000;//选中类型为角色
    public final static int SZSC_choice_type_asset      			=1001;//选中类型为物品
    public final static int SZSC_choice_type_character_default      =1002;//选中类型为系统提供的默认角色

   
    
    public final static int SZSC_excute_command			=1;//执行，不用返回结果
    public final static int SZSC_excute_inquire			=2;//查询，需要返回结果
	
    
    
    

}
