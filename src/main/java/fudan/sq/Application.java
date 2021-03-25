package fudan.sq;

import fudan.sq.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {

    @Autowired
    JwtUserDetailsService jwtUserDetailsService;
    private static final String ADMIN="admin";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }



}

