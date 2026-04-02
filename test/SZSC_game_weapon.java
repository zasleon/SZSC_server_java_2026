package test;

import java.util.ArrayList;
import java.util.List;

public class SZSC_game_weapon {
	
	
	
	public static SZSC_Buff get_player_weapon_buff(SZSC_player p1,int which_weapon,int which_effect) {
		for(SZSC_Buff buff:p1.buff)
			if(buff.check_source_type(SZSC_game_protocol.Buff_source_my_weapon_effect_fix))
				if(buff.get_source_which_item()==which_weapon)
					if(buff.get_source_which_effect()==which_effect)
						return buff;
		return null;
	}
	
	
	
	
	
	

	public static void refresh_weapon(SZSC_game this_room,SZSC_player p1)//刷新武器，个人武器增删后会使用
	{
		SZSC_game_transmit.weapon_change(this_room,p1);
	}

	public static List<Integer> get_weapon_weaponeffect(int weaponID) {
        List<Integer> effectIDs = new ArrayList<>();
        
        SZSC.General_Info general_Info=SZSC_game_dictionary.EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "weapon", "No", weaponID);
        
        int pointer=1;
        while(true) {
        	int effect_ID=general_Info.get_int("effect"+pointer++);
        	if(effect_ID==SZSC_protocol.code_none)
        		break;
        	effectIDs.add(effect_ID);
        }
        
        return effectIDs;
    }
	
	
	public static String equip_weapon(SZSC_game this_room,SZSC_player p1,int weapon_ID)//将所给的武器号choice进行装备
	{
		String result=SZSC_game_protocol.still_fight;
		
		if(p1.get_weapon_number()==p1.get_weapon_limit())
		{
			show("武器槽满！无法装备!");
			return result;
		}
		
		SZSC.Weapon new_weapon=new SZSC.Weapon(weapon_ID);
		p1.weapon.add(new_weapon);
		int which_weapon=p1.get_weapon_number()-1;
		
		List<Integer> player_Weapon_effects=get_weapon_weaponeffect(weapon_ID);
		
		set_player_weapon_Buff(this_room, p1, player_Weapon_effects, which_weapon);
		
		//进行客户端玩家状态刷新
		SZSC_game_transmit.weapon_change(this_room,p1);
		
		//广播
		String msg=p1.get_room_name()+"装备了武器: "+SZSC_game_dictionary.search_card(SZSC_game_protocol.p_name,weapon_ID);
		this_room.game_broadcast(msg);

		return result;
	}
	public static int check_arm_weapon_number(SZSC_player p1)//查看该角色装备了几张装备卡
	{
		
		return p1.weapon.size();
	}



	public static void random_arm_weapon(SZSC_game this_room,SZSC_player p1)//随机给他装一件备武器
	{
		int result=SZSC_game_general_function.getrandom(0, 9);
		
		equip_weapon(this_room,p1,result);
	}

	

	public static void delete_weapon_buff(SZSC_player p1,int which_weapon)//删除自己某把武器所有buff
	{
		
		for(int pointer=p1.buff.size()-1;pointer>=0;pointer--){
			SZSC_Buff current_buff=p1.buff.get(pointer);
			if(current_buff.check_source_type(SZSC_game_protocol.Buff_source_my_weapon_effect_fix)) 
			{
				
				int this_weapon_No=current_buff.get_source_which_item();
					
				if(this_weapon_No==which_weapon)//如果该buff来源为武器且是该武器
				{
					show("删除buff"+current_buff.get_ID());
					p1.buff.remove(pointer);//删除该buff
				}
				else {
					if(this_weapon_No>which_weapon)//该武器之后的武器编号都往前移动一个
						current_buff.set_source(SZSC_game_protocol.Buff_source_my_weapon_effect_fix, p1.get_player_No(),this_weapon_No-1);
				}
				
			}
		}
		
		
		
		
		
	}
	public static void delete_weapon_all(SZSC_game this_room,SZSC_player p1)//删除该玩家全部武器
	{
		int weapon_number=p1.get_weapon_number();
		p1.weapon.clear();
		while(weapon_number>0)
			delete_weapon_buff(p1,weapon_number--);
		

		SZSC_game_transmit.weapon_change(this_room,p1);

	}
	
	public static void destory_weapon(SZSC_game this_room,SZSC_player p1,int whichweapon) {
		if(!SZSC_game_judge.weapon_choice_valid(p1, whichweapon)) {
			show("destory_weapon 错误whichweapon   "+whichweapon);
			return;
		}
			
		delete_weapon(this_room,p1,whichweapon);
		refresh_weapon(this_room, p1);
	}
	
	public static void delete_weapon(SZSC_game this_room,SZSC_player p1,int whichweapon)//删除某个槽内的一把武器,该槽号为whichweapon
	{
		if(!SZSC_game_judge.weapon_choice_valid(p1, whichweapon))
		{
			show("whichweapon错误:"+whichweapon);
			return;
		}
		p1.weapon.remove(whichweapon);
		delete_weapon_buff(p1,whichweapon);//删除自己某把武器所有buff
		
		refresh_weapon(this_room,p1);
	}
	public static void delete_all_enemy_weapon(SZSC_game this_room,SZSC_player p1)//删除p1敌对势力的所有人的武器
	{
		for(SZSC_player player:this_room.players)
			if(player.not_none()&&player.get_camp()!=p1.get_camp())//若该位子上不为空//如果不是同一阵营
				delete_weapon_all(this_room,player);
	}
	
	//获取玩家第几把武器的武器ID
	public static int get_weapon_ID(SZSC_player p1,int which_weapon) {
		int result=SZSC_protocol.code_none;
		if(SZSC_game_judge.weapon_choice_valid(p1, which_weapon)) {
			result=p1.weapon.get(which_weapon).get_weapon_ID();
		}
		return result;
	}
	
	public static int get_weapon_effect_ID(int weapon_ID,int which_effect) {
		int result=SZSC_protocol.code_none;
		SZSC.General_Info general_Info=SZSC_game_dictionary.EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH,"weapon", "No", weapon_ID);
		result=general_Info.get_int("effect"+(which_effect+1));
		return result;
	}
	public static int get_weapon_effect_ID(SZSC_player p1,int which_weapon,int which_effect) {
		return p1.weapon.get(which_weapon).get_weapon_effect(which_effect);
	}
	
	
	
	public static SZSC_Buff get_weapon_effect(SZSC_player p1,int which_weapon,int which_effect)//获取该武器的buff,需要在之前判断是否存在
	{
		SZSC_Buff result=null;
		if(!SZSC_game_judge.weapon_choice_valid(p1, which_weapon))
			return null;
		
		if(!p1.weapon.get(which_weapon).is_valid()){
			p1.game_tips("当前武器为失效状态!无法发动该武器效果!");
			return null;
		}
		
		//获取该武器id
		int weapon_effect_ID=p1.weapon.get(which_weapon).get_weapon_ID();
		//获取武器id对应的效果列
		List<Integer> weapon_effects=get_weapon_weaponeffect(weapon_effect_ID);
		if(weapon_effects.size()<which_effect) {
			show("该武器没有该效果!  "+ which_effect);
			return null;
		}
		//从效果列中获取对应效果id
		int Buff_ID=weapon_effects.get(which_effect);
		
		
		//先通过字典查询该武器的该效果槽的效果是否为buff类效果，如果是则根据玩家个人信息内buff栏中提取，否则按字典里进行提取
		
		result=SZSC_game_Buff_process.get_buff_data(Buff_ID);
		if(result==null)
			return null;
		if(!result.is_buff) {
			show("该buff_ID并非buff"+Buff_ID+"  "+weapon_effect_ID+"    "+which_effect);
			return result;
		}
		
		for(SZSC_Buff current_buff:p1.buff)
			if(current_buff.check_source_type(SZSC_game_protocol.Buff_source_my_weapon_effect_fix))
				if(current_buff.get_source_which_item()==which_weapon)
				{
					if(current_buff.get_ID()==Buff_ID){//找到对应装备的对应效果了
						return current_buff;
					}
					
				}

		//正常来说应该在上面那个for循环里找到对应buff并且返回，如果执行到这里，必然没找到，为异常
		show("武器buff获取失败!  "+which_effect);
		return null;
	}
	public static void set_player_weapon_Buff(SZSC_game this_room,SZSC_player p1,List<Integer> Buff_IDs,int which_weapon)//根据特定id效果赋予buff
	{
		//show("添加武器buff ID"+Buff_ID);
		List<SZSC_Buff> buffs=SZSC.getNewArrayList();
		
		int which_effect=0;
		for(int buff_ID:Buff_IDs) {
			SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(buff_ID);
			if(buff!=null&&buff.is_buff) {
				buff.set_source(SZSC_game_protocol.Buff_source_my_weapon_effect_fix, p1.get_player_No(), which_weapon);
				buff.set_Source_which_effect(which_effect);
				buffs.add(buff);
			}
			which_effect++;
		}
		//show("buff_effect "+buff.get_effect_ID()+"    value   "+buff.get_effect_value());
		
		//查询buff_id在总表内对应的具体数据
		SZSC_game_Buff_process.player_add_Buff(this_room,buffs,p1,p1);
	}
	









	

	public static void show(String msg) {
		SZSC_service.show(msg);
	}
}
