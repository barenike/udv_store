package com.example.udv_store;

import com.example.udv_store.infrastructure.product.ProductResponse;
import com.example.udv_store.model.service.DropboxService;
import com.example.udv_store.model.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UdvStoreApplication.class)
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private DropboxService dropboxService;

    private void createProduct() throws Exception {
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

    private String getAdminJWT() throws Exception {
        String jwt = mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\" : \"admin@ussc.ru\", \"password\" : \"123456Aa\"}"))
                .andReturn().getResponse().getContentAsString();
        jwt = jwt.substring(10, jwt.length() - 2);
        return jwt;
    }

    private String getProductId() {
        List<ProductResponse> productResponseList = productService.getAllProducts();
        String productId;
        productId = productResponseList.stream().filter(productResponse -> productResponse.getName().equals("Juuur")).findFirst().map(ProductResponse::getId).orElse(null);
        return productId;
    }

    private void deleteProduct() {
        dropboxService.delete("/Juuur.jpg");
    }

    @Test
    public void createProductSuccess() throws Exception {
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
                            .header("Authorization", "Bearer " + getAdminJWT()))
                    .andExpect(status().isCreated());
            deleteProduct();
        }
    }

    @Test
    public void changeProductAmountSuccess() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.post("/admin/product_amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\" : \"" + getProductId() + "\", \"amount\" : 500}")
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isOk());
        deleteProduct();
    }

    @Test
    public void changeProductAmountFailureProductDoesNotExist() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.post("/admin/product_amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\" : \"a04096a1-014c-40a8-8471-46dbf85113b4\", \"amount\" : 500}")
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isForbidden());
        deleteProduct();

    }

    @Test
    public void getProductsSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/products")
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isOk());
    }

    @Test
    public void getProductSuccess() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.get("/products/{productId}", getProductId())
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isOk());
        deleteProduct();

    }

    @Test
    public void getProductFailureProductDoesNotExist() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.get("/products/{productId}", "a04096a1-014c-40a8-8471-46dbf85113b4")
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isNotFound());
        deleteProduct();

    }

    @Test
    public void deleteProductSuccess() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.delete("/admin/product/{productId}", getProductId())
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isOk());

    }

    @Test
    public void deleteProductFailureProductDoesNotExist() throws Exception {
        createProduct();
        mvc.perform(MockMvcRequestBuilders.delete("/admin/product/{productId}", "a04096a1-014c-40a8-8471-46dbf85113b4")
                        .header("Authorization", "Bearer " + getAdminJWT()))
                .andExpect(status().isNotModified());
        deleteProduct();

    }
}
