package test;


import java.text.SimpleDateFormat;

import java.util.*;

public class core_main {
	
	public static int getrandom(int bottomlimit,int uplimit) {
		if(bottomlimit==uplimit)
			return bottomlimit;
		Random random = new Random();
		if(bottomlimit>=uplimit) {
			show("随机取值错误 "+bottomlimit+" "+uplimit);
			return bottomlimit;
		}
		
        int randomNumber = random.nextInt(uplimit) + bottomlimit;
        return randomNumber;
	}
	
	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	public static void sleep(int time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO: handle exception
			
		}
	}
	public static void show(String content)
	{
		System.out.println(content);
	}
	
	
	public static Client[] clientHandlers=new Client[core_protocol.memberlimit];

	
	
	
	
	
	
    //程序入口
    public static void main(String[] args) {
        
        
    	SZSC_asset_process.checkAndCreateDB(SZSC_protocol.DEFAULT_PATH_STRING, SZSC_protocol.DB_path);
    	
    	SZSC_asset_command.SZSC_DB_ini();
    	
    	
    	/*
    	int user_ID=SZSC_DB.get_user_ID("123");
    	show("钻石数量"+SZSC_DB.get_user_diamond(user_ID));
    	
    	show("钻石数量"+SZSC_DB.get_user_diamond(user_ID));
    	SZSC_asset.provide_user_asset(user_ID, 20, 100010, 1);
    	SZSC_asset.provide_user_asset(user_ID, 40, 20000330, 1);
    	
    	for (int i = 1; i <= 2; i++) {
    		System.out.println("第" + i + "次抽奖结果:");
            SZSC_asset.purchase("123",SZSC_asset.order_10);
        }
        show("\n抽奖后钻石数量"+SZSC_DB.get_user_diamond(user_ID));  
        SZSC_asset.show_user_all_asset("123");
        show("开始新增角色");
        SZSC_asset.create_character("123", 1);
        show("展示角色");
        SZSC_asset.show_user_all_character("123");
        show("创建角色后资产");
        SZSC_asset.show_user_all_asset("123");
        show("修改角色");
        SZSC_asset.update_character("123",true, 1,2,2);
        SZSC_asset.update_character("123",false, 1,SZSC_asset.code_none,2);
        show("展示角色");
        SZSC_asset.show_user_all_character("123");
        show("修改角色后资产");
        SZSC_asset.show_user_all_asset("123");*/
    	
    	/*SZSC.Launch_Info situation_Info=new SZSC.Launch_Info(40);
    	SZSC_player p1=new SZSC_player();
    	SZSC_game_effect.launch_effect( null, null, p1, null,situation_Info);*/
    	
    	SZSC_room.SZSC_room_ini();
    	
        VV_DB.VV_DB_ini();
        network.ini(core_protocol.PORT);
    }

    
    

    

    
	
	
}
