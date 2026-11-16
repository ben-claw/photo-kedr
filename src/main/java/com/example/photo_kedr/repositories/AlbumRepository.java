package com.example.photo_kedr.repositories;

import com.example.photo_kedr.model.Album;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AlbumRepository {
	private final JdbcTemplate jdbcTemplate;

	public AlbumRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Album> findByOwner(int ownerId) {
		return jdbcTemplate.query(
			"SELECT id, owner, name FROM albums WHERE owner = ? ORDER BY id DESC",
			(rs, rowNum) -> new Album(
				rs.getInt("id"),
				rs.getInt("owner"),
				rs.getString("name")
			),
			ownerId
		);
	}

	public Optional<Album> findById(int id) {
		List<Album> albums = jdbcTemplate.query(
			"SELECT id, owner, name FROM albums WHERE id = ?",
			(rs, rowNum) -> new Album(
				rs.getInt("id"),
				rs.getInt("owner"),
				rs.getString("name")
			),
			id
		);
		return albums.stream().findFirst();
	}

	public Album add(Album album) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection
				.prepareStatement(
					"INSERT INTO albums (owner, name) VALUES (?, ?)",
					Statement.RETURN_GENERATED_KEYS
				);
			statement.setInt(1, album.getOwner());
			statement.setString(2, album.getName());
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (generatedId == null) {
			throw new IllegalStateException(
				"Database did not return the generated album id"
			);
		}
		album.setId(generatedId.intValue());
		return album;
	}
}
