package test;

import java.util.List;

public class SZSC_game_selfeffect_process {
	private static void show(String msg) {
		SZSC_service.show(msg);
	}
	public static void sleep(int time) {
		SZSC_service.sleep(time);
	}
	public static void set_player_selfeffect_Buff(SZSC_game this_room,SZSC_player p1,List<Integer> Buff_IDs)//根据特定id效果赋予buff
	{
		//show("添加武器buff ID"+Buff_ID);
		List<SZSC_Buff> buffs=SZSC.getNewArrayList();
		
		int which_effect=0;
		for(int buff_ID:Buff_IDs) {
			SZSC_Buff buff=SZSC_game_Buff_process.get_buff_data(buff_ID);
			if(buff!=null&&buff.is_buff) {
				//show("添加人物效果buff "+SZSC_game_dictionary.get_name(buff_ID));
				buff.set_source(SZSC_game_protocol.Buff_source_my_character_effect, p1.get_player_No(), which_effect);
				buff.set_Source_which_effect(which_effect);
				buffs.add(buff);
			}
			which_effect++;
		}
		//show("buff_effect "+buff.get_effect_ID()+"    value   "+buff.get_effect_value());
		
		//查询buff_id在总表内对应的具体数据
		SZSC_game_Buff_process.player_add_Buff(this_room,buffs,p1,p1);
	}
	public static void choose_character(SZSC_room this_room,SZSC_player p1) {
		int character_page=1;//当前显示的自己的角色页数
		int default_character_page=1;//当前显示的默认角色页数
		
		//开始选择角色死循环
		while(true){
			show("开始接收玩家选择英雄");
			
			int user_ID=p1.get_DB_user_ID();
			
			//发送信号让玩家页面切换到选择角色页面，展示所拥有的所有角色以及默认角色
			JSON_process send_msg=new JSON_process();
			send_msg.add("signal", SZSC_protocol.SZSC_show_character_choice);
			SZSC_asset_process.load_user_all_characters(send_msg, user_ID, character_page, SZSC_protocol.SZSC_page_character_limit);
			
			
			SZSC_game_dictionary.Default_character_load_list_character(send_msg, default_character_page);
			p1.send(send_msg);
			
			//接收客户端发来消息，如果此时断开通讯结束沟通
			JSON_process reply_msg=p1.main_listen();
			if(p1.offline())
			{show(p1.get_user_name()+"掉线了");return;}
			if(this_room.game_over()){show("有人掉线");return;}//如果有人中途退出,确认没断开通讯，确认房间内是否有人退出
			
			int signal=reply_msg.getInt("signal");
			SZSC.Character character=null;
			
			switch (signal) {
				case SZSC_protocol.SZSC_apply_choose_character:
				{
					int character_rowid=reply_msg.getInt("character_rowid");
					//获取该角色信息
					character=SZSC_asset_command.Character_get_character(user_ID, character_rowid);
					if(character==null)
					{show("没有找到该角色 user_ID="+user_ID+"   character_rowid="+character_rowid);continue;}
					
				}
					break;
				case SZSC_protocol.SZSC_apply_choose_character_default://使用系统设置的默认角色
				{
					int character_rowid=reply_msg.getInt("character_rowid");
					character=SZSC_game_dictionary.Default_Character_get_ID_info(character_rowid);
					if(character==null)
					{
						show("选取默认角色出错  user_ID="+user_ID+" character_rowid="+character_rowid);
						continue;
					}
				}
					break;
					
				case SZSC_protocol.SZSC_apply_choose_character_change_page:
					default_character_page=reply_msg.getInt("default_character_page");
					character_page=reply_msg.getInt("page");
					continue;
				default:
					show("选角 收到错误signal="+signal);
					continue;
			}
			
			
			//个人角色初始化
			ini_character(this_room.game,p1, character);
			
			
			
			
			

			//执行到这里，说明选角成功
			this_room.character_choose_ready_number++;//创建完成者+1
			
			
			
			//封锁用户界面选择按钮，提示选择了什么并请进行等待
			p1.send_signal(SZSC_protocol.SZSC_lock_character_choice);
			//如果是房主选择完成，查看房内是否有机器人
			if(p1.whether_host())
			{
				for(SZSC_player player:this_room.game.players)
					if(player.bot())//对不同的机器人有待处理！
					{
						int bot_character_ID=player.get_type();
						SZSC.Character bot_character=SZSC_game_dictionary.Character_get_bot_character(bot_character_ID);
						//初始化机器人人物数据
						ini_character(this_room.game,player,bot_character);
						this_room.character_choose_ready_number++;//机器人创建完成
					}
				
				
			}
			
			
			
			//等待其他玩家选角结束
			while(!this_room.choose_complete()) {
				SZSC_game_general_function.sleep(500);
				if(this_room.game_over())
					return;
			}
			
			

			break;
		}//结束选择角色死循环
	}
	
	//游戏开始，初始化个人角色
    public static void ini_character(SZSC_game this_room,SZSC_player p1,SZSC.Character character) {
		
		if(character!=null) {
			SZSC_service.show("开始进行角色初始化： "+character.get_effect(0));
			
			List<Integer>effectIDs=SZSC.getNewArrayList();
			
			for(int i=0;i<SZSC_protocol.abilitylimit;i++) {
				int effect_ID=character.get_effect(i);
				int kind_ID=character.get_kind(i);
				SZSC.ability ability=SZSC.get_new_ability(kind_ID, effect_ID);
				p1.add_ability(ability);
				
				if(effect_ID!=SZSC_protocol.code_none)
					effectIDs.add(effect_ID);
			}
			//根据对应效果编号赋予个人buff
			if(!effectIDs.isEmpty())
				set_player_selfeffect_Buff(this_room, p1, effectIDs);
			
			String character_name=character.get_name();
			p1.set_character_name(character_name);
			
			float blood=character.get_bloodlimit();
			float attack=character.get_attack();//Character_get_value(character_ID, "攻");
			
			p1.set_blood_attack(blood, attack);
			
			//SZSC_service.show(character_name+"  "+blood+"   "+attack);
		}
		else {
			SZSC_service.show("角色为空 初始化失败!");
		}
		
		
		
	}
}
