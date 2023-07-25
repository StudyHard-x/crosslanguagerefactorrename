package com.mcs.mcsproject.controller;

import com.mcs.mcsproject.entity.Users;
import com.mcs.mcsproject.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (Users)表控制层
 *
 * @author makejava
 * @since 2023-06-18 19:01:43
 */
//@RestController 用于前后端分离
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;


    @GetMapping("/wel")
    public Map<String, Object> test(){
        Map<String,Object> map = new HashMap<>();
        map.put("id",1);
        map.put("he",2);
        return map;
    }

    @GetMapping("/test")
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

