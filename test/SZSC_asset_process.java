package test;




public class SZSC_asset_process {
	public static final int order_10=10;
    public static final int order_1=1;
	//资产类型
	public static final int kind_character=1;//角色
	public static final int kind_effect=2;//词条
	public static final int kind_diamond=3;//钻石
	
	//public static final int kind_confirm_effect=10;
	
	public static final int kind_character_king=10;//王
	public static final int kind_character_hero=20;//英
	public static final int kind_character_solider=30;//兵
	public static final int kind_character_effect=40;//士、词条
	public static final int kind_character_fixed=50;//固定词条无法改动
	

	public static final int code_none=SZSC_protocol.code_none;//该栏为空
	public static final int effect_limit=SZSC_protocol.abilitylimit;//个人角色效果上限
	
	
		
	
	
	
	
	
	
	
	
	
	
	
	private static void show(String msg) {
		core_main.show(msg);
		
	}
	
	
	
	public static void load_lottery_result(SZSC.Asset_bag asset_bag,JSON_process reply_msg) {
		
		
		reply_msg.add("signal",SZSC_protocol.SZSC_show_lottery_result);
		int total_mount=asset_bag.get_total_mount();
		reply_msg.add("total_mount",total_mount);
		
		
		for(int i=0;i<total_mount;i++)
		{
			SZSC.Asset asset=asset_bag.get_asset(i);
			reply_msg.addToArray("kind", asset.get_kind());
			reply_msg.addToArray("effect", asset.get_code_number());
		}
		
		
		
		
				
	}
	
	//用户获取自己所有角色信息
	public static SZSC.Character_bag show_user_all_character(int user_ID) {
		
		SZSC.Character_bag character_bag=SZSC_asset_command.Character_show_all(user_ID);
		return character_bag;
		/*
		if(character_bag==null)
		{return;}
		int total_mount=character_bag.get_total_mount();
		show("用户角色项共"+total_mount+"项");
		
		for(int i=0;i<total_mount;i++)
		{
			Character character=character_bag.get_characters(i);
			if(character==null)
				continue;
			String effect_msg="";
			int c_rowid=character.get_rowID();
			for(int j=0;j<effect_limit;j++) {
				int c_kind=character.get_kind(j);
				int c_effect=character.get_effect(j);
				effect_msg=effect_msg+"\n"+"效果"+(j+1)+" "+c_kind+" "+c_effect;
			}
			String msg=(i+1)+"号角色 rowid="+c_rowid+effect_msg;
			show(msg);
		}*/
	}
	
	//用户获取自己所有资产信息（数量+物品名称）
	public static SZSC.Asset_bag show_user_all_asset(int user_ID) {
		
		SZSC.Asset_bag asset_bag=SZSC_asset_command.Asset_show_all(user_ID);
		
		/*int total_mount=asset_bag.get_total_mount();
		show("用户资产项共"+total_mount+"项");
		
		for(int i=0;i<total_mount;i++)
		{
			Asset asset=asset_bag.get_asset(i);
			if(asset==null)
				continue;
			String msg=(i+1)+" rowid "+asset.get_rowid()+" kind "+asset.get_kind()+" code "+asset.get_code_number()+"\t\tmount "+asset.get_mount();
			show(msg);
		}*/
		return asset_bag;
	}
	
	//用户获得钻石
	public static void user_get_diamond(String user_name,int mount) {
		int user_ID=SZSC_asset_command.get_user_ID(user_name);
		if(user_ID==code_none)
		{
			show("未找到用户名"+user_name);
			return;
		}
		SZSC_asset_command.user_get_diamond(user_ID, 6000);
	}
	
	//购买物品，消耗钻石，获得物资
	public static SZSC.Asset_bag purchase(int user_ID,int order_kind) {
		
		int cost=0;
		SZSC.Asset_bag asset_bag=new SZSC.Asset_bag(0,user_ID);
		switch(order_kind) {
		case order_10:
			cost=900;
			break;
		case order_1:
			cost=10;
			break;
		default:
			show("抽奖错误订单类别 order_kind="+order_kind);
			asset_bag.set_result(SZSC_protocol.SZSC_purchase_result_order_wrong);
			return asset_bag;
		}
		
		if(!SZSC_asset_command.user_cost_diamond(user_ID,cost))
		{
			show("支付失败!");
			asset_bag.set_result(SZSC_protocol.SZSC_purchase_result_lack_money);
			return asset_bag;
		}
		
		//进行抽奖
		asset_bag=SZSC_lotterySystem.start(order_kind,user_ID);
		
		int total_mount=asset_bag.get_total_mount();
		String asset_result="本次抽奖结果:\n";
		for (int i=0;i<total_mount;i++) {
			
        	//core_main.show(asset_bag.get_asset(i).+" "+prize.getcode());
        	
        	//录入数据库
			SZSC.Asset asset=asset_bag.get_asset(i);
        	if(asset==null)
        	{
        		break;
        	}
        	int asset_kind=asset.get_kind();//品质
        	int asset_ID=asset.get_code_number();//物品ID
        	
        	asset_result+="【品质】"+SZSC_game_dictionary.lottory_result_get_quality(asset_kind)+" 【名称】"+SZSC_game_dictionary.get_name(asset_ID)+"\n";
        	asset_result+=SZSC_game_dictionary.get_brief(asset_ID);
        	SZSC_asset_command.asset_change_mount(asset,true);
        }
		show(asset_result);
		return asset_bag;
	}
	//创建角色
	public static void create_character(int user_ID,int rowid) {
		
		
		//判断该资产是否为当前用户所有
		if(!SZSC_asset_command.Asset_belong(rowid, user_ID))
		{
			show("资产归属非当前用户 rowid="+rowid+" user_ID="+user_ID);
			return;
		}
		//提取资产属性
		SZSC.Asset asset=SZSC_asset_command.get_asset_attribute(rowid);
		if(asset==null)
			return;
		
		
		//根据资产生成对应角色
		SZSC.Character character=new SZSC.Character(asset, null);
		
		boolean result=false;
		//创建角色
		result=SZSC_asset_command.Character_insert_object(character);
		
		//修改资产数量：减少1个
		if(result) {
			result=SZSC_asset_command.asset_change_mount(asset, false);
			if(!result)
				show("创建角色成功但扣除角色未成功!");
			}
		else {
			show("没创建成功");
		}
		
		
	}
	//删除角色
	public static int delete_character(int user_ID,int character_rowid) {
		
		//判断该资产是否为当前用户所有
		if(!SZSC_asset_command.Character_belong(character_rowid, user_ID))
		{
			return SZSC_protocol.SZSC_result_delete_character_not_owner;
		}
		//判断该角色词条是否都已卸载为空
		if(!SZSC_asset_command.Character_whether_left_none(character_rowid))
			return SZSC_protocol.SZSC_result_delete_character_not_empty;
		//提取资产属性
		SZSC.Asset asset=SZSC_asset_command.get_Character_attribute(character_rowid,1);
		if(asset==null)
			return SZSC_protocol.SZSC_result_delete_character_wrong_DB;
		//删除角色
		if(!SZSC_asset_command.Character_delete_object(character_rowid))
		{
			show("删除角色失败");
			return SZSC_protocol.SZSC_result_delete_character_wrong_DB;
		}
		//修改资产数量：增加1个
		if(SZSC_asset_command.asset_change_mount(asset, true))
			return SZSC_protocol.SZSC_result_delete_character_success;
		else
			return SZSC_protocol.SZSC_result_delete_character_wrong_DB;
	}
	//修改角色,镶嵌词条/卸下词条
	public static void update_character(Client client,int user_ID,int update_type,String character_name,int character_rowid,int asset_rowid,int which) {
		if(which==1)
		{
			SZSC_service.SZSC_tips(client,"首个词条不能修改");
			return;
		}
		
		//判断该资产是否为当前用户所有
		if(!SZSC_asset_command.Character_belong(character_rowid, user_ID))
		{
			SZSC_service.SZSC_tips(client, character_name);
			return;
		}
		if(asset_rowid!=code_none) {
			if(!SZSC_asset_command.Asset_belong(asset_rowid, user_ID))
			{
				SZSC_service.SZSC_tips(client,"角色资产归属非当前用户");
				return;
			}
		}
		else {//如果说asset_rowid为空，而又要增加
			if(update_type==SZSC_protocol.SZSC_character_update_insert_effect)
			{
				show("添加词条时获取asset_rowid="+asset_rowid);
				return;
			}
			
		}
		
		
		
		
		
		
		
		switch(update_type) {
			case SZSC_protocol.SZSC_character_update_name:
				{
					int length=character_name.length();
					if(length<8&&length>0)
						SZSC_asset_command.Character_change_name(user_ID, character_rowid, character_name);
					else {
						show("修改名称有误  "+character_name);
					}
				}
				break;
			case SZSC_protocol.SZSC_character_update_drop_effect:
				{
					if(which<1||which>effect_limit)
					{
						show("修改角色属性时which出错"+which);
						return;
					}
					//提取资产属性
					SZSC.Asset Character_asset=SZSC_asset_command.get_Character_attribute(character_rowid,which);
					//获取该词条属性，判断该词条是否可以删除
					if(!character_whether_effect_change(Character_asset.get_kind()))
					{
						SZSC_service.SZSC_tips(client,"当前词条无法被更改");return;
					}
					{
						//如果是删除某一词条，则在角色词条里置空该项，再资产里数量+1
						//判断该词条是否为空
						if(Character_asset.get_code_number()==code_none)
						{
							SZSC_service.SZSC_tips(client,"已经为空，无需删除");
							return;
						}
						
						//角色该项词条置空
						SZSC.Asset asset=new SZSC.Asset(code_none,user_ID,kind_character_effect,code_none);
						if(!SZSC_asset_command.Character_change_object(asset,which,character_rowid))
						{
							show("角色设置词条错误");
							return;
						}
					
						//原有资产+1
						SZSC_asset_command.asset_change_mount(Character_asset, true);
					}
				}
				break;
			case SZSC_protocol.SZSC_character_update_insert_effect:
				{
					if(which<1||which>effect_limit)
					{
						show("修改角色属性时which出错"+which);
						return;
					}
					//提取资产属性
					SZSC.Asset Character_asset=SZSC_asset_command.get_Character_attribute(character_rowid,which);
					if(!character_whether_effect_change(Character_asset.get_kind()))
					{
						SZSC_service.SZSC_tips(client,"当前词条无法被更改");return;
					}
					if(Character_asset.get_code_number()!=code_none){
						SZSC_service.SZSC_tips(client,"当前词条不为空，无法添加!需先卸下!");return;
					}
					
					
					
					{
						//获取要添加进去的资产信息
						SZSC.Asset asset=SZSC_asset_command.get_asset_attribute(asset_rowid);
						//判断是否是可添加的词条，如果是其他英雄士兵等种类角色则表示无法添加，如果是一般词条则可以添加
						if(!character_whether_effect_change(asset.get_kind()))
						{
							SZSC_service.SZSC_tips(client,"该词条无法被镶嵌进角色");
							return;
						}
						//不能添加已有相同编号的词条
						for(int i=2;i<effect_limit;i++)
							if(SZSC_asset_command.get_Character_attribute(character_rowid,i).get_code_number()==asset.get_code_number())
							{
								SZSC_service.SZSC_tips(client,"不能添加相同词条");
								return;
							}
						//原有资产-1
						if(!SZSC_asset_command.asset_change_mount(asset, false))
						{
							show("原有资产扣除错误");
							return;
						}
						//进行镶嵌
						SZSC_asset_command.Character_change_object(asset,which,character_rowid);
					}
				}
				break;
		}
		
		
		
		
		
	}
	
	//查看当前词条种类可否修改（目前只有士阶的词条可以用于更替）
	private static boolean character_whether_effect_change(int code) {
		if(code==kind_character_effect)
			return true;
		return false;
	}
	
	//直接获取某个资产
	public static void provide_user_asset(int user_ID,int kind,int code_number,int mount) {
		SZSC.Asset asset=new SZSC.Asset(code_none,user_ID,kind,code_number);
		SZSC_asset_command.asset_insert_object(asset);
	}
	
	
	//刷新个人角色详细内容
	public static void refresh_own_character(Client client,int character_rowid) {
		String user_name=client.get_client_name();
		int user_asset_ID=SZSC_asset_command.get_user_ID(user_name);
		
		//判断该资产是否为当前用户所有
		if(!SZSC_asset_command.Character_belong(character_rowid, user_asset_ID))
		{
			show("角色资产归属非当前用户");
			return;
		}
		
		JSON_process reply_msg=new JSON_process();
		reply_msg.add("signal",SZSC_protocol.SZSC_refresh_own_character); 
		
		
		if(load_user_speicific_character(reply_msg, user_asset_ID, character_rowid))
			SZSC_service.send_msg(client, reply_msg);
		
	}
	
	//加载特定某个角色数据
	public static boolean load_user_speicific_character(JSON_process reply_msg,int character_user_ID,int character_rowid) {
		SZSC.Character_bag character_bag=show_user_all_character(character_user_ID);
		if(character_bag!=null)
		{
			int total_mount_character=character_bag.get_total_mount();
			
			reply_msg.add("effect_limit", effect_limit);
			
			for(int i=0;i<total_mount_character;i++)
			{
				SZSC.Character character=character_bag.get_characters(i);
				if(character==null)
					continue;
				
				int c_rowid=character.get_rowID();
				if(character_rowid==c_rowid)
				{
					reply_msg.add("character_rowid", c_rowid);
					reply_msg.add("character_name", character.get_name());
					for(int j=0;j<effect_limit;j++) {
						int c_kind=character.get_kind(j);
						int c_effect=character.get_effect(j);
						reply_msg.addToArray("kind", c_kind);
						reply_msg.addToArray("effect", c_effect);
					}
					
				}
				return true;
			}
			show("加载特定角色失败!未找到对应rowid="+character_rowid+" 角色!");
			
		}
		return false;
	}
	
	public static void load_user_all_characters(JSON_process reply_msg,int asset_user_ID,int page,int page_limit) {
		
		int character_current_mount=0;//当次查询展示数量
		SZSC.Character_bag character_bag=show_user_all_character(asset_user_ID);
		if(character_bag!=null)
		{
			int total_mount=character_bag.get_total_mount();
			reply_msg.add("character_page", page);	
			reply_msg.add("total_mount_character",total_mount);
			reply_msg.add("character_total_page", (total_mount+page_limit-1)/page_limit);
			
			for(int i=0;i<total_mount;i++)
			{
				if(i<(page-1)*page_limit||i>=page*page_limit)
					continue;
				
				SZSC.Character character=character_bag.get_characters(i);
				if(character==null)
					continue;
				
				
				int c_rowid=character.get_rowID();
				reply_msg.addToArray("character_rowid", c_rowid);
				/*
				for(int j=0;j<effect_limit;j++) {
					int c_kind=character.get_kind(j);
					int c_effect=character.get_effect(j);
					reply_msg.add_array("c_kind"+i, c_kind, "i");
					reply_msg.add_array("c_effect"+i, c_effect, "i");
				}*/
				String character_name=character.get_name();
				reply_msg.addToArray("character_name", character_name);
				character_current_mount++;
			}
			
			reply_msg.add("character_current_mount", character_current_mount);
		}
		
	}
	public static void load_user_all_assets(JSON_process reply_msg,int asset_user_ID,int page,int page_limit) {
		int asset_current_mount=0;
		SZSC.Asset_bag asset_bag=show_user_all_asset(asset_user_ID);
		if(asset_bag!=null)
		{
			int total_mount=asset_bag.get_total_mount();
			reply_msg.add("total_mount_asset",total_mount);
			reply_msg.add("asset_page", page);	
			reply_msg.add("asset_total_page", (total_mount+page_limit-1)/page_limit);
			
			
			for(int i=0;i<total_mount;i++)
			{
				if(i<(page-1)*page_limit||i>=page*page_limit)
					continue;
				
				SZSC.Asset asset=asset_bag.get_asset(i);
				if(asset==null)
					continue;
				reply_msg.addToArray("asset_rowid", asset.get_rowid());
				reply_msg.addToArray("asset_kind", asset.get_kind());
				reply_msg.addToArray("asset_code_number", asset.get_code_number());
				reply_msg.addToArray("asset_mount", asset.get_mount());
				asset_current_mount++;
			}
			
			reply_msg.add("asset_current_mount", asset_current_mount);			
		}
		
	}
	
	//刷新个人资产
	public static void refresh_own_asset(Client client,int page,int signal_type) {
		if(page<=0) {
			show("更新page出错 "+page);
			return;
		}
		
		
		String user_name=client.get_client_name();
		int user_asset_ID=SZSC_asset_command.get_user_ID(user_name);
		
		
		
		JSON_process reply_msg=new JSON_process();
		int reply_signal=SZSC_protocol.code_none;
		switch(signal_type) {
			case SZSC_protocol.SZSC_apply_refresh_asset_asset:
				reply_signal=SZSC_protocol.SZSC_refresh_own_asset_asset;
				load_user_all_assets(reply_msg, user_asset_ID,page,SZSC_protocol.SZSC_page_asset_limit);
				reply_msg.add("asset_page", page);
				break;
				
			case SZSC_protocol.SZSC_apply_refresh_asset_character:
				reply_signal=SZSC_protocol.SZSC_refresh_own_asset_character;
				load_user_all_characters(reply_msg, user_asset_ID,page,SZSC_protocol.SZSC_page_character_limit);
				reply_msg.add("character_page", page);
				break;
			case SZSC_protocol.SZSC_apply_refresh_character_asset:
				reply_signal=SZSC_protocol.SZSC_refresh_own_character_asset;
				load_user_all_assets(reply_msg, user_asset_ID,page,SZSC_protocol.SZSC_page_asset_limit);
				reply_msg.add("asset_page", page);
				break;
			case SZSC_protocol.SZSC_apply_choose_character_change_page:
				reply_signal=SZSC_protocol.SZSC_show_character_choice;
				load_user_all_characters(reply_msg, user_asset_ID,page,SZSC_protocol.SZSC_page_character_limit);
				reply_msg.add("character_page", page);
				break;
			default:
				show("未知type"+signal_type);
		}
		reply_msg.add("signal", reply_signal);
		
		
		SZSC_service.send_msg(client, reply_msg);
	}
	
	public static boolean service(Client client,JSON_process json_msg) {
		boolean result=true;
		int signal=json_msg.getInt("signal");
		int user_ID=SZSC_service.get_client_unique_identity(client);
		String user_name=client.get_client_name();
		int Asset_user_ID=SZSC_asset_command.get_user_ID(user_name);
		
		switch (signal) {
		case SZSC_protocol.SZSC_apply_check_asset: {
			JSON_process reply_msg=new JSON_process();
			reply_msg.add("signal",SZSC_protocol.SZSC_show_own_asset);
			
			SZSC_service.send_msg(client, reply_msg);
			
			//刷新个人资产
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_asset);
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_character);
		}
			break;
		case SZSC_protocol.SZSC_apply_refresh_asset_asset:{
			int asset_page=json_msg.getInt("page");
			//刷新个人资产
			refresh_own_asset(client,asset_page,signal);
			
		}
			break;
		case SZSC_protocol.SZSC_apply_refresh_asset_character:{
			int character_page=json_msg.getInt("page");
			//刷新个人资产
			refresh_own_asset(client,character_page,signal);
			
		}
			break;
		case SZSC_protocol.SZSC_apply_refresh_character_asset:{
			int asset_page=json_msg.getInt("page");
			//刷新个人资产
			refresh_own_asset(client,asset_page,signal);
			
		}
			break;	
			
			
		case SZSC_protocol.SZSC_apply_create_character:
		{
			int rowid=json_msg.getInt("asset_rowid");
			create_character(Asset_user_ID, rowid);
			//刷新个人资产
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_asset);
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_character);
		}
			break;
		case SZSC_protocol.SZSC_apply_update_character:
		{
			int which=json_msg.getInt("which");
			int character_rowid=json_msg.getInt("character_rowid");
			int asset_rowid=json_msg.getInt("asset_rowid");
			int update_type=json_msg.getInt("update_type");
			String character_name=json_msg.getString("character_name");
			update_character(client,Asset_user_ID, update_type,character_name, character_rowid, asset_rowid, which);
			
			//刷新个人角色
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_character_asset);
			refresh_own_character(client, character_rowid);
			
		}
			break;
		case SZSC_protocol.SZSC_apply_delete_character:
		{
			int rowid=json_msg.getInt("character_rowid");
			int delete_result=delete_character(Asset_user_ID, rowid);
			switch (delete_result) {
				case SZSC_protocol.SZSC_result_delete_character_not_empty: 
					SZSC_service.SZSC_tips(client, "须将角色词条全部卸下后方可删除角色!");
					break;
				case SZSC_protocol.SZSC_result_delete_character_not_owner:
					show("删除角色失败，并非角色持有者");
					break;
				case SZSC_protocol.SZSC_result_delete_character_wrong_DB:
					SZSC_service.SZSC_tips(client, "数据库错误!");
					break;
				case SZSC_protocol.SZSC_result_delete_character_success:
					//如果删除角色成功，关闭角色显示页面
					SZSC_service.send_msg_signal(client, SZSC_protocol.SZSC_delete_character_success);
					//刷新个人资产
					refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_asset);
					refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_asset_character);
					
					break;
					default:
						show("删除角色错误反馈");
			}
			
		}
			break;
		case SZSC_protocol.SZSC_apply_get_character:
		{
			//打开页面
			SZSC_service.send_msg_signal(client, SZSC_protocol.SZSC_show_own_character);
			
			//刷新个人角色
			int rowid=json_msg.getInt("character_rowid");
			
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_character_asset);
			refresh_own_character(client, rowid);
			
		}
			break;
			
		case SZSC_protocol.SZSC_apply_refresh_character:
		{
			int rowid=json_msg.getInt("character_rowid");
			
			
			refresh_own_asset(client,1,SZSC_protocol.SZSC_apply_refresh_character_asset);
			refresh_own_character(client, rowid);
		}
			break;
		
			
		case SZSC_protocol.SZSC_apply_go_lottery:
		{
			//打开页面
			JSON_process reply_msg=new JSON_process();
			reply_msg.add("signal",SZSC_protocol.SZSC_show_lottery_plate);
			reply_msg.add("diamonds_remain", SZSC_asset_command.inquire_user_diamond(Asset_user_ID));
			SZSC_service.send_msg(client, reply_msg);
		}
			break;
		case SZSC_protocol.SZSC_apply_do_lottery:
		{
			int order_kind=json_msg.getInt("order_kind");
			SZSC.Asset_bag asset_bag=SZSC_asset_process.purchase(Asset_user_ID,order_kind);
			//向用户展示结果
			int purchase_result=asset_bag.get_result();
			switch (purchase_result){
			case SZSC_protocol.SZSC_purchase_result_success: 
			{
				JSON_process reply_msg=new JSON_process();
				int diamonds_remain=SZSC_asset_command.inquire_user_diamond(Asset_user_ID);
				reply_msg.add("diamonds_remain", diamonds_remain);
				load_lottery_result(asset_bag,reply_msg);
				SZSC_service.send_msg(client, reply_msg);
			}
				
				break;
			case SZSC_protocol.SZSC_purchase_result_lack_money:
				SZSC_service.SZSC_tips(client, "余额不足!");
				break;
			case SZSC_protocol.SZSC_purchase_result_failed:
				SZSC_service.SZSC_tips(client, "未知原因错误!抽奖失败!");
				break;
			case SZSC_protocol.SZSC_purchase_result_order_wrong:
				SZSC_service.SZSC_tips(client, "订单号出错!");
				break;
				
			
			default:
				show("购买的奇怪结果:"+result);
			}
			
				
		}
			break;
		
			
		default:
			result=false;
		}
		return result;
	}
	
	
	
  //根据asset编号获取对应的角色各个技能
  	public static SZSC.Character get_character_effect(SZSC.Asset asset) {
  		int character_id=asset.get_code_number();
  		
  		SZSC.Character character=null;
  		
  		
  		SYSTEM_EXCEL excel_file=new SYSTEM_EXCEL(SZSC_protocol.Character_EXCEL_PATH,"Sheet1");
  		
  		int ID_column=excel_file.get_column("序号");
  		int effect1_column=excel_file.get_column("效果1");
  		int name_column=excel_file.get_column("名字");
  		int row_total_mount=excel_file.getLastRowNum();
  		boolean not_found=true;
  		for(int row=1;row<row_total_mount;row++) {
  			
  			if(character_id==excel_file.getInt(row, ID_column)) {//该行角色为指定角色id
  				String character_name=excel_file.getString(row, name_column);
  				character=new SZSC.Character(character_id,asset.get_user_ID(),character_name,null);
  				
  				for(int j=0;j<SZSC_protocol.abilitylimit;j++) {
  					int code_number=excel_file.getInt(row, effect1_column+j);//技能列
  					if(code_number!=SZSC_protocol.code_none)
  						character.set_ability(j,SZSC_asset_process.kind_character_fixed,code_number);
  					else 
  						character.set_ability(j,SZSC_protocol.code_none,SZSC_protocol.code_none);
  					
  				}
  				not_found=false;
  				break;//已经找到指定角色，不做之后查找
  			}
  			
  		}
  		if(not_found)
  			show("没找到对应角色"+character_id);
  		excel_file.close();
  		return character;
  		
  		
  	}
	
  	
  	
  	public static boolean hasDBFile(String directoryPath, String fileName) {
  	    // 如果目录路径为空或空白，使用当前目录
  	    if (directoryPath == null || directoryPath.isBlank()) {
  	        directoryPath = ".";
  	    }
  	    
  	    java.io.File directory = new java.io.File(directoryPath);
  	    
  	    // 检查目录是否存在
  	    if (!directory.exists() || !directory.isDirectory()) {
  	        System.out.println("目录不存在或不是有效目录: " + directoryPath);
  	        return false;
  	    }
  	    
  	    // 遍历目录下的所有文件
  	    java.io.File[] files = directory.listFiles();
  	    if (files != null) {
  	        for (java.io.File file : files) {
  	            if (file.isFile()) {
  	                String name = file.getName().toLowerCase();
  	                // 如果有指定文件名，需要匹配文件名
  	                if (fileName != null && !fileName.isBlank()) {
  	                    if (name.equals(fileName.toLowerCase()) && name.endsWith(".db")) {
  	                        System.out.println("找到.db文件: " + file.getAbsolutePath());
  	                        return true;
  	                    }
  	                } 
  	                // 如果没有指定文件名，只需要是.db文件
  	                else if (name.endsWith(".db")) {
  	                    System.out.println("找到.db文件: " + file.getAbsolutePath());
  	                    return true;
  	                }
  	            }
  	        }
  	    }
  	    
  	    return false;
  	}
    
    /**
     * 在指定目录创建新的.db文件
     * @param directoryPath 目录路径
     * @param dbFileName 要创建的.db文件名（不包含路径）
     * @return 如果创建成功返回true，否则返回false
     */
    public static boolean createDBFile(String directoryPath, String dbFileName) {
    	
        try {
            // 确保目录存在
        	if(!directoryPath.isBlank()) {
	            java.io.File dir = new java.io.File(directoryPath);
	            if (!dir.exists()) {
	                boolean created = dir.mkdirs();
	                if (!created) {
	                    System.out.println("无法创建目录: " + directoryPath);
	                    return false;
	                }
	            }
        	}
            
            // 确保文件名以.db结尾
            if (!dbFileName.toLowerCase().endsWith(".db")) {
                dbFileName = dbFileName + ".db";
            }
            
            // 创建完整的文件路径
            java.nio.file.Path dbFilePath = java.nio.file.Paths.get(directoryPath, dbFileName);
            
            // 创建文件
            java.nio.file.Files.createFile(dbFilePath);
            System.out.println("已创建.db文件: " + dbFilePath.toAbsolutePath());
            return true;
            
        } catch (java.io.IOException e) {
            System.out.println("创建.db文件时出错: " + e.getMessage());
            return false;
        }
    }
    
    
    public static void checkAndCreateDB(String directoryPath, String defaultDBName) {
        System.out.println("正在检查目录: " + directoryPath+"\\"+defaultDBName);
        
        // 检查是否有.db文件
        if (hasDBFile(directoryPath,defaultDBName)) {
            System.out.println("目录中已存在"+defaultDBName+" 文件，无需创建新文件。");
        } else {
            System.out.println("目录中没有找到.db文件，正在创建新文件...");
            boolean created = createDBFile(directoryPath, defaultDBName);
            if (created) {
                System.out.println("成功创建.db文件！");
            } else {
                System.out.println("创建.db文件失败！");
            }
        }
    }
}
