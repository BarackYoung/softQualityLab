package fudan.sq.service;


import fudan.sq.entity.Stock;
import fudan.sq.httpUtils.httpUtils;
import fudan.sq.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class StockService {
    LoanService loanService;
    @Autowired
    StockRepository stockRepository;
    Logger logger = LoggerFactory.getLogger(StockService.class);

    public Connection getConnection() {
        Connection conn = null;
        Connection con = null;
        PreparedStatement ps = null;
        //ResultSet rs = null;
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            //链接sqlite和mysql

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("加载成功");

            //String url = "jdbc:sqlite:C:/sqlite/xxxdatabase.db";   //定义连接数据库的url(url:访问数据库的URL路径),test为数据库名称

            System.out.println("数据库连接成功！\n");//数据库连接成功输出提示
        } catch (Exception ex) {
            System.out.println("加载失败");
            // handle the error
        }

        try {
            conn =
                    DriverManager.getConnection("jdbc:mysql://localhost/stock?serverTimezone=UTC", "root", "922626@hyq");
            //此处test为mysql提前建立的数据库，root为用户名，最后为密码

            System.out.println("连接成功");


        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.out.println("连接失败");
        }
        return conn;
    }

    public List<Map<String, Object>> getProduct(String productType) throws SQLException {
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        List<Map<String, Object>> stocks = new ArrayList<>();
        if(productType.equals("股票")){
            String findProductName = "SELECT distinct productId FROM stock.stock";
            ResultSet productNameSet = statement.executeQuery(findProductName);
            List<Integer> productName = new ArrayList<>();
            while (productNameSet.next()) {
                //System.out.println("成功");
                System.out.println(productNameSet.getInt("productId"));
                productName.add(productNameSet.getInt("productId"));
            }

            for (int stockName : productName) {
                String sql = "SELECT * FROM stock.stock where productId = " + stockName + "";
                System.out.println(sql);
                ResultSet productSet = statement.executeQuery(sql);

                while (productSet.next()) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("productName", productSet.getString("productName"));
                    stock.put("productPrice", productSet.getDouble("productPrice"));
                    stock.put("date", productSet.getDate("date"));
                    stock.put("productType", "股票");
                    stock.put("productId", productSet.getInt("productId"));
                /*System.out.println(stock.get("date"));
                System.out.println(stock.get("productName"));
                System.out.println(stock.get("price"));
                System.out.println(stock.get("productId"));*/
                    stocks.add(stock);
                }
            }
        }
        else if(productType.equals("基金")){
            String findFundName = "SELECT distinct productId FROM stock.fund";
            ResultSet fundNameSet = statement.executeQuery(findFundName);
            List<Integer> fundName = new ArrayList<>();
            while (fundNameSet.next()) {
                //System.out.println("成功");
                System.out.println(fundNameSet.getInt("productId"));
                fundName.add(fundNameSet.getInt("productId"));
            }
            for (int fundId : fundName) {
                String sql = "SELECT * FROM stock.fund where productId = " + fundId + "";
                System.out.println(sql);
                ResultSet productSet = statement.executeQuery(sql);
                while (productSet.next()) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("productName", productSet.getString("productName"));
                    stock.put("rate", productSet.getDouble("rate"));
                    stock.put("date", productSet.getDate("date"));
                    stock.put("productType", "基金");
                    stock.put("productId", productSet.getInt("productId"));
               /* System.out.println(stock.get("date"));
                System.out.println(stock.get("productName"));
                System.out.println(stock.get("price"));
                System.out.println(stock.get("productId"));*/
                    stocks.add(stock);
                }
            }
        }
        else{
            String findRegularName = "SELECT distinct productId FROM stock.regular";
            ResultSet regularNameSet = statement.executeQuery(findRegularName);
            List<Integer> regularName = new ArrayList<>();
            while (regularNameSet.next()) {
                //System.out.println("成功");
                System.out.println(regularNameSet.getInt("productId"));
                regularName.add(regularNameSet.getInt("productId"));
            }
            for (int regularId : regularName) {
                String sql = "SELECT * FROM stock.regular where productId = " + regularId + "";
                System.out.println(sql);
                ResultSet productSet = statement.executeQuery(sql);
                while (productSet.next()) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("productName", productSet.getString("productName"));
                    stock.put("productPrice", productSet.getInt("productPrice"));
                    stock.put("period", productSet.getInt("period"));
                    stock.put("productType", "定期");
                    stock.put("productId", productSet.getInt("productId"));
                    stock.put("rate", productSet.getDouble("rate"));
               /* System.out.println(stock.get("date"));
                System.out.println(stock.get("productName"));
                System.out.println(stock.get("price"));
                System.out.println(stock.get("productId"));*/
                    stocks.add(stock);
                }
            }
        }


        System.out.println(stocks);


        return stocks;
    }

    public Map<String,Object> buyProduct(String customerNum, int productId, java.util.Date tradeTime, int purchase) throws Exception {
        Map<String, Object> returnMsg = new HashMap<>();
        int credit = (int) loanService.getCredit(customerNum).get("credit");


        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        List<Map<String,Object>> stockProducts = getProduct("股票");
        List<Map<String,Object>> fundProducts = getProduct("基金");
        List<Map<String,Object>> regularProducts = getProduct("定期");
        List<Map<String,Object>> products = new ArrayList<>();
        for(Map stock:stockProducts){
            products.add(stock);
        }
        for(Map fund:fundProducts){
            products.add(fund);
        }
        for(Map regular:regularProducts){
            products.add(regular);
        }
        String productType = "";
        for(Map product:products){
            int id = (int) product.get("productId");
            if(id == productId){
                productType = product.get("productType").toString();
            }
            else{
                returnMsg.put("flag",false);
                returnMsg.put("message","产品不存在");
            }
        }
        if(productType.equals("股票")){
            if(credit != 1){
                returnMsg.put("flag",false);
                returnMsg.put("message","当前客户信用等级不足，无法购买股票");
                return returnMsg;
            }
            else{
                List<Map<String, Object>> list = (List<Map<String, Object>>) loanService.getLoanList(customerNum).get("res");
                List<String> iouNums = new ArrayList<>();
                for (Map map : list) {
                    iouNums.add(map.get("iouNum").toString());
                }
                List<Map<String, Object>> overdue = new ArrayList<>();
                for (String iouNum : iouNums) {
                    overdue = (List<Map<String, Object>>) loanService.getLoanPlan(iouNum).get("overdue");

                }
                if (!overdue.isEmpty()) {//如果有罚金
                    for (String iouNum : iouNums) {
                        Map<String, Object> res2 = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum=" + iouNum);
                        Object o4 = res2.get("data");
                        String loanJson = httpUtils.gson.toJson(o4);
                        Map<String, Object>[] loanMap = httpUtils.gson.fromJson(loanJson, Map[].class);
                        Map<String, Object> loanDetails = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/" + iouNum);
                        Object o2 = loanDetails.get("data");
                        String loanJson2 = httpUtils.gson.toJson(o2);
                        Map<String, Object> loanMap2 = httpUtils.gson.fromJson(loanJson2, Map.class);
                        String accountNum = loanMap2.get("accountNum").toString();
                        String customerCode = loanMap2.get("customerCode").toString();


                        Map<String, Object> res3 = httpUtils.httpClientGet("http://10.176.122.172:8012/account?customerCode=" + customerCode);

                        Object o6 = res3.get("data");
                        String accountJson = httpUtils.gson.toJson(o6);
                        Map<String, Object>[] customerMap = httpUtils.gson.fromJson(accountJson, Map[].class);
                        Object account = customerMap[0].get("accountDtos");
                        String accountDetail = httpUtils.gson.toJson(account);
                        Map<String, Object>[] accountMap = httpUtils.gson.fromJson(accountDetail, Map[].class);
                        Double balance = 0.0;
                        for (Map accountMaps : accountMap) {
                            if (accountMaps.get("accountNum").toString().equals(accountNum)) {
                                balance = Double.parseDouble(accountMaps.get("balance").toString());
                            }
                        }
                        logger.info("balance" + balance);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        for (int i = 0; i < loanMap.length; i++) {
                            String id_ = loanMap[i].get("id").toString();
                            int id = (int) Double.parseDouble(id_);
                            String date = loanMap[i].get("planDate").toString();
                            Double remainAmount = Double.parseDouble(loanMap[i].get("remainAmount").toString());
                            java.util.Date planDate = df.parse(date);
                            //java.util.Date currentDate2 = df.parse(tradeTime);
                            if (planDate.before(tradeTime)) {
                                Double penalty = remainAmount * 0.05;
                                if (balance < penalty) {
                                    returnMsg.put("flag",false);
                                    returnMsg.put("message", "购买失败，请先缴纳罚金");
                                    return returnMsg;

                                } else if (balance >= penalty & balance < remainAmount) {
                                    loanService.repaymentOverdue(iouNum, i, penalty, tradeTime.toString());

                                }

                            }


                        }
                    }

                }
                int recordId;
                String getRecordId = "SELECT recordID FROM stock.property order by recordID desc limit 0,1";
                ResultSet rs = statement.executeQuery(getRecordId);
                if (rs.next()) {
                    recordId = rs.getInt("recordID");
                    recordId++;
                } else {
                    recordId = 1;
                }
                String insertProperty = "INSERT INTO stock.property (recordID,customerID,productID,amount,purchaseDay) VALUES (" + recordId + "," + customerNum + "," + productId + "," + purchase + ",\'" + tradeTime + "\')";
                System.out.println(insertProperty);
                statement.execute(insertProperty);
                returnMsg.put("flag",true);
                returnMsg.put("message","购买成功");

            }

        }
        else if(productType.equals("基金")){
            if(credit==3){
                returnMsg.put("flag",false);
                returnMsg.put("message","当前客户信用等级不足，无法购买基金");
                return returnMsg;
            }
            else{
                List<Map<String, Object>> list = (List<Map<String, Object>>) loanService.getLoanList(customerNum).get("res");
                List<String> iouNums = new ArrayList<>();
                for (Map map : list) {
                    iouNums.add(map.get("iouNum").toString());
                }
                List<Map<String, Object>> overdue = new ArrayList<>();
                for (String iouNum : iouNums) {
                    overdue = (List<Map<String, Object>>) loanService.getLoanPlan(iouNum).get("overdue");

                }
                if (!overdue.isEmpty()) {//如果有罚金
                    for (String iouNum : iouNums) {
                        Map<String, Object> res2 = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum=" + iouNum);
                        Object o4 = res2.get("data");
                        String loanJson = httpUtils.gson.toJson(o4);
                        Map<String, Object>[] loanMap = httpUtils.gson.fromJson(loanJson, Map[].class);
                        Map<String, Object> loanDetails = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/" + iouNum);
                        Object o2 = loanDetails.get("data");
                        String loanJson2 = httpUtils.gson.toJson(o2);
                        Map<String, Object> loanMap2 = httpUtils.gson.fromJson(loanJson2, Map.class);
                        String accountNum = loanMap2.get("accountNum").toString();
                        String customerCode = loanMap2.get("customerCode").toString();


                        Map<String, Object> res3 = httpUtils.httpClientGet("http://10.176.122.172:8012/account?customerCode=" + customerCode);

                        Object o6 = res3.get("data");
                        String accountJson = httpUtils.gson.toJson(o6);
                        Map<String, Object>[] customerMap = httpUtils.gson.fromJson(accountJson, Map[].class);
                        Object account = customerMap[0].get("accountDtos");
                        String accountDetail = httpUtils.gson.toJson(account);
                        Map<String, Object>[] accountMap = httpUtils.gson.fromJson(accountDetail, Map[].class);
                        Double balance = 0.0;
                        for (Map accountMaps : accountMap) {
                            if (accountMaps.get("accountNum").toString().equals(accountNum)) {
                                balance = Double.parseDouble(accountMaps.get("balance").toString());
                            }
                        }
                        logger.info("balance" + balance);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        for (int i = 0; i < loanMap.length; i++) {
                            String id_ = loanMap[i].get("id").toString();
                            int id = (int) Double.parseDouble(id_);
                            String date = loanMap[i].get("planDate").toString();
                            Double remainAmount = Double.parseDouble(loanMap[i].get("remainAmount").toString());
                            java.util.Date planDate = df.parse(date);
                            //java.util.Date currentDate2 = df.parse(tradeTime);
                            if (planDate.before(tradeTime)) {
                                Double penalty = remainAmount * 0.05;
                                if (balance < penalty) {
                                    returnMsg.put("flag",false);
                                    returnMsg.put("message", "购买失败，请先缴纳罚金");
                                    return returnMsg;

                                } else if (balance >= penalty & balance < remainAmount) {
                                    loanService.repaymentOverdue(iouNum, i, penalty, tradeTime.toString());

                                }

                            }


                        }
                    }

                }
                int recordId;
                String getRecordId = "SELECT recordID FROM stock.property order by recordID desc limit 0,1";
                ResultSet rs = statement.executeQuery(getRecordId);
                if (rs.next()) {
                    recordId = rs.getInt("recordID");
                    recordId++;
                } else {
                    recordId = 1;
                }
                String insertProperty = "INSERT INTO stock.property (recordID,customerID,productID,amount,purchaseDay) VALUES (" + recordId + "," + customerNum + "," + productId + "," + purchase + ",\'" + tradeTime + "\')";
                System.out.println(insertProperty);
                statement.execute(insertProperty);
                returnMsg.put("flag",true);
                returnMsg.put("message","购买成功");
            }
        }
        else{
            List<Map<String, Object>> list = (List<Map<String, Object>>) loanService.getLoanList(customerNum).get("res");
            List<String> iouNums = new ArrayList<>();
            for (Map map : list) {
                iouNums.add(map.get("iouNum").toString());
            }
            List<Map<String, Object>> overdue = new ArrayList<>();
            for (String iouNum : iouNums) {
                overdue = (List<Map<String, Object>>) loanService.getLoanPlan(iouNum).get("overdue");

            }
            if (!overdue.isEmpty()) {//如果有罚金
                for (String iouNum : iouNums) {
                    Map<String, Object> res2 = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/plan?iouNum=" + iouNum);
                    Object o4 = res2.get("data");
                    String loanJson = httpUtils.gson.toJson(o4);
                    Map<String, Object>[] loanMap = httpUtils.gson.fromJson(loanJson, Map[].class);
                    Map<String, Object> loanDetails = httpUtils.httpClientGet("http://10.176.122.172:8012/loan/" + iouNum);
                    Object o2 = loanDetails.get("data");
                    String loanJson2 = httpUtils.gson.toJson(o2);
                    Map<String, Object> loanMap2 = httpUtils.gson.fromJson(loanJson2, Map.class);
                    String accountNum = loanMap2.get("accountNum").toString();
                    String customerCode = loanMap2.get("customerCode").toString();


                    Map<String, Object> res3 = httpUtils.httpClientGet("http://10.176.122.172:8012/account?customerCode=" + customerCode);

                    Object o6 = res3.get("data");
                    String accountJson = httpUtils.gson.toJson(o6);
                    Map<String, Object>[] customerMap = httpUtils.gson.fromJson(accountJson, Map[].class);
                    Object account = customerMap[0].get("accountDtos");
                    String accountDetail = httpUtils.gson.toJson(account);
                    Map<String, Object>[] accountMap = httpUtils.gson.fromJson(accountDetail, Map[].class);
                    Double balance = 0.0;
                    for (Map accountMaps : accountMap) {
                        if (accountMaps.get("accountNum").toString().equals(accountNum)) {
                            balance = Double.parseDouble(accountMaps.get("balance").toString());
                        }
                    }
                    logger.info("balance" + balance);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    for (int i = 0; i < loanMap.length; i++) {
                        String id_ = loanMap[i].get("id").toString();
                        int id = (int) Double.parseDouble(id_);
                        String date = loanMap[i].get("planDate").toString();
                        Double remainAmount = Double.parseDouble(loanMap[i].get("remainAmount").toString());
                        java.util.Date planDate = df.parse(date);
                        //java.util.Date currentDate2 = df.parse(tradeTime);
                        if (planDate.before(tradeTime)) {
                            Double penalty = remainAmount * 0.05;
                            if (balance < penalty) {
                                returnMsg.put("flag",false);
                                returnMsg.put("message", "购买失败，请先缴纳罚金");
                                return returnMsg;

                            } else if (balance >= penalty & balance < remainAmount) {
                                loanService.repaymentOverdue(iouNum, i, penalty, tradeTime.toString());

                            }

                        }


                    }
                }

            }
            int recordId;
            String getRecordId = "SELECT recordID FROM stock.property order by recordID desc limit 0,1";
            ResultSet rs = statement.executeQuery(getRecordId);
            if (rs.next()) {
                recordId = rs.getInt("recordID");
                recordId++;
            } else {
                recordId = 1;
            }
            String insertProperty = "INSERT INTO stock.property (recordID,customerID,productID,amount,purchaseDay) VALUES (" + recordId + "," + customerNum + "," + productId + "," + purchase + ",\'" + tradeTime + "\')";
            System.out.println(insertProperty);
            statement.execute(insertProperty);
            returnMsg.put("flag",true);
            returnMsg.put("message","购买成功");
        }


        return returnMsg;


    }
    public List<Map<String,Object>> getProperty(String customerNum) throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        List<Map<String,Object>> properties = new ArrayList<>();
        String getCustomerProperty = "SELECT * FROM stock.property where customerID = \""+customerNum+"\"";
        ResultSet rs = statement.executeQuery(getCustomerProperty);
        
        while(rs.next()){
            Map<String,Object> property = new HashMap<>();
            property.put("recordId",rs.getInt("recordID"));
            property.put("customerId",rs.getString("customerID"));
            property.put("productId",rs.getInt("productID"));
            property.put("amount",rs.getInt("amount"));
            property.put("purchaseDay",rs.getDate("purchaseDay"));
            properties.add(property);

            System.out.println(property);
        }
        return properties;
    }
}
