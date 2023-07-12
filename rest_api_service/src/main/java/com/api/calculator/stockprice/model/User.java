package com.api.calculator.stockprice.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "userId")
@Entity(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="userId", updatable=false, unique=true, nullable=false)
    private UUID userId;

    private String googleUserId;

    @Nonnull
    @NotBlank(message = "Forneça seu e-mail.")
    private String email;

    @Nonnull
    @NotBlank(message = "Crie uma senha forte.")
    private String password;

    @Nonnull
    @NotBlank(message = "Está faltando seu nome ou apelido.")
    private String name;

    private String role;

    private String verificationCode;

    public User(){

    }

    public User(String name, String email, String password, String role, String verificationCode){
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verificationCode = verificationCode;
    }

    public User(UUID userId, String name, String email, String password, String role, String verificationCode){
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verificationCode = verificationCode;
    }

    @Override
    public boolean equals(Object object){
        if (object == null){
            return false;
        }
        User user = (User) object;
        return Objects.equals(this.email, user.email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(UUID userId){
        this.userId = userId;
    }

    public UUID getUserId(){
        return userId;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Role.getGrandedAuthorities(this.role);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public void setGoogleUserId(String googleUserId) {
        this.googleUserId = googleUserId;
    }
}