package com.api.calculator.stockprice.controller.user;


import com.api.calculator.stockprice.TestsUtils;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.api.persistence.model.Role;
import com.api.calculator.stockprice.api.persistence.model.User;
import com.api.calculator.stockprice.api.persistence.repository.UserRepository;
import com.api.calculator.stockprice.api.persistence.service.user.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerUserTests {

    @Inject
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private HashMap<String, Object> userAuth;

    @Before
    public void beforeAach() throws ResourceAlreadyExists {
        User userCreated = authService.signup(new User("test","uodateUserTest@gmail.com", "password", Role.USER, null));
        userCreated.setPassword("password");
        userAuth = authService.signin(userCreated);
    }

    @After
    public void afterEach() {
        userRepository.deleteById(UUID.fromString(userAuth.get("userId").toString()));
    }

    @Test
    public void fetchUser() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/users/").contentType(TestsUtils.CONTENT_TYPE)
                        .header("authorization", "Bearer " + userAuth.get("token"))
                )
                .andExpect(status().isOk())
                .andReturn();

        final HashMap<String, Object> userFromResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(), HashMap.class);

        assert ( userFromResponse.get("id") != null);


    }

    @Test
    public void update() throws Exception {

        userAuth.put("name", "Felizardo");
        userAuth.put("password", "password");
        userAuth.put("passwordConfirmation", "password");

        mockMvc.perform(patch("/api/users/")
                        .contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(userAuth))
                        .header("authorization", "Bearer " + userAuth.get("token"))
                )
                .andExpect(status().isOk());
    }

}