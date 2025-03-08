package com.thg.zohodesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Forward all routes to index.html for React router to handle
    @GetMapping(value = {"/", "/login", "/dashboard", "/tickets/**", "/profile"})
    public String forward() {
        return "forward:/index.html";
    }
}