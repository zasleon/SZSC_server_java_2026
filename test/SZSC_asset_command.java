package test;



public class SZSC_asset_command {
	
	
	private static void show(String msg) {
		core_main.show(msg);
	}
	
	public static final String DROP_DIAMOND_TABLE="drop table IF EXISTS Diamond";
	public static final String DROP_Asset_TABLE="drop table IF EXISTS Asset";
	public static final String DROP_Character_TABLE="drop table IF EXISTS Character";
	public static final String sql_create_Diamond_table= "Create Table Diamond("
			 +"user_name VARCHAR(100) NOT NULL UNIQUE,"
			 +"diamonds INT NOT NULL"
			 +");";
	public static final String SQL_insert_user= "Insert into Diamond"
			 +"(user_name,diamonds)"
			 +"values"
			 +"(?,?)";
	public static final String SQL_GET_USER_ID="SELECT rowid FROM Diamond WHERE user_name = ?";
	public static final String SQL_Get_User_Diamond="SELECT rowid,user_name, diamonds FROM Diamond WHERE rowid = ?";
	public static final String SQL_Update_user_Diamond = "UPDATE Diamond SET diamonds = ? WHERE rowid = ?";
	//个人资产
	public static final String sql_create_Asset_table= "Create Table Asset("
			 +"user_ID INT NOT NULL,"
			 +"kind INT NOT NULL,"//英雄、王、兵、士
			 +"code_number INT NOT NULL,"
			 +"mount INT NOT NULL"
			 +");";
	//个人角色创建
	public static final String sql_create_Character_table= "Create Table Character("
			 +"refresh_time VARCHAR(50) NOT NULL,"
			 +"name VARCHAR(50) NOT NULL,"
			 +"user_ID INT NOT NULL,"
			 
			 +"kind1 INT NOT NULL,"
			 +"effect1 INT NOT NULL,"
			 +"kind2 INT NOT NULL,"
			 +"effect2 INT NOT NULL,"
			 +"kind3 INT NOT NULL,"
			 +"effect3 INT NOT NULL,"
			 +"kind4 INT NOT NULL,"
			 +"effect4 INT NOT NULL,"
			 +"kind5 INT NOT NULL,"
			 +"effect5 INT NOT NULL"
			 +");";
	//插入资产
	public static final String SQL_Asset_insert= "Insert into Asset"
			 +"(user_ID,kind,code_number,mount)"
			 +"values"
			 +"(?,?,?,?)";
	
	//获取角色名字
	public static final String SQL_Character_get_name= "Select name from Character where rowid= ?";

	//查询特定资产的数量
	public static final String SQL_Asset_search_mount= "Select mount From Asset where "
			+ "user_ID=? and kind=? and code_number=?";
	
	//获取所有词条资产
	public static final String SQL_Asset_search_all= "Select rowid,kind,code_number,mount From Asset where "
			+ "user_ID=? ORDER BY kind ASC,mount DESC,code_number ASC";
	//获取所有角色资产
	public static final String SQL_Character_search_all= "Select rowid,name,refresh_time,"
			+ "effect1,effect2,effect3,effect4,effect5,"
			+ "kind1,kind2,kind3,kind4,kind5 "
			+ "From Character where user_ID=? "
			+ "ORDER BY refresh_time DESC";
	
	public static final String SQL_Asset_search_belong= "Select user_ID From Asset where "
			+ "rowid=?";
	public static final String SQL_Character_search_belong= "Select user_ID From Character where "
			+ "rowid=?";
	public static final String SQL_Asset_get_attribute= "Select user_ID,kind,code_number From Asset where "
			+ "rowid=?";
	public static final String SQL_Character_get_attribute= "Select name,user_ID,kind1,kind2,kind3,kind4,kind5,"
			+ "effect1,effect2,effect3,effect4,effect5 "
			+ "From Character where "
			+ "rowid=?";
	private static final String SQL_Character_check_effect = "Select effect1,effect2,effect3,effect4,effect5,"
			+ "kind1,kind2,kind3,kind4,kind5 "
			+ " FROM Character WHERE rowid = ? ";
	private static final String SQL_Character_get_character = "Select name,"
			+ "effect1,effect2,effect3,effect4,effect5,"
			+ "kind1,kind2,kind3,kind4,kind5 "
			+ " FROM Character WHERE rowid = ? and user_ID = ?";
	
	
	//创建个人角色
	public static final String SQL_Character_insert= "Insert into Character"
			+ "(refresh_time,name,user_ID,kind1,kind2,kind3,kind4,kind5,"
			+ "effect1,effect2,effect3,effect4,effect5)"
			+"values"
			+ "(?,?,?,"
			+ "?,?,?,?,?,"
			+ "?,?,?,?,?)";
	
	//更新特定资产的数量
	public static final String SQL_Asset_update= "UPDATE Asset SET mount=? where "
			+"user_ID=? and kind=? and code_number=?";
	//修改个人角色 effect? kind?
	public static final String SQL_Character_update_effect= "Update Character SET %s=? ,%s=? "
			+ "where user_ID=? and rowid=?";
	//修改个人角色名称
	public static final String SQL_Character_update_name= "Update Character SET name=? "
			+ "where user_ID=? and rowid=?";
	

	
	private static final String SQL_Character_delete = "DELETE FROM Character WHERE rowid = ? ";
	
	//展示所有角色

	static public SZSC.Character_bag Character_show_all(int user_ID) {
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Character_show_all");
		szsc_DB_access.set_command(SQL_Character_search_all);
		szsc_DB_access.set_value(1, user_ID);
		szsc_DB_access.execute();
		SZSC.Character_bag character_bag=null;
		int total_mount=0;
		while(szsc_DB_access.next())
			total_mount++;
		if(total_mount==0) {
			character_bag=new SZSC.Character_bag(0,user_ID); 
			szsc_DB_access.close();
			return character_bag;
		}
		else {
			character_bag=new SZSC.Character_bag(total_mount,user_ID); 
		}
		
		szsc_DB_access.execute();//重新再执行一遍
		
		int pointer=0;
		while(szsc_DB_access.next()) {
			SZSC.Effect[] effects=new SZSC.Effect[5];
			String name="untitled";
			for(int i=0;i<SZSC_asset_process.effect_limit;i++)
			{
				int kind=szsc_DB_access.getInt("kind"+(i+1));
				int effect=szsc_DB_access.getInt("effect"+(i+1));
				effects[i]=new SZSC.Effect(kind,effect);
				name=szsc_DB_access.getString("name");
			}
			
			character_bag.set_Character(pointer, total_mount,user_ID, name,effects);
			pointer++;
			if(pointer>=total_mount)
				break;
		}
		szsc_DB_access.close();
		return character_bag;
			
		
	}
	//展示所有资产
	static public SZSC.Asset_bag Asset_show_all(int user_ID) {
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Asset_show_all");
		szsc_DB_access.set_command(SQL_Asset_search_all);
		szsc_DB_access.set_value(1, user_ID);
		szsc_DB_access.execute();
		
		int total_mount=0;
		while(szsc_DB_access.next())
		{
			if(szsc_DB_access.getInt("mount")==0)
				continue;
			
			total_mount++;
		}
		
		if(total_mount==0)
		{
			szsc_DB_access.close();
			SZSC.Asset_bag asset_bag=new SZSC.Asset_bag(0,user_ID); 
			return asset_bag;
		}
		SZSC.Asset_bag asset_bag=new SZSC.Asset_bag(total_mount,user_ID); 
		szsc_DB_access.execute();//再执行一次
		
		int pointer=0;
		while(szsc_DB_access.next()) {
			if(szsc_DB_access.getInt("mount")==0)
				continue;
			int rowid=szsc_DB_access.getInt("rowid");
			int kind=szsc_DB_access.getInt("kind");
			int code_number=szsc_DB_access.getInt("code_number");
			int mount=szsc_DB_access.getInt("mount");
			asset_bag.set_Asset(pointer,rowid, user_ID, kind, code_number, mount);
			pointer++;
			if(pointer>=total_mount)
				break;
		}
		szsc_DB_access.close();
		return asset_bag;
		/*
		try {
			Class.forName("org.sqlite.JDBC");
		        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		        
			java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_search_all);
			statement.setInt(1, user_ID);
		        
			// 执行查询
			ResultSet rs =statement.executeQuery();
			int total_mount=0;
			while(rs.next())
			{
				if(rs.getInt("mount")==0)
					continue;
				total_mount++;
			}
			if(total_mount==0)//用户资产为空
			{
				connection.close();	
				SZSC_asset.Asset_bag asset_bag=new SZSC_asset.Asset_bag(0,user_ID); 
				return asset_bag;
			}
			SZSC_asset.Asset_bag asset_bag=new SZSC_asset.Asset_bag(total_mount,user_ID); 
			rs =statement.executeQuery();
			
			
			int pointer=0;
			while(rs.next())
	        {
				if(rs.getInt("mount")==0)
					continue;
				int rowid=rs.getInt("rowid");
				int kind=rs.getInt("kind");
				int code_number=rs.getInt("code_number");
				int mount=rs.getInt("mount");
				asset_bag.set_Asset(pointer,rowid, user_ID, kind, code_number, mount);
				pointer++;
				if(pointer>=total_mount)
					break;
	        }
			connection.close();	
	        
	        
	        return asset_bag;
	        
	        
		}catch ( Exception e ) {
			show("执行核对资产属性出错"+e.getMessage());
			return null;
		}*/
	}
	
	static public SZSC.Character Character_get_character(int user_ID,int rowid){
		SZSC.Character character=null;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Character_get_character");
		szsc_DB_access.set_command(SQL_Character_get_character);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.set_value(2, user_ID);
		szsc_DB_access.execute();
		if(!szsc_DB_access.next()) {
			show("执行查询资产属性出错，未找到对应rowid");
		}
		else {
			String character_name=szsc_DB_access.getString("name");
			character=new SZSC.Character(rowid,user_ID,character_name,null);
	        for(int i=1;i<=SZSC_asset_process.effect_limit;i++)
	        {
	        	character.set_kind(i-1, szsc_DB_access.getInt("kind"+i));
	        	character.set_effect(i-1, szsc_DB_access.getInt("effect"+i));
	        }
	        	
		}
		szsc_DB_access.close();
		return character;
		
		
	}
	

	//获取该项资产各种属性
	static public boolean Character_whether_left_none(int rowid){
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Character_whether_left_none");
		szsc_DB_access.set_command(SQL_Character_check_effect);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		if(!szsc_DB_access.next()) {
			show("执行查询资产属性出错，未找到对应rowid");
        	result=false;
		}
		else {
	        for(int i=2;i<=SZSC_asset_process.effect_limit;i++)
	        	if(szsc_DB_access.getInt("kind"+i)==SZSC_asset_process.kind_character_effect&&szsc_DB_access.getInt("effect"+i)!=SZSC_protocol.code_none)
	        	{
	        		result=false;
	        		break;
	        	}
		}
		szsc_DB_access.close();
		return result;
		
		/*
		try {
			Class.forName("org.sqlite.JDBC");
		        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		        
			java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_check_effect);
			statement.setInt(1, rowid);
		        
			// 执行查询
			ResultSet rs =statement.executeQuery();
			if(!rs.next())
	        {
	        	
	        	show("执行查询资产属性出错，未找到对应rowid");
	        	connection.close();	
	        	return false;
	        }
	        boolean whether_all_none=true;
	        for(int i=2;i<=SZSC_asset.effect_limit;i++)
	        	if(rs.getInt("kind"+1)==SZSC_asset.kind_character_effect&&rs.getInt("effect"+i)!=SZSC_asset.code_none)
	        	{
	        		whether_all_none=false;
	        		break;
	        	}
	        connection.close();	
	        return whether_all_none;
	        
	        
		}catch ( Exception e ) {
			show("执行核对资产属性出错"+e.getMessage());
			
		}
		return false;*/
	}
	
	//删除角色，返回true表示成功，false表示失败
	static public boolean Character_delete_object(int rowid) {
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"Character_delete_object");
		szsc_DB_access.set_command(SQL_Character_delete);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())//如果出错，结果为删除失败
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
			Class.forName("org.sqlite.JDBC");
			        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
			        
			java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_delete);
			statement.setInt(1,rowid);
			statement.execute();
			connection.close();
		}catch ( Exception e ) {
			show("执行删除角色出错"+e.getMessage());
					result=false;
			}
		return result;*/
	}
	//获取该项资产各种属性
	static public SZSC.Asset get_Character_attribute(int rowid,int which){
		SZSC.Asset asset=null;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"get_Character_attribute");
		szsc_DB_access.set_command(SQL_Character_get_attribute);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		if(!szsc_DB_access.next()) {
			show("执行查询角色属性出错"+rowid+" "+which);
		}
		else {
			int user_ID=szsc_DB_access.getInt("user_ID");
			int kind=szsc_DB_access.getInt("kind"+which);
			int effect=szsc_DB_access.getInt("effect"+which);
			asset=new SZSC.Asset(rowid,user_ID,kind,effect);
		}
		szsc_DB_access.close();
		return asset;
		
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_get_attribute);

	        statement.setInt(1, rowid);
	        
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        if(!rs.next())
	        {
	        	connection.close();	
	        	show("执行查询角色属性出错"+rowid+" "+which);
	        	return null;
	        }
	        int user_ID=rs.getInt("user_ID");
	        int kind=rs.getInt("kind"+which);
	        int effect=rs.getInt("effect"+which);
	        asset=new SZSC_asset.Asset(rowid,user_ID,kind,effect);
	        
	        connection.close();	
	        return asset;
	        
	        
		}catch ( Exception e ) {
			show("get_Character_attribute 执行查询资产属性出错"+e.getMessage());
			
		}
		return null;*/
	}
	
	
	
	//获取该项资产各种属性
	static public SZSC.Asset get_asset_attribute(int rowid){
		SZSC.Asset asset=null;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"get_asset_attribute");
		szsc_DB_access.set_command(SQL_Asset_get_attribute);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		
		if(!szsc_DB_access.next()) {
			show("get_asset_attribute 执行查询资产属性出错，未找到对应rowid");
		}
		else {
			int user_ID=szsc_DB_access.getInt("user_ID");
	        int kind=szsc_DB_access.getInt("kind");
	        int code_number=szsc_DB_access.getInt("code_number");
	        asset=new SZSC.Asset(rowid,user_ID,kind,code_number);
		}
		szsc_DB_access.close();
		return asset;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_get_attribute);
	        statement.setInt(1, rowid);
	        
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        if(!rs.next())
	        {
	        	connection.close();	
	        	show("get_asset_attribute 执行查询资产属性出错，未找到对应rowid");
	        	return null;
	        }
	        int user_ID=rs.getInt("user_ID");
	        int kind=rs.getInt("kind");
	        int code_number=rs.getInt("code_number");
	        asset=new SZSC_asset.Asset(rowid,user_ID,kind,code_number);
	        
	        connection.close();	
	        return asset;
	        
	        
		}catch ( Exception e ) {
			show("get_asset_attribute 执行查询资产属性出错"+e.getMessage());
			
		}return null;*/
		
	}
	//查看所有权
	static public boolean Character_belong(int rowid,int user_ID) {
		boolean result=false;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Character_belong");
		szsc_DB_access.set_command(SQL_Character_search_belong);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		if(szsc_DB_access.next()) {
			if(user_ID==szsc_DB_access.getInt("user_ID"))
				result=true;
		}
		else {
			show("未查询到对应角色rowid "+rowid +"   user_ID  "+user_ID);
		}
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_search_belong);
	        statement.setInt(1, rowid);
	        
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        if(rs.next())
	        {
	        	belong_ID=rs.getInt("user_ID");
	        	if(belong_ID==user_ID)
	        	{
	        		connection.close();	
	        		return true;
	        	}
	        }
	        else {
	        	show("未查询到对应角色rowid "+rowid);
	        }
	        	
	        
	        connection.close();
		}catch ( Exception e ) {
			show("执行查询角色归属出错"+e.getMessage());
			
		}
		return false;*/
	}
	//查看所有权
	static public boolean Asset_belong(int rowid,int user_ID) {
		boolean result=false;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"Asset_belong");
		szsc_DB_access.set_command(SQL_Asset_search_belong);
		szsc_DB_access.set_value(1, rowid);
		szsc_DB_access.execute();
		
		if(szsc_DB_access.next()) {
			if(szsc_DB_access.getInt("user_ID")==user_ID)
				result=true;
		}
		else {
			show("未查询到对应asset rowid "+rowid +"   user_ID  "+user_ID);
		}
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_search_belong);
	        statement.setInt(1, rowid);

			int belong_ID;
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        if(rs.next())
	        {
	        	belong_ID=rs.getInt("user_ID");
	        	if(belong_ID==user_ID)
	        		result=true;
	        }
	        	
	        
	        connection.close();
		}catch ( Exception e ) {
			show("执行查询资产归属出错"+e.getMessage());
			
		}
		return result;*/
	}
	
	//修改一项
	static public boolean Character_change_object(SZSC.Asset asset,int which,int rowid) {
		boolean result=true;
		int pointer=1;
		String final_cmd=String.format(SQL_Character_update_effect, "effect"+which,"kind"+which);
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"Character_change_object");
		szsc_DB_access.set_command(final_cmd);
		szsc_DB_access.set_value(pointer++, asset.get_code_number());
		szsc_DB_access.set_value(pointer++, asset.get_kind());
		szsc_DB_access.set_value(pointer++, asset.get_user_ID());
		szsc_DB_access.set_value(pointer++, rowid);
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		
		/*
		try {
			Class.forName("org.sqlite.JDBC");
		        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		    
			String final_cmd=String.format(SQL_Character_update_effect, "effect"+which,"kind"+which);
			
			java.sql.PreparedStatement statement = connection.prepareStatement(final_cmd);
			
			statement.setInt(1, asset.get_code_number());
			statement.setInt(2, asset.get_kind());
			statement.setInt(3, asset.get_user_ID());
			statement.setInt(4, rowid);
			
			statement.execute();
			connection.close();
		}catch ( Exception e ) {
			show("执行修改角色词条出错"+e.getMessage());
			result=false;
			
			}
		return result;*/
	}
	//修改角色名称
	static public boolean Character_change_name(int user_ID,int character_rowid,String name) {
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"Character_change_name");
		szsc_DB_access.set_command(SQL_Character_update_name);
		szsc_DB_access.set_value(1, name);
		szsc_DB_access.set_value(2, user_ID);
		szsc_DB_access.set_value(3, character_rowid);
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
			Class.forName("org.sqlite.JDBC");
		        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		    
			
			java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_update_name);
			
			statement.setString(1, name);
			statement.setInt(2, user_ID);
			statement.setInt(3, character_rowid);
			
			statement.execute();
			connection.close();
		}catch ( Exception e ) {
			show("执行修改角色名称出错"+e.getMessage());
			result=false;
			
			}
		return result;*/
	}
	//新增一项
	static public boolean Character_insert_object(SZSC.Character character) {
		boolean result=true;
		int pointer=1;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"Character_insert_object");
		szsc_DB_access.set_command(SQL_Character_insert);
		szsc_DB_access.set_value(pointer++, SZSC_service.getCurrentTime());
		szsc_DB_access.set_value(pointer++,"untitled");
		szsc_DB_access.set_value(pointer++,character.get_user_ID());
		
		for(int i=0;i<SZSC_asset_process.effect_limit;i++)
		{
			szsc_DB_access.set_value(pointer++, character.get_kind(i));
		}
		for(int i=0;i<SZSC_asset_process.effect_limit;i++)
		{
			szsc_DB_access.set_value(pointer++, character.get_effect(i));
		}
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
			Class.forName("org.sqlite.JDBC");
		        
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		        
			java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Character_insert);
			int pointer=1;
			statement.setString(pointer++,SZSC_service.getCurrentTime());
			statement.setString(pointer++,"untitled");
			statement.setInt(pointer++,character.get_user_ID());
			
			for(int i=0;i<SZSC_asset.effect_limit;i++)
			{
				statement.setInt(pointer++, character.get_kind(i));
			}
			for(int i=0;i<SZSC_asset.effect_limit;i++)
			{
				statement.setInt(pointer++, character.get_effect(i));
			}
			
		       
			statement.execute();
			connection.close();
		}catch ( Exception e ) {
			show("执行新增角色出错"+e.getMessage());
			success=false;
			}
		return success;*/
	}
	
	//修改用户资产数量
	static public boolean asset_change_mount(SZSC.Asset asset,boolean increase) {
		boolean result=false;
		//先获取当前用户资产
		int mount=asset_get_mount(asset);

		
		if (increase) {
			if(mount==SZSC_asset_process.code_none)//如果资产数目不存在，进行insert添加
				result=asset_insert_object(asset);
			else//如果资产数目存在，则+1
				result=asset_set_mount(asset,mount+1);
		}
		else {
			switch (mount) {
				case 0:
					show("数量已为0还在减少 ID:"+asset.get_user_ID()+" kind:"+asset.get_kind()+" code_number:"+asset.get_code_number());
					break;
				case SZSC_asset_process.code_none:
					show("并不存在该物品，却还在减少 ID:"+asset.get_user_ID()+" kind:"+asset.get_kind()+" code_number:"+asset.get_code_number());
					break;
				default:
					result=asset_set_mount(asset,mount-1);
			}
			
		}
		
		return result;
	}
	//新插入一项
	static public boolean asset_insert_object(SZSC.Asset asset) {
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"asset_insert_object");
		szsc_DB_access.set_command(SQL_Asset_insert);
		szsc_DB_access.set_value(1, asset.get_user_ID());
		szsc_DB_access.set_value(2, asset.get_kind());
		szsc_DB_access.set_value(3, asset.get_code_number());
		szsc_DB_access.set_value(4, 1);
		szsc_DB_access.execute();
		
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_insert);
	        
	        statement.setInt(1, asset.get_user_ID());
	        statement.setInt(2, asset.get_kind());
	        statement.setInt(3, asset.get_code_number());
	        statement.setInt(4, 1);
	       
	        statement.execute();
	        connection.close();
		}catch ( Exception e ) {
			show("执行新增资产数量出错"+e.getMessage());
			result=false;
		}
		return result;*/
	}
	static private boolean asset_set_mount(SZSC.Asset asset,int mount) {
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"asset_set_mount");
		szsc_DB_access.set_command(SQL_Asset_update);
		szsc_DB_access.set_value(1, mount);
		szsc_DB_access.set_value(2, asset.get_user_ID());
		szsc_DB_access.set_value(3, asset.get_kind());
		szsc_DB_access.set_value(4, asset.get_code_number());
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        java.sql.Connection connection = java.sql.DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_update);
	        statement.setInt(1, mount);
	        statement.setInt(2, asset.get_user_ID());
	        statement.setInt(3, asset.get_kind());
	        statement.setInt(4, asset.get_code_number());
	        
	        statement.execute();
	        connection.close();
		}catch ( Exception e ) {
			show("执行修改资产数量出错"+e.getMessage());
			result=false;
		}
		return result;*/
	}
	//根据角色id获取角色名称
	static public String character_get_name(int character_rowid) {
		String character_name="not found";
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"character_get_name");
		szsc_DB_access.set_command(SQL_Character_get_name);
		szsc_DB_access.set_value(1, character_rowid);
		szsc_DB_access.execute();
		if(szsc_DB_access.next()) {
			character_name=szsc_DB_access.getString("name");
		}
		szsc_DB_access.close();
		return character_name;
	}
	
	
	
	//获取用户资产
	static public int asset_get_mount(SZSC.Asset asset) {
		int mount=SZSC_protocol.code_none;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"asset_get_mount");
		szsc_DB_access.set_command(SQL_Asset_search_mount);
		szsc_DB_access.set_value(1, asset.get_user_ID());
		szsc_DB_access.set_value(2, asset.get_kind());
		szsc_DB_access.set_value(3, asset.get_code_number());
		szsc_DB_access.execute();
		if(szsc_DB_access.next()) {
			mount=szsc_DB_access.getInt("mount");
		}
		szsc_DB_access.close();
		return mount;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Asset_search_mount);
	        statement.setInt(1, asset.get_user_ID());
	        statement.setInt(2, asset.get_kind());
	        statement.setInt(3, asset.get_code_number());
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        if(rs.next())
	        {
	        	mount=rs.getInt("mount");
	        }
	        	
	        
	        connection.close();
		}catch ( Exception e ) {
			show("执行查询资产数量出错"+e.getMessage());
			
		}
		return mount;*/
	}
	
	
	//系统初始化
	static public void SZSC_DB_ini() {
		command(DROP_Character_TABLE);
		command(DROP_DIAMOND_TABLE);
		command(DROP_Asset_TABLE);
    	command(sql_create_Diamond_table);
    	command(sql_create_Asset_table);
    	command(sql_create_Character_table);
	}
	
	static public void user_register(String user_name) {
		if(create_user(user_name))
			user_get_diamond(get_user_ID(user_name),10000);//发放初始资金
		
	}
	
	static public boolean create_user(String user_name){
		boolean result=true;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"create_user");
		szsc_DB_access.set_command(SQL_insert_user);
		szsc_DB_access.set_value(1, user_name);
		szsc_DB_access.set_value(2, 0);
		szsc_DB_access.execute();
		
		if(szsc_DB_access.get_wrong())
			result=false;
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_insert_user);
	        statement.setString(1, user_name);
	        statement.setInt(2, 0);
	        // 执行查询
	        statement.execute();
	        connection.close();
		}catch ( Exception e ) {
			show("执行创建出错"+e.getMessage());
			return false;
		}
		return true;*/
	}

	static public int get_user_ID(String user_name) {
		int user_ID=SZSC_protocol.code_none;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"get_user_ID");
		szsc_DB_access.set_command(SQL_GET_USER_ID);
		szsc_DB_access.set_value(1, user_name);
		szsc_DB_access.execute();
		if(!szsc_DB_access.next())
		{
			show("执行查询用户名对应user_id出错 user_name="+user_name);
		}
		else {
			user_ID=szsc_DB_access.getInt("rowid");
		}
		szsc_DB_access.close();
		return user_ID;
		
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_GET_USER_ID);
	        statement.setString(1, user_name);
            
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        user_ID=rs.getInt("rowid");
	        connection.close();
	        
		}catch ( Exception e ) {
			show("执行查询出错"+e.getMessage());
		}
		if(user_ID==find_nothing)
			show("未找到该用户");
		return user_ID;*/
	}
	
	
	
	//查询用户拥有钻石数量
	static public int inquire_user_diamond(int user_ID) {
		
		int diamonds_mount=SZSC_protocol.code_none;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_inquire,"inquire_user_diamond");
		szsc_DB_access.set_command(SQL_Get_User_Diamond);
		szsc_DB_access.set_value(1, user_ID);
		szsc_DB_access.execute();
		if(!szsc_DB_access.next())
		{
			show("执行查询用户名对应diamond出错 user_ID="+user_ID);
		}
		else {
			diamonds_mount=szsc_DB_access.getInt("diamonds");
		}
		szsc_DB_access.close();
		return diamonds_mount;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Get_User_Diamond);
	        statement.setInt(1, user_ID);
            
	        // 执行查询
	        ResultSet rs =statement.executeQuery();
	        diamonds_mount=rs.getInt("diamonds");
	        connection.close();
	        
		}catch ( Exception e ) {
			show("执行出错"+e.getMessage());
		}
		if(diamonds_mount==find_nothing)
			show("未找到该用户");
		return diamonds_mount;*/
	}
	
	//用户花费钻石
	static public boolean user_cost_diamond(int user_ID,int cost) {
		int current_mount=inquire_user_diamond(user_ID);
		if(current_mount<cost)
			return false;
		change_user_diamond(user_ID,cost,false);
		return true;
	}
	//用户获取钻石
	static public void user_get_diamond(int user_ID,int mount) {
		change_user_diamond(user_ID,mount,true);
	}
	static private boolean change_user_diamond(int user_ID,int mount,boolean increase)
	{
		boolean result=true;
		int final_value=inquire_user_diamond(user_ID);
		if(increase)
			final_value+=mount;
		else 
			final_value-=mount;
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"change_user_diamond");
		szsc_DB_access.set_command(SQL_Update_user_Diamond);
		szsc_DB_access.set_value(1, final_value);
		szsc_DB_access.set_value(2, user_ID);
		szsc_DB_access.execute();
		
		
		if (szsc_DB_access.get_wrong()) {
			result=false;
		}
		szsc_DB_access.close();
		return result;
		/*
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
	        
	        java.sql.PreparedStatement statement = connection.prepareStatement(SQL_Update_user_Diamond);
	        statement.setInt(1, final_value);
	        // 执行查询
	        statement.execute();
	        connection.close();
		}catch ( Exception e ) {
			show("执行改动钻石出错"+e.getMessage());
		}*/
	}
	
	static public void command(String sqlcommand)//单纯执行不获取结果
	{
		
		SZSC_DB szsc_DB_access=new SZSC_DB(SZSC_protocol.SZSC_excute_command,"command");
		szsc_DB_access.set_command(sqlcommand);
		szsc_DB_access.execute();
		szsc_DB_access.close();
		/*
		try {
            
        	Connection connection = null;
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
            
            Statement statement = connection.createStatement();
            
            statement.execute(sqlcommand);
            connection.close();
            
          } catch ( Exception e ) {
            show("数据库命令执行出错!\n"+ sqlcommand+"\n"+e.getClass().getName() + ": " + e.getMessage() );
          }*/
	}
	
	
	
	// 判断Diamond表中是否存在指定user_name的用户
	static public boolean checkDiamondUserExists(String username) {
	    boolean result = false;
	    
	    SZSC_DB szsc_DB_access = new SZSC_DB(SZSC_protocol.SZSC_excute_inquire, "checkDiamondUserExists");
	    // 定义查询指定user_name是否存在的SQL语句
	    String SQL_CHECK_USER_EXISTS = "SELECT user_name FROM Diamond WHERE user_name = ?";
	    szsc_DB_access.set_command(SQL_CHECK_USER_EXISTS);
	    szsc_DB_access.set_value(1, username);
	    szsc_DB_access.execute();
	    
	    // 如果查询到结果，说明存在该用户
	    if (szsc_DB_access.next()) {
	        result = true;
	    }
	    
	    szsc_DB_access.close();
	    return result;
	}
}
