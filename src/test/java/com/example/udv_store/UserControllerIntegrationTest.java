package com.example.udv_store;

import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UdvStoreApplication.class)
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TestService testService;

    @Test
    public void register_Returns_201() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void register_Returns_400_When_EmailPatternIsViolated() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@gmail.ru\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_Returns_400_When_PasswordPatternIsViolated() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"12345Aa\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_Returns_403_When_EmailAlreadyRegistered() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void auth_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void auth_Returns_403_When_UserIsNotEnabled() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void auth_Returns_401_When_EmailIsIncorrect() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"ilo-games@mail.ru\", \"password\" : \"" + testService.password + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void auth_Returns_401_When_PasswordIsIncorrect() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + testService.email + "\", \"password\" : \"123456A\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void info_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isOk());
    }

    // Do I really need this?
    @Test
    public void info_Returns_401_When_JSONWebTokenIsIncorrect() throws Exception {
        testService.register();
        testService.enableUser();
        String jwt = "fake" + testService.getUserJWT().substring(4);
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUser_Returns_200() throws Exception {
        testService.register();
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders
                        .delete("/admin/{userId}", user.getId().toString())
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUser_Returns_304_When_UserIsNotFound() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders
                        .delete("/admin/{userId}", "c4f44950-2b80-4cf0-a060-ad99d19cc636")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isNotModified());
    }

    @Test
    public void changeUserBalance_Returns_200() throws Exception {
        testService.register();
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testService.getAdminJWT())
                        .content("{\"userId\" : \"" + user.getId().toString() + "\", \"userBalance\" : 200}"))
                .andExpect(status().isOk());
    }

    @Test
    public void changeUserBalance_Returns_403_When_UserIsNotFound() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testService.getAdminJWT())
                        .content("{\"userId\" : \"c4f44950-2b80-4cf0-a060-ad99d19cc636\", \"userBalance\" : 200}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAllUsersInfo_Returns_200() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.get("/admin/info")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
    }
}
