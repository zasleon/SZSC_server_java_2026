package test;

import java.util.ArrayList;
import java.util.List;



//用于存放类的定义
public class SZSC {
	private static void show(String msg) {
		SZSC_service.show(msg);
	}
	
	
	//从general表中对应No获取到对应列的数据
	static class General_Info{
		
		List<String> column_name;
		List<String> value;
		public General_Info() {
			column_name=new ArrayList<>();
			value=new ArrayList<>();
		}
		public void add(String column_name,String value){
			this.column_name.add(column_name);
			this.value.add(value);
		}
		
		private int get_target_place(String request_name) {
			int result=SZSC_protocol.code_none;
			for(int i=0;i<column_name.size();i++)
				if(request_name.equals(column_name.get(i)))
				{
					result=i;
					break;
				}
			return result;
		}
		
		
		public String get_string(String request_name) {
			String result="";
			
			int which_one=get_target_place(request_name);
			if(which_one==SZSC_protocol.code_none)
			{
				show("get_string没找到对应目标   :"+request_name);
			}
			else {
				if(value.get(which_one).length()>0)
					result=value.get(which_one);
			}
			
			return result;
		}
		public int get_int(String request_name) {
			int result=SZSC_protocol.code_none;
			
			int which_one=get_target_place(request_name);
			if(which_one==SZSC_protocol.code_none)
			{
				show("get_int没找到对应目标   :"+request_name);
			}
			else {
				if(value.get(which_one).length()>0)
					result=(int)Double.parseDouble(value.get(which_one));
			}
			
			return result;
		}
		public float get_float(String request_name) {
			float result=SZSC_protocol.code_none;
			
			int which_one=get_target_place(request_name);
			if(which_one==SZSC_protocol.code_none)
			{
				show("get_float没找到对应目标   :"+request_name);
			}
			else {
				if(value.get(which_one).length()>0)
					result=Float.parseFloat(value.get(which_one));
			}
			
			return result;
		}
		public boolean column_exist(String request_name) {
			boolean result=false;
			if(SZSC_protocol.code_none!=get_target_place(request_name))
				result=true;
			return result;
		}
		
		
		
		
	}
	
	public static class Asset{
		private final int rowid;
		private final int user_ID;
		private final int kind;
		private final int code_number;
		private int mount;
		//private Effect[] effect;
		public Asset(int rowid,int user_ID,int kind,int code_number) {
			//effect=new Effect[effect_limit];
			this.rowid=rowid;
			this.user_ID=user_ID;
			if(kind==SZSC_protocol.code_none)
			{
				show("设置类型默认值必须为kind_effect");
				this.kind=SZSC_asset_process.kind_character_effect;
			}
			else
				this.kind=kind;
			this.code_number=code_number;
			this.mount=0;
		}
		public Asset() {
			this.rowid=SZSC_protocol.code_none;
			this.user_ID=SZSC_protocol.code_none;
			this.kind=SZSC_protocol.code_none;
			this.code_number=SZSC_protocol.code_none;
			this.mount=0;
		}
		
		public void set_mount(int mount) {
			this.mount=mount;
		}
		public int get_rowid() {
			return rowid;
		}
		public int get_user_ID() {
			return user_ID;
		}
		public int get_kind() {
			return kind;
		}
		public int get_code_number() {
			return code_number;
		}
		public int get_mount() {
			return mount;
		}
		
	} 

	//单个效果具有“类型”（主词条(角色的第一个词条)/固定词条/可更换词条）、效果号码
	static class Effect{
		private int effect;
		private int kind;
		public Effect(int kind,int effect) {
			this.effect=effect;
			this.kind=kind;
		}
		public Effect() {
			effect=SZSC_protocol.code_none;
			kind=SZSC_protocol.code_none;
		}
		public int get_effect() {
			return effect;
		}
		public int get_kind() {
			return kind;
		}
		public void set_effect(int code) {
			 effect=code;
		}
		public void set_kind(int code) {
			kind=code;
		}
		public void set(int kind_code,int effect_code) {
			effect=effect_code;
			kind=kind_code;
		}
		
	}

	static class Character{
		private final int rowid;
		private final int user_ID;
		private String name;
		
		private float attack;
		private float bloodlimit;
		
		
		//effect[]的首项词条属性就是该物品属性
		
		private Effect[] effect;
		public Character(int rowid,int user_ID,String name,Effect[] e) {
			if(e!=null)
				effect=e;
			else 
			{
				effect=new Effect[SZSC_protocol.abilitylimit];
				for(int i=0;i<SZSC_protocol.abilitylimit;i++)
					effect[i]=new Effect(SZSC_asset_process.kind_character_effect,SZSC_protocol.code_none );
			}
			this.name=name;
			this.rowid=rowid;
			this.user_ID=user_ID;
		}
		public Character(Asset asset,Effect[] e) {
			if(asset==null)
				show("character创建失败!asset为空!");
			if(e!=null)
				this.effect=e;
			else 
			{
				this.effect=new Effect[SZSC_protocol.abilitylimit];
				for(int i=0;i<SZSC_protocol.abilitylimit;i++)
					this.effect[i]=new Effect( SZSC_asset_process.kind_character_effect,SZSC_protocol.code_none);
			}
			this.name="untitled";
			this.rowid=asset.get_user_ID();
			this.user_ID=asset.get_user_ID();
			int code_number=asset.get_code_number();
			int kind=asset.get_kind();
			
			
			//如果不是单纯词条，则根据角色定位进行固定词条确认
			if(asset.get_kind()!=SZSC_asset_process.kind_character_effect)
			{
				Character character=SZSC_asset_process.get_character_effect(asset);
				
				if(copy(character))
					return;//如果顺利找到对应id，进行赋值后退出，否则说明赋值失败，进行通用清空赋值
			}
			
			//根据通用词条新建角色
			this.effect[0]=new Effect(kind,code_number);
			
				
		}
		public void set_attack_blood(float attack,float bloodlimit) {
			this.attack=attack;
			this.bloodlimit=bloodlimit;
		}
		public float get_attack() {
			return attack;
		}
		public float get_bloodlimit() {
			return bloodlimit;
		}
		
		public boolean copy(SZSC.Character character) {
			if(character==null) {
				return false;
			}
			this.name=character.get_name();
			this.effect=character.get_effect();
			return true;
		}
		public Effect[] get_effect() {
			return effect;
		}
		
		
		
		public String get_name() {
			return name;
		}
		
		public int get_rowID() {
			return rowid;
		}
		public int get_user_ID() {
			return user_ID;
		}
		public void set_ability(int i,int kind_ID,int effect_ID) {
			if(i<0||i>SZSC_protocol.abilitylimit)
			{show("查看错误对象 "+i);return;}
			effect[i].set_kind(kind_ID);
			effect[i].set_effect(effect_ID);
		}
		public void set_kind(int i,int code) {
			if(i<0||i>SZSC_protocol.abilitylimit)
			{show("查看错误对象 "+i);return;}
			effect[i].set_kind(code);
		}
		public void set_effect(int i,int code) {
			if(i<0||i>SZSC_protocol.abilitylimit)
			{show("查看错误对象 "+i);return;}
			effect[i].set_effect(code);
		}
		public int get_kind(int i) {
			if(i<0||i>SZSC_protocol.abilitylimit)
			{show("查看错误对象 "+i);return SZSC_protocol.code_none;}
			return effect[i].get_kind();
		}
		public int get_effect(int i) {
			if(i<0||i>SZSC_protocol.abilitylimit)
			{show("查看错误对象 "+i);return SZSC_protocol.code_none;}
			return effect[i].get_effect();
		}
		
	} 
		
	static class Asset_bag{
		private int result;
		private int total_mount;
		private Asset[] asset;
		public Asset_bag(int total_mount,int user_ID) {
			result=SZSC_protocol.SZSC_purchase_result_success;
			this.total_mount=total_mount;
			asset=new Asset[total_mount];
			
		}
		public void set_result(int value) {
			result=value;
		}
		public int get_result() {
			return result;
		}
		
		public void set_Asset(int which,int rowid,int user_ID,int kind,int code_number,int mount) {
			asset[which]=new Asset(rowid,user_ID, kind, code_number);
			asset[which].set_mount(mount);
		}
		public Asset get_asset(int which) {
			if(which>=total_mount)
			{
				show("获取Asset_bag信息内部asset错误 "+which);
				return null;
			}
			return asset[which];
		}
		public int get_total_mount() {
			return total_mount;
		}
	}
	static class Character_bag{
		private int total_mount;
		private Character[] characters;
		public Character_bag(int total_mount,int user_ID) {
			this.total_mount=total_mount;
			characters=new Character[total_mount];
		}
		public void set_Character(int which,int rowid,int user_ID,String name,Effect[] effects) {
			characters[which]=new Character(rowid,user_ID, name, effects);
		}
		public Character get_characters(int which) {
			if(which>=total_mount)
			{
				show("获取Character_bag信息内部Character错误 "+which);
				return null;
			}
			return characters[which];
		}
		public int get_total_mount() {
			return total_mount;
		}
	} 
	
	public static Launch_Info get_new_Launch_Info(int card_No) {
		return new Launch_Info(card_No);
	}
	public static Launch_Info get_new_Launch_Info(SZSC_Buff buff) {
		return new Launch_Info(buff);
	}
	public static class Launch_Info{
		
		//创建该类对象时需输入的参数，general表格内的No编号
		private int card_No=SZSC_protocol.code_none;//卡号
		//创建该类对象时输入的玩家已持有的buff参数，如果不是创建时输入，则根据卡号确定buff
		private SZSC_Buff buff=null;
		//根据卡号，查询excel法典表格后，系统进行自动配置
		private String launch_source_type="";//发动类别（武器效果/自身效果/卡片效果）
		
		private String condition_name="";
		private float condition_value=SZSC_protocol.code_none;
		//需函数手动配置
		private int which_card=SZSC_protocol.code_none;//若发动类别为手卡，记录发动哪张卡
		private int which_weapon=SZSC_protocol.code_none;//若发动类别为武器，记录发动哪个武器
		private int which_effect=SZSC_protocol.code_none;//自身效果/武器的第 几 个效果
		
		private String effect_name="";
		private float effect_value=SZSC_protocol.code_none;
		
		public Launch_Info(SZSC_Buff buff) {
			
			this.buff=buff;
			if(buff==null) {
				show("状态包内 buff 初始化出错! buff为null!");
			}
			//show("从buff获取发动信息   "+buff.get_ID());
			this.card_No=buff.get_ID();
			get_general_info(card_No);
			
			//show("从buff获取发动信息结束");
		}
		public Launch_Info(int card_No) {
			this.card_No=card_No;
			get_general_info(card_No);
		}
		
		
		private void get_general_info(int card_No) {
			SZSC.General_Info general_Info=SZSC_game_dictionary.get_card_info(card_No);
			
			this.condition_name=general_Info.get_string("condition_name");
			this.condition_value=general_Info.get_float("condition_value");
			this.launch_source_type=general_Info.get_string("type");
			this.effect_name=general_Info.get_string("effect_name");
			this.effect_value=general_Info.get_float("effect_value");
			if(general_Info.get_string("是否buff").equals(SZSC_game_protocol.is_buff))
				if(buff==null) {
					
					buff=SZSC_game_Buff_process.get_buff_data(card_No);
					this.condition_name=buff.get_condition_ID();
					this.condition_value=buff.get_cost_value();
					this.launch_source_type=buff.get_type();
				}
			
		}
		public SZSC_Buff get_buff() {
			return this.buff;
		}
		
		public String get_condition_name() {
			return condition_name;
		}
		public String get_effect_name() {
			return effect_name;
		}
		public float get_effect_value() {
			return effect_value;
		}
		public void set_effect_info(String effect_name,float effect_value) {
			this.effect_name=effect_name;
			this.effect_value=effect_value;
		}
		
		public float get_condition_value() {
			return condition_value;
		}
		public void set_condition_name(String condition_name){
			this.condition_name=condition_name;
		}
		public void set_which_card(int which_card) {
			this.which_card=which_card;
		}
		public int get_which_card() {
			return which_card;
		}
		
		public int get_card_No() {
			return card_No;
		}
		public void set_which_effect(int which_effect) {
			this.which_effect=which_effect;
		}
		
		public void set_weapon_info(int which_weapon,int which_effect) {
			this.which_weapon=which_weapon;
			this.which_effect=which_effect;
		}
		public int get_which_weapon() {
			if(buff!=null)
				return buff.get_source_which_item();
			return this.which_weapon;
		}
		public int get_which_effect() {
			if(buff!=null)
				return buff.get_source_which_effect();
			return this.which_effect;
		}
		public String get_launch_source_type() {
			return launch_source_type;
		}
		public void set_launch_source_type(String card_type) {
			this.launch_source_type=card_type;
		}
		
		public boolean is_buff() {
			if(buff==null)
				return false;
			return buff.is_buff;
		}
		
		public boolean is_card() {
			String situation_type=get_launch_source_type();
			switch(situation_type) {
				case SZSC_game_protocol.TYPE_launch_assist:
				case SZSC_game_protocol.TYPE_launch_effect:
				case SZSC_game_protocol.TYPE_launch_hide:
				case SZSC_game_protocol.TYPE_launch_weapon:
					return true;
			}
			return false;
		}
		
	}
	
	public static Event_info get_new_event_info(String event_type,float event_value,SZSC_player event_reactor,SZSC_player event_launcher_or_target) {
		return new Event_info(event_type, event_value, event_reactor, event_launcher_or_target);
	}
	public static <T> List<T> getNewArrayList() {
	    return new ArrayList<>();
	}
	
	public static class Event_info{
		String event_type="";
		float event_value=SZSC_protocol.code_none;
		float probability=100;//命中率
		SZSC_player event_reactor=null;
		SZSC_player event_target=null;
		public Event_info(String event_type,float event_value,SZSC_player event_reactor,SZSC_player event_launcher_or_target) {
			this.event_type=event_type;
			this.event_value=event_value;
			this.event_reactor=event_reactor;
			this.event_target=event_launcher_or_target;
		}
		public boolean launch_event() {
			if(event_type.equals(SZSC_game_protocol.someone_launch_effect))
				return true;
			return false;
		}
		
		public String get_type() {
			return event_type;
		}
		public float get_value() {
			return event_value;
		}
		public SZSC_player get_launcher() {
			return event_target;
		}
		public SZSC_player get_reactor() {
			return event_reactor;
		}
		public SZSC_player get_target() {
			return event_target;
		}
		public float get_probability() {
			return probability;
		}
		public void set_probability(float probability) {
			this.probability=probability;
		}
		
		int source_type=SZSC_protocol.code_none;
		int which_item=SZSC_protocol.code_none;
		int which_effect=SZSC_protocol.code_none;
		public void set_source(int source_type,int which_item,int which_effect) {
			this.source_type=source_type;
			this.which_item=which_item;
			this.which_effect=which_effect;
		}
		String source_specific_type="";
		public void set_source_specific(String source_specific_type) {
			this.source_specific_type=source_specific_type;
		}
		public String get_source_specific(String source_specific_type) {
			return this.source_specific_type;
		}
		
		public int get_source_type() {
			return source_type;
		}
		public int get_which_item() {
			return which_item;
		}
		
	}
	
	public static Result_info get_new_Result_info() {
		return new Result_info();
	}
	
	public static class Result_info{
		String result_type="";
		float result_value=SZSC_protocol.code_none;
		public Result_info(String type,float value) {
			this.result_type=type;
			this.result_value=value;
		}
		public Result_info() {
			this.result_type=SZSC_game_protocol.still_fight;
		}
		
		public String get_type() {
			return result_type;
		}
		public float get_value() {
			return result_value;
		}

		public void set_value(float result_value) {
			this.result_value=result_value;
		}
		public void set_type(String result_type) {
			this.result_type=result_type;
		}
	}
	
	
	// 奖品结果类
    public static class PrizeResult {
        private final int type;
        private final String name;
        private final int code_number;

        public PrizeResult(int type, String name,int code_number) {
            this.type = type;
            this.name = name;
            this.code_number = code_number;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }
        public int getcode() {
            return code_number;
        }
        
        
    }
    
    
    
    public static ability get_new_ability(int kind_ID,int effect_ID) {
    	return new ability(kind_ID, effect_ID);
    }
    
    public static class ability {

    	public ability() {
    		ini();
    	}
    	public ability(int kind_ID,int effect_ID) {
    		this.kind_ID=kind_ID;
    		this.effect_ID=effect_ID;
    	}
    	
    	private int kind_ID;
    	private int effect_ID;
    	
    	private void ini() {
    		effect_ID=SZSC_protocol.code_none;
    		kind_ID=SZSC_protocol.code_none;
    	}
    	
    	public void set_ability(int kind_ID,int effect_ID) {
    		this.kind_ID=kind_ID;
    		this.effect_ID=effect_ID;
    	}
    	
    	public int get_effect_ID() {
    		return effect_ID;
    	}
    	
    }
    public static class card {//单张卡设计
		
    	private int card_No;//拥有手卡
    	private int hideeffect;//隐效果
    	
    	public card(int card_No) {
    		//use=true;
    		this.card_No=card_No;
    		this.hideeffect=SZSC_game_dictionary.card_get_hide_effect(card_No);
    	}
    	
    	private void ini() {
    		//use=false;
    		card_No=SZSC_protocol.code_none;
    		hideeffect=SZSC_protocol.code_none;
    	}
    	
    	public int get_card_No() {
    		return card_No;
    	}
    	public int get_hide_effect() {
    		return hideeffect;
    	}
    	
    }
    public static class Weapon {

    	private boolean valid=true;
    	private int weapon_ID=SZSC_protocol.code_none;
    	List<Integer> effectIDs;
    	
    	public Weapon(int weapon_ID) {
    		this.weapon_ID=weapon_ID;
    		effectIDs = SZSC_game_weapon.get_weapon_weaponeffect(weapon_ID);
    		
    		if (effectIDs.isEmpty()) {
                SZSC_service.show("未找到武器! ID=" + weapon_ID);
                return;
            }
            
            for (int which_effect : effectIDs) {
            	//SZSC_service.show("武器效果" + (which_effect) );
            }
    		ini();
    	}
    	private void ini() {
    		this.valid=true;
    	}
    	
    	
    	public void set_weapon_ID(int value) {
    		weapon_ID=value;
    	}
    	
    	public int get_weapon_ID() {
    		return this.weapon_ID;
    	}
    	public int get_weapon_effect(int which) {
    		if(which<0||which>=effectIDs.size())
    			SZSC_service.show("?获取weapon 第 " +which+ " 个效果错误!");
    		return effectIDs.get(which);
    		
    	}
    	
    	public boolean is_valid() {
    		return this.valid;
    	}
    	
    	public void set_valid(boolean value) {
    		this.valid=value;
    	}
    	
    	
    }
	
}
