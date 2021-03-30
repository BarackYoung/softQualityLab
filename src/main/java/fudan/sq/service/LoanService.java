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
   Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan?pageNum=0&pageSize=0&params=%7B%22orderBy%22:%22order+by+b.updateTime+desc%22%7D");
   Map<String,Object> returnMsg = new HashMap<>();
   List<Map<String,Object>> list = new LinkedList<>();
   if (res.get("total")==null){
      returnMsg.put("res",new LinkedList<>());
   }
   logger.info("贷款列表:"+res);
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
    * repaymentStatus:1（未还清） 2（已还清）
    * */
   public Map<String,Object> getLoanPlan(String iouNum) throws Exception {
      Map<String,Object> returnMsg = new HashMap<>();
      Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum="+iouNum);
      List<Map<String,Object>> overdue = new LinkedList<>();
      List<Map<String,Object>> remain = new LinkedList<>();
      List<Map<String,Object>> finished = new LinkedList<>();
      Object o = res.get("data");
      String json = httpUtils.gson.toJson(o);
      Map<String ,Object>[] maps = httpUtils.gson.fromJson(json,Map[].class);
      if (maps==null){
         returnMsg.put("message","no information");
      }
      for (Map<String,Object> map:maps){
         String dataStr = map.get("planDate").toString();
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         Date date = new Date(System.currentTimeMillis());
         Date planData = df.parse(dataStr);
        double repaymentStatus = Double.parseDouble(map.get("repaymentStatus").toString());
         if (repaymentStatus>1.0){
           finished.add(map);
         }else if (planData.before(date)){
            double planAmount = Double.parseDouble(map.get("planAmount").toString());
            double penaltyInterest = planAmount*0.05;
            map.put("penaltyInterest",penaltyInterest);
            overdue.add(map);
         }else {
            remain.add(map);
         }
      }
      returnMsg.put("message",res.get("message").toString());
      returnMsg.put("overdue",overdue);
      returnMsg.put("remain",remain);
      returnMsg.put("finished",finished);
      return returnMsg;
   }
   /**
    * 缴纳罚息
    *
    * */


   /**
   * 归还贷款
    * 参数：借据号：iouNum
    * 还款期数：id
    * 还款金额：amount
    * 应还利息：planInterest
    * 罚息：penaltyInterest
    *
   * */
   public Map<String,Object> repayment(String iouNum,double id,double amount,double interest,double penaltyInterest) throws Exception {
      Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum="+iouNum);
      Object o = res.get("data");
      String json = httpUtils.gson.toJson(o);
      Map<String ,Object>[] maps = httpUtils.gson.fromJson(json,Map[].class);
      Map<String,Object> returnMsg = new HashMap<>();
      Map<String,Object> repaymentBill = new HashMap<>();
      for (Map<String,Object> map:maps){
         double repaymentStatus = Double.parseDouble(map.get("repaymentStatus").toString());
         double planId = Double.parseDouble(map.get("id").toString());
         if (planId<id&&repaymentStatus<2.0){
            //提示先还清之前的账单
            returnMsg.put("status","1");
            return returnMsg;
         }else if (planId==id&&repaymentStatus==2.0){
            //还款
           repaymentBill = map;
           break;
         }
      }
      Map<String,Object> loanPlanDto = new HashMap<>();
      double compoundInterest = 0;
      Object creatTime = repaymentBill.get("creatTime");
      double creator = 0;
      Date currentDate = new Date(System.currentTimeMillis());
      double id_ = id;
      String iouNum_ = iouNum;
      int payMethod = 0;
      double penaltyInterest_ = 0;
      double planAmount = Double.parseDouble(repaymentBill.get("planAmount").toString());
      Object planDate = repaymentBill.get("planDate");
      double planInterest = Double.parseDouble(repaymentBill.get("planInterest").toString());
      double planNum = 0;
      double planPrincipal = Double.parseDouble(repaymentBill.get("planPrincipal").toString());
      double remainAmount = Double.parseDouble(repaymentBill.get("remainAmount").toString());
      double remainInterest = Double.parseDouble(repaymentBill.get("remainInterest").toString());
      double remainPrincipal = Double.parseDouble(repaymentBill.get("remainPrincipal").toString());
      double repaymentStatus = 1;
      String transactionCode = "";
      Date updateTime = new Date(System.currentTimeMillis());
      double updater = 0;

      String dataStr = repaymentBill.get("planDate").toString();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      Date date = new Date(System.currentTimeMillis());
      Date planData = df.parse(dataStr);
      if (planData.before(date)){

      }
      return null;
   }



//   public static void main(String[] args) throws ParseException {
//      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//      Date date = new Date(System.currentTimeMillis());
//      Date date1 = simpleDateFormat.parse("2021-3-30");
//      System.out.println(date.before(date1));
//   }

}





