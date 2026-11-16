package com.example.photo_kedr.services;

import com.example.photo_kedr.model.Album;
import com.example.photo_kedr.repositories.AlbumRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumService {
	private static final int MAX_NAME_LENGTH = 100;

	private final AlbumRepository albumRepository;

	public AlbumService(AlbumRepository albumRepository) {
		this.albumRepository = albumRepository;
	}

	public List<Album> getAlbumsForOwner(int ownerId) {
		return albumRepository.findByOwner(ownerId);
	}

	public Album getAlbum(int albumId, int ownerId) {
		return albumRepository.findById(albumId)
			.filter(album -> album.getOwner() == ownerId)
			.orElseThrow(() -> new AlbumNotFoundException());
	}

	@Transactional
	public Album createAlbum(int ownerId, String name) {
		String normalizedName = name == null ? "" : name.trim();
		if (normalizedName.isBlank()) {
			throw new IllegalArgumentException(
				"Укажите название альбома."
			);
		}
		if (normalizedName.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(
				"Название альбома должно содержать не более 100 символов."
			);
		}
		return albumRepository
			.add(new Album(0, ownerId, normalizedName));
	}

	public static class AlbumNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public AlbumNotFoundException() {
			super("Альбом не найден.");
		}
	}
}
