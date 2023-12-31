package com.api.calculator.stockprice.ws.data.service.user;

import com.api.calculator.stockprice.ws.data.repository.UserRepository;
import com.api.calculator.stockprice.ws.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthDetailsHundlerService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = userRepository.findByEmail(username);
        if (!users.isEmpty()){
            return users.get(0);
        }else{
            return null;
        }
    }
}
