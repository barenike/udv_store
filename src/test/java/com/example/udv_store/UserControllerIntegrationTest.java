package com.example.udv_store;

import com.example.udv_store.model.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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

    @Test
    public void registerSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void registerFailureEmailPatternIsViolated() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@gmail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerFailurePasswordPatternIsViolated() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"12345Aa\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerFailureEmailAlreadyRegistered() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        userService.enableUser(userService.findByEmail("lilo-games@mail.ru"));
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void authFailureUserIsNotEnabled() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authFailureIncorrectEmail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        userService.enableUser(userService.findByEmail("lilo-games@mail.ru"));
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"ilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void authFailureIncorrectPassword() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        userService.enableUser(userService.findByEmail("lilo-games@mail.ru"));
        mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456A\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void infoSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        userService.enableUser(userService.findByEmail("lilo-games@mail.ru"));
        String jwt = mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andReturn().getResponse().getContentAsString();
        jwt = jwt.substring(10, jwt.length() - 2);
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

    @Test
    public void infoFailureJSONWebTokenIsIncorrect() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"));
        userService.enableUser(userService.findByEmail("lilo-games@mail.ru"));
        String jwt = mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"lilo-games@mail.ru\", \"password\" : \"123456Aa\"}"))
                .andReturn().getResponse().getContentAsString();
        jwt = jwt.substring(10, jwt.length() - 2);
        jwt = "fake" + jwt.substring(4);
        mvc.perform(MockMvcRequestBuilders.get("/info")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
    }
}
