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



//    /**
//     * 通过ID查询单条数据
//     *
//     * @param id 主键
//     * @return 实例对象
//     */
//    Users queryById(Integer id);
//
//    /**
//     * 分页查询
//     *
//     * @param users 筛选条件
//     * @param pageRequest      分页对象
//     * @return 查询结果
//     */
//    Page<Users> queryByPage(Users users, PageRequest pageRequest);
//
//    /**
//     * 新增数据
//     *
//     * @param users 实例对象
//     * @return 实例对象
//     */
//    Users insert(Users users);
//
//    /**
//     * 修改数据
//     *
//     * @param users 实例对象
//     * @return 实例对象
//     */
//    Users update(Users users);
//
//    /**
//     * 通过主键删除数据
//     *
//     * @param id 主键
//     * @return 是否成功
//     */
////    boolean deleteById(Integer id);

}
