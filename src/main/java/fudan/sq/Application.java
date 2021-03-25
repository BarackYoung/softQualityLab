package fudan.sq;

import fudan.sq.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import fudan.se.lab2.controller.AuthController;
import fudan.se.lab2.domain.Authority;
import fudan.se.lab2.domain.User;
import fudan.se.lab2.repository.AuthorityRepository;
import fudan.se.lab2.repository.UserRepository;

import fudan.se.lab2.service.MyTest;
import fudan.se.lab2.service.Token;
import fudan.se.lab2.service.loanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;


@SpringBootApplication
public class Application {

    @Autowired
    JwtUserDetailsService jwtUserDetailsService;
    private static final String ADMIN="admin";

    public static void main(String[] args) {
        //SpringApplication.run(Application.class, args);
        MyTest myTest = new MyTest();
        loanService loanService = new loanService();
        String s = loanService.postCardAuthenticationJson("http://10.176.122.172:8012/sys/login/restful","username=BA2103154881&password=imbus123");
        System.out.println(s);
        Token tokenObject=JSON.parseObject(JSON.parse(s).toString(),Token.class);
        String token = tokenObject.getToken();
        loanService.insertLoan("http://10.176.122.172:8012/loan",token,"1",500,"1",1,"100","2021/3/26 00:00:00","2021/3/25 00:00:00");

        System.out.println(token);
        String param = "pageNum=10&pageSize=10&params=%7B%22loanStatus%22:1%7D&"+"login-token="+tokenObject.getToken();


        System.out.println(loanService.sendGet("http://10.176.122.172:8012/loan",param));


        //SpringApplication.run(Lab2Application.class, args);
    }



}

