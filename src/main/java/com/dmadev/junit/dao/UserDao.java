package com.dmadev.junit.dao;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UserDao {

    @SneakyThrows
    public boolean delete(Long userId){
        try {
            Connection connection = DriverManager.getConnection(
                    "url",
                    "username",
                    "password");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
