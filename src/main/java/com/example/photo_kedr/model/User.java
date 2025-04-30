package com.example.photo_kedr.model;

import lombok.AllArgsConstructor;

//import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
@AllArgsConstructor
public class User /*implements UserDetails*/ {
    private int id;
    private String login;
    private String password;
    private int privileges;
}
