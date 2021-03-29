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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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


/**
 * 通过客户号查询该用户的贷款列表
 * */
public Map<String,Object> getLoanList(String customerCode) throws Exception {
   Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan?pageNum=0&pageSize=0&params=%7B%22loanStatus%22:1%7D");
   Map<String,Object> returnMsg = new HashMap<>();
   List<Map<String,Object>> list = new LinkedList<>();
   if (res.get("total")==null){
      returnMsg.put("res",new LinkedList<>());
   }
   Object o = res.get("list");
   String json = httpUtils.gson.toJson(o);
   Map<String ,Object>[] maps = httpUtils.gson.fromJson(json,Map[].class);
   for (Map map:maps){
      if (map.get("customerCode").toString().hashCode()==customerCode.hashCode()){
         list.add(map);
      }
   }
   returnMsg.put("res",list);
   return returnMsg;
}
   /**
    * 获取还款计划
    * */
   public Map<String,Object> getLoanPlan(String iouNum) throws Exception {
      Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum="+iouNum);
      List<Map<String,Object>> overdue = new LinkedList<>();
      List<Map<String,Object>> remain = new LinkedList<>();
      List<Map<String,Object>> finished = new LinkedList<>();
      Object o = res.get("data");
      String json = httpUtils.gson.toJson(o);
      Map<String ,Object>[] maps = httpUtils.gson.fromJson(json,Map[].class);
      for (Map<String,Object> map:maps){
         String dataStr = map.get("planDate").toString();
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         Date date = new Date(System.currentTimeMillis());
         Date planData = df.parse(dataStr);
         Double remainAmount = Double.parseDouble(map.get("remainAmount").toString());
         if (remainAmount<=0){
           finished.add(map);
         }else if (planData.before(date)){
            overdue.add(map);
         }else {
            remain.add(map);
         }
      }
      Map<String,Object> returnMsg = new HashMap<>();
      returnMsg.put("message",res.get("message").toString());
      returnMsg.put("overdue",overdue);
      returnMsg.put("remain",remain);
      returnMsg.put("finished",finished);
      return returnMsg;
   }

   public static void main(String[] args) throws ParseException {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      Date date = new Date(System.currentTimeMillis());
      Date date1 = simpleDateFormat.parse("2021-3-30");
      System.out.println(date.before(date1));
   }

}





