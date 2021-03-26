package fudan.sq.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fudan.sq.httpUtils.httpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
public class LoanService {
   Logger logger= LoggerFactory.getLogger(LoanService.class);


   /**
 * 查询客户信息
 * */
public Map<String,Object> getClientInfo(String IDNumber) throws Exception {
   Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/account?IDNumber="+IDNumber);
   Map<String,Object> returnMessage = new HashMap<>();
   logger.debug("返回结果："+res);
   if (res.get("flag").toString().equals("false")){
      returnMessage.put("result","no match");
   }else {
      Object o = res.get("data");
      String json = httpUtils.gson.toJson(o);
      Map<String ,Object>[] maps = httpUtils.gson.fromJson(json,Map[].class);
      logger.debug("用户信息："+maps[0]);
      returnMessage.put("username",maps[0].get("name"));
      returnMessage.put("customNumber",maps[0].get("code"));
   }
   return returnMessage;
}


}





