package com.mcs.mcsproject.controller;

import com.mcs.mcsproject.entity.Users;
import com.mcs.mcsproject.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;


    @GetMapping("/newTest1")
    public Map<String, Object> test1(){
        Map<String,Object> map = new HashMap<>();
        map.put("test112",1);
        map.put("test2",2);
        return map;
    }

    @GetMapping("/TestNew2")
    public Map<String, Object> test2(){
        Map<String,Object> map = new HashMap<>();
        map.put("newTest123",1);
        map.put("test2",2);
        return map;
    }

    @GetMapping("/newTest03")
    public Map<String, Object> test3(){
        Map<String,Object> map = new HashMap<>();
        map.put("test1",1);
        map.put("test2",2);
        return map;
    }


    @GetMapping("/test01")
    public String wecHome(@RequestParam("name") String name, Model model,@PathVariable Integer id){
        model.addAttribute("msg",name);
        Map<String,Object> map = new HashMap<>();
        map.put("id",1);
        map.put("name",2);
        return "Home";
    }

    @GetMapping("/getUsers")
    public List<Users> getAllUsers() {
        return usersService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Users getUserById(@PathVariable Integer id) {

        return usersService.getUserById(id);
    }

    @PostMapping
    public Users createUser(@RequestBody Users users) {
        return usersService.createUser(users);
    }

    @PutMapping("/{id}")
    public Users updateUser(@PathVariable Integer id, @RequestBody Users users) {
        return usersService.updateUser(id, users);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        usersService.deleteUser(id);
    }

    @PostMapping("/{updateAge}/{id}")
    public Users updateAge(@PathVariable Integer id, Integer age){
        return usersService.updateAge(id, age);
    }

}

