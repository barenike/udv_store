package com.example.udv_store;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.OrderService;
import com.example.udv_store.model.service.UserService;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UdvStoreApplication.class)
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestService testService;

    @Autowired
    private JwtProvider jwtProvider;

    @AfterEach
    void cleanup() {
        testService.deleteProduct();
    }

    private void setUserBalance(Integer balance) throws Exception {
        UserEntity user = userService.findByEmail("lilo-games@mail.ru");
        mvc.perform(MockMvcRequestBuilders.post("/admin/user_balance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + testService.getAdminJWT())
                .content("{\"userId\" : \"" + user.getId().toString() + "\", \"userBalance\" : " + balance + "}"));
    }

    private void createOrder() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/user/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderCreationDetails\" : [{\"productId\" : \"" + testService.getProductId() + "\",\"quantity\" : 2}]}")
                .header("Authorization", "Bearer " + testService.getUserJWT()));
    }

    private String getOrderId() throws Exception {
        List<OrderEntity> orders = orderService.findOrdersByUserId(UUID.fromString(jwtProvider.getUserIdFromToken(testService.getUserJWT())));
        return orders.get(0).getId().toString();
    }

    @Test
    public void createOrder_Returns_201() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        mvc.perform(MockMvcRequestBuilders.post("/user/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderCreationDetails\" : [{\"productId\" : \"" + testService.getProductId() + "\",\"quantity\" : 2}]}")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isCreated());
        testService.deleteProduct();
    }

    @Test
    public void createOrder_Returns_403_When_NotEnoughCoins() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(10);
        testService.createProduct();
        mvc.perform(MockMvcRequestBuilders.post("/user/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderCreationDetails\" : [{\"productId\" : \"" + testService.getProductId() + "\",\"quantity\" : 2}]}")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isForbidden());
        testService.deleteProduct();
    }

    @Test
    public void createOrder_Returns_403_When_ProductIsNotFound() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        mvc.perform(MockMvcRequestBuilders.post("/user/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderCreationDetails\" : [{\"productId\" : \"a04096a1-014c-40a8-8471-46dbf85113b4\",\"quantity\" : 2}]}")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isForbidden());
        testService.deleteProduct();
    }

    @Test
    public void getMyOrders_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.get("/user/orders")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }

    @Test
    public void deleteMyOrder_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.delete("/user/orders/{orderId}", getOrderId())
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }

    @Test
    public void deleteMyOrder_Returns_304_When_OrderDoesNotExist() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.delete("/user/orders/{orderId}", "fb96924c-f4a2-4576-8b8b-42b903d9a822")
                        .header("Authorization", "Bearer " + testService.getUserJWT()))
                .andExpect(status().isNotModified());
        testService.deleteProduct();
    }

    @Test
    public void manipulateOrders_GetAllOrders_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.get("/admin/orders")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }

    @Test
    public void manipulateOrders_GetOrdersByUserId_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.get("/admin/orders")
                        .param("userId", jwtProvider.getUserIdFromToken(testService.getUserJWT()))
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }

    @Test
    public void manipulateOrders_ChangeStatus_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.get("/admin/orders")
                        .param("orderId", getOrderId())
                        .param("status", "SHIPPED")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }

    @Test
    public void manipulateOrders_ChangeStatus_Returns_304_When_OrderDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/admin/orders")
                        .param("orderId", "fb96924c-f4a2-4576-8b8b-42b903d9a822")
                        .param("status", "SHIPPED")
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isNotModified());
    }

    @Test
    public void manipulateOrders_Delete_Returns_200() throws Exception {
        testService.register();
        testService.enableUser();
        setUserBalance(20);
        testService.createProduct();
        createOrder();
        mvc.perform(MockMvcRequestBuilders.get("/admin/orders")
                        .param("orderId", getOrderId())
                        .header("Authorization", "Bearer " + testService.getAdminJWT()))
                .andExpect(status().isOk());
        testService.deleteProduct();
    }
}
