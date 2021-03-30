package fudan.sq;

import fudan.sq.httpUtils.httpUtils;


import fudan.sq.service.JwtUserDetailsService;
import fudan.sq.service.LoanService;
import fudan.sq.service.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


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
        LoanService loanService = new LoanService();
        loanService.getClientInfo("533023199908314312");
        loanService.batchRepaymentLoan();
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

