package com.example.photo_kedr.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
	private int id;
	private String login;
	private String password;
	private int privileges;
}
