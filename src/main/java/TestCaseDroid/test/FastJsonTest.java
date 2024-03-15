package TestCaseDroid.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FastJsonTest {
    public static void main(String[] args) {
        User user = new User(18,"FantaC");
        String jsonString = JSONObject.toJSONString(user, SerializerFeature.WriteClassName);
        System.out.println(jsonString);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        System.out.println(jsonObject);
    }

    public void FastJsonTest(){
        System.out.println("Thanks for call this class!");
    }

    public void testFunction(){
        System.out.println("Can I add a new function?");
        User user = new User(18,"FantaC");
        String jsonString = JSONObject.toJSONString(user, SerializerFeature.WriteClassName);
        System.out.println(jsonString);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        System.out.println(jsonObject);
    }


}
