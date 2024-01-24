package com.dmadev.junit.dto;

import com.dmadev.junit.dao.UserDao;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoMock extends UserDao {

    private Map<Integer, Boolean> answers = new HashMap<>();
//    private Answer1<Long,Boolean> answer1;

    @Override
    public boolean delete(Long userId) {
        return answers.getOrDefault(userId, false);
    }
}
