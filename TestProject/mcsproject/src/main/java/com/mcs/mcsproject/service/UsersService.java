package com.mcs.mcsproject.service;

import com.mcs.mcsproject.entity.Users;

import java.util.List;

/**
 * (Users)表服务接口
 *
 * @author makejava
 * @since 2023-06-18 19:01:47
 */
public interface UsersService {
    List<Users> getAllUsers();
    Users getUserById(Integer id);
    Users createUser(Users users);
    Users updateUser(Integer id, Users users);
    void deleteUser(Integer id);
    Users updateAge(Integer id, Integer age);

}
