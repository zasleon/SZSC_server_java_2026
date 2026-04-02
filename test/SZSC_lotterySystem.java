package test;
import java.util.*;

public class SZSC_lotterySystem {
	
	private static void show(String msg) {
		core_main.show(msg);
	}
    // 概率配置
    private static final double HERO_PROBABILITY = 0.02;  // 可单独设置
    private static final double SOLDIER_PROBABILITY = 0.07; // 可单独设置
    private static final double ITEM_PROBABILITY = 1 - (HERO_PROBABILITY + SOLDIER_PROBABILITY);
        
    
    static {
        validateProbabilities();
    }
    private static void validateProbabilities() {
        if(ITEM_PROBABILITY<0)
            show("通用order抽奖概率设置错误!总和不能超过100%");
        
    }
 
    private static final Random random = new Random();
    //单次抽奖（根据动态概率计算）
    public static SZSC.PrizeResult drawSinglePrize() {
        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;

        // 检查英雄
        cumulativeProbability += HERO_PROBABILITY;
        if (rand < cumulativeProbability) {
            return getRandomName(SZSC_asset_process.kind_character_hero);
        }

        // 检查士兵
        cumulativeProbability += SOLDIER_PROBABILITY;
        if (rand < cumulativeProbability) {
            return getRandomName(SZSC_asset_process.kind_character_solider);
        }

        // 默认返回物品
        return getRandomName(SZSC_asset_process.kind_character_effect);
    }

    // 批量抽奖方法（每次count个物品）
    public static List<SZSC.PrizeResult> drawPrizes(int count) {
        List<SZSC.PrizeResult> prizes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            prizes.add(drawSinglePrize());
        }
        
        return prizes;
    }
    
    
    
    public static SZSC.Asset_bag start(int order_kind,int user_ID) {
    	List<SZSC.PrizeResult> prizes;
    	int total_mount=0;
    	switch (order_kind) {
		case SZSC_asset_process.order_10: 
			prizes = drawPrizes(10);
			total_mount=10;
			break;
		case SZSC_asset_process.order_1:
			prizes = drawPrizes(1);
			total_mount=1;
			break;
		default:
			show("订单异常Unexpected value: " + order_kind);
			return null;
		}
    	
    	int pointer=0;
    	SZSC.Asset_bag asset_bag=new SZSC.Asset_bag(total_mount,user_ID);
    	for (SZSC.PrizeResult prize : prizes) {
        	//core_main.show(prize.getName()+" "+prize.getcode());
        	asset_bag.set_Asset(pointer++, SZSC_asset_process.code_none, user_ID, prize.getType(), prize.getcode(), 1);
        }
    	
    	return asset_bag;
    	
    }
 
    
    
    
    
 
 // 从Excel随机获取一个该类型对应的一个名字
    public static SZSC.PrizeResult getRandomName(int type) {
    	
    	List<SZSC.General_Info> general_Infos=SZSC_game_dictionary.EXCEL_get_info_list(SZSC_protocol.Asset_EXCEL_PATH, "Sheet1", "类型",type);
    	if(!general_Infos.isEmpty())
    	{
    		int random_one=random.nextInt(general_Infos.size());
    		SZSC.General_Info general_Info=general_Infos.get(random_one);
    		String character_name=general_Info.get_string("name");
    		int character_ID=general_Info.get_int("No");
    		return new SZSC.PrizeResult(type,character_name, character_ID);
    		
    	}
    	return null;
    	
    	
        
    	
    }
    
}
