package com.example.photo_kedr.repositories;

import com.example.photo_kedr.model.Photo;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PhotoRepository {
	private final JdbcTemplate jdbcTemplate;

	public PhotoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private Photo mapPhoto(java.sql.ResultSet rs, int rowNum)
		throws java.sql.SQLException {
		return new Photo(
			rs.getInt("id"),
			rs.getInt("album_id"),
			rs.getString("file_name"),
			rs.getString("storage_name"),
			rs.getString("content_type"),
			rs.getString("description"),
			rs.getDate("photo_date").toLocalDate()
		);
	}

	public List<Photo> findByAlbumId(int albumId) {
		return jdbcTemplate.query(
			"SELECT id, album_id, file_name, storage_name, content_type, description, photo_date "
				+ "FROM photos WHERE album_id = ? ORDER BY photo_date DESC, id DESC",
			this::mapPhoto,
			albumId
		);
	}

	public Optional<Photo> findById(int id) {
		List<Photo> photos = jdbcTemplate.query(
			"SELECT id, album_id, file_name, storage_name, content_type, description, photo_date "
				+ "FROM photos WHERE id = ?",
			this::mapPhoto,
			id
		);
		return photos.stream().findFirst();
	}

	public Photo add(Photo photo) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection
				.prepareStatement(
					"INSERT INTO photos (album_id, file_name, storage_name, content_type, description, photo_date) "
						+ "VALUES (?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS
				);
			statement.setInt(1, photo.getAlbumId());
			statement.setString(2, photo.getFileName());
			statement.setString(3, photo.getStorageName());
			statement.setString(4, photo.getContentType());
			statement.setString(5, photo.getDescription());
			statement
				.setDate(6, Date.valueOf(photo.getPhotoDate()));
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (generatedId == null) {
			throw new IllegalStateException(
				"Database did not return the generated photo id"
			);
		}
		photo.setId(generatedId.intValue());
		return photo;
	}

	public void updateDetails(
		int id,
		String description,
		java.time.LocalDate date
	) {
		jdbcTemplate.update(
			"UPDATE photos SET description = ?, photo_date = ? WHERE id = ?",
			description,
			Date.valueOf(date),
			id
		);
	}
}
