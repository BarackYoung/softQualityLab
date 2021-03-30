package fudan.sq.controller;


import fudan.sq.service.LoanService;
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
    public Controller(LoanService loanService) {
     this.loanService = loanService;
    }

    /**
     * 查询客户信息
     * */
    @GetMapping("/getClientInfo/{id}")
     public ResponseEntity<?> getClientInfo(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(loanService.getClientInfo(id));
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


}
