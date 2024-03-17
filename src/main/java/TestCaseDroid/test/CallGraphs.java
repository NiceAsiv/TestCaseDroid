package TestCaseDroid.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class CallGraphs {
    public static void main(String[] args) {
        doStuff();
    }
    public static void doStuff() {
        new A2().foo();
        User user = new User(18,"FantaC");
        String jsonString = JSONObject.toJSONString(user, SerializerFeature.WriteClassName);
        System.out.println(jsonString);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        System.out.println(jsonObject);
    }
}

class A2{
    public void foo(){
        bar();
    }

    public void bar(){
        System.out.println("you finally reach bar!");
    }
}
