package fudan.sq.config;


import fudan.sq.entity.User;
import fudan.sq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MeetingInterceptor implements HandlerInterceptor
{
    @Autowired
    private UserRepository userRepository;
    Logger log= LoggerFactory.getLogger(MeetingInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.debug("开始拦截审核请求");
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByUsername(username);
        String admin="admin";

        log.debug("对不起，您没有权限访问该端口");
        //重置response
        response.reset();
        //设置编码格式
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.sendError(403,"对不起，您没有管理员权限");
        return false;
    }
 
}
