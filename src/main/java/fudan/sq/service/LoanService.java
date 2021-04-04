package fudan.sq.service;

import fudan.sq.entity.Repayment;
import fudan.sq.httpUtils.httpUtils;
import fudan.sq.repository.RepaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;



@Service
public class LoanService {
   Logger logger = LoggerFactory.getLogger(LoanService.class);

   @Autowired
   RepaymentRepository repaymentRepository;

   public void LoanService(){

   }


   /**
    * 查询客户信息
    */
   public Map<String, Object> getClientInfo(String IDNumber) throws Exception {
      Map<String, Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/account?IDNumber=" + IDNumber);
      Map<String, Object> returnMessage = new HashMap<>();
      logger.debug("返回结果：" + res);
      if (res.get("flag").toString().equals("false")) {
         returnMessage.put("result", "no match");
      } else {
         Object o = res.get("data");
         String json = httpUtils.gson.toJson(o);
         Map<String, Object>[] maps = httpUtils.gson.fromJson(json, Map[].class);
         logger.debug("用户信息：" + maps[0]);
         returnMessage.put("username", maps[0].get("name"));
         returnMessage.put("customNumber", maps[0].get("code"));
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
//      DecimalFormat df = new DecimalFormat("#.00");
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
         Repayment repayment = repaymentRepository.findByIouNumAndPanNum(map.get("iouNum").toString(),(int) Double.parseDouble(map.get("planNum").toString()));
         if (repayment!=null){
            map.put("remainAmount",repayment.getRemainAmount());
            map.put("remainPrincipal",repayment.getRemainPrincipal());
            map.put("remainInterest",repayment.getRemainInterest());
            map.put("penaltyInterest",repayment.getPenaltyInterest());
         }

         String dataStr = map.get("planDate").toString();
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         Date date = df.parse("2021-8-4");
         Date planData = df.parse(dataStr);
        double repaymentStatus = Double.parseDouble(map.get("repaymentStatus").toString());
         if (repaymentStatus>1.0){
            map.put("remainAmount",0);
            map.put("remainPrincipal",0);
            map.put("remainInterest",0);
           finished.add(map);
         }else if (planData.before(date)){
            overdue.add(map);
         }else {
            remain.add(map);
         }
      }
      int i = overdue.size();
      for (Map<String,Object> map:overdue){
         Repayment repayment = repaymentRepository.findByIouNumAndPanNum(map.get("iouNum").toString(),(int) Double.parseDouble(map.get("planNum").toString()));
         double remainAmount = Double.parseDouble(map.get("remainAmount").toString());
         double penaltyInterest = remainAmount*0.05*i;
         if (repayment!=null){
            map.put("remainAmount",repayment.getRemainAmount());
            map.put("penaltyInterest",repayment.getPenaltyInterest());
            map.put("remainInterest",repayment.getRemainInterest());
            map.put("remainPrincipal",repayment.getRemainPrincipal());
         }else {
            repayment = new Repayment();
            repayment.setIouNum(map.get("iouNum").toString());
            repayment.setPanNum((int) Double.parseDouble(map.get("planNum").toString()));
            repayment.setPenaltyInterest(penaltyInterest);
            repayment.setRemainPrincipal(Double.parseDouble(map.get("remainPrincipal").toString()));
            repayment.setRemainInterest(penaltyInterest+Double.parseDouble(map.get("remainInterest").toString()));
            repayment.setRemainAmount(Double.parseDouble(map.get("remainAmount").toString())+penaltyInterest);
            repayment.setPenaltyInterestClear(false);
            map.put("remainAmount",Double.parseDouble(map.get("remainAmount").toString())+penaltyInterest);
            map.put("remainPrincipal",Double.parseDouble(map.get("remainPrincipal").toString()));
            map.put("penaltyInterest",penaltyInterest);
            map.put("remainInterest",penaltyInterest+Double.parseDouble(map.get("remainInterest").toString()));
            repaymentRepository.save(repayment);
         }
         i--;
      }
      returnMsg.put("message",res.get("message").toString());
      returnMsg.put("overdue",overdue);
      returnMsg.put("remain",remain);
      returnMsg.put("finished",finished);
      return returnMsg;
   }

   /**
   * 归还贷款
    * 参数：借据号：iouNum
    * 还款期数：id
    * 还款金额：amount
    * 罚息：penaltyInterest
    *
    * "flag": true,
    * "code": 2000.0,
    * "message": "贷款 L2103301100141  第 3 期还款成功",
    */


   public Map<String,Object> repayment(String iouNum,int id,double amount) throws Exception {
      Map<String,Object> res = getLoanPlan(iouNum);
      logger.info("取得的订单:"+res);
      Object o1 = res.get("overdue");
      String json = httpUtils.gson.toJson(o1);
      Map<String ,Object>[] overdueBills = httpUtils.gson.fromJson(json,Map[].class);
      Object o2 = res.get("remain");
      String json2 = httpUtils.gson.toJson(o2);
      Map<String ,Object>[] remainBills = httpUtils.gson.fromJson(json2,Map[].class);

      Map<String,Object> returnMsg = new HashMap<>();
      Map<String,Object> repaymentBill = new HashMap<>();

      for (Map<String,Object> map:overdueBills){
         double planNum = Double.parseDouble(map.get("planNum").toString());
         logger.info("planId:"+planNum+";id:"+id);
        if (planNum==id){
            //还款
           repaymentBill = map;
           break;
         }
      }
      if (repaymentBill.size()==0){
         for (Map<String,Object> map:remainBills){
            double planNum = Double.parseDouble(map.get("planNum").toString());
            logger.info("planId:"+planNum+";id:"+id);
            if (planNum==id){
               //还款
               repaymentBill = map;
               break;
            }
         }
      }
      logger.info("正在还得账单："+repaymentBill);
      if (repaymentBill.size()==0){
         returnMsg.put("flag",false);
         returnMsg.put("message","贷款 "+iouNum+"  第 "+id+" 期还款失败,没有找到订单");
         return returnMsg;
      }

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


      Date date = df2.parse("2021-8-4 12:22:22");





      Map<String,Object> loanPlanDto = new HashMap<>();

      double compoundInterest = 0;
      Object creatTime = repaymentBill.get("creatTime");
      double creator = 0;
//      String currentDate = df2.format(new Date(System.currentTimeMillis()));
      String currentDate = repaymentBill.get("planDate").toString()+" 11:00:18";
      int id_ = (int) Double.parseDouble(repaymentBill.get("id").toString());
      String iouNum_ = iouNum;
      int payMethod = 2;
      double penaltyInterest = Double.parseDouble(repaymentBill.get("penaltyInterest").toString());
      double planAmount = Double.parseDouble(repaymentBill.get("planAmount").toString());
      Object planDate = repaymentBill.get("planDate");
      double planInterest = Double.parseDouble(repaymentBill.get("planInterest").toString());
      int planNum = (int) Double.parseDouble(repaymentBill.get("planNum").toString());
      double planPrincipal = Double.parseDouble(repaymentBill.get("planPrincipal").toString());
      double remainAmount = Double.parseDouble(repaymentBill.get("remainAmount").toString());
      double remainInterest = Double.parseDouble(repaymentBill.get("remainInterest").toString());
      double remainPrincipal = Double.parseDouble(repaymentBill.get("remainPrincipal").toString());
      int repaymentStatus = 1;
      String transactionCode = "";
      String updateTime = df2.format(new Date(System.currentTimeMillis()));
      double updater = 0;

      String dataStr = repaymentBill.get("planDate").toString();

      Date planData = df.parse(dataStr);

      if (planData.before(date)) {
         /**
          * 过期订单
          * */
         //double should_penaltyInterest = remainAmount * 0.5;
            if (amount < remainAmount) {
               /**
                * 部分还款
                * */
               //先还罚息
               Repayment repayment = repaymentRepository.findByIouNumAndPanNum(repaymentBill.get("iouNum").toString(),(int)Double.parseDouble(repaymentBill.get("planNum").toString()));
               logger.info("准备还款金额："+amount);
               logger.info("罚息："+repayment.getPenaltyInterest());
               if (amount<remainInterest){
                  if (amount<repayment.getPenaltyInterest()){
                     returnMsg.put("flag",false);
                     returnMsg.put("message","贷款 "+iouNum+"  第 "+id+" 期部分还款失败,先缴清罚金");
//                     returnMsg.put("data",repayment);
                     return returnMsg;
                  }
//                  if (repayment == null){
//                     repayment = new Repayment();
//                     repayment.setIouNum(repaymentBill.get("iouNum").toString());
//                     repayment.setPanNum((int) Double.parseDouble(repaymentBill.get("planNum").toString()));
//                     repayment.setPenaltyInterest(penaltyInterest);
//                     repayment.setRemainAmount(remainAmount-amount);
//                     repayment.setRemainInterest(remainInterest-amount);
//                     repayment.setRemainPrincipal(remainPrincipal);
//                     repaymentRepository.save(repayment);
//                  }else {

                     repayment.setRemainAmount(remainAmount-amount);
                     repayment.setRemainInterest(repayment.getRemainInterest()-amount);
                     repayment.setRemainPrincipal(remainPrincipal);
                     repayment.setPenaltyInterestClear(true);
                     repaymentRepository.save(repayment);
//                  }
               }else {
//                  if (repayment == null){
//                     repayment = new Repayment();
//                     repayment.setIouNum(repaymentBill.get("iouNum").toString());
//                     repayment.setPanNum(Integer.parseInt(repaymentBill.get("planNum").toString()));
//                     repayment.setPenaltyInterest(remainAmount * 0.5);
//                     repayment.setRemainAmount(remainAmount-amount);
//                     repayment.setRemainPrincipal(remainPrincipal-(amount-repayment.getRemainInterest()));
//                     repayment.setRemainInterest(0);
//                     repaymentRepository.save(repayment);
//                  }else {
                     repayment.setRemainAmount(remainAmount-amount);
                     repayment.setRemainPrincipal(remainPrincipal-(amount-repayment.getRemainInterest()));
                     repayment.setRemainInterest(0);
                     repayment.setPenaltyInterestClear(true);
                     repaymentRepository.save(repayment);
//                  }
               }

               returnMsg.put("flag",true);
               returnMsg.put("message","贷款 "+iouNum+"  第 "+id+" 期部分还款成功");
               returnMsg.put("data",repayment);
               return returnMsg;
            } else {
               /**
                * 全额还款
                * */
               loanPlanDto.put("compoundInterest", compoundInterest);
               loanPlanDto.put("creatTime", creatTime);
               loanPlanDto.put("creator", creator);
               loanPlanDto.put("currentDate", currentDate);
               loanPlanDto.put("id", id_);
               loanPlanDto.put("iouNum", iouNum);
               loanPlanDto.put("payMethod", payMethod);
               loanPlanDto.put("penaltyInterest", penaltyInterest);
               loanPlanDto.put("planAmount", planAmount);
               loanPlanDto.put("planDate", planDate);
               loanPlanDto.put("planInterest", planInterest);
               loanPlanDto.put("planNum", planNum);
               loanPlanDto.put("planPrincipal", planPrincipal);
               loanPlanDto.put("remainAmount", remainAmount);
               loanPlanDto.put("remainInterest", remainInterest);
               loanPlanDto.put("remainPrincipal", remainPrincipal);
               loanPlanDto.put("repaymentStatus", repaymentStatus);
               loanPlanDto.put("transactionCode", transactionCode);
               loanPlanDto.put("updateTime", updateTime);
               loanPlanDto.put("updater", updater);
               Map<String, Object> result = httpUtils.doPut("http://10.176.122.172:8012/loan/repayment", httpUtils.gson.toJson(loanPlanDto));
               logger.info("还款结果：" + result);
               return result;
            }
      } else {
         /**
          * 非逾期订单
          * */

         Repayment repayment = repaymentRepository.findByIouNumAndPanNum(iouNum,planNum);

         if (amount<remainAmount){
            /**
             * 部分还款
             * */
            double MyRemainAmount;
              if (repayment!=null){
                 MyRemainAmount = repayment.getRemainAmount();
              }else {
                 MyRemainAmount = Double.parseDouble(repaymentBill.get("remainAmount").toString());
              }
              if (amount>=MyRemainAmount){
                 //全额还款
                 loanPlanDto.put("compoundInterest", compoundInterest);
                 loanPlanDto.put("creatTime", creatTime);
                 loanPlanDto.put("creator", creator);
                 loanPlanDto.put("currentDate", currentDate);
                 loanPlanDto.put("id", id_);
                 loanPlanDto.put("iouNum", iouNum);
                 loanPlanDto.put("payMethod", payMethod);
                 loanPlanDto.put("penaltyInterest", penaltyInterest);
                 loanPlanDto.put("planAmount", planAmount);
                 loanPlanDto.put("planDate", planDate);
                 loanPlanDto.put("planInterest", planInterest);
                 loanPlanDto.put("planNum", planNum);
                 loanPlanDto.put("planPrincipal", planPrincipal);
                 loanPlanDto.put("remainAmount", remainAmount);
                 loanPlanDto.put("remainInterest", remainInterest);
                 loanPlanDto.put("remainPrincipal", remainPrincipal);
                 loanPlanDto.put("repaymentStatus", repaymentStatus);
                 loanPlanDto.put("transactionCode", transactionCode);
                 loanPlanDto.put("updateTime", updateTime);
                 loanPlanDto.put("updater", updater);
                 Map<String, Object> result = httpUtils.doPut("http://10.176.122.172:8012/loan/repayment", httpUtils.gson.toJson(loanPlanDto));
                 logger.info("还款结果：" + result);
                 return result;
              }else {
                 //部分还款
                 double remain = MyRemainAmount-amount;
                 if (repayment==null){
                    repayment = new Repayment();
                    repayment.setIouNum(iouNum);
                    repayment.setPanNum(planNum);
                 }
                 if (amount<remainInterest){
                    repayment.setRemainAmount(remain);
                    repayment.setRemainInterest(remainInterest-amount);
                    repayment.setRemainPrincipal(remainPrincipal);
                    repayment.setPenaltyInterest(0);
                 }else {
                    repayment.setRemainAmount(remain);
                    repayment.setRemainPrincipal(remain);
                    repayment.setRemainInterest(0);
                 }
                 repaymentRepository.save(repayment);
                 returnMsg.put("flag",true);
                 returnMsg.put("code","2000.0");
                 returnMsg.put("message","贷款 "+iouNum+"  第 "+id+" 期还款成功");
                 returnMsg.put("data",repayment);
                 logger.info(repayment.toString());
                 return returnMsg;
              }

         }else {
            /**
             * 全额还款
             * */
            loanPlanDto.put("compoundInterest", compoundInterest);
            loanPlanDto.put("creatTime", creatTime);
            loanPlanDto.put("creator", creator);
            loanPlanDto.put("currentDate", currentDate);
            loanPlanDto.put("id", id_);
            loanPlanDto.put("iouNum", iouNum);
            loanPlanDto.put("payMethod", payMethod);
            loanPlanDto.put("penaltyInterest", penaltyInterest);
            loanPlanDto.put("planAmount", planAmount);
            loanPlanDto.put("planDate", planDate);
            loanPlanDto.put("planInterest", planInterest);
            loanPlanDto.put("planNum", planNum);
            loanPlanDto.put("planPrincipal", planPrincipal);
            loanPlanDto.put("remainAmount", remainAmount);
            loanPlanDto.put("remainInterest", remainInterest);
            loanPlanDto.put("remainPrincipal", remainPrincipal);
            loanPlanDto.put("repaymentStatus", repaymentStatus);
            loanPlanDto.put("transactionCode", transactionCode);
            loanPlanDto.put("updateTime", updateTime);
            loanPlanDto.put("updater", updater);
            Map<String, Object> result = httpUtils.doPut("http://10.176.122.172:8012/loan/repayment", httpUtils.gson.toJson(loanPlanDto));
            logger.info("还款结果：" + result);
            repaymentRepository.delete(repayment);
            return result;
         }
      }
   }



   /**
    * 日终结算
    */
   public Map<String, Object> batchRepaymentLoan() throws Exception {
      Map<String, Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/loan?pageNum=0&pageSize=0&params=%7B%22loanStatus%22:1%7D");
      Map<String, Object> returnMsg = new HashMap<>();
      List<Map<String, Object>> list = new LinkedList<>();
      List<String> iouNums = new ArrayList<>();
      List<Map<String, Object>> toRepay = new LinkedList<>();


//   public static void main(String[] args) throws ParseException {
//      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//      Date date = new Date(System.currentTimeMillis());
//      Date date1 = simpleDateFormat.parse("2021-3-30");
//      System.out.println(date.before(date1));
//   }





      if (res.get("total") == null) {
         returnMsg.put("result", new LinkedList<>());
      }
      Object o = res.get("list");
      String json = httpUtils.gson.toJson(o);
      Map<String, Object>[] maps1 = httpUtils.gson.fromJson(json, Map[].class);
      for (Map map : maps1) {
         String iouNum = map.get("iouNum").toString();
         iouNums.add(iouNum);
      }
      for (String iouNum : iouNums) {
        /* Map<String,Object> res2 = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum="+iouNum);
         Object o4 = res2.get("data");
         String loanIds = */
         Map<String, Object> loanDetails = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/" + iouNum);
         Object o2 = loanDetails.get("data");
         String loanJson = httpUtils.gson.toJson(o2);
         Map<String, Object> loanMap = httpUtils.gson.fromJson(loanJson, Map.class);
         String accountNum = loanMap.get("accountNum").toString();
         String customerCode = loanMap.get("customerCode").toString();

         Map<String, Object> accountDetails = httpUtils.httpClientGet("http://10.176.122.172:8012/account?customerCode=" + customerCode);
         Object o3 = accountDetails.get("data");
         String customerJson = httpUtils.gson.toJson(o3);
         Map<String, Object>[] customerMap = httpUtils.gson.fromJson(customerJson, Map[].class);
         Object account = customerMap[0].get("accountDtos");
         String accountDetail = httpUtils.gson.toJson(account);
         Map<String, Object>[] accountMap = httpUtils.gson.fromJson(accountDetail, Map[].class);
         Double balance = 0.0;
         for (Map accountMaps : accountMap) {
            if (accountMaps.get("accountNum").toString().equals(accountNum)) {
               balance = Double.parseDouble(accountMaps.get("balance").toString());
            }
         }


         Map<String, Object> loanPlans = getLoanPlan(iouNum);
         List<Map<String, Object>> overdues = (List<Map<String, Object>>) loanPlans.get("overdue");
         List<Map<String, Object>> finisheds = (List<Map<String, Object>>) loanPlans.get("finished");
         List<Map<String, Object>> remains = (List<Map<String, Object>>) loanPlans.get("remain");

         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Date date = new Date(System.currentTimeMillis());

         for (Map overdue : overdues) {
            Double remainAmount = Double.parseDouble(overdue.get("remainAmount").toString());
            Double penalty = remainAmount * 0.05;
            //httpUtils.doPut("http://10.176.122.172:8012/loan/plan?iouNum="+iouNum,)
            if (balance < penalty) {
               returnMsg.put("status", "余额不足");
            } else if (balance < remainAmount) {
               returnMsg.put("status", "余额不足");
            } else {
               Map<String, Object> loanPlanDto = new HashMap<>();
               double compoundInterest = 0;
               Object creatTime = loanMap.get("creatTime");
               double creator = 0;
               String currentDate = df2.format(new Date(System.currentTimeMillis()));
               int id_ = (int) Double.parseDouble(overdue.get("id").toString());
               String iouNum_ = iouNum;
               int payMethod = 0;
               //double penaltyInterest_ = 0;
               double planAmount = Double.parseDouble(overdue.get("planAmount").toString());
               Object planDate = overdue.get("planDate");
               double planInterest = Double.parseDouble(overdue.get("planInterest").toString());
               int planNum = (int) Double.parseDouble(overdue.get("planNum").toString());
               double planPrincipal = Double.parseDouble(overdue.get("planPrincipal").toString());
               //double remainAmount = Double.parseDouble(repaymentBill.get("remainAmount").toString());
               double remainInterest = Double.parseDouble(overdue.get("remainInterest").toString());
               double remainPrincipal = Double.parseDouble(overdue.get("remainPrincipal").toString());
               int repaymentStatus = 1;
               String transactionCode = "";
               String updateTime = df2.format(new Date(System.currentTimeMillis()));
               double updater = 0;

               String dataStr = overdue.get("planDate").toString();

               //Date planData = df.parse(dataStr);
               loanPlanDto.put("compoundInterest", compoundInterest);
               loanPlanDto.put("creatTime", creatTime);
               loanPlanDto.put("creator", creator);
               loanPlanDto.put("currentDate", currentDate);
               loanPlanDto.put("id", id_);
               loanPlanDto.put("iouNum", iouNum);
               loanPlanDto.put("payMethod", payMethod);
               loanPlanDto.put("penaltyInterest", penalty);
               loanPlanDto.put("planAmount", planAmount);
               loanPlanDto.put("planDate", planDate);
               loanPlanDto.put("planInterest", planInterest);
               loanPlanDto.put("planNum", planNum);
               loanPlanDto.put("planPrincipal", planPrincipal);
               loanPlanDto.put("remainAmount", remainAmount);
               loanPlanDto.put("remainInterest", remainInterest);
               loanPlanDto.put("remainPrincipal", remainPrincipal);
               loanPlanDto.put("repaymentStatus", repaymentStatus);
               loanPlanDto.put("transactionCode", transactionCode);
               loanPlanDto.put("updateTime", updateTime);
               loanPlanDto.put("updater", updater);
               Map<String, Object> result = httpUtils.doPut("http://10.176.122.172:8012/loan/repayment", httpUtils.gson.toJson(loanPlanDto));
               logger.info("还款结果：" + result);
               returnMsg = result;

            }


         }
         for (Map remain : remains) {
            String dataStr = remain.get("planDate").toString();
            Date planDate = df.parse(dataStr);
            Double remainAmount = Double.parseDouble(remain.get("remainAmount").toString());
            if (planDate.equals(date)) {
               toRepay.add(remain);
               if (balance < remainAmount) {
                  returnMsg.put("status", "余额不足");
               } else {
                  Map<String, Object> loanPlanDto = new HashMap<>();
                  double compoundInterest = 0;
                  Object creatTime = loanMap.get("creatTime");
                  double creator = 0;
                  String currentDate = df2.format(new Date(System.currentTimeMillis()));
                  int id_ = (int) Double.parseDouble(remain.get("id").toString());
                  String iouNum_ = iouNum;
                  int payMethod = 0;
                  //double penaltyInterest_ = 0;
                  double planAmount = Double.parseDouble(remain.get("planAmount").toString());
                  //Object planDate = remain.get("planDate");
                  double planInterest = Double.parseDouble(remain.get("planInterest").toString());
                  int planNum = (int) Double.parseDouble(remain.get("planNum").toString());
                  double planPrincipal = Double.parseDouble(remain.get("planPrincipal").toString());
                  //double remainAmount = Double.parseDouble(repaymentBill.get("remainAmount").toString());
                  double remainInterest = Double.parseDouble(remain.get("remainInterest").toString());
                  double remainPrincipal = Double.parseDouble(remain.get("remainPrincipal").toString());
                  int repaymentStatus = 1;
                  String transactionCode = "";
                  String updateTime = df2.format(new Date(System.currentTimeMillis()));
                  double updater = 0;

                  //String dataStr = overdue.get("planDate").toString();

                  Date planData = df.parse(dataStr);
                  loanPlanDto.put("compoundInterest", compoundInterest);
                  loanPlanDto.put("creatTime", creatTime);
                  loanPlanDto.put("creator", creator);
                  loanPlanDto.put("currentDate", currentDate);
                  loanPlanDto.put("id", id_);
                  loanPlanDto.put("iouNum", iouNum);
                  loanPlanDto.put("payMethod", payMethod);
                  loanPlanDto.put("penaltyInterest", 0);
                  loanPlanDto.put("planAmount", planAmount);
                  loanPlanDto.put("planDate", planDate);
                  loanPlanDto.put("planInterest", planInterest);
                  loanPlanDto.put("planNum", planNum);
                  loanPlanDto.put("planPrincipal", planPrincipal);
                  loanPlanDto.put("remainAmount", remainAmount);
                  loanPlanDto.put("remainInterest", remainInterest);
                  loanPlanDto.put("remainPrincipal", remainPrincipal);
                  loanPlanDto.put("repaymentStatus", repaymentStatus);
                  loanPlanDto.put("transactionCode", transactionCode);
                  loanPlanDto.put("updateTime", updateTime);
                  loanPlanDto.put("updater", updater);
                  Map<String, Object> result = httpUtils.doPut("http://10.176.122.172:8012/loan/repayment", httpUtils.gson.toJson(loanPlanDto));
                  logger.info("还款结果：" + result);
                  returnMsg = result;
               }

            }


         }
         if(overdues.isEmpty()&&toRepay.isEmpty()){
            returnMsg.put("result","暂无还款计划");
         }




      }
      logger.debug("批量还款返回结果：" + returnMsg);
      return returnMsg;
   }


   /**
    * 查询流水信息
    * 查询条件
    * {
    *     startTime:开始时间
    *     endTime:结束时间
    *     account：账号
    *     branchName：办理机构
    *     transactionType：交易类型
    *
    * }
    * */
   public Map<String,Object> getTransaction(Map<String,String> condition) throws Exception {
      Map<String,Object> res = httpUtils.httpClientGet("http://10.176.122.172:8012/transaction?pageSize=0&pageNum=0&params=%7B%22orderBy%22:%22order+by+updateTime+DESC%22%7D");
      Object o = res.get("list");
      String json = httpUtils.gson.toJson(o);
      Map<String, Object>[] maps = httpUtils.gson.fromJson(json, Map[].class);
      List<Map<String,Object>> chooseResult = new LinkedList<>();
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      for (Map<String,Object> map:maps){
         boolean isMatch = true;
         for (Map.Entry<String,String> entry:condition.entrySet()){
            if (entry.getKey().hashCode()=="startTime".hashCode()){
               Date date = simpleDateFormat.parse(entry.getValue());
               Date operatorTime = simpleDateFormat.parse(map.get("operatorTime").toString());
               logger.info("startTime");
               logger.info("date:"+date);
               logger.info("operatorTime:"+operatorTime);
               logger.info(String.valueOf(operatorTime.before(date)));
               if (operatorTime.before(date)){
                  logger.info("不符合");
                  isMatch = false;
                  continue;
               }
            }
            if (entry.getKey().hashCode()=="endTime".hashCode()){
               Date date = simpleDateFormat.parse(entry.getValue());
               Date dateGet = simpleDateFormat.parse(map.get("operatorTime").toString());
               logger.info("endTime");
               logger.info("date:"+date);
               logger.info("dateGet:"+dateGet);
               if (date.before(dateGet)){
                  isMatch=false;
                  continue;
               }
            }
            logger.info("key:"+entry.getKey());
            if (entry.getKey().hashCode()!="startTime".hashCode()&&entry.getKey().hashCode()!="endTime".hashCode()&&!map.containsKey(entry.getKey())){
               isMatch=false;
               continue;
            }
            if (map.containsKey(entry.getKey())&&map.get(entry.getKey()).toString().contains(entry.getValue())){
               isMatch=false;
            }
         }
         if (isMatch){
            chooseResult.add(map);
         }
      }
    Map<String,Object> returnMsg = new HashMap<>();
      returnMsg.put("result",chooseResult);
      return returnMsg;
   }
}



