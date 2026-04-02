package test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SZSC_player {
	
	
	public SZSC_player() {
		ini();
	}
	public void reset() {
		ini();
	}
	private void ini() {
		
		interface_state=SZSC_game_protocol.player_interface_state_return_normal;
		player_type=SZSC_protocol.SZSC_real_player;//默认为活人，如果是机器人需要手动修改
		
		alive=true;
		myturn=false;
		self_game_state=SZSC_protocol.SZSC_normal_online;
		card_limit=SZSC_protocol.cardlimit;
		weapon_limit=SZSC_protocol.weaponlimit;
		
		fight_chance=0;
		attacktime_turn=0;
		i_soon_die=false;
		
		buff.clear();
		//手卡全部清空
		card.clear();
		//个人效果全部清空
		ability.clear();
		weapon.clear();//武器全部清空
	}
	
	private Client c1;
	
	private boolean host;//是否是房主
	
	public	int		member_No;

	private int		player_type;//活人或机器人
	private int		self_game_state;//当前游戏玩家状态,正常在线或掉线，在游戏结束回到房间时，期间结算由房主执行，重置全部为正常在线，即便位置上为空
	private int		camp;//阵营
	private Date	enter_time;//进入房间时间
	private int		think_time;//已经思考的时间
	private int		interface_state;//用户界面显示状态
	private boolean	whether_end_turn;//是否主动结束该回合
	
	private int 	card_limit;
	private int		weapon_limit;

	

	private String	character_name;//角色名称
	private int		character_ID;//角色序号
	private int		player_No;//第几个玩家
	private boolean	myturn;//是否是当前角色的回合？是的为1，不是为0
	private boolean	alive;//是否存活,true为活，false为死
	public float	bloodlimit;//血量上限
	public float	blood;//当前血量
	public float	attack;
	
	
	public List<SZSC_Buff> buff=SZSC.getNewArrayList();//个人buff，助攻效果，自身异常状态、自身获得加持等,默认为空值SZSC_Buff_none
	public List<SZSC.ability> ability= SZSC.getNewArrayList();//abilitylimit
	public List<SZSC.card> card= SZSC.getNewArrayList();//cardlimit
	public List<SZSC.Weapon> weapon= SZSC.getNewArrayList();
	
	;//int storage_weapon[10];//破败披风效果专属，统计自己有多少把武器，可以不停与装备武器切换，即披风内存放了大量武器，但只能抽出其中两个进行使用【有待设计】
    ;//int speed;//人物速度【有待设计】
	public int		fight_chance;//自己当前拥有的普攻次数
	private int		whether_in_attack;//是否处于搏斗中,在普攻、反击中+1、-1
	
	private boolean	i_soon_die;//我是否即将死亡？
	
	private int	attacktime_turn;	;//此回合自己普攻成功次数

	private JSON_process listen(boolean main_listen) {
		
		JSON_process msg=null;
		if(human()) {
			String msg_content=c1.listen(main_listen);
			if(!msg_content.isBlank())
				msg=new JSON_process(msg_content);
		}
		return msg;
	}
	public void lock_player_listen() {
		if(human())
			c1.lock_listen();
	}
	public void unlock_player_listen() {
		if(human())
			c1.unlock_listen();
	}
	
	public JSON_process game_listen() {
		return listen(false);
	}
	public JSON_process main_listen() {
		return listen(true);
	}
	
	public boolean offline() {//判断是否掉线,机器人肯定不会掉线
		boolean result=false;
		if(self_game_state==SZSC_protocol.SZSC_force_offline)
			result=true;
		if(human())//只有活人有client对应，否则为null
			if(c1.network_error()) {
				result=true;
				self_game_state=SZSC_protocol.SZSC_force_offline;
			}
		return result;
	}
	
	public int get_server_ID() {//获取对应服务器槽位
		int result=SZSC_protocol.code_none;
		if(human())
		{
			result=c1.get_client_unique_identity();
		}
		
		return result;
	}
	public int get_DB_user_ID() {
		return SZSC_asset_command.get_user_ID(c1.get_client_name());
		
	}
	
	public void set_game_state(int value) {
		self_game_state=value;
		if(human()) {
			c1.setclientstate(SZSC_protocol.SZSC_in_game);
		}
	}
	
	
	public void set_interface_state(int value) {
		interface_state=value;
	}
	public int get_interface_state() {
		return interface_state;
	}
	public void set_client_state(int value) {
		if(human())
			c1.setclientstate(value);
	}
	public boolean whether_host() {
		return host;
	}
	public void set_type(int value) {
		player_type=value;
	}
	public void set_character_name(String character_name) {
		this.character_name=character_name;
	}
	
	public void set_host(boolean result) {
		host=result;
	}
	
	public float get_blood() {
		return this.blood;
	}
	
	public void set_blood(float blood) {
		this.blood=blood;
	}
	public void change_attack(float value) {
		this.attack+=value;
	}
	public void change_blood(float value) {
		this.blood+=value;
	}
	
	//初始化时用
	public void set_blood_attack(float blood,float attack) {
		
		this.attack=attack;
		this.blood=blood;
		this.bloodlimit=blood;
	}
	
	public boolean bot() {
		boolean result=true;
		switch(player_type) {
		case SZSC_protocol.SZSC_none_player:
			result=false;
			break;
		case SZSC_protocol.SZSC_real_player:
			result=false;
			break;
		case SZSC_protocol.SZSC_wood_robot:
			break;
			
		}
		return result;
	}
	public int check_player_type() {
		return player_type;
	}
	public boolean not_none() {//该位子是否为空
		if(check_player_type()==SZSC_protocol.SZSC_none_player)
			return false;
		return true;
	}
	public boolean human() {
		if(check_player_type()==SZSC_protocol.SZSC_real_player)
			return true;
		return false;
	}
	
	public int get_card_limit() {
		return this.card_limit;
	}
	public int get_weapon_limit() {
		return this.weapon_limit;
	}
	
	
	
	public void send(JSON_process reply_msg) {
		reply_msg.add("my_ID", player_No);
		reply_msg.add("interface_state", get_interface_state());
		if(human())
			SZSC_service.send_msg(c1, reply_msg);
	}
	public void send_signal(int signal) {
		
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal", signal);
		send(reply_msg);
	}
	public void set_client(Client client) {
		reset();
		c1=client;
		
		set_type(SZSC_protocol.SZSC_real_player);//变为活人
	}
	
	public String get_name() {
		return character_name;
	}
	public String get_room_name() {
		return " "+(player_No+1)+"号玩家: "+character_name+" ";
	}
	public boolean is_alive() {
		return this.alive;
	}
	public boolean soon_die() {
		return i_soon_die;
	}
	public void set_soon_die() {
		i_soon_die=true;
	}
	public void cancel_soon_die() {
		i_soon_die=false;
	}
	public void attack_success() {
		attacktime_turn++;
	}
	public int get_attack_success_time() {
		return attacktime_turn;
	}
	public void set_attack_success_time(int value) {
		attacktime_turn=value;
	}
	public void set_whether_my_turn(boolean value) {
		myturn=value;
	}
	
	
	public boolean check_player(SZSC_player p1) {//是否是同一人
		if(p1==null)
			return false;
		if(this.player_No==p1.get_player_No())
			return true;
		return false;
	}
	public void set_character(int character_ID,String character_name) {
		this.character_ID=character_ID;
		this.character_name=character_name;
		
	}
	
	
	public int get_buff_number() {//统计自身共有多少buff
		
		return this.buff.size();
	}
	public boolean in_attack_event() {
		if(this.whether_in_attack>0)
			return true;
		return false;
	}
	public boolean whether_my_turn() {
		return myturn;
	}
	public int get_camp() {
		return camp;
	}
	public void set_camp(int value) {
		camp=value;
	}
	
	public void attack_event(boolean involve) {
		if(involve)
			this.whether_in_attack++;
		else
			this.whether_in_attack--;
	}
	public int get_weapon_number()//查看该角色装备了几张装备卡
	{
		return weapon.size();
	}
	public void set_die() {
		alive=false;
	}
	public void set_turn_end(boolean value) {
		whether_end_turn=value;
	}
	public boolean choose_turn_end() {
		return whether_end_turn;
	}
	
	public int get_player_No() {
		return player_No;
	}
	public int get_type() {
		return player_type;
	}
	
	public void set_self_game_state(int value) {
		this.self_game_state=value;
	}
	public int get_self_game_state() {
		return self_game_state;
	}
	public boolean check_game_state(int value) {
		if(self_game_state==value)
			return true;
		return false;
	}
	
	public void game_tips(String msg) {
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal", SZSC_game_protocol.Signal_game_tips);
		reply_msg.add("content", msg);
		send(reply_msg);
	}
	
	public static void show(String msg) {
		SZSC_service.show(msg);
	}
	public Date get_enter_time() {
		return enter_time;
	}
	public void set_enter_time(Date date) {
		enter_time=date;
	}
	
	public String get_user_name() {
		String result_name="???";
		if(human())
			result_name= c1.get_client_name();
		return result_name;
	}
	public void set_player_No(int player_No) {
		this.player_No=player_No;
	}
	public SZSC_Buff get_selfeffect(int which_effect) {
		for(SZSC_Buff this_buff:buff) {
			if(this_buff.check_source_type(SZSC_game_protocol.Buff_source_my_character_effect))
				if(this_buff.get_source_which_effect()==which_effect)
					return this_buff;
		}
		return null;
	}
	public int get_ability_number()//获取该角色拥有自身效果个数
	{
		return ability.size();
	}
	public void add_ability(SZSC.ability this_ability) {
		if(this_ability.get_effect_ID()==SZSC_protocol.code_none)
			return;
		ability.add(this_ability);
	}
	
	
}
