package com.cassini.graph.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PublicController {

    @GetMapping("/public/hello")
    @ResponseBody
    public String hello() {
        return "Hello, World!";
    }
}