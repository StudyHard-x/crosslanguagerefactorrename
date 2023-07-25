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


//    /**
//     * 通过ID查询单条数据
//     *
//     * @param id 主键
//     * @return 实例对象
//     */
//    @Override
//    public Users queryById(Integer id) {
//        return this.usersDao.queryById(id);
//    }
//
//
//    /**
//     * 分页查询
//     *
//     * @param users 筛选条件
//     * @param pageRequest      分页对象
//     * @return 查询结果
//     */
//    @Override
//    public Page<Users> queryByPage(Users users, PageRequest pageRequest) {
//        long total = this.usersDao.count(users);
//        return new PageImpl<>(this.usersDao.queryAllByLimit(users, pageRequest), pageRequest, total);
//    }
//
//    /**
//     * 新增数据
//     *
//     * @param users 实例对象
//     * @return 实例对象
//     */
//    @Override
//    public Users insert(Users users) {
//        this.usersDao.insert(users);
//        return users;
//    }
//
//    /**
//     * 修改数据
//     *
//     * @param users 实例对象
//     * @return 实例对象
//     */
//    @Override
//    public Users update(Users users) {
//        this.usersDao.update(users);
//        return this.queryById(users.getId());
//    }
//
//    /**
//     * 通过主键删除数据
//     *
//     * @param id 主键
//     * @return 是否成功
//     */
////    @Override
////    public boolean deleteById(Integer id) {
////
////    }
}
