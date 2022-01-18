package com.hazq.data.service.starter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Controller
public class TargetController {
    @GetMapping("/listHeaders")
    public ResponseEntity<Map> listAllHeaders(@RequestHeader Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        headers.forEach((key, value) -> {
            sb.append(String.format("Header '%s' = %s " + "", key, value) + "\n");
        });
        //log.info(sb.toString());
        return new ResponseEntity<Map>(headers, HttpStatus.OK);
    }
}
