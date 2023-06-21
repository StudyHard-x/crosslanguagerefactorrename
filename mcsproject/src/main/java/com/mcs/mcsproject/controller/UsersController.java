package com.mcs.mcsproject.controller;

import com.mcs.mcsproject.entity.Users;
import com.mcs.mcsproject.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
//import javax.annotation.Resource;
import java.util.List;

/**
 * (Users)表控制层
 *
 * @author makejava
 * @since 2023-06-18 19:01:43
 */
//@RestController 用于前后端分离
@RestController
@RequestMapping("users")
public class UsersController {
    @Autowired
    private UsersService usersService;

    @GetMapping("/wel")
    public String wec(@RequestParam("name") String name, Model model){
        model.addAttribute("msg",name);
        return "Home";
    }

    @GetMapping("/api/users/{userId}")
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

//    /**
//     * 分页查询
//     *
//     * @param users 筛选条件
//     * @param pageRequest      分页对象
//     * @return 查询结果
//     */
//    @GetMapping
//    public ResponseEntity<Page<Users>> queryByPage(Users users, PageRequest pageRequest) {
//        return ResponseEntity.ok(this.usersService.queryByPage(users, pageRequest));
//    }
//
//    /**
//     * 通过主键查询单条数据
//     *
//     * @param id 主键
//     * @return 单条数据
//     */
//    @GetMapping("{id}")
//    public ResponseEntity<Users> queryById(@PathVariable("id") Integer id) {
//        return ResponseEntity.ok(this.usersService.queryById(id));
//    }
//
//    /**
//     * 新增数据
//     *
//     * @param users 实体
//     * @return 新增结果
//     */
//    @PostMapping
//    public ResponseEntity<Users> add(Users users) {
//        return ResponseEntity.ok(this.usersService.insert(users));
//    }
//
//    /**
//     * 编辑数据
//     *
//     * @param users 实体
//     * @return 编辑结果
//     */
//    @PutMapping
//    public ResponseEntity<Users> edit(Users users) {
//        return ResponseEntity.ok(this.usersService.update(users));
//    }
//
//    /**
//     * 删除数据
//     *
//     * @param id 主键
//     * @return 删除是否成功
//     */
////    @DeleteMapping
////    public ResponseEntity<Boolean> deleteById(Integer id) {
////        return ResponseEntity.ok(this.usersService.deleteById(id));
////    }

}

