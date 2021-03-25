package fudan.sq.service;

import fudan.sq.entity.User;
import fudan.sq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;




@Service
public class JwtUserDetailsService implements UserDetailsService {
    Logger logger= LoggerFactory.getLogger(JwtUserDetailsService.class);
    org.slf4j.Marker marker;

    @Autowired
    private UserRepository userRepository;


    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)  {
        User user=userRepository.findByUsername(username);
        if (user==null){
            throw new UsernameNotFoundException("User: '" + username + "' not found.");
        }
        else{
            if (logger.isDebugEnabled()){
                logger.debug(marker,"loadByUsername被调用 user信息 {}",user.getUsername());
            }
            //返回一个新的user对象
            return user;
        }

    }
}
