package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.lang.ref.Cleaner;

public class SZSC_DB implements AutoCloseable{
	private static void show(String msg) {
		SZSC_service.show(msg);
	}
	
	private Connection connection;
	private PreparedStatement preparedstatement;
	private String command;
	private ResultSet rs;
	
	
	private boolean got_wrong;//是否出错
	private int command_type;
	private String function_name;
	
	
	public void wrong(String msg) {
		show("函数"+function_name+"出错!\n"+msg);
		got_wrong=true;
	}
	public boolean get_wrong() {
		return got_wrong;
	}
	
	public SZSC_DB(int command_type,String function_name) {
		this.command_type=command_type;
		this.function_name=function_name;
		got_wrong=false;
		
		try {

			//System.setProperty("org.sqlite.lib.loadNative", "false");
			System.setProperty("org.sqlite.lib.native", "false");
			Class.forName("org.sqlite.JDBC");
	        
			connection = DriverManager.getConnection("jdbc:sqlite:"+SZSC_protocol.DB_path);
		}catch (Exception e) {
			wrong("执行数据库访问出错"+e.getMessage());
			
		}
		
	}
	public void set_command(String command) {
		this.command=command;
		try {
			
			preparedstatement= connection.prepareStatement(command);
			
			
			
		} catch (Exception e) {
			wrong("设置数据库访问command出错"+e.getMessage()+"\n执行的命令为:"+command);
		}
	}
	
	
	public void set_value(int which,int value) {
		try {
			preparedstatement.setInt(which, value);
		}catch (Exception e) {
			wrong("执行数据库设置command值出错  "+which+" "+value+"\n"+e.getMessage());
			
		}
	}
	public void set_value(int which,String value) {
		try {
			preparedstatement.setString(which, value);
		}catch (Exception e) {
			wrong("执行数据库设置command值出错  "+which+" "+value+"\n"+e.getMessage());
			
		}
	}
	
	public ResultSet execute() {
		try {
			switch (command_type) {
			case SZSC_protocol.SZSC_excute_command: {
				preparedstatement.execute();
				return null;
			}
			case SZSC_protocol.SZSC_excute_inquire:{
				rs=preparedstatement.executeQuery();
				return rs;
			}
			default:
				show("错误的command_type "+command_type);
			}
			
			close();
			
		} catch (Exception e) {
			wrong("执行数据库访问command出错  "+command_type+"  "+e.getMessage()+"\n执行命令为 "+command);
			
		}
		return null;
	}
	public boolean next() {
		boolean result=false;
		try {
			result=rs.next();
		}catch (Exception e) {
			wrong("执行数据库rs.next出错  "+e.getMessage());
			
		}
		return result;
		
	}
	public String getString(String name){
		String result="wrong";
		try {
			result=rs.getString(name);
		}catch (Exception e) {
			wrong("执行数据库getString出错  参数:"+name+"\n"+e.getMessage());
		}
		return result;
	}
	public int getInt(String name){
		int result=SZSC_asset_process.code_none;
		try {
			result=rs.getInt(name);
		}catch (Exception e) {
			wrong("执行数据库getint出错  参数:"+name+"\n"+e.getMessage());
			
		}
		return result;
	}
	@Override
	public void close() {
		
		try {
			if (rs != null) {
	            rs.close();
	        }
	        if (preparedstatement != null) {
	            preparedstatement.close();
	        }
	        if (connection != null && !connection.isClosed()) {
	            connection.close();
	        }
            
		}catch (Exception e) {
			wrong("执行关闭数据库访问出错"+e.getMessage());
			
		}
	}
	

}
