package test;

public class VV_DB_protocol {
	public static String AV_path="日本";
	public static String Animation_path="动漫";
	public static String Porn_path="欧美";
	
	public static final int VV_result_limit=10;//单次搜索上限内容

    public static final int kind            =4;
    public static final int VV_content      =-1;//搜索字段内容
    public static final int VV_random	    =0;//随机
    public static final int VV_AV			=1;//日本
    public static final int VV_Porn			=2;//欧美
    public static final int VV_animation	=3;//动漫
    //public static final int VV_comic		=4;//漫画
    //public static final int VV_domestic		=5;//国产
    public static final int VV_new          =6;//最新
    


    public static final int VV_user_apply_search	    =30000;//用户申请搜索
    public static final int VV_user_apply_column	    =30001;//用户申请查看专栏信息
    public static final int VV_user_apply_add_new_AV	=30002;//用户申请添加新视频数据
    public static final int VV_user_apply_change_AV	    =30003;//用户申请修改视频数据
    public static final int VV_user_apply_detail_AV	    =30004;//用户申请查看指定ID视频详细信息
    public static final int VV_user_apply_see_AV		=30005;//用户申请查看此ID视频

    public static final int VV_user_apply_see_VV		=30007;//用户申请查看此视频

    public static final int VV_search_result		    =31000;//统一向用户发布搜寻结果信息
    public static final int VV_toast				    =31001;//信息提示
    public static final int VV_show_this_video_detail	=31002;//显示该AV视频详细信息
    public static final int VV_show_this_video			=31003;//提供视频路径，客户端打开播放
}
