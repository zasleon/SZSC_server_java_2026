package test;

import java.util.List;

public class SZSC_game_Buff_process {
	private static void show(String msg)
	{
		SZSC_service.show(msg);
	}
	

	
	
	//仅当【效果结算后】或【流程不同时间阶段】时才会触发reduce_Buff_duration函数
	//比如，效果伤血结算后触发“本次伤血效果结束”、普攻伤血结算后触发“本次普攻结束”
	//特殊：在某次普攻宣言成功后，触发“本次事件结束”将之前所有连锁事件强制清空
	//本次事件结束后触发“本次事件结束”，回合结束时触发“本回合结束”，即将轮到某个人时触发“此人轮回结束”
	//对剩余时间进行-1，如果-1后小于等于0则删除该buff,如果whether_all为true，则全部删除，不只是SZSC_maintain_Buff持续性buff
	//每次效果发动后触发“连锁层结束”,对有“连锁层”限制的buff进行消除（如果当前回退连锁层小于等于buff连锁层）
	public static void reduce_Buff_duration(SZSC_game this_room,SZSC_player p1,SZSC.Event_info event_info)
	{
		String duration_type=event_info.get_type();
		
		boolean whether_change=false;
		//从后往前查buff，为了remove时不会出现差错
		for(int pointer=p1.buff.size()-1;pointer>=0;pointer--) {
			SZSC_Buff current_buff=p1.buff.get(pointer);
			if(current_buff.got_chain_level_limit())
				if(this_room.locktime!=current_buff.got_chain_level())//非当前连锁层的buff效果不作任何处理
					continue;
			if(current_buff.check_duration_type(duration_type))
			{
				current_buff.reduce_duration();
				
				if(current_buff.duration_no_remain())
				{
					p1.buff.remove(pointer);//删除buff
					whether_change=true;
				}
			}
			//如果是回合结束，重置带有回合限制次数
			if(duration_type.equals(SZSC_game_protocol.Time_turn_end))
				switch(current_buff.get_limit_type()) {
					case "自己回合":
					case "1回合":{
						current_buff.reset_use_time_reamin();
					}
						
	
				}
		}
		if(whether_change)
			SZSC_game_transmit.Buff_change(this_room,p1);
	}
	
	
	//清除带连锁层的buff
	public static void chain_level_erase(SZSC_game this_room,SZSC_player p1) {
		for(int pointer=p1.buff.size()-1;pointer>=0;pointer--) {
			SZSC_Buff current_buff=p1.buff.get(pointer);
			if(current_buff.got_chain_level_limit())//如果当前buff为连锁层限制buff
				if(this_room.locktime==current_buff.got_chain_level())//对当前连锁层的buff效果进行清除
					p1.buff.remove(pointer);
		}
		
	}
	
	public static void event_end(SZSC_game this_room,String event_name) {
		SZSC.Event_info event_info=new SZSC.Event_info(event_name, 0, null, null);
		for(SZSC_player player:this_room.players)
			reduce_Buff_duration(this_room, player,event_info);
	}
	//单体玩家的时间过期，例如“轮回”
	public static void event_end(SZSC_game this_room,String event_name,SZSC_player player) {
		SZSC.Event_info event_info=new SZSC.Event_info(event_name, 0, null, null);
		
		reduce_Buff_duration(this_room, player,event_info);
	}
	

	//被动buff触发
	public static String activate_Buff(SZSC_game this_room,SZSC_player player_target,SZSC_player player_launcher,SZSC.Event_info event_info)//player_target个人遇到事件,player_launcher施加的，player_launcher为空则为自己施加，被动激活
	{
		String result=SZSC_game_protocol.still_fight;

		boolean force_end=false;
		String tmp_result="";
		
		if(player_target!=null)
		for(SZSC_Buff current_buff:player_target.buff)
			if(!current_buff.is_positive()) {//如果该buff栏是被动触发，进行触发
				
				//如果是自身效果而此时角色被沉默时，无法发动
				if(current_buff.get_source_type()==SZSC_game_protocol.TYPE_source_selfeffct)
				{
					if(SZSC_game_judge.character_is_silent(player_launcher))
						continue;
				}
				
				
				if(SZSC_game_judge.whether_activate_buff(this_room,player_target,event_info,current_buff,false)){//如果事件满足发动buff前置条件
					SZSC.Launch_Info launch_Info=SZSC.get_new_Launch_Info(current_buff);
					
					if(SZSC_game_judge.judge_launch_condition(this_room,event_info,player_launcher,player_target,launch_Info))	
					{
						
						show("触发被动 "+current_buff.get_ID());
						//if(!SZSC_game_judge.whether_immune_this_effect(this_room,player_target,event_info))//如果不能免疫该buff，发动效果
						{
							SZSC_game_player_choose.lock(this_room, player_launcher);
							result=SZSC_game_effect.launch_effect(this_room,event_info,player_target,player_launcher,launch_Info);
							
							SZSC_game_player_choose.unlock(this_room, player_launcher);
							if(SZSC_game_judge.event_force_end(result)) {
								force_end=true;
								tmp_result=result;
							}
							
						}
						//else
							//return SZSC_game_protocol.i_immune_effect;//如果免疫该效果，则
					}
				}
			}
		

		
		
		if(force_end)
			return tmp_result;

		return result;
	}





	
	

	public static SZSC_Buff got_this_Buff_place(SZSC_player p1,int Buff_ID,String effect_type,int which_one)//检索是否持有这个buff效果，如果有，返回buff，如果没有，返回null
	{
		SZSC_Buff result=null;
		for(SZSC_Buff current_buff:p1.buff)
		{
			if(current_buff.check_effect_type(effect_type))//如果是对应的人物第几个效果/第几个武器
				if(current_buff.get_ID()==Buff_ID&&current_buff.get_source_which_effect()==which_one) {
					result=current_buff;
					break;
				}
		}
		if(result==null)
			show("没找到对应buff   "+Buff_ID+"  "+effect_type+"  "+which_one);
		
		return result;
	}

	public static int got_this_Buff_place(SZSC_player p1,int Buff_ID)//检索是否持有这个buff效果，如果有，返回所在第几栏，是个大于-1的值，如果没有，返回-1
	{
		int result=SZSC_protocol.code_none;
		int pointer=0;//当前查过的buff数量
		for(SZSC_Buff current_buff:p1.buff) {
			if(current_buff.get_ID()==Buff_ID)
				result=pointer;
			pointer++;
		}
		return result;
		
		
	}


	public static void player_add_Buff(SZSC_game this_room,List<SZSC_Buff> buff,SZSC_player player_target,SZSC_player player_launcher) {
		for(SZSC_Buff this_buff:buff)
			add_Buff(this_room, this_buff, player_target, player_launcher);
		SZSC_game_transmit.Buff_change(this_room,player_target);
	}
	public static void player_add_Buff(SZSC_game this_room,SZSC_Buff buff,SZSC_player player_target,SZSC_player player_launcher) {
		
		add_Buff(this_room, buff, player_target, player_launcher);
		SZSC_game_transmit.Buff_change(this_room,player_target);
	}

	private static void add_Buff(SZSC_game this_room,SZSC_Buff buff,SZSC_player player_target,SZSC_player player_launcher)//添加buff
	{
		//查看buff的效果id是否为状态类buff
		//获取buff的effect_name
		String effect_name=buff.get_effect_ID();
		if(effect_name.equals(SZSC_game_protocol.Buff_unmovable)) {
			SZSC.Event_info event_info=SZSC.get_new_event_info(SZSC_game_protocol.i_will_be_effect_unmovable, 0, player_target, player_launcher);
			if(SZSC_game_judge.whether_immune_this_effect(this_room, player_target, event_info))
				return;
		}
		
		if(buff.got_chain_level_limit())//标记连锁层
			buff.set_chain_level(this_room.locktime);
			
		
		//查询effect表内对应的
		//如果是，进行判断是否免疫该效果

		boolean exist_buff=false;
		switch(buff.get_add_type())//查询该Buff_ID是否具有重置效果,或叠加效果
		{
			case SZSC_game_protocol.Buff_add_type_independent:
				{
					//只有独立buff需要增加一个新buff，其他都是覆盖原有效果
					player_target.buff.add(buff);
					exist_buff=true;
				}
				break;
			case SZSC_game_protocol.Buff_add_type_overlay://叠加
				{
					
					for(SZSC_Buff current_buff:player_target.buff) {
						if(current_buff.get_ID()==buff.get_ID()){//如果存在相同buff，叠加，
							if(buff.is_token()) {//如果是token，则堆叠token数量即可，目前暂无其他需要堆叠额外处理的buff效果，暂时不做深入
								int current_token_value=current_buff.get_token_value();
								int get_token_value=buff.get_token_value();
								current_buff.set_token_value(current_token_value+get_token_value);
							}
							
							
							exist_buff=true;
							break;
						}
					}
					
					
					
				}
				break;
			case SZSC_game_protocol.Buff_add_type_reset://重置buff，如果重置数量比原来的小，则不重置
				{
					for(SZSC_Buff current_buff:player_target.buff) {
						//如果存在相同buff，进行重置
						if(current_buff.get_ID()==buff.get_ID()) {
							exist_buff=true;
							
							//如果重置数量比原来的大，重置，否则不重置
							if(current_buff.get_duration_reamin()<buff.get_duration_remain()||current_buff.get_use_time_reamin()<buff.get_use_time_reamin())
								current_buff=buff;
							//如果找到相同buff，必然只有一个，不用进行后续搜索
							break;
						}
					}
				}
				break;
		}
		if(!exist_buff) //如果不存在，则新增
			player_target.buff.add(buff);

		
		
		
	}
	
	public static boolean whether_buff(int card_No) {
		SZSC_Buff this_buff=get_buff_data(card_No);
		
		return this_buff.is_buff;
		
	}
	
	//获取buff数据，如果effect_ID为空，则默认为buff_ID
	public static SZSC_Buff get_buff_data(int Buff_ID) {
		SZSC_Buff result=new SZSC_Buff();
		result.set_ID(Buff_ID);
		
	    SZSC.General_Info general_Info=SZSC_game_dictionary.get_card_info(Buff_ID);
    	if(general_Info==null) {
    		show("buff 获取出错  "+Buff_ID);
    		return null;
    	}
    	
        //找到了
    	boolean is_buff=general_Info.get_string("是否buff").equals("是");
    	result.set_buff(is_buff);//表示该项是不是buff
    	
    	{
	    	String buff_name=general_Info.get_string("name");
	    	String whether_positive=general_Info.get_string("主被动");
	    	
	    	String type=general_Info.get_string("type");
	    	
	    	//int trigger=excel_file.getInt(row, trigger_column);
	    	//int chain=excel_file.getInt(row, chain_column);
	    	String token=general_Info.get_string(SZSC_game_protocol.List_token);
	    	int first_token_value=general_Info.get_int(SZSC_game_protocol.List_first_remain);
	    	String add_type=general_Info.get_string(SZSC_game_protocol.List_add_type);
	    	String duration_type=general_Info.get_string(SZSC_game_protocol.List_duration_type);
	    	int duration_value=general_Info.get_int(SZSC_game_protocol.List_duration_first_remain_value);
	    	boolean chain_level_limit=general_Info.get_string("触发限制类型").equals("连锁层");
	    	
	    	
	    	String limit_type=general_Info.get_string("限制类型");
	    	int limit_value=general_Info.get_int("限制次数");
	    	String condition_name=general_Info.get_string("condition_name");
	    	float condition_value=general_Info.get_float("condition_value");
	    	String cost_condition_name=general_Info.get_string("cost_condition_name");
	    	float cost_condition_value=general_Info.get_float("cost_condition_value");
	    	String cost_name=general_Info.get_string("cost_name");
	    	float cost_value=general_Info.get_float("cost_value");
	    	String effect_name=general_Info.get_string("effect_name");
	    	float effect_value=general_Info.get_float("effect_value");
	    	
	    	boolean is_positive=whether_positive.equals(SZSC_game_protocol.is_positive);
	    	boolean is_token=token.equals(SZSC_game_protocol.is_token);
	    	result.set_info(buff_name, Buff_ID,type, true, is_positive, add_type);
	    	
	    	//获取源统一处理？通过武器获得的在武器栏实现，通过卡片获得的在发动卡片效果时实现，通过人物效果实现的在人物效果实现
	    	//result.set_source(add_type, row_total_mount, Buff_ID);
	    	
	    	result.set_use_limit(chain_level_limit,limit_type, (int)limit_value);
	    	if(is_token)
	    		result.set_token(is_token, first_token_value);
	    	
	    	result.set_cost(cost_name, cost_value);
	    	result.set_condition(condition_name, condition_value);
	    	result.set_effect(effect_name, effect_value);
	    	result.set_duration(duration_type, duration_value);
    	}
        	
		       
		
		
		return result;
	}
	public static SZSC_Buff get_self_effect(SZSC_player p1,int which_effect) {
		SZSC_Buff result=null;
		
		//获取自己第which_effect个自身效果的buff具体信息，如果有的话
		for(SZSC_Buff current_buff:p1.buff)
			if(current_buff.check_source_type(SZSC_game_protocol.Buff_source_my_character_effect))
			{
				if(current_buff.get_source_which_effect()==which_effect) {
					result=current_buff;
					break;
				}
				
			}
		
		return result;
	}
	public static int get_weapon_token_value(SZSC_player p1,int which_weapon,int which_effect) {
		return get_item_token_value(p1,SZSC_game_protocol.Buff_source_my_weapon_effect_fix, which_weapon, which_effect);
	}
	
	
	
	public static int get_item_token_value(SZSC_player p1,int source_type,int which_item,int which_effect) {
		int result=SZSC_protocol.code_none;
		for(SZSC_Buff buff:p1.buff)
			if(buff.check_source_type(source_type))
				if(buff.get_source_which_item()==which_item)
					if(buff.get_source_which_effect()==which_effect)
						if(buff.is_token()) {
							result=buff.get_token_value();
							break;
						}
		return result;
	}

}
