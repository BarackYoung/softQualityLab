package fudan.sq.service;

import fudan.sq.entity.User;
import fudan.sq.repository.UserRepository;
import fudan.sq.security.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    Logger logger= LoggerFactory.getLogger(AuthService.class);
    org.slf4j.Marker marker;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;


    public String login(String username, String password) {
        if (logger.isDebugEnabled()){
            logger.debug("*开始登录");
        }
        User user=userRepository.findByUsername(username);
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();

        if (user==null || !encoder.matches(password,user.getPassword())){
            logger.debug("*登录login failed，该用户不存在或密码错误");
            return "fail:username or password is wrong";
        }
        logger.debug(marker,"查找出的对应的用户密码 {} ,输入的密码 {}",user.getPassword(),password);
        //否则返回token
        return jwtTokenUtil.generateToken(user);
    }



}
