package com.api.calculator.stockprice.security;

import com.api.calculator.stockprice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthProvider implements AuthenticationProvider {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User userPrincipal = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateJwtToken(userPrincipal);

        return new UsernamePasswordAuthenticationToken(userPrincipal.getUsername(), token, userPrincipal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}