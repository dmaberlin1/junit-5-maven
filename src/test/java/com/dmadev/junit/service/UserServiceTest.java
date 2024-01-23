package com.dmadev.junit.service;

import com.dmadev.junit.dto.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;


import java.util.List;
import java.util.Map;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;



@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.Random.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
//testMethodOrder лучше не юзать, но имхо неплохо юзать рандом.класс)
public class UserServiceTest {


    private UserService userService;

     UserServiceTest(TestInfo testInfo) {
         System.out.println();
    }

    private final User IVAN = User.of(1L, "Ivan", "123");
    private final User TOM = User.of(2L, "Tom", "123");


    @BeforeAll
    void prepareAll() {
        System.out.println("Before All: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }


    @Test
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1 :" + this);

        var users = userService.getAll();
        assertTrue(users.isEmpty());
        assertFalse(!users.isEmpty(), () -> "User list should be empty");
        //input -> [box ==func] ->actual output
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2 :" + this);


        userService.add(IVAN);
        userService.add(TOM);

        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
        assertEquals(2, users.size());
    }





    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, TOM);
        Map<Long, User> users = userService.getAllConvertedById();
        assertAll(() -> {
            assertThat(users).containsKeys(IVAN.getId(), TOM.getId());
            assertThat(users).containsValues(IVAN, TOM);
        });


    }



    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After Each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all , example close pool");
    }


    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    class LoginTest{
        @Test
        void logicFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "incorrect");
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login("dummy user", IVAN.getPassword());
            assertTrue(maybeUser.isEmpty());
        }
        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
//        try{
//            userService.login(null,"dummy password");
//            fail("login should throw exception on null username");
//        }catch (IllegalArgumentException ex){
//            assertTrue(true);
//        }
            //замена кода выше
            assertAll(() -> {
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> userService.login(null, "dummy password"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("demo", null))
            );
        }
        @Test
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            //assertTrue(maybeUser.isPresent());
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        maybeUser.ifPresent(user-> assertEquals(IVAN,user));

        }
    }


}
