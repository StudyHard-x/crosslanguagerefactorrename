package com.mcs.mcsproject.service.impl;

import com.mcs.mcsproject.entity.Users;
import com.mcs.mcsproject.dao.UsersDao;
import com.mcs.mcsproject.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@Service("usersService")
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersDao usersDao;

    @Override
    public List<Users> getAllUsers() {
        return usersDao.findAll();
    }

    @Override
    public Users getUserById(Integer id) {
        return usersDao.findById(id).orElse(null);
    }

    @Override
    public Users createUser(Users users) {
        return usersDao.save(users);
    }

    @Override
    public Users updateUser(Integer id, Users users) {
        return null;
    }


    @Override
    public void deleteUser(Integer id) {
        usersDao.deleteById(id);
    }

    @Override
    public Users updateAge(Integer id, Integer age) {
        return null;
    }


}
