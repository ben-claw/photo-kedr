package com.example.photo_kedr.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
	@GetMapping("/login")
	public String loginPage(
		@RequestParam(name = "error", required = false) String error,
		@RequestParam(name = "registered", required = false) String registered,
		Model model
	) {
		model.addAttribute("loginError", error != null);
		model.addAttribute("registered", registered != null);
		return "login";
	}
}
