package fudan.sq.security.jwt;

import fudan.sq.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Write your code to make this filter works.
 *
 * @author LBW
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    Logger logger1=LoggerFactory.getLogger(JwtRequestFilter.class);
    org.slf4j.Marker marker;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader("Authorization");
        String tokenHead="Bearer ";
        logger1.debug(marker,"进入jwt过滤器 {}",authHeader);
        try{
            if (authHeader!=null&&authHeader.startsWith(tokenHead)&& !"Bearer null".equals(authHeader)){
                String authToken=authHeader.substring(tokenHead.length());
                logger1.debug(marker,"得到authToken： {} ",authToken);
                String username=this.jwtTokenUtil.getUsernameFromToken(authToken);

                logger1.debug(marker,"已从token中获得当前用户名 {}",username);
                if (username!=null&&SecurityContextHolder.getContext().getAuthentication()==null){
                    UserDetails userDetails=this.jwtUserDetailsService.loadUserByUsername(username);
                    logger1.debug(marker,"成功获取userDetail {} ",userDetails.getUsername());
                    if(jwtTokenUtil.validateToken(authToken,userDetails)){
                        logger1.debug("开始进行用户名密码认证");
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                                new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        logger1.debug("重新设置当前用户");
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
            logger1.debug("传递过滤器执行权");
            filterChain.doFilter(request, response);

        }catch (ExpiredJwtException e){

                logger1.debug("token过期，重定向至登录界面");
                response.addHeader("Access-Control-Allow-origin","*");
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("sorry,your token is expired, please login");
                response.sendRedirect("/login.html");
                logger1.debug("token 已过期");


        }

    }
}
