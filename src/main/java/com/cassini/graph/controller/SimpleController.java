package com.cassini.graph.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simple")
public class SimpleController {

    @GetMapping
    public String test() {
        return "Simple Controller Test Successful";
    }
}