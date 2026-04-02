package test;


public class SZSC_Buff {
	
	//一般人物主动技能不会放到buff栏，在人物技能栏主动触发，但人物被动效果会自动添加到buff栏，如果人物效果被持续无效化，会在buff栏加个无效化人物效果buff，如果自己发动效果会进行判定，如果该效果来源是自身效果，则返回结果“发动失败”
	
	
		public boolean is_buff;
		private String buff_type;
		
		private int ID;//每个buff都有自己对应的id和名字，如果为SZSC_Buff_none说明该buff栏为空
		//效果来源ID，例如不死效果来源是黑曜剑，则是黑曜剑ID
		private boolean show;//是否显示给玩家看，有些是实现某些buff而临时创建的buff
		private String name;//buff名字
		private String add_type;//添加类型，可能是重置、累加、独立新增等
		private boolean positive;//是否主动，如果被动触发则在事件发生后尝试触发
		//如果是被动的，事件发生后，对buff条件尝试是否满足，如果满足，发动

		
		//一轮回一次和持续一轮回会做不同处理，
		//比如，你在x轮回的末尾那个回合发动效果，该效果限制一轮回1次，那你这回合用一次后下回合就到下轮回了，马上就能再用此效果
		//如果发动某效果持续一轮回，会一直持续到下个你发动回合者主回合的回合，而不是当你在某轮回最后一回合发动该效果，该回合结束后进入下个轮回该效果就消失了，该效果则会一直保持

		private String duration_type="";//效果持续时间类型，回合/轮回/永久
		private float duration_remain;//效果持续剩余时间，用“回合”计量单位来表示，如果类型为轮回，则按总人数x轮回持续数进行填充
		
		private boolean chain_level_limit;//限制触发条件，是否需要限制连锁层级
		private int chain_level;
		
		private String use_limit_type;//限制使用时域类型，无使用次数限制/此次游戏/1轮回/1回合/自己回合
		private int use_times_limit;//时域下的限制次数，比如一回合一次，那这里就是1 	每次时域更新后重置剩余使用量use_time	
		private int use_times_remain;//剩余使用量
		private int consume_value_per_time;//每次使用量	检测是否还剩使用次数，先查看是否符合发动条件condition，再看剩余使用量是否大于每次使用量
		private boolean whether_token;//可使用的量是否是token类型，如果是token类型，则不随时域限制次数，默认为false
		private int token_value=0;
		//所有无使用次数但都有剩余使用量，表明是token
		//积累token分为两个buff实现，一个是增加token buff的buff A，另一个是消耗token的buff B。
		//buff A在满足条件后找到ID为 buff B ，并添加剩余使用量
		//一回合1次，消耗x个token发动效果
		//token类型buff的add_type为可叠加类型buff

		private String condition="";//触发条件，如果是多个条件，统一用一个代号表示，判断是否满足条件时，还需判断是否拥有足够cost成本
		//如果被动buff没有条件则无法在事件中触发，如果主动buff没条件，则基本随时都能触发
		private float condition_value;
		private String cost_condition="";//消耗前置条件
		private float cost_condition_value;
		private String cost="";//消耗效果ID
		private float cost_value;
		private String effect_ID="";//发动的效果ID
		private float effect_value;//发动值,最多存储3个,自己抽x卡并回复x血
		

		

		//短时间持续效果：例如，此次普攻攻+2，在普攻结束后发动，消除此buff

		//在发动的x回合后，发动效果。实现：新建B buff，设定为如果轮到x玩家后则cumulate_value++，如果x玩家掉线，则在回合切换时会经过掉线玩家从而满足发生条件，在下个玩家回合激活。
		//而本buff中每回合初进行判定，B buff的cumulate_value值是否满足

		private int Source_type;//buff获得来源，敌人，自己武器，队友武器，敌人武器，自己卡片效果，自身效果，场景效果
		//如果是来源于武器的效果，不予在主动发动buff效果选项中显示，只是对使用次数等信息存储在buff栏内
		//如果来源于某些但独立于某些，统一为卡片效果，比如隐天盾，黑曜剑的不死效果，即便黑曜剑离场依然存在，因此为卡片效果
		private int Source_ID;//哪个玩家的
		private int Source_item_column=SZSC_protocol.code_none;//第几个武器或者物品的
		private int Source_effect_column=SZSC_protocol.code_none;//某个武器的第几个效果/第几个人物效果的（可能是x玩家的第y个武器的或者第Z个人物效果）
		//int type;//buff类型，增益，减益，中立机制，增益停止机制（时效机制）


		SZSC_Buff() {
			ini();
		}
		public void delete() {
			ini();
		}
		
		private void ini() {
			ID=SZSC_protocol.code_none;
			show=false;
			positive=false;
			whether_token=false;
			
			is_buff=false;
			buff_type="";
			
			condition="";
			cost_condition="";
			cost_condition_value=SZSC_protocol.code_none;
			cost="";
			effect_ID="";
			effect_value=SZSC_protocol.code_none;
			add_type=SZSC_game_protocol.Buff_add_type_independent;
			
			use_limit_type="";
			use_times_remain=SZSC_protocol.code_none;
			use_times_limit=SZSC_protocol.code_none;
			consume_value_per_time=1;
			
			chain_level_limit=false;
			chain_level=SZSC_protocol.code_none;
		}
		
		
		//如果是武器主动效果/个人主动效果，则positive则为false，该效果通过主动点击武器、点击人物效果进行发动，而不是在buff栏内发动
		public void set_info(String name,int ID,String buff_type,boolean show,boolean positive,String add_type) {
			this.name=name;
			this.buff_type=buff_type;
			this.ID=ID;
			this.show=show;
			this.positive=positive;
			this.add_type=add_type;
		}
		//如何获得该buff的，通过武器、卡片效果等，可能是敌人可能是自己可能是队友给的
		public void set_source(int Source_type,int Source_player_ID,int which_one) {
			this.Source_type=Source_type;
			this.Source_ID=Source_player_ID;
			this.Source_item_column=which_one;
		}
		
		public void set_buff(boolean is_buff) {
			this.is_buff=is_buff;
		}
		public boolean whether_buff() {
			return is_buff;
		}
		
		
		public void set_condition(String condition,float condition_value)
		{
			this.condition=condition;
			this.condition_value=condition_value;
		}
		public void set_token(boolean whether_token,int first_token_numbers) {
			if(whether_token) {
				this.whether_token=whether_token;
				this.token_value=first_token_numbers;
			}
		}
		
		public void set_use_limit(boolean chain_level_limit,String use_limit_type,int use_times_limit)//buff添加使用次数限制
		{
			this.chain_level_limit=chain_level_limit;
			this.use_limit_type=use_limit_type;
			this.use_times_limit=use_times_limit;
			this.consume_value_per_time=1;
			this.use_times_remain=use_times_limit;
		}
		public void set_duration(String duration_type,float duration_remain)//buff添加持续时间
		{
			this.duration_type=duration_type;
			this.duration_remain=duration_remain;
		}
		public void set_cost_condition(String cost_condition,float cost_condition_value) {
			this.cost_condition=cost_condition;
			this.cost_condition_value=cost_condition_value;
		}
		public void set_cost(String cost_effect_ID,float cost_effect_value)//设置发动消耗
		{
			this.cost=cost_effect_ID;
			this.cost_value=cost_effect_value;
		}
		public void set_effect(String effect_ID,float effect_value)//设置发动效果
		{
			this.effect_ID=effect_ID;
			this.effect_value=effect_value;
		}
		public void set_effect_value(float effect_value)//设置发动效果
		{
			this.effect_value=effect_value;
		}
		public boolean check_source_type(int type) {
			if(type==this.Source_type)
				return true;
			return false;
		}
		public boolean check_condition_type(String type) {
			if(this.condition.equals(type))
				return true;
			return false;
		}
		public boolean check_duration_type(String type) {
			if(this.duration_type.equals(type))
				return true;
			return false;
		}
		public boolean check_limit_type(String type) {
			if(this.use_limit_type.equals(type))
				return true;
			return false;
		}
		public String get_limit_type() {
			return use_limit_type;
		}
		
		public String get_add_type() {
			return this.add_type;
		}
		public float get_duration_reamin() {
			return this.duration_remain;
		}
		public String get_condition_ID() {
			return this.condition;
		}
		
		
		public int get_use_time_reamin() {
			return use_times_remain;
		}
		public void reset_use_time_reamin() {
			use_times_remain=use_times_limit;
		}
		
		public int get_consume_per_time() {
			return this.consume_value_per_time;
		}
		
		public void reduce_times(int times) {
			if(times*this.consume_value_per_time>this.use_times_remain)
			{
				SZSC_service.show("消耗大于剩余次数!"+(times*this.consume_value_per_time)+"   "+this.use_times_remain);
				return;
			}
				
			this.use_times_remain-=this.consume_value_per_time*times;
		}
		
		public boolean check_effect_type(String type) {
			if(this.effect_ID.equals(type))
				return true;
			return false;
			
		}
		public boolean not_none() {
			if(this.ID==SZSC_protocol.code_none)
				return false;
			return true;
		}
		public boolean is_positive() {
			return this.positive;
		}
		public boolean is_token() {
			return this.whether_token;
		}
		public void set_positive(boolean result) {
			this.positive=result;
		}
		public int get_ID() {
			return this.ID;
		}
		public void set_ID(int ID) {
			this.ID=ID;
		}
		public String get_effect_ID() {
			return this.effect_ID;
		}
		public String get_cost_ID() {
			return this.cost;
		}
		public float get_cost_value(){
			return this.cost_value;
		}
		public float get_condition_value(){
			return this.condition_value;
		}
		public String get_type() {
			return buff_type;
		}
		public int get_source_type() {
			return Source_type;
		}
		
		
		public void reduce_duration() {
			this.duration_remain--;
		}
		public boolean use_time_no_remain() {
			if(this.use_limit_type.length()<=0)
				return false;
			if(this.use_times_remain<=0||this.use_times_remain<this.consume_value_per_time)
				return true;
			return false;
		}
		public boolean duration_no_remain() {
			//一般都会设置duration_type，如果没设置需要报错
			if(this.duration_type.isBlank()) {
				SZSC_service.show("buff没有持续时间 ID"+ID);
			}
			switch(this.duration_type){
				case SZSC_game_protocol.TYPE_Duration_permanent:
				case SZSC_game_protocol.TYPE_Duration_permanent_selfeffect:
				case SZSC_game_protocol.TYPE_Duration_permanent_weaponeffect:
					return true;
			}
			
				
			if(this.duration_remain<=0)
				return true;
			return false;
		}
		public String get_duration_type() {
			return duration_type;
		}
		
		public int get_source_which_effect() {
			return this.Source_effect_column;
		}
		public int get_source_which_item() {
			return this.Source_item_column;
		}
		public void set_Source_which_effect(int which) {
			this.Source_effect_column=which;
		}
		public void set_Source_which_item_which_effect(int which_item,int which_effect) {
			this.Source_item_column=which_item;
			this.Source_effect_column=which_effect;
		}
		
		public float get_duration_remain() {
			return this.duration_remain;
		}
		
		public float get_effect_value() {
			
			return effect_value;
		}
		public void increase_use_times(int times) {
			use_times_remain+=times;
		}
		public void set_token_value(int value) {
			token_value=value;
		}
		public void change_token_value(int value) {
			token_value+=value;
		}
		public int get_token_value() {
			return token_value;
		}
		
		public void set_chain_level_limit(boolean chain_level_limit) {
			this.chain_level_limit=chain_level_limit;
		}
		
		public boolean got_chain_level_limit() {
			return chain_level_limit;
		}
		public int got_chain_level() {
			return chain_level;
		}
		public void set_chain_level(int chain_level) {
			this.chain_level=chain_level;
		}
		
		
}
