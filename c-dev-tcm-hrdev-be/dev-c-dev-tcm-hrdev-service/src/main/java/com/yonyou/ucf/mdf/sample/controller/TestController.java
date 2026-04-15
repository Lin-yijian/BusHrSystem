package com.yonyou.ucf.mdf.sample.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {



    @GetMapping("/success")
    public String test() {
        return "success";
    }
}
