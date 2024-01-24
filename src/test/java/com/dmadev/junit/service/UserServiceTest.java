package com.dmadev.junit.service;

import com.dmadev.junit.dao.UserDao;
import com.dmadev.junit.dto.User;

import com.dmadev.junit.extension.GlobalExtension;
import com.dmadev.junit.extension.PostProcessingExtension;
import com.dmadev.junit.extension.ThrowableExtension;
import com.dmadev.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;


import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.Random.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
//testMethodOrder лучше не юзать, но имхо неплохо юзать рандом.класс)
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
//        GlobalExtension.class
})
class UserServiceTest {

    private static final User IVAN = User.of(1L, "Ivan", "123");
    private static final User TOM = User.of(2L, "Tom", "123");


    @Captor
    private ArgumentCaptor<Long>argumentCaptor;
    @Mock(lenient = true)
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }


    @BeforeAll
    void prepareAll() {
        System.out.println("Before All: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        doReturn(true).when(userDao).delete(IVAN.getId());
//        this.userDao=Mockito.mock(UserDao.class);
//        this.userDao=Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable(){
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());
        assertThrows(RuntimeException.class,()->userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser(){
        userService.add(IVAN);
        doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.anyLong());
//        Mockito.when(userDao.delete(IVAN.getId())).thenReturn(true).thenReturn(false);
        boolean deleteResult = userService.delete(IVAN.getId());
        System.out.println(deleteResult);
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

        verify(userDao, times(3)).delete(argumentCaptor.capture());
//        Mockito.verifyNoInteractions();
        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());
//        Mockito.reset(userDao); not recommended
        assertThat(deleteResult).isTrue();
    }

    @Test
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded() throws IOException {

        if(true){
            throw new IOException();
        }
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
    class LoginTest {


        @Test
        @Disabled("flaky, need to see")
        void logicFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "incorrect");
            assertTrue(maybeUser.isEmpty());
        }

        //        @Test
        //Repeated - для уменьшение флаки тестов,  если тест допустим падает на 4-5 итерации
        @RepeatedTest(value = 3, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo) {
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

//        @Test
//        void checkLoginFunctionalityPerformance() {
//            Optional<User> maybeUser = assertTimeoutPreemptively(
//                    Duration.ofMillis(200L), () -> {
//                        Thread.sleep(300L);
//                        return userService.login("dummy",IVAN.getPassword());
//                    }
//            );
//        }

        @Test
//        @Timeout(value = 200,unit = TimeUnit.MILLISECONDS)
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            //assertTrue(maybeUser.isPresent());
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        maybeUser.ifPresent(user-> assertEquals(IVAN,user));

        }


        @ParameterizedTest(name = "{arguments} test")
//    @ArgumentsSource()
//    @NullSource
//    @EmptySource
//    @NullAndEmptySource
//    @ValueSource(strings = {
//            "Ivan","Tom"
//    })

        @MethodSource("com.dmadev.junit.service.UserServiceTest#getArgumentsForLoginTest")
//    @CsvFileSource(resources = "/login-test-data.csv",delimiter = ',',numLinesToSkip = 1)
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, TOM);

            Optional<User> maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Tom", "123", Optional.of(TOM)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "dimmy", Optional.empty())
        );
    }

}
