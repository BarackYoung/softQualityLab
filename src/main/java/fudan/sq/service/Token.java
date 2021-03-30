package fudan.sq.service;


import fudan.sq.httpUtils.httpUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Token {
    public static String token;
    String expireTime;
    public Token(){

    }
    public void setToken(String token){
        this.token = token;
    }
    public String getToken(){
        return token;
    }
    public static void main(String[] args) throws Exception {
        String parameter = "username=JT2103258421&password=imbus123";
        Map<String,Object> res = httpUtils.httpClientPost("http://10.176.122.172:8012/sys/login/restful?username=JT2103258421&password=imbus123","");
        System.out.println(res);
    }
}
