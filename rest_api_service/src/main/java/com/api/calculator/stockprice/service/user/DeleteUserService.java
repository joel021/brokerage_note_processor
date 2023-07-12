package com.api.calculator.stockprice.service.user;

import com.api.calculator.stockprice.model.User;
import com.api.calculator.stockprice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    public void deleteUserByEmail(String email){
        User userFound = userService.findByEmail(email);
        if(userFound != null){
            userRepository.deleteById(userFound.getUserId());
        }
    }


}
