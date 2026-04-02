package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SZSC_game_dictionary {
	

	public static void set_character_Buff(SZSC_game this_room,SZSC_player p1,List<Integer> effect_ID)//根据人物自身效果ID给人物以buff方式存储
	{
		List<SZSC_Buff> buffs=SZSC.getNewArrayList();
		for(int which_effect:effect_ID) {
			SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(which_effect);
			buffs.add(buff);
		 }
		//buff.set_info("个人角色效果", effect_ID, false, false, effect_ID);//默认为被动效果，如果要改成主动发动的需要手动改
		//buff.set_source(SZSC_game_protocol.Buff_source_my_character_effect, p1.get_player_No(), effect_ID);

		//buff.set_duration(SZSC_protocol.SZSC_duration_permanent_selfeffect, SZSC_protocol.code_none);//人物效果，必然是永久存在的，只不过可能有发动次数限制，或者苛刻的触发条件

		
		SZSC_game_Buff_process.player_add_Buff(this_room, buffs, p1, p1);
	}



	public static String search_ID_name(int card_No) {
		
		return search_card(SZSC_game_protocol.p_name, card_No);
	}
	

	public static String search_card(int situation,int card_No)                     //查询编号代表的手卡效果
	{
		String result="莫名其妙号卡"+card_No+" "+situation;
		switch(situation)
		{
			case SZSC_game_protocol.p_name:
			{
				result=get_card_info(card_No).get_string("name");
			}
				break;
			case SZSC_game_protocol.p_details:
			{
				SZSC.General_Info general_Info=get_card_info(card_No);
				String limit_type_description=general_Info.get_string("限制类型");
				String limit_times_description=general_Info.get_string("限制次数");
	            String condition_description=general_Info.get_string("condition_description");
	            String cost_description=general_Info.get_string("cost_description");
	            String effect_description=general_Info.get_string("effect_description");
	            String launch_type=general_Info.get_string("发动类型");
	            String limit_description="";
	            if(!limit_type_description.isEmpty())
	            	limit_description=limit_type_description+" 限 "+limit_times_description+" 次,";
                if(!condition_description.isEmpty()) {
                	switch(launch_type) {
                		
                		case "选发":
                			condition_description=condition_description+"可";
                		case "必发":
                			condition_description=condition_description+"发动,";
                			break;
                		default:
                			condition_description=condition_description+"可发动,";
                	}
                }
                if(!cost_description.isEmpty())
                    cost_description=cost_description+",";
                if(!effect_description.isEmpty())
                    effect_description=""+effect_description+"";
                result=limit_description+condition_description+cost_description+effect_description;
				
			}
				break;
		}
		return result;


	
	}
	

	public static String search_character_skill(int card_No)//查询编号代表的武器/卡片效果
	{
		String result="未知角色  "+card_No;
		SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Asset_EXCEL_PATH, "Sheet1", "No", card_No);
		result=general_Info.get_string("具体描述");
		
		
		return result;
	}
	
	public static int card_get_hide_effect(int card_No) {
		int result =SZSC_protocol.code_none;
		SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "hide", "No", card_No);
		result=general_Info.get_int("hide_effect_No");
		
		
        return result;
	}
	public static String event_get_name(String event_name) {
		String result ="";
		SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "event", "condition_name", event_name);
		result=general_Info.get_string("description");
		
		
        return result;
	}
	//武器卡片特殊展示，在展示详细效果时，需展示该武器所拥有的所有效果
	public static String get_specific_info_weapon(int card_No) {
		String result ="";
		SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "weapon", "No", card_No);
		for(int i=1;i<=SZSC_protocol.weaponeffectlimit;i++) {
			int weapon_effect_ID=general_Info.get_int("effect"+i);
			if(weapon_effect_ID==SZSC_protocol.code_none)
				break;
			result+="【武器效果"+i+"】\n"+get_brief(weapon_effect_ID);
		}
		return result;
		
		
	}
	
	
	public static String get_name(int card_No) {


        SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "general", "No", card_No);
        String result=general_Info.get_string("name");
        if(result.isBlank())
        	result="暂无name "+card_No;
        
        return result;
    }
	public static String get_specific_description(int card_No) {
        
        SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "general", "No", card_No);
        
        if(general_Info.get_string("type").equals(SZSC_game_protocol.TYPE_launch_weapon))
        	return get_specific_info_weapon(card_No);
        
        boolean is_buff=general_Info.get_string(SZSC_game_protocol.is_buff_column).equals(SZSC_game_protocol.is_buff);
        boolean is_must_activate=general_Info.get_string(SZSC_game_protocol.List_trigger_type).equals(SZSC_game_protocol.is_must_activate);
        
        
        String limit=general_Info.get_string("限制类型");
        String limit_description="";
        if(!limit.isBlank())
        	limit_description="(限制)"+limit+general_Info.get_string("限制次数")+"次\n";
        
        String condition=general_Info.get_string("condition_description");
        String condition_description="";
        if(!condition.isBlank()) {
        	condition_description="(发动条件)"+condition+" 时";
        	if(!is_must_activate)
        		condition_description+="可";
        	condition_description+="发动\n";
			
        }
        
        String cost=general_Info.get_string("cost_description");
        String cost_description="";
        if(!cost.isBlank())
        	cost_description="(消耗)"+cost+"\n";
        
        String front_info=limit_description+condition_description+cost_description;
        
        String effect=general_Info.get_string("effect_description");
        String effect_description="";
        if(!effect.isBlank()) {
        	if(!front_info.isBlank())
        		effect_description+="(效果)";
        	effect_description+=effect+"\n";
        }
        
        String description=limit_description+condition_description+cost_description+effect_description;
        description=description.replace(".0", "");

        

        return description;
    }
	
	public static String get_brief(int card_No) {

        String description = "没找到  效果:"+card_No;
        
        SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "general", "No", card_No);
        
        String limit_type_description=general_Info.get_string("限制类型");
        String limit_time_description=general_Info.get_string("限制次数");
        String limit_description="";

        String condition_description=general_Info.get_string("condition_description");
        boolean is_must_activate=general_Info.get_string(SZSC_game_protocol.trigger_type_column).equals(SZSC_game_protocol.is_must_activate);
        String cost_description=general_Info.get_string("cost_description");
        String effect_description=general_Info.get_string("effect_description");
        if(!limit_type_description.isEmpty())
            limit_description=limit_type_description+limit_time_description+"次,";

        if(!condition_description.isBlank()) {
            condition_description+=" 时";
            if(!is_must_activate)
                condition_description+="可";
            condition_description+="发动,";
        }
        if(!cost_description.isEmpty())
            cost_description=""+cost_description+",";
        if(!effect_description.isEmpty())
            effect_description=""+effect_description+"";


        description=limit_description+condition_description+cost_description+effect_description+"\n";
        
        description=description.replace(".0", "");


        return description;
    }
	
	public static String get_type(int card_No) {
        SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH, "general", "No", card_No);
        String type_name=general_Info.get_string("type");
        if(type_name.isBlank())
        	type_name="没找到  类型:"+card_No;
        
        return type_name;
    }
	
	
	//从excel加载卡片效果数据
		public static SZSC.General_Info get_card_info(int card_No){
			return EXCEL_get_info(SZSC_protocol.Game_card_EXCEL_PATH,"general", "No", card_No);
		}
		
		public static SZSC.General_Info EXCEL_get_info(String excel_path,String sheet_name,String column_name,String value) {
			SZSC.General_Info result=new SZSC.General_Info();
			try(SYSTEM_EXCEL excel_file=SZSC_game_general_function.get_system_asset(excel_path, sheet_name)){
				int row=excel_file.getrow(column_name, value);
				if(row<0) {
					show("EXCEL_get_info 没找到对应项  "+sheet_name+"  "+column_name+"    "+value);
				}
				else {
					int max_col=excel_file.getLastColNum();
					for(int col=0;col<max_col;col++) {
						String col_name=excel_file.getString(0, col);//列名
						String col_value=excel_file.getString(row, col);//列值
						result.add(col_name, col_value);
					}
				}
			}
			return result;
		}
		public static SZSC.General_Info EXCEL_get_info(String excel_path, String sheet_name, String column_name, int value) {
	        SZSC.General_Info result = new SZSC.General_Info();
	        

	        try (SYSTEM_EXCEL excel_file = SZSC_game_general_function.get_system_asset(excel_path, sheet_name)) {

	            int row = excel_file.getrow(column_name, value);
	            if (row < 0) {
	                show("EXCEL_get_info 没找到对应项  " + sheet_name + "  " + column_name + "    " + value);

	            } else {
	                int max_col = excel_file.getLastColNum();
	                for (int col = 0; col < max_col; col++) {
	                    String col_name = excel_file.getString(0, col);
	                    String col_value = excel_file.getString(row, col);
	                    result.add(col_name, col_value);
	                }
	            }
	        } 

	        return result;
	    }
		public static List<SZSC.General_Info> EXCEL_get_info_list(String excel_path,String sheet_name,String column_name,String value) {
			
			List<SZSC.General_Info> result=new ArrayList<>();
			
			try(SYSTEM_EXCEL excel_file=SZSC_game_general_function.get_system_asset(excel_path, sheet_name)){
				{
					int max_col=excel_file.getLastColNum();
					int row_total_mount=excel_file.getLastRowNum();
					int pointer_col=excel_file.get_column(column_name);
					
			        for(int which_row=1;which_row<row_total_mount;which_row++) {
			        	if(excel_file.getString(which_row,pointer_col).equals(value)){//如果当前行该列值相同
			        		SZSC.General_Info general_Info=new SZSC.General_Info(); 
			        		for(int col=0;col<max_col;col++) {
								
								String col_name=excel_file.getString(0, col);//列名
								String col_value=excel_file.getString(which_row, col);//列值
								
								general_Info.add(col_name, col_value);
								
							}
			        		result.add(general_Info);
			        	}
			        }
					
					
				}
			}
			return result;
		}
		public static List<SZSC.General_Info> EXCEL_get_info_list(String excel_path,String sheet_name,String column_name,int value) {
			
			List<SZSC.General_Info> result=new ArrayList<>();
			
			try(SYSTEM_EXCEL excel_file=SZSC_game_general_function.get_system_asset(excel_path, sheet_name)){
				{
					int max_col=excel_file.getLastColNum();
					int row_total_mount=excel_file.getLastRowNum();
					int pointer_col=excel_file.get_column(column_name);
					
			        for(int which_row=1;which_row<row_total_mount;which_row++) {
			        	if(excel_file.getInt(which_row,pointer_col)==value){//如果当前行该列值相同
			        		SZSC.General_Info general_Info=new SZSC.General_Info(); 
			        		for(int col=0;col<max_col;col++) {
								
								String col_name=excel_file.getString(0, col);//列名
								int col_value=excel_file.getInt(which_row, col);//列值
								
								general_Info.add(col_name,String.valueOf(col_value));
								
							}
			        		result.add(general_Info);
			        	}
			        }
					
					
				}
			}
			return result;
		}
		//获取某表内x-y行内所有数据
		public static List<SZSC.General_Info> EXCEL_get_list(String excel_path,String sheet_name,int start_row,int last_row) {
			List<SZSC.General_Info> result=new ArrayList<>();
			if(start_row<0||start_row>last_row)
			{
				show("获取excel list出错! "+start_row+"   "+last_row);
				return result;
			}
			try(SYSTEM_EXCEL excel_file=SZSC_game_general_function.get_system_asset(excel_path, sheet_name)){
				for(int row=start_row;row<=excel_file.getLastRowNum()&&row<=last_row;row++) {
					SZSC.General_Info current_record=new SZSC.General_Info();
					int max_col=excel_file.getLastColNum();
					for(int col=0;col<max_col;col++) {
						String col_name=excel_file.getString(0, col);//列名
						String col_value=excel_file.getString(row, col);//列值
						current_record.add(col_name, col_value);
					}
					result.add(current_record);
				}
			}
			return result;
		}
		
		
		
		public static int Excel_get_total_mount(String excel_path,String sheet_name) {
			try(SYSTEM_EXCEL excel_file=SZSC_game_general_function.get_system_asset(excel_path, sheet_name)){
				return excel_file.getLastRowNum();
			}
			
		}
		
		
		//从excel加载默认角色的名字
		public static void Default_character_load_list_character(JSON_process data,int page) {
			
			String excel_path=SZSC_protocol.Character_default_EXCEL_PATH;
			String sheet_name="Sheet1";
			
			int limit=SZSC_protocol.SZSC_page_character_limit;//单次显示个数上限
			int start_offset=1;//起始偏移量
			int character_offset=limit*(page-1);//页数偏移量
			
			int start_row=start_offset+character_offset;
			int last_row=start_offset+character_offset+limit-1;
			int total_mount=Excel_get_total_mount(excel_path,sheet_name);
			int total_page=total_mount/limit+1;
			if(total_mount%limit==0) {
				total_page=total_mount/limit;
			}
			//show("start_row  "+start_row+" last_row  "+last_row);
			List<SZSC.General_Info>general_Infos=EXCEL_get_list(excel_path,sheet_name,start_row, last_row);
			for(SZSC.General_Info general_Info:general_Infos) {
				String character_name=general_Info.get_string("name");
				int character_rowid=general_Info.get_int("No");
				data.addToArray("default_character_name", character_name);
				data.addToArray("default_character_rowid", character_rowid);
			}
			data.add("default_character_current_mount", general_Infos.size());
			data.add("default_character_total_page", total_page);
			data.add("default_character_page", page);
			
			
		}

		// 从Excel获取默认角色中指定id的信息
	    public static SZSC.Character Default_Character_get_ID_info(int character_ID) {
	    	
	    	SZSC.Character character=null;
	    	SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Character_default_EXCEL_PATH, "Sheet1", "No",character_ID);
	    	
	    	int result_ID=general_Info.get_int("No");
	    	if(result_ID!=character_ID)
	    		SZSC_service.show("匹配对应默认角色失败!  character_ID="+character_ID+"  result_ID="+result_ID);
	    	else {
	    		character=get_character(general_Info);
	        	
			}
	    	
	    	return character;
	    }
	    
	    public static SZSC.Character get_character(SZSC.General_Info general_Info) {
	    	if(general_Info==null) {
	    		SZSC_service.show("get_character加载general_Info数据失败!");
	    		return null;
	    	}
	    	SZSC.Character character=null;
	    	String character_name=general_Info.get_string("name");
	    	int character_ID=general_Info.get_int("No");
			character=new SZSC.Character(character_ID, SZSC_protocol.code_none, character_name, null);
			int effect_pointer=0;
			float attack=general_Info.get_float("攻");
			float bloodlimit=general_Info.get_float("血");
			character.set_attack_blood(attack, bloodlimit);
			
	    	while(true) {
	    		int effect_ID=general_Info.get_int("效果"+(effect_pointer+1));
	    		if(effect_ID==SZSC_protocol.code_none)
	    			break;
	    		int kind_ID=SZSC_asset_process.kind_character_effect;
	    		character.set_ability(effect_pointer, kind_ID, effect_ID);
	    		effect_pointer++;
	    		if(effect_pointer+1>SZSC_protocol.abilitylimit)
	    			break;
	    	}
	    	return character;
	    }
	    
	    
	    
	    public static SZSC.Character Character_get_bot_character(int character_ID) {
	    	SZSC.Character character=null;
	    	SZSC.General_Info general_Info=EXCEL_get_info(SZSC_protocol.Character_bot_EXCEL_PATH, "Sheet1", "No",character_ID);
	    	int result_ID=general_Info.get_int("No");
	    	if(result_ID!=character_ID)
	    		SZSC_service.show("匹配对应机器人bot角色失败!  character_ID="+character_ID+"  result_ID="+result_ID);
	    	else {
	    		character=get_character(general_Info);
			}
	    	return character;
	    }
	    
	    public static String lottory_result_get_quality(int kind){
	        switch (kind){
	            case 20:
	                return "英雄";
	            case 30:
	                return "士兵";
	            case 40:
	                return "普通";
	            default:
	                return "未知错误 "+kind;
	        }
	    }
	
	
	public static void show(String msg) {
		SZSC_service.show(msg);
	}
}
