package fudan.sq.controller;


import fudan.sq.service.LoanService;
import fudan.sq.service.StockService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class Controller {

    Logger logger = LoggerFactory.getLogger(Controller.class);
    org.slf4j.Marker marker;

    @Autowired
    LoanService loanService;
    @Autowired
    StockService stockService;
    @Autowired
    public Controller(LoanService loanService,StockService stockService) {
        this.loanService = loanService;
        this.stockService = stockService;
    }

    /**
     * 查询客户信息
     * */
    @GetMapping("/getClientInfo/{id}")
    public ResponseEntity<?> getClientInfo(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(loanService.getClientInfo(id));
    }

    /**
     * 批量还款
     * */
    @PostMapping("/batchRepayment")
    public ResponseEntity<?> batchRepaymentLoan(@RequestBody Map<String,String> request) throws Exception {
        return ResponseEntity.ok(loanService.batchRepaymentLoan(request.get("currentDate")));
    }


    /**
     * 根据客户号获取还款列表
     * 参数：客户号customerCode
     * */
    @GetMapping("/getLoanList/{customerCode}")
    public ResponseEntity<?> getLoanList(@PathVariable String customerCode) throws Exception {
        return ResponseEntity.ok(loanService.getLoanList(customerCode));
    }

    /**
     * 获取还款计划
     * */
    @GetMapping("/getLoanPlan/{iouNum}")
    public ResponseEntity<?> getLoanPlan(@PathVariable String iouNum) throws Exception {
        return ResponseEntity.ok(loanService.getLoanPlan(iouNum));
    }

    /**
     * 还款
     * */
    @PostMapping("/repayment")
    public ResponseEntity<?> repayment(@RequestBody Map<String,String> request) throws Exception {
        logger.info("请求参数："+request);
        String iouNum = request.get("iouNum");
        int id = Integer.parseInt(request.get("planNum"));
        double amount = Double.parseDouble(request.get("amount"));
        return ResponseEntity.ok(loanService.repayment(iouNum,id,amount));
    }

    /**
     * 查询流水号
     * * 查询条件
     *     * {
     *     *     startTime:开始时间
     *     *     endTime:结束时间
     *     *     account：账号
     *     *     branchName：办理机构
     *     *     transactionType：交易类型
     *     *
     * */
    @PostMapping("/getTransaction")
    public ResponseEntity<?> getTransaction(@RequestBody Map<String,String> request) throws Exception {
        return ResponseEntity.ok(loanService.getTransaction(request));
    }

    /**
     * 查看所有贷款
     * */
    @GetMapping("/getAllLoans")
    public ResponseEntity<?> getAllLoans() throws Exception {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
    /**
     * 查看用户信用等级
     * */
    @GetMapping("/getCredit/{customerCode}")
    public ResponseEntity<?> getCredit(@PathVariable String customerCode) throws Exception {

        return ResponseEntity.ok(loanService.getCredit(customerCode));
    }

    /**
     * 获取产品
     * */
    @GetMapping("/getProduct/{productType}")
    @ResponseBody
    public ResponseEntity<?> getProduct(@PathVariable String productType) throws Exception {

        return ResponseEntity.ok(stockService.getProduct(productType));
    }


    /**
     * 购买产品
     * */
    @PostMapping("/buyProduct")
    public ResponseEntity<?> buyProduct(@RequestBody Map<String,String> request) throws Exception {
        String customerNum = request.get("customerNum");
        int productId = Integer.parseInt(request.get("productId"));

       /* System.out.println("转换前"+request.get("tradeTime"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = df.parse(request.get("tradeTime"));
        Date date2 = df.parse(request.get("tradeTime"));
        System.out.println(date2);*/
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Date date = df.parse(request.get("tradeTime"));
        System.out.println(date);

        int purchase = Integer.parseInt(request.get("purchase"));
        String accountNum = request.get("accountNumber");


        return ResponseEntity.ok(stockService.buyProduct(customerNum,productId, date,purchase,accountNum));
    }

    /**
     * 查看用户购买产品
     * */
    @GetMapping("/getProperties/{customerCode}")
    public ResponseEntity<?> getProperty(@PathVariable String customerCode) throws Exception {
        return ResponseEntity.ok(stockService.getProperty(customerCode));
    }
    /**
     * 查看用户余额
     * */
    @GetMapping("/getBalance/{customerCode}")
    public ResponseEntity<?> getBalance(@PathVariable String customerCode) throws Exception {
        return ResponseEntity.ok(loanService.getBalanceByCustomerCode(customerCode));
    }



    /**
     * 查看用户盈利
     * */
    @GetMapping("/getProfit/{customerCode}/{currentDate}")
    public ResponseEntity<?> getProfit(@PathVariable String customerCode,@PathVariable String currentDate) throws Exception {
        return ResponseEntity.ok(stockService.getProfit(customerCode,currentDate));
    }

    /**
     * 获取所有逾期订单
     * */
    @PostMapping("/searchOverdueLoans")
    public ResponseEntity<?> searchOverdueLoans(@RequestBody Map<String,String> request) throws Exception {
        return ResponseEntity.ok(loanService.searchBatchRepayment(request.get("data")));
    }

    /**
     * 获取客户账户
     * */
    @GetMapping("/getAccountNums/{customerCode}")
    public ResponseEntity<?> getAccountNums(@PathVariable String customerCode) throws Exception {
        return ResponseEntity.ok(loanService.getAccountNumByCustomerId(customerCode));
    }

}
