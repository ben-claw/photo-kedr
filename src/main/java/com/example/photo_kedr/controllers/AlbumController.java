package com.example.photo_kedr.controllers;

import com.example.photo_kedr.model.Album;
import com.example.photo_kedr.services.AlbumService;
import com.example.photo_kedr.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/albums")
@Controller
public class AlbumController {
	private final AlbumService albumService;
	private final UserService userService;

	public AlbumController(
		AlbumService albumService,
		UserService userService
	) {
		this.albumService = albumService;
		this.userService = userService;
	}

	@GetMapping("/new")
	public String newAlbumForm(Model model) {
		model.addAttribute("albumForm", new AlbumForm());
		return "album-form";
	}

	@PostMapping
	public String createAlbum(
		@ModelAttribute AlbumForm albumForm,
		Authentication authentication,
		Model model
	) {
		try {
			int ownerId = userService
				.getProfile(authentication.getName())
				.id();
			Album album = albumService
				.createAlbum(ownerId, albumForm.getName());
			return "redirect:/albums/" + album.getId();
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			return "album-form";
		}
	}

	@GetMapping("/{id}")
	public String viewAlbum(
		@PathVariable int id,
		Authentication authentication,
		Model model
	) {
		int ownerId = userService.getProfile(authentication.getName())
			.id();
		model.addAttribute("album", albumService.getAlbum(id, ownerId));
		return "album";
	}

	public static class AlbumForm {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
