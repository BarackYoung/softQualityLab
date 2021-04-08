package fudan.sq;

import fudan.sq.httpUtils.httpUtils;


import fudan.sq.service.JwtUserDetailsService;
import fudan.sq.service.LoanService;
import fudan.sq.service.StockService;
import fudan.sq.service.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.SimpleDateFormat;
import java.util.*;


@SpringBootApplication
public class Application {

    @Autowired
    JwtUserDetailsService jwtUserDetailsService;
    private static final String ADMIN="admin";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
             /**
              * 获取调用接口所需的login-token
              * */
        Map<String,Object> res = httpUtils.httpClientPost("http://10.176.122.172:8012/sys/login/restful?username=JT2103258421&password=imbus123","");
        if (res.containsKey("token")){
            Token.token = res.get("token").toString();

        }
        /*
        loanService.getClientInfo("533023199908314312");
        //loanService.getAllLoans();
        //loanService.getLoanPlan("L2104032001451");
        loanService.getLoanPlanByDate("L2103301020571","2021-04-03");


        //格式化

        String time=sim.format(dd);
        //System.out.println(time);


        //loanService.repayment("L2104032001451",52,33.7233,33.7233*0.05);
        loanService.getCredit("ylh1234202103252");*/
        /*LoanService loanService = new LoanService();
        loanService.batchRepaymentLoan("2021-07-04 15:30:30");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = df2.parse("2077-4-4 12:22:22");*/
        StockService stockService = new StockService();
        stockService.getProduct("1");
        LoanService loanService = new LoanService();
        //loanService.getBalanceByCustomerCode("ylh1234202103271");
        //stockService.getProfit("123","2021-04-03");
        //loanService.getCredit("ylh1234202103271");
        loanService.getLoanList("ylh1234202103271");

    }



//    @Bean
//    public CommandLineRunner dataLoader() {
//        return new CommandLineRunner() {
//            @Override
//            public void run(String... args) throws Exception {
//             /**
//              * 获取调用接口所需的login-token
//              * */
//                Map<String,String> parameter = new HashMap<>();
//                parameter.put("username","JT2103258421");
//                parameter.put("password","imbus123");
//                Map<String,Object> res = httpUtils.httpClientPost("http://10.176.122.172:8012/sys/login/restful",httpUtils.gson.toJson(parameter));
//                Token.token = res.get("token").toString();
//                System.out.println(Token.token);
//            }
//        };
//    }


}

