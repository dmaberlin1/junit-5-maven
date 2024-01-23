package com.dmadev.junit.dto;

import lombok.Value;

@Value(staticConstructor = "of")
public class User {
    Long id;
    String username;
    String password;
}
