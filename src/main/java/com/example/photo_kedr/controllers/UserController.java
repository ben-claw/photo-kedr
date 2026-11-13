package com.example.photo_kedr.controllers;

import com.example.photo_kedr.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/register")
	public String registrationForm(Model model) {
		model.addAttribute("registrationForm", new RegistrationForm());
		return "register";
	}

	@PostMapping("/register")
	public String register(
		@ModelAttribute RegistrationForm registrationForm, Model model
	) {
		if (registrationForm.getPassword() == null || !registrationForm
			.getPassword()
			.equals(registrationForm.getPasswordConfirmation())) {
			model.addAttribute("error", "Пароли не совпадают.");
			return "register";
		}

		try {
			userService.register(
				registrationForm.getLogin(),
				registrationForm.getPassword()
			);
			return "redirect:/login?registered";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			return "register";
		}
	}

	@GetMapping("/user")
	public String profile(Authentication authentication, Model model) {
		model.addAttribute(
			"user",
			userService.getProfile(authentication.getName())
		);
		return "user";
	}
}
