package com.dmadev.junit.dto;

import com.dmadev.junit.dao.UserDao;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {

    private final UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();

    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }
//    private Answer1<Long,Boolean> answer1;

    @Override
    public boolean delete(Long userId) {
        //invocation ++;
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
