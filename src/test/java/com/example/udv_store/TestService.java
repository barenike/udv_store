package com.example.udv_store;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.product.ProductResponse;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.DropboxService;
import com.example.udv_store.model.service.OrderService;
import com.example.udv_store.model.service.ProductService;
import com.example.udv_store.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class TestService {
    // In production, change to @ussc.ru
    final String email = "lilo-games@mail.ru";
    final String password = "123456Aa";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private DropboxService dropboxService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtProvider jwtProvider;

    void register() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\" : \"" + email + "\", \"password\" : \"" + password + "\"}"));
    }

    void enableUser() {
        userService.enableUser(userService.findByEmail(email));
    }

    void setUserBalance(Integer balance) throws Exception {
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getAdminJWT())
                .content("{\"userId\" : \"" + user.getId().toString() + "\", \"userBalance\" : " + balance + "}"));
    }

    String getUserJWT() throws Exception {
        String jwt = mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"" + email + "\", \"password\" : \"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        jwt = jwt.substring(10, jwt.length() - 2);
        return jwt;
    }

    String getAdminJWT() throws Exception {
        String jwt = mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"admin@ussc.ru\", \"password\" : \"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        jwt = jwt.substring(10, jwt.length() - 2);
        return jwt;
    }

    void createProduct() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Juuur.jpg")) {
            MockMultipartFile file = new MockMultipartFile("file", "Juuur.jpg", "application/json", inputStream);
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.add("name", "Juuur");
            parameters.add("price", "10");
            parameters.add("description", "Great juuur");
            parameters.add("amount", "100");
            mvc.perform(MockMvcRequestBuilders.multipart("/admin/product")
                    .file(file)
                    .params(parameters)
                    .header("Authorization", "Bearer " + getAdminJWT()));
        }
    }

    String getProductId() {
        List<ProductResponse> productResponseList = productService.getAllProducts();
        String productId;
        productId = productResponseList.stream().filter(productResponse -> productResponse.getName().equals("Juuur")).findFirst().map(ProductResponse::getId).orElse(null);
        return productId;
    }

    void deleteProduct() {
        dropboxService.delete("/Juuur.jpg");
    }

    void createOrder() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/user/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderCreationDetails\" : [{\"productId\" : \"" + getProductId() + "\",\"quantity\" : 2}]}")
                .header("Authorization", "Bearer " + getUserJWT()));
    }

    String getOrderId() throws Exception {
        List<OrderEntity> orders = orderService.findOrdersByUserId(UUID.fromString(jwtProvider.getUserIdFromToken(getUserJWT())));
        return orders.get(0).getId().toString();
    }
}
