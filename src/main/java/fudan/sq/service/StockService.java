package fudan.sq.service;


import fudan.sq.entity.Account;
import fudan.sq.entity.Repayment;
import fudan.sq.entity.Stock;
import fudan.sq.httpUtils.httpUtils;
import fudan.sq.repository.AccountRepository;
import fudan.sq.repository.RepaymentRepository;
import fudan.sq.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.sql.*;
import java.text.ParseException;
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
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    RepaymentRepository repaymentRepository;
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
        if(productType.equals("1")){
            productType = "股票";
        }
        else if(productType.equals("2")){
            productType = "基金";
        }
        else{
            productType = "定期";
        }
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
                List<Double> prices = new ArrayList<>();
                List<Date> dates = new ArrayList<>();
                Map<String, Object> stock = new HashMap<>();
                String stockNAME = "";
                while (productSet.next()) {

                    prices.add(productSet.getDouble("productPrice"));
                    dates.add(productSet.getDate("date"));

                    stockNAME = productSet.getString("productName");
                }
                stock.put("productName",stockNAME);
                stock.put("productId",stockName);
                stock.put("productPrice",prices.get(prices.size()-1));
                stock.put("price",prices);
                stock.put("date",dates);
                stock.put("productType", "股票");
                stocks.add(stock);
            }
            for(int i = 0;i<stocks.size();i++){
                System.out.println(stocks.get(i));
            };
        }
        else if(productType.equals("基金")){
            String findProductName = "SELECT distinct productId FROM stock.fund";
            ResultSet productNameSet = statement.executeQuery(findProductName);
            List<Integer> productName = new ArrayList<>();
            while (productNameSet.next()) {
                //System.out.println("成功");
                System.out.println(productNameSet.getInt("productId"));
                productName.add(productNameSet.getInt("productId"));
            }

            for (int stockName : productName) {
                String sql = "SELECT * FROM stock.fund where productId = " + stockName + "";
                System.out.println(sql);
                ResultSet productSet = statement.executeQuery(sql);
                List<Double> prices = new ArrayList<>();
                List<Date> dates = new ArrayList<>();
                Map<String, Object> stock = new HashMap<>();
                String stockNAME = "";
                while (productSet.next()) {

                    prices.add(productSet.getDouble("rate"));
                    dates.add(productSet.getDate("date"));

                    stockNAME = productSet.getString("productName");
                }
                stock.put("productName",stockNAME);
                stock.put("productId",stockName);
                stock.put("productRate",prices.get(prices.size()-1));
                stock.put("rate",prices);
                stock.put("date",dates);
                stock.put("productType", "基金");
                stocks.add(stock);
            }
            for(int i = 0;i<stocks.size();i++){
                System.out.println(stocks.get(i));
            };
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

    public Map<String,Object> buyProduct(String customerNum, int productId, java.util.Date tradeTime, int purchase,String accountNumber) throws Exception {
        Map<String, Object> returnMsg = new HashMap<>();
        int credit = (int) loanService.getCredit(customerNum).get("credit");


        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        List<Map<String,Object>> stockProducts = getProduct("1");
        List<Map<String,Object>> fundProducts = getProduct("2");
        List<Map<String,Object>> regularProducts = getProduct("3");
        List<Map<String,Object>> products = new ArrayList<>();
        products.addAll(stockProducts);
        products.addAll(fundProducts);
        products.addAll(regularProducts);
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
            else{//如果有罚金先付清罚金
                Map<String, Object> res = loanService.getLoanList(customerNum);
                List<Map<String,Object>> list = (List<Map<String, Object>>) res.get("res");
                List<String> iouNums = new ArrayList<>();
                List<String> overdueIouNums = new ArrayList<>();
                for (Map map : list) {
                    iouNums.add(map.get("iouNum").toString());
                }
                List<Map<String, Object>> overdues = new ArrayList<>();
                for (String iouNum : iouNums) {

                    List<Map<String,Object>> temps = loanService.getOverdueLoanPlanByDate(iouNum,tradeTime.toString());//找到该用户所有过期的贷款
                    overdues.addAll(temps);

                }
                if (!overdues.isEmpty()) {//如果有罚金，


                    for(Map overdue:overdues){
                        String customerCode = overdue.get("customerCode").toString();
                        //根据客户号获取客户账户余额
                        Double balance = loanService.getBalanceByCustomerCode(customerCode);
                        String iouNum = overdue.get("iouNum").toString();
                        int planNum = (int) Double.parseDouble(overdue.get("planNum").toString());
                        //获得罚金
                        Repayment repayment = repaymentRepository.findByIouNumAndPanNum(iouNum,planNum);
                        Double penalty = 0.0;
                        if(!repayment.isPenaltyInterestClear()){
                            penalty = repayment.getPenaltyInterest();
                        }
                        Map<String,Object> result = loanService.repayment(iouNum,planNum,penalty);
                        result.put("customerCode",customerCode);
                        result.put("balance",balance);
                        returnMsg.put("message",result);
                        if(result.get("message").equals("贷款 "+iouNum+"  第 "+planNum+" 期部分还款失败,先缴清罚金")){
                            returnMsg.put("flag",false);
                            returnMsg.put("message","罚金不足，产品购买失败");
                        }
                        return returnMsg;
                    }


                }
                Double allBalance = loanService.getBalanceByCustomerCode(customerNum);
                Account account = accountRepository.findByAccountNumAndCustomerNum(accountNumber,customerNum);
                Double balance = account.getBalance();
                String findProduct = "SELECT * FROM stock.stock where productId = "+productId+" and date = \""+tradeTime+"\"";
                ResultSet resultSet = statement.executeQuery(findProduct);
                Double price = 0.0;
                while(resultSet.next()){
                    price = resultSet.getDouble("productPrice")*purchase;
                }
                if(balance<price){
                    returnMsg.put("flag",false);
                    returnMsg.put("message","当前账户余额不足，购买失败");
                    return returnMsg;
                }
                else{
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
                    account.setBalance(balance-price);
                    returnMsg.put("flag",true);
                    returnMsg.put("message","购买成功");
                }



            }

        }
        else if(productType.equals("基金")){
            if(credit==3){
                returnMsg.put("flag",false);
                returnMsg.put("message","当前客户信用等级不足，无法购买基金");
                return returnMsg;
            }
            else{
                Map<String, Object> res = loanService.getLoanList(customerNum);
                List<Map<String,Object>> list = (List<Map<String, Object>>) res.get("res");
                List<String> iouNums = new ArrayList<>();
                List<String> overdueIouNums = new ArrayList<>();
                for (Map map : list) {
                    iouNums.add(map.get("iouNum").toString());
                }
                List<Map<String, Object>> overdues = new ArrayList<>();
                for (String iouNum : iouNums) {

                    List<Map<String,Object>> temps = loanService.getOverdueLoanPlanByDate(iouNum,tradeTime.toString());//找到该用户所有过期的贷款
                    overdues.addAll(temps);

                }
                if (!overdues.isEmpty()) {//如果有罚金，


                    for(Map overdue:overdues){
                        String customerCode = overdue.get("customerCode").toString();
                        //根据客户号获取客户账户余额
                        Double balance = loanService.getBalanceByCustomerCode(customerCode);
                        String iouNum = overdue.get("iouNum").toString();
                        int planNum = (int) Double.parseDouble(overdue.get("planNum").toString());
                        //获得罚金
                        Repayment repayment = repaymentRepository.findByIouNumAndPanNum(iouNum,planNum);
                        Double penalty = 0.0;
                        if(!repayment.isPenaltyInterestClear()){
                            penalty = repayment.getPenaltyInterest();
                        }
                        Map<String,Object> result = loanService.repayment(iouNum,planNum,penalty);
                        result.put("customerCode",customerCode);
                        result.put("balance",balance);
                        returnMsg.put("message",result);
                        if(result.get("message").equals("贷款 "+iouNum+"  第 "+planNum+" 期部分还款失败,先缴清罚金")){
                            returnMsg.put("flag",false);
                            returnMsg.put("message","罚金不足，产品购买失败");
                        }
                        return returnMsg;
                    }


                }
                Double allBalance = loanService.getBalanceByCustomerCode(customerNum);
                Account account = accountRepository.findByAccountNumAndCustomerNum(accountNumber,customerNum);
                Double balance = account.getBalance();
                String findProduct = "SELECT * FROM stock.fund where productId = "+productId+" and date = \""+tradeTime+"\"";
                ResultSet resultSet = statement.executeQuery(findProduct);
                Double price = 0.0;
                while(resultSet.next()){
                    price = resultSet.getDouble("productPrice")*purchase;
                }
                if(balance<purchase){
                    returnMsg.put("flag",false);
                    returnMsg.put("message","当前账户余额不足，购买失败");
                    return returnMsg;
                }
                else{
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
                    account.setBalance(balance-purchase);
                    returnMsg.put("flag",true);
                    returnMsg.put("message","购买成功");
                }
            }
        }
        else{
            Map<String, Object> res = loanService.getLoanList(customerNum);
            List<Map<String,Object>> list = (List<Map<String, Object>>) res.get("res");
            List<String> iouNums = new ArrayList<>();
            List<String> overdueIouNums = new ArrayList<>();
            for (Map map : list) {
                iouNums.add(map.get("iouNum").toString());
            }
            List<Map<String, Object>> overdues = new ArrayList<>();
            for (String iouNum : iouNums) {

                List<Map<String,Object>> temps = loanService.getOverdueLoanPlanByDate(iouNum,tradeTime.toString());//找到该用户所有过期的贷款
                overdues.addAll(temps);

            }
            if (!overdues.isEmpty()) {//如果有罚金，


                for(Map overdue:overdues){
                    String customerCode = overdue.get("customerCode").toString();
                    //根据客户号获取客户账户余额
                    Double balance = loanService.getBalanceByCustomerCode(customerCode);
                    String iouNum = overdue.get("iouNum").toString();
                    int planNum = (int) Double.parseDouble(overdue.get("planNum").toString());
                    //获得罚金
                    Repayment repayment = repaymentRepository.findByIouNumAndPanNum(iouNum,planNum);
                    Double penalty = 0.0;
                    if(!repayment.isPenaltyInterestClear()){
                        penalty = repayment.getPenaltyInterest();
                    }
                    Map<String,Object> result = loanService.repayment(iouNum,planNum,penalty);
                    result.put("customerCode",customerCode);
                    result.put("balance",balance);
                    returnMsg.put("message",result);
                    if(result.get("message").equals("贷款 "+iouNum+"  第 "+planNum+" 期部分还款失败,先缴清罚金")){
                        returnMsg.put("flag",false);
                        returnMsg.put("message","罚金不足，产品购买失败");
                    }
                    return returnMsg;
                }


            }
            Double allBalance = loanService.getBalanceByCustomerCode(customerNum);
            Account account = accountRepository.findByAccountNumAndCustomerNum(accountNumber,customerNum);
            Double balance = account.getBalance();
            String findProduct = "SELECT * FROM stock.regular where productId = "+productId;
            ResultSet resultSet = statement.executeQuery(findProduct);
            Double price = 0.0;
            while(resultSet.next()){
                price = resultSet.getDouble("productPrice")*purchase;
            }
            if(balance<price){
                returnMsg.put("flag",false);
                returnMsg.put("message","当前账户余额不足，购买失败");
                return returnMsg;
            }
            else{
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
                account.setBalance(balance-price);
                returnMsg.put("flag",true);
                returnMsg.put("message","购买成功");
            }
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

    public Map<String,Object> getProfit(String customerID,String currentDate) throws SQLException, ParseException {
        Connection connection = getConnection();
        List<Map<String,Object>> customerProperties = getProperty(customerID);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = df.parse(currentDate);
        List<Map<String,Object>> stockMap = new ArrayList<>();
        List<Map<String,Object>> fundMap = new ArrayList<>();
        List<Map<String,Object>> regularMap = new ArrayList<>();
        Statement statement = connection.createStatement();
        int recordId;
        int productId;
        int amount;
        String purchaseDay;
        Double profit;
        List<Map<String,Object>> stockProducts = getProduct("1");
        List<Map<String,Object>> fundProducts = getProduct("2");
        List<Map<String,Object>> regularProducts = getProduct("3");
        List<Map<String,Object>> products = new ArrayList<>();
        Map<String,Object> returnMap = new HashMap<>();
        products.addAll(stockProducts);
        products.addAll(fundProducts);
        products.addAll(regularProducts);
        String productType = "";
        for(Map customerProperty:customerProperties){

            recordId = Integer.parseInt(customerProperty.get("recordId").toString());
            productId = Integer.parseInt(customerProperty.get("productId").toString());
            amount = Integer.parseInt(customerProperty.get("amount").toString());
            purchaseDay = customerProperty.get("purchaseDay").toString();

            for(Map product:products){
                int id = (int) product.get("productId");
                if(id == productId){
                    productType = product.get("productType").toString();
                }

            }
            System.out.println("产品类型"+productType);
            if(productType.equals("股票")){
                //System.out.println(111);
                Map<String,Object> product = new HashMap<>();
                Double price = 0.0;
                Double currentPrice = 0.0;
                int priceChange =0;
                String dateChange="";
                String productName = "";
                List<Integer> prices = new ArrayList<>();
                List<String> dateSet = new ArrayList<>();

                String sql = "SELECT * FROM stock.stock where productId = \""+productId+"\" and date = \""+ purchaseDay +"\"";
                ResultSet rs = statement.executeQuery(sql);
                while(rs.next()){

                    price = rs.getDouble("productPrice");
                    System.out.println("购买日价格："+price);
                    productName = rs.getString("productName");
                }
                String sql2 = "SELECT * FROM stock.stock where productId = \""+productId+"\" and date = \""+currentDate +"\"";
                ResultSet rs2 = statement.executeQuery(sql2);
                while(rs2.next()){
                    currentPrice = rs2.getDouble("productPrice");
                    System.out.println("购买日价格："+currentPrice);
                }
                String sql3 = "SELECT * FROM stock.stock where productId = \""+productId+"\" and date between \""+purchaseDay+"\" and \""+ currentDate+"\"";
                ResultSet rs3 = statement.executeQuery(sql3);
                while(rs3.next()){
                    priceChange = rs3.getInt("productPrice");
                    prices.add(priceChange);
                    dateChange= String.format(rs3.getDate("date").toString(), "yyyy-MM-dd");

                    dateSet.add(dateChange);

                }
                System.out.println(prices);
                profit = (currentPrice-price)*amount;
                product.put("profit",profit);
                product.put("productId",productId);
                product.put("productName",productName);
                product.put("priceChange",prices);
                product.put("amount",amount);
                product.put("purchaseDay",purchaseDay);
                product.put("recordId",recordId);
                product.put("customerId",customerID);
                product.put("date",dateSet);
                System.out.println(product);
                stockMap.add(product);


            }
            else if(productType.equals("基金")){
                Map<String,Object> product = new HashMap<>();
                Double price = 0.0;
                Double currentPrice = 0.0;
                Double priceChange = 0.0;
                String dateChange="";
                String productName = "";
                List<Double> prices = new ArrayList<>();
                List<String> dateSet = new ArrayList<>();

                String sql = "SELECT * FROM stock.fund where productId = \""+ productId +"\" and date = \""+ purchaseDay +"\"";
                ResultSet rs = statement.executeQuery(sql);
                while(rs.next()){

                    price = rs.getDouble("rate");
                    System.out.println("购买日价格："+price);
                    productName = rs.getString("productName");
                }
                String sql2 = "SELECT * FROM stock.fund where productId = \""+productId+"\" and date = \""+currentDate +"\"";
                ResultSet rs2 = statement.executeQuery(sql2);
                while(rs2.next()){
                    currentPrice = rs2.getDouble("rate");
                    System.out.println("购买日价格："+currentPrice);
                }
                String sql3 = "SELECT * FROM stock.fund where productId = \""+productId+"\" and date between \""+purchaseDay+"\" and \""+ currentDate+"\"";
                ResultSet rs3 = statement.executeQuery(sql3);
                while(rs3.next()){
                    priceChange = rs3.getDouble("rate");

                    prices.add(priceChange);
                    dateChange= String.format(rs3.getDate("date").toString(), "yyyy-MM-dd");

                    dateSet.add(dateChange);


                }
                System.out.println(prices);
                profit = (currentPrice-price)*amount/100;
                product.put("profit",profit);
                product.put("productId",productId);
                product.put("productName",productName);
                product.put("priceChange",prices);
                product.put("amount",amount);
                product.put("purchaseDay",purchaseDay);
                product.put("recordId",recordId);
                product.put("customerId",customerID);
                product.put("date",dateSet);
                System.out.println(product);
                fundMap.add(product);
            }
            else{
                Map<String,Object> product = new HashMap<>();
                int price = 0;
                int currentPrice = 0;
                int priceChange =0;
                int period = 0;
                Double rate = 0.0;
                String productName = "";
                List<Integer> prices = new ArrayList<>();
                String sql = "SELECT * FROM stock.regular where productId = \""+ productId +"\" ";
                ResultSet rs = statement.executeQuery(sql);
                while(rs.next()){
                    rate = rs.getDouble("rate");
                    period = rs.getInt("period");

                    price = rs.getInt("productPrice");
                    System.out.println("购买日价格："+price);
                    productName = rs.getString("productName");
                }
                String dueDate = "";
                profit = price*rate*amount/100;
                product.put("profit",profit);
                product.put("productId",productId);
                product.put("productName",productName);
                product.put("productPrice",price);
                product.put("priceChange",rate);
                product.put("amount",amount);
                product.put("purchaseDay",purchaseDay);
                product.put("recordId",recordId);
                product.put("customerId",customerID);
                product.put("period",period);
                System.out.println(product);
                regularMap.add(product);
            }


        }
        returnMap.put("股票",stockMap);
        returnMap.put("基金",fundMap);
        returnMap.put("定期",regularMap);
        System.out.println(returnMap);
        return returnMap;
    }
}
