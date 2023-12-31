package com.api.calculator.stockprice.controller.user;

import com.api.calculator.stockprice.TestsUtils;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.ws.data.model.Role;
import com.api.calculator.stockprice.ws.data.model.User;
import com.api.calculator.stockprice.ws.data.repository.UserRepository;
import com.api.calculator.stockprice.ws.data.service.user.AuthService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SigninTests {

    @Inject
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private User dreamSigninUser;

    @Before
    public void beforeAach() {
        try {
            dreamSigninUser = authService.signup(new User("Dream User","dreanSigninUser@gmail.com", "password", Role.USER, null));
        }catch (ResourceAlreadyExists ignored){
            dreamSigninUser = userRepository.findByEmail("dreanSigninUser@gmail.com").get(0);
        }

        dreamSigninUser.setPassword("password");
    }

    @After
    public void afterEach(){
        userRepository.deleteById(dreamSigninUser.getId());
    }

    @Test
    public void dreamSignin() throws Exception {
        HashMap<String, Object> userObject = new HashMap<>();
        userObject.put("email", dreamSigninUser.getEmail());
        userObject.put("password", dreamSigninUser.getPassword());

        String bodyContent = TestsUtils.objectToJson(userObject);

        final MvcResult result = mockMvc.perform(post("/api/users/signin").contentType(TestsUtils.CONTENT_TYPE)
                        .content(bodyContent))
                .andExpect(status().isOk())
                .andReturn();

        final HashMap<String, Object> responseBody = new ObjectMapper().readValue(result.getResponse().getContentAsString(), HashMap.class);
        assert(!responseBody.get("token").equals(""));
    }

    @Test
    public void signinWhenWrongPass() throws Exception {
        HashMap<String, Object> userObject = new HashMap<>();
        userObject.put("name", dreamSigninUser.getName());
        userObject.put("email", dreamSigninUser.getEmail());
        userObject.put("password", dreamSigninUser.getPassword()+"ss");

        String bodyContent = TestsUtils.objectToJson(userObject);

        mockMvc.perform(post("/api/users/signin").contentType(TestsUtils.CONTENT_TYPE)
                        .content(bodyContent))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    public void signinWithoutEmail() throws Exception {
        HashMap<String, Object> userObject = new HashMap<>();
        userObject.put("password", dreamSigninUser.getPassword()+"ss");

        String bodyContent = TestsUtils.objectToJson(userObject);

        mockMvc.perform(post("/api/users/signin").contentType(TestsUtils.CONTENT_TYPE)
                        .content(bodyContent))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

}
