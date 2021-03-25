package fudan.sq.controller;


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
    public Controller() {

    }

    @GetMapping("/welcome")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("Hello QingQing");
    }

}
