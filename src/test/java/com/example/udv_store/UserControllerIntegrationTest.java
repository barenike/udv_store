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
    public void register_201() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void registerEmailPatternIsViolated_400() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@gmail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerPasswordPatternIsViolated_400() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"12345Aa\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerEmailAlreadyRegistered_403() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void auth_200() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void authUserIsNotEnabled_403() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authIncorrectEmail_401() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"ilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void authIncorrectPassword_401() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456A\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void info_200() throws Exception {
        testService.register();
        testService.enableUser();
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isOk());
    }

    @Test
    public void infoJSONWebTokenIsIncorrect_401() throws Exception {
        testService.register();
        testService.enableUser();
        String jwt = "fake" + testService.getUserJWT().substring(4);
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUser_200() throws Exception {
        testService.register();
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders
                        .delete("/admin/{userId}", user.getId().toString())
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserUserIsNotFound_304() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders
                        .delete("/admin/{userId}", "c4f44950-2b80-4cf0-a060-ad99d19cc636")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isNotModified());
    }

    @Test
    public void changeUserBalance_200() throws Exception {
        testService.register();
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testService.getAdminJWT())
                        .content("{\"userId\" : \"" + user.getId().toString() + "\", \"userBalance\" : 200}"))
                .andExpect(status().isOk());
    }

    @Test
    public void changeUserBalanceUserIsNotFound_403() throws Exception {
        testService.register();
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testService.getAdminJWT())
                        .content("{\"userId\" : \"c4f44950-2b80-4cf0-a060-ad99d19cc636\", \"userBalance\" : 200}"))
                .andExpect(status().isForbidden());
    }
}
