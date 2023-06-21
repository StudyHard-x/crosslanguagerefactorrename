package com.mcs.mcsproject.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
    @GetMapping("/hello")
    public String Hello(@RequestParam("name") String name, Model model){

        model.addAttribute("msg",name);

        return "haha";
    }
}
