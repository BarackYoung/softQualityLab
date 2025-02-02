package fudan.sq.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * You can handle all of your exceptions here.
 *
 * @author LBW
 */
@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
    private static final String MESSAGE="message";
    @ExceptionHandler(UsernameNotFoundException.class)
    ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameHasBeenRegisteredException.class)
    ResponseEntity<Map<String, String>> handlerUsernameHasBeenRegisteredException(UsernameHasBeenRegisteredException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

}
