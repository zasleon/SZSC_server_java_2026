package test;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

public class JSON_process{
	public static final int invalid_parameter=-999;

    private JSONObject content;
    
    
    public JSON_process() {
    	this.content=new org.json.JSONObject();
    }
    public JSON_process(String json_msg) {
    	if(json_msg==null||json_msg.isEmpty()) {
    		core_main.show("创建json出错!    内容:"+json_msg);
    		this.content=new org.json.JSONObject();
    		return;
    	}
    	this.content=new org.json.JSONObject(json_msg);
    }
    
    
    public String getString() {
		return content.toString();
	}
    

    public void delete(String name)
    {
    	content.remove(name);
    }
    
    
    public <T> void add(String name, T value) {
        try {
            content.put(name, value);
        } catch (org.json.JSONException e) {
            core_main.show(e.getMessage());
        }
    }
    public void send_signal(int signal)//客户端用
    {
    	try {
            content.put("signal",signal);
            //monitor_all.send(object_this.toString());

        } catch (org.json.JSONException e) {

        }

    }

    public void send()//客户端用
    {
        //network.set_send_content(content.toString());
    	content=new org.json.JSONObject();
    }

    public int getInt(String raw_msg,String name)
    {
        try {
            org.json.JSONObject object=new org.json.JSONObject(raw_msg);
            return object.getInt(name);
        } catch (org.json.JSONException e) {
            return invalid_parameter;
        }
    }
    public String getString(String raw_msg,String name)
    {
        try {
            org.json.JSONObject object=new org.json.JSONObject(raw_msg);
            return object.getString(name);
        } catch (org.json.JSONException e) {
            return null;
        }
    }
    public String getString(String name)
    {
        try {
            return content.getString(name);
        } catch (org.json.JSONException e) {
            return null;
        }
    }
    public int getInt(String name)
    {
        try {
        	return content.getInt(name);
        } catch (org.json.JSONException e) {
            return invalid_parameter;
        }
    }


    private void createListArray(String arrayName) {
        try {
            JSONArray jsonArray = new JSONArray();
            content.put(arrayName, jsonArray);
        } catch (JSONException e) {
            System.err.println("创建List数组失败: " + e.getMessage());
        
        }
    }
    
    public void addToArray(String arrayName, String value) {
        try {
            if (!content.has(arrayName)) {
                createListArray(arrayName);
            }
            JSONArray jsonArray = content.getJSONArray(arrayName);
            jsonArray.put(value);
        } catch (JSONException e) {
            System.err.println("向数组添加字符串元素失败: " + e.getMessage());
        }
    }
    public void addToArray(String arrayName, int value) {
        try {
            if (!content.has(arrayName)) {
                createListArray(arrayName);
            }
            JSONArray jsonArray = content.getJSONArray(arrayName);
            jsonArray.put(value);
        } catch (JSONException e) {
            System.err.println("向数组添加整型元素失败: " + e.getMessage());
        }
    }
    
    public List<String> getStringList(String arrayName) {
        List<String> result = new ArrayList<>();
        try {
            JSONArray jsonArray = content.getJSONArray(arrayName);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            System.err.println("获取字符串List失败: " + e.getMessage());
        }
        return result;
    }
    public List<Integer> getIntList(String arrayName) {
        List<Integer> result = new ArrayList<>();
        try {
            JSONArray jsonArray = content.getJSONArray(arrayName);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getInt(i));
            }
        } catch (JSONException e) {
            System.err.println("获取整型List失败: " + e.getMessage());
        }
        return result;
    }
    public String getStringFromList(String arrayName, int index) {
        try {
            JSONArray jsonArray = content.getJSONArray(arrayName);
            if (index >= 0 && index < jsonArray.length()) {
                return jsonArray.getString(index);
            }
        } catch (JSONException e) {
            System.err.println("从List获取字符串元素失败: " + e.getMessage());
        }
        return "";
    }
    public int getIntFromList(String arrayName, int index) {
        try {
            JSONArray jsonArray = content.getJSONArray(arrayName);
            if (index >= 0 && index < jsonArray.length()) {
                return jsonArray.getInt(index);
            }
        } catch (JSONException e) {
            System.err.println("从List获取整型元素失败: " + e.getMessage());
        }
        return invalid_parameter;
    }
    
    
    
}
