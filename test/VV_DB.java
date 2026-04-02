package test;
import java.io.*;
import java.lang.classfile.instruction.ReturnInstruction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;


public class VV_DB {
	public static void show(String content)
	{
		System.out.println(content);
	}
	static String DB_path="D:\\project\\eclipse_test\\test\\VV.db";
	static String folder_absolute_Path = "E:/ppsspp/terrible/大四杂物";
	
	
	static final String SQL_refresh_AV_ID="UPDATE AV "
			+ "SET ID = ROWID";
	static final String SQL_refresh_Porn_ID="UPDATE Porn "
			+ "SET ID = ROWID";
	static final String SQL_DROP_Porn_TABLE="Drop Table Porn";
	static final String SQL_DROP_AV_TABLE="Drop Table AV";
	static final String sql_create_AV_table= "Create Table AV(ID AUTO_INCREMENT PRIMARY KEY,"
    		+ "AV_Number VARCHAR(50) NOT NULL UNIQUE,"
    		+ "chinese_name VARCHAR(100) NOT NULL,"
    		+ "original_name VARCHAR(100) NOT NULL,"
    		+ "refresh_time VARCHAR(50) NOT NULL,"
    		+ "public_time VARCHAR(100) NOT NULL,"
    		+ "img_type VARCHAR(30) NOT NULL,"
    		+ "v_type VARCHAR(30) NOT NULL,"
    		+ "v_path  VARCHAR(200) NOT NULL,"
    		+ "img_path  VARCHAR(200) NOT NULL,"
    		+ "description VARCHAR(100) NOT NULL,"
    		+ "kind INT NOT NULL"
    		+ ");";
	
	static final String sql_create_Porn_table= "Create Table Porn("
			 +"ID AUTO_INCREMENT PRIMARY KEY,"
			 +"kind INT NOT NULL,"
			 +"chinese_name VARCHAR(100) NOT NULL,"
			 +"refresh_time VARCHAR(50) NOT NULL,"
			 +"v_path  VARCHAR(200) NOT NULL"
			 +");";
	
	//执行sql命令，不返回查询值
	static public void command(String sqlcommand)
	{
		try {
            
        	Connection connection = null;
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection("jdbc:sqlite:"+DB_path);
            
            Statement statement = connection.createStatement();
            
            statement.execute(sqlcommand);
            
            
          } catch ( Exception e ) {
            show("数据库命令执行出错!\n"+ sqlcommand+"\n"+e.getClass().getName() + ": " + e.getMessage() );
          }
	}
	
	public static String insertPercentSigns(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sb.append(str.charAt(i));
            if (i < str.length() - 1) {
                sb.append('%');
            }
        }
        return sb.toString();
    }
	
	//执行sql命令，返回查询值
static public String command_search_column(int kind,int page)
{
	JSON_process reply_msg=new JSON_process();
	int total_mount=0;int total_page=0;int result_number=0;
	String sql="";
	switch (kind) {
	
		case VV_DB_protocol.VV_AV:
			sql="SELECT AV_Number , chinese_name, kind , ID , refresh_time , img_path FROM AV ORDER BY refresh_time DESC;";
			break;
		
		case VV_DB_protocol.VV_animation:
		case VV_DB_protocol.VV_Porn:
			sql="SELECT chinese_name, kind , ID , refresh_time FROM Porn where kind="+kind+" ORDER BY refresh_time DESC;";
			break;
		case VV_DB_protocol.VV_random:
			sql="SELECT AV_Number , chinese_name, kind , ID , refresh_time , img_path FROM AV ORDER BY refresh_time DESC;";
			break;
	}
	
	
	show(sql);
	
	try {
        int count=0;
        Class.forName("org.sqlite.JDBC");
        
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+DB_path);
        
        Statement statement = connection.createStatement();
        
        // 执行查询
        ResultSet rs =statement.executeQuery(sql);
        
        //判断是否只搜索到一个数据
        boolean only_one=false;
        while (rs.next()){
        	total_mount++;
        }
        if(total_mount==1)
        	only_one=true;
        rs =statement.executeQuery(sql);//重置查询
		
        
        boolean randomPick[]=new boolean[total_mount];
        if(kind==VV_DB_protocol.VV_random)
        {
        	// 初始化全为false
            for (int i = 0; i < total_mount; i++) {
            	randomPick[i] = false;
            }
            // 随机选择20个位置设为true
            Random rand = new Random();
            for (int i = 0; i < VV_DB_protocol.VV_result_limit; i++) {
                int j = i + rand.nextInt(total_mount - i);
                // 交换位置
                
                if(randomPick[j])
                {
                	i--;
                	continue;
                }
                randomPick[j] = true;
            }
            /*for (int i=0;i<total_mount;i++) {
                if (randomPick[i]) show("随机"+i);
            }*/
        }
        	
        //处理查询结果
        while (rs.next()){
        	
        	if(kind==VV_DB_protocol.VV_random&&randomPick[count])
        	{
        		String chinese_name=rs.getString("chinese_name");
        		if(chinese_name.length()<1)chinese_name="null";
        		reply_msg.addToArray("name", chinese_name);
	            reply_msg.addToArray("AV_Number",  rs.getString("AV_Number"));
	            reply_msg.addToArray("kind",  kind);
	            reply_msg.addToArray("img_path",  rs.getString("img_path"));
	            reply_msg.addToArray("ID",  rs.getInt("ID"));
	            result_number++;
        	}
        	else
        	if(kind!=VV_DB_protocol.VV_random)
        	if(count<VV_DB_protocol.VV_result_limit*page&&count>=VV_DB_protocol.VV_result_limit*(page-1))
        	{
        		String chinese_name=rs.getString("chinese_name");
        		if(chinese_name.length()<1)chinese_name="null";
	            String refresh_time= rs.getString("refresh_time");
	            int ID= rs.getInt("ID");
	            String AV_Number="null";
	            String img_path="null";
	            if(kind==VV_DB_protocol.VV_AV)
	            { 
	            	img_path=rs.getString("img_path");
	            	AV_Number= rs.getString("AV_Number");
	            }
	            if(only_one)
	            {
	            	reply_msg.add("name", chinese_name);
		            reply_msg.add("AV_Number",  AV_Number);
		            reply_msg.add("kind",  kind);
		            reply_msg.add("img_path",  img_path);
		            reply_msg.add("ID",  ID);
	            }
	            else {
		            show((count+1)+" "+kind+" "+refresh_time+" "+AV_Number+" "+chinese_name);
		            reply_msg.addToArray("name", chinese_name);
		            reply_msg.addToArray("AV_Number",  AV_Number);
		            reply_msg.addToArray("kind",  kind);
		            reply_msg.addToArray("img_path",  img_path);
		            reply_msg.addToArray("ID",  ID);
	            }
	            result_number++;
        	}
            count++;
           
        }
        
        
      } catch ( Exception e ) {
        show("数据库命令执行出错!\n"+ sql+"\n"+ e.getClass().getName() + ": " + e.getMessage() );
        reply_msg.add("signal", VV_DB_protocol.VV_toast);
        reply_msg.add("content", "数据库命令执行出错!");
      }
	
	if(total_mount%VV_DB_protocol.VV_result_limit==0)
		total_page=total_mount/VV_DB_protocol.VV_result_limit;
		
	else
		total_page=total_mount/VV_DB_protocol.VV_result_limit+1;
	show("总量:"+total_mount);show("总页数:"+total_page);show("当前页数"+page);
	if(total_mount==0){
		reply_msg.add("content", "未搜索到任何数据!");//当次搜索到项目的总数
	    reply_msg.add("signal", VV_DB_protocol.VV_toast);
	    return reply_msg.getString();	
    }
	if(kind==VV_DB_protocol.VV_random)
		reply_msg.add("total_page", 1);
	else
		reply_msg.add("total_page", total_page);
	reply_msg.add("current_page", page);
	reply_msg.add("result_number", result_number);//当次搜索到项目的总数
	reply_msg.add("signal", VV_DB_protocol.VV_search_result);
	return reply_msg.getString();			
}
		
		
	
	
	
	//执行sql命令，返回查询值
static public String command_search(String content,int page)
{
	JSON_process reply_msg=new JSON_process();
	int result_number=0;
	int total_mount=0;
	int total_page=0;
	String name="%"+insertPercentSigns(content)+"%";
	String sql ="SELECT AV_Number , chinese_name, kind , ID , refresh_time , img_path "
			+ "FROM ("
			+ "SELECT ID , kind , chinese_name, refresh_time , NULL AS AV_Number,NULL AS img_path "
			+ "FROM Porn "
			+ "WHERE chinese_name LIKE '"+name+"' "
			+ "UNION ALL "
			+ "SELECT ID , kind , chinese_name, refresh_time , AV_Number, img_path "
			+ "FROM AV "
			+ "WHERE chinese_name LIKE '"+name+"' OR AV_Number LIKE '"+name+"' "
			+ ") AS combined_results "
			+ "ORDER BY refresh_time DESC "
			+";";
			//+ "LIMIT "+(VV_DB_protocol.VV_result_limit)
			//+ " OFFSET "+(VV_DB_protocol.VV_result_limit*(offset-1))+";";
	//show(sql);
	
	try {
        int count=0;
        Class.forName("org.sqlite.JDBC");
        
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+DB_path);
        
        Statement statement = connection.createStatement();
        
     
        // 执行查询
        ResultSet rs =statement.executeQuery(sql);
        //判断是否只搜索到一个数据
        boolean only_one=false;
        while (rs.next()){
        	total_mount++;
        }
        if(total_mount==1)
        	only_one=true;
        rs =statement.executeQuery(sql);//重置查询
		
        //处理查询结果
        while (rs.next()){
        	
        	if(count<VV_DB_protocol.VV_result_limit*page&&count>=VV_DB_protocol.VV_result_limit*(page-1))
        	{
	            String chinese_name = rs.getString("chinese_name");
	            if(chinese_name.length()<1)chinese_name="null";
	            
	            String refresh_time= rs.getString("refresh_time");
	            int ID= rs.getInt("ID");
	            int kind= rs.getInt("kind");
	            String img_path="null";
	            String AV_Number="null";
	            if(kind==VV_DB_protocol.VV_AV)
	            {
	            	AV_Number= rs.getString("AV_Number");
	            	img_path=rs.getString("img_path");
	            }
	            
	            if(only_one)
	            {
	            	reply_msg.add("name", chinese_name);
		            reply_msg.add("AV_Number",  AV_Number);
		            reply_msg.add("kind",  kind);
		            reply_msg.add("img_path",  img_path);
		            reply_msg.add("ID",  ID);
	            }
	            else
	            {
		            //show((count+1)+" "+refresh_time+" "+AV_Number+" "+chinese_name);
		            reply_msg.addToArray("name", chinese_name);
		            reply_msg.addToArray("AV_Number",  AV_Number);
		            reply_msg.addToArray("kind",  kind);
		            reply_msg.addToArray("img_path",  img_path);
		            reply_msg.addToArray("ID",  ID);
		            
	            }
	            result_number++;
            
            }
            count++;
           
        }
       
      } catch ( Exception e ) {
        show("数据库命令执行出错!\n"+ sql+"\n"+ e.getClass().getName() + ": " + e.getMessage() );
      
      }
	show("总量:"+total_mount);
	if(total_mount%VV_DB_protocol.VV_result_limit==0)
		total_page=total_mount/VV_DB_protocol.VV_result_limit;
		
	else
		total_page=total_mount/VV_DB_protocol.VV_result_limit+1;
	//show("总页数:"+total_page);
	if(total_mount==0){
		reply_msg.add("content", "未搜索到任何数据!");//当次搜索到项目的总数
	    reply_msg.add("signal", VV_DB_protocol.VV_toast);
	    return reply_msg.getString();	
    }
	reply_msg.add("total_page", total_page); 
	reply_msg.add("result_number", result_number);//当次搜索到项目的总数
	reply_msg.add("current_page", page);
    reply_msg.add("signal", VV_DB_protocol.VV_search_result);
    
	return reply_msg.getString();
}
	
	//获取txt文本里所有内容
	static public String get_txt_content(String file_path) {
		
		String whole_content="";
		try (FileInputStream fis = new FileInputStream(file_path);
	             InputStreamReader isr = new InputStreamReader(fis, "GBK"); // 指定GBK编码
	             BufferedReader br = new BufferedReader(isr)) {
	            
			String content;
	            while ((content = br.readLine()) != null) {
	                whole_content=whole_content+content;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		return whole_content;
		
	}
	
	public static String get_relative_path(String absolute_Path,String current_Path ) {
		// 使用nio.file.Paths和Paths.get方法将File对象转换为Path对象
        Path rootPath = Paths.get(new File(absolute_Path).getAbsolutePath());
        Path targetPath = Paths.get(current_Path);

        // 使用relativize方法获取相对路径
        Path relativePath = rootPath.relativize(targetPath);
        return relativePath.toString();
	}
	
	// 提取文件后缀名的方法
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex != fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";
        }
    }
    
    public static String getFile_refresh_time(long refresh_time) {
    	Date lastModifiedDate = new Date(refresh_time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       return dateFormat.format(lastModifiedDate);
	}
    
    
 	
 	public static String InsertPercentBeforeEachSingleQuote(String originalString) {
 		StringBuilder newString = new StringBuilder();

        // 遍历原始字符串的每一个字符
        for (int i = 0; i < originalString.length(); i++) {
            char currentChar = originalString.charAt(i);
            // 如果当前字符是'
            if (currentChar == '\'') {
                // 在'前插入'\'
                newString.append('\'');
            }
            // 追加原始字符
            newString.append(currentChar);
        }

        return newString.toString();
    }
 	//遍历文件夹
	 	public static void traverseFolder_Other(File folder,int kind) {
	         File[] files = folder.listFiles();
	         if (files != null) {
	         	
	         	
	     		String chinese_name="";
	     		
	     		String refresh_time="";
	     		
	     		String v_path="";
	     		
	             for (File file : files) {
	                 if (file.isDirectory()) {
	                     // 如果是文件夹，则递归遍历
	                	 traverseFolder_Other(file,kind);
	                 } else {
	                 	
	                     // 如果是文件，则处理文件
	                 	String fileName = file.getName();
	               
	                     String fileExtension = getFileExtension(fileName);

	                     switch (fileExtension.toLowerCase()) {
	                         case "txt":
	                             break;
	                         case "mp4":
	                         case "mkv":
	                         {
	                         	 refresh_time = getFile_refresh_time(file.lastModified());
	                             chinese_name=file.getName();
	                             v_path=get_relative_path(folder_absolute_Path,InsertPercentBeforeEachSingleQuote(file.getAbsolutePath()));
	                             show(kind+" "+refresh_time+" "+chinese_name);
	                             chinese_name=InsertPercentBeforeEachSingleQuote(chinese_name);
	                             String commandString="Insert into Porn"
	              	            		+ "(kind,chinese_name,v_path,refresh_time)"
	              	            		+ "Values("
	              	            		+ kind+",'"+chinese_name+"','"+v_path+"','"+refresh_time+"');";
	              	            command(commandString);
	                         }
	                             break;
	                         case "jpg":
	                             break;
	                         default:
	                             // 其他格式的文件，可以选择忽略或进行其他处理
	                             break;
	                     }
	                 }
	             }
	             
	             
	 	            
	 	            
	         }
	    }	
     
    //遍历文件夹
	public static void traverseFolder_AV(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
        	Boolean txt_exist=false;
        	Boolean video_exist=false;
        	Boolean img_exist=false;
        	Boolean is_folder=true;
        	
        	String AV_number="";
    		String chinese_name="";
    		String original_name="";
    		String refresh_time="";
    		String public_time="";
    		String video_type="";
    		String img_type="";
    		String v_path="";
    		String img_path="";
    		String description="暂无";
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归遍历
                	traverseFolder_AV(file);
                } else {
                	is_folder=false;
                    // 如果是文件，则处理文件
                	String fileName = file.getName();
              
                    String fileExtension = getFileExtension(fileName);

                    switch (fileExtension.toLowerCase()) {
                        case "txt":
                        {
                    		JSON_process jsonObject=new JSON_process(get_txt_content(file.getPath()));
                    		AV_number=jsonObject.getString("番号");
                    		chinese_name=jsonObject.getString("中文名称");
                    		txt_exist=true;
                        }
                            break;
                        case "mp4":
                        case "mkv":
                        {
                        	refresh_time = getFile_refresh_time(file.lastModified());
                            video_type=fileExtension.toLowerCase();
                            v_path=get_relative_path(folder_absolute_Path,file.getAbsolutePath());
                            video_exist=true;
                        }
                            break;
                        case "jpg":
                        	img_path=get_relative_path(folder_absolute_Path,file.getAbsolutePath());
                            img_exist=true;
                            break;
                        default:
                            // 其他格式的文件，可以选择忽略或进行其他处理
                            break;
                    }
                }
            }
            //show(AV_number+"   "+formattedDate+"  "+chinese_name+"  ");
            
            if(!is_folder){//如果不是文件夹，校验是否有文件丢失
	            if(!txt_exist){
	            	show("出错!"+folder.getPath()+"丢失txt!");return;
	            }
	            if(!video_exist) {
	            	show("出错!"+folder.getPath()+"丢失video!");return;
	            }
	            if(!img_exist) {
	            	show("出错!"+folder.getPath()+"丢失img!");return;
	            }
	            String commandString="Insert into AV"
	            		+ "(AV_Number,kind,chinese_name,original_name,v_type,img_type,v_path,img_path,refresh_time,public_time,description)"
	            		+ "Values"
	            		+ "('"+AV_number+"',"+VV_DB_protocol.VV_AV+",'"+chinese_name+"','"+original_name+"','"
	            		+video_type+"','"+img_type+"','"+v_path+"','"+img_path+"','"+refresh_time+"','"+public_time+"','"
	            		+description+"');";
	            command(commandString);
	            
            }
        }
    }
	
	
	static private String watch_this_video(int ID,int kind) {
		
		JSON_process reply_msg=new JSON_process();
		String sql ="SELECT v_path "
				+ "FROM ("
				+ "SELECT v_path,ID,kind "
				+ "FROM Porn "
				+ "WHERE kind="+kind+" and ID="+ID+" "
				+ "UNION ALL "
				+ "SELECT v_path,ID,kind "
				+ "FROM AV "
				+ "WHERE kind="+kind+" and ID="+ID
				+ ") AS combined_results "
				+";";
		show(sql);
		
		try {
	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+DB_path);
	        
	        Statement statement = connection.createStatement();
	        // 执行查询
	        ResultSet rs =statement.executeQuery(sql);
	        //处理查询结果
	        if (rs.next()){
	        	
	            reply_msg.add("signal",VV_DB_protocol.VV_show_this_video);
	            reply_msg.add("path", rs.getString("v_path"));
	            
	        }
	        else {
				reply_msg.add("signal", VV_DB_protocol.VV_toast);
				reply_msg.add("content", "数据错误未找到该视频");
			}
	            
	        
	        
	      } catch ( Exception e ) {
	        show("数据库命令执行出错!\n"+ sql+"\n"+ e.getClass().getName() + ": " + e.getMessage() );
	      }
		
		//show("总页数:"+total_page);
	    reply_msg.add("v_type", "null");
		return reply_msg.getString();
	}
	
	private static String watch_this_video_details(int ID) {
		JSON_process reply_msg=new JSON_process();
		
		String sql ="SELECT AV_Number,ID,refresh_time,chinese_name,description,img_path,original_name "
				+ "FROM AV "
				+ "WHERE ID="+ID+" "
				+";";
		
		
		show(sql);
		
		try {

	        Class.forName("org.sqlite.JDBC");
	        
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+DB_path);
	        
	        Statement statement = connection.createStatement();
	        // 执行查询
	        ResultSet rs =statement.executeQuery(sql);
	        //处理查询结果
	        if (rs.next()){
	        	reply_msg.add("signal",VV_DB_protocol.VV_show_this_video_detail);
	            reply_msg.add("AV_Number", rs.getString("AV_Number"));
	            reply_msg.add("ID", rs.getInt("ID"));
	            reply_msg.add("actor_number", 0);
	            reply_msg.add("chinese_name", rs.getString("chinese_name"));
	            reply_msg.add("description", rs.getString("description"));
	            reply_msg.add("img_path", rs.getString("img_path"));
	            reply_msg.add("original_name", rs.getString("original_name"));
	            reply_msg.add("date", rs.getString("refresh_time"));
	        }
	        else {
				reply_msg.add("signal", VV_DB_protocol.VV_toast);
				reply_msg.add("content", "数据错误未找到该视频");
			}
	            
	        
	        
	      } catch ( Exception e ) {
	        show("数据库命令执行出错!\n"+ sql+"\n"+ e.getClass().getName() + ": " + e.getMessage() );
	      }
		return reply_msg.getString();
		
	}
	
	static public String provide_service(JSON_process json_msg)
	{
		JSON_process reply_msg=new JSON_process();
		String result_msg="";
		switch (json_msg.getInt("signal")) {
		
		case VV_DB_protocol.VV_user_apply_column:
			result_msg=command_search_column(json_msg.getInt("kind"),json_msg.getInt("start_page"));
			break;
		case VV_DB_protocol.VV_user_apply_search:
			result_msg=command_search(json_msg.getString("content"),json_msg.getInt("start_page"));
			break;
		case VV_DB_protocol.VV_user_apply_see_AV:
			result_msg=watch_this_video(json_msg.getInt("ID"),VV_DB_protocol.VV_AV);
			break;
			
		case VV_DB_protocol.VV_user_apply_see_VV:
			result_msg=(watch_this_video(json_msg.getInt("ID"),json_msg.getInt("kind")));
			break;
			
		case VV_DB_protocol.VV_user_apply_detail_AV:
			result_msg=(watch_this_video_details(json_msg.getInt("ID")));
			break;
			default:
			{
				reply_msg.add("signal", VV_DB_protocol.VV_toast);
				reply_msg.add("content", "错误的服务信号!"+json_msg.getInt("signal"));
				return reply_msg.toString();
			}
		}
		reply_msg=new JSON_process(result_msg);
		reply_msg.add("VV_ip", network.ip_address+"/"+core_protocol.VV_PORT);
		return reply_msg.getString();
	}
	
	static public void VV_DB_ini() {
		command(SQL_refresh_AV_ID);
		command(SQL_refresh_Porn_ID);
		boolean go_scan=false;
		if(go_scan)
		{
			
			command(SQL_DROP_AV_TABLE);command(sql_create_AV_table);
			command(SQL_DROP_Porn_TABLE);command(sql_create_Porn_table);
			
			
			
			
			//扫描"日本"文件夹内所有
	        File folder = new File(folder_absolute_Path+"/"+VV_DB_protocol.AV_path);
	        traverseFolder_AV(folder);
	        
	        {
				//扫描"欧美"文件夹内所有
				File folder2 = new File(folder_absolute_Path+"/"+VV_DB_protocol.Porn_path);
				traverseFolder_Other(folder2,VV_DB_protocol.VV_Porn);
				//扫描"动漫"文件夹内所有
				File folder3 = new File(folder_absolute_Path+"/"+VV_DB_protocol.Animation_path);
				traverseFolder_Other(folder3,VV_DB_protocol.VV_animation);
	        }
		}
        //command_search("al",1);
		//show("开始查找");
        //command_search_column(VV_DB_protocol.VV_Porn,1);
        //show("结束!");
		
		
	}
}
