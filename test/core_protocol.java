package test;

public class core_protocol {
	
	//static final String IP_ADDRESS = "192.168.168.182"; // 指定监听的IP地址

	//static final String IP_ADDRESS = "Zasleon"; // 指定监听的IP地址
	
	static final int PORT = 8001; // 监听的端口号
    static final int VV_PORT = 9090; // 监听的端口号
    

    public final static int memberlimit					=1000;//服务器承载人数上限

	public final static int user_apply_login			=1000;
    public final static int user_apply_register			=1001;
    
    public static final int VV_service					=300;//提供VV服务	
    public static final int SZSC_service				=301;//提供SZSC服务
    
    public final static int system_overload				=9999;
    public final static int start_link					=6666;//告诉客户端成功通讯连接了（当客户端收不到这个数据时会告诉自己“服务器可能超载了”）
    public final static int please_cls					=6667;//让客户端刷新屏幕system("cls");
    public final static int show_choice					=6668;//让客户端显示选择界面
    public final static int show_user					=6669;//让客户端显示所有用户状态
    public final static int stop_show_user				=6700;//停止显示所有用户状态
    public final static int client_get_message			=6701;//客户端发来自己接收到信息
    public final static int show_video  			    =6702;

    public final static int system_inform				=6723;//登录或注册时的通知
    public final static int username_be_used			=6724;//用户名已被占用！（注册时使用）
    public final static int username_too_short			=6725;
    public final static int username_too_long			=6726;
    public final static int login_success				=6727;//能合法顺利登录了
    public final static int login_fail					=6728;//用户名密码错误
    public final static int register_success			=6729;//注册成功

    public final static int toast_tips                  =6730;

    public final static int apply_refresh_online_member=-4;//请求刷新页面


    public final static int battle_choice				=6900;//让客户端显示战斗选项一览
    public final static int character_choice			=6901;//让客户端显示角色选取界面
    public final static int in_online					=6800;//处于在线状态
    public final static int in_offline					=6804;//处于离线状态

    public final static int you_are_in_the_lobby       =7000;//返回大厅界面
    public final static int force_offline				=7000;//客户端下线了
    public final static int event_happen				=7001;//某事件发生了,强制锁定别人的自由行动输入输出
    public final static int event_end					=7002;//事件结束，解除别人的自由行动输入输出
    public final static int event_happen_N				=7003;//事件发生，不思考
    public final static int your_event					=7004;//你的事件
    //public final static int lock_action					=7004;//【图形界面客户端】防止恶意或过快点击输入
    //public final static int release_action				=7005;//【图形界面客户端】允许输入





    //#define refresh_state				7081//开始传输人物数据了
//#define please_reload				7082//重新传输
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
    public final static int refresh_state_weapon_H		=7110;//更新对手武器信息



    public final static int start_turn_settle			=7300;//开始回合结算
    public final static int start_another_turn			=7301;//本回合开始
    public final static int show_weapon_list			=7302;//【图形界面客户端】显示武器栏武器和效果
    public final static int get_c_number				=7303;//【图形界面客户端】想要装备武器，让他发送来想要装备第几张手卡
    public final static int get_we_number				=7304;//【图形界面客户端】获取发动第几个效果
    public final static int show_Buff_list				=7305;//【图形界面客户端】显示buff栏效果
    public final static int chooseYN					    =7306;//【图形界面客户端】做“是否”选择，一般后面都跟一句问话
    public final static int whether_fight_back			=7307;//是否反击？你是否行动？（0:不行动；1:发动手卡；2:发动个人效果；3:发动武器效果；4.加附效果）\n请选择：
    public final static int show_rivals_card_P			=7308;//显示对手所有手卡并抢夺
    public final static int show_enemy_list				=7309;//显示敌人目录
    public final static int show_alive_list				=7310;//显示存活者目录
    public final static int now_is_your_turn			=7311;//确认是你的回合
    public final static int whether_launch_hideeffect	=7312;//是否发动卡片隐效果？
    public final static int card_launch					=7313;//发动了卡片
    public final static int delete_which_weapon			=7314;//【图形界面客户端】丢弃哪一个武器？
    public final static int which_to_hatred				=7315;//对什么发动憎恨？？？get_w_number
    public final static int get_w_number				=7316;//选中了哪一个武器？


    public final static int android_phone				=9999;//安卓手机端
    public final static int win_console					=9998;//win32控制台版

    
}
