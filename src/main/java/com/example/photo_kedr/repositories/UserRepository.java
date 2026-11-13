package com.example.photo_kedr.repositories;

import com.example.photo_kedr.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
	private final JdbcTemplate jdbcTemplate;

	public UserRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private User mapUser(ResultSet rs) throws SQLException {
		return new User(
			rs.getInt("id"),
			rs.getString("login"),
			rs.getString("password"),
			rs.getInt("privileges")
		);
	}

	public Optional<User> findByLogin(String login) {
		List<User> users = jdbcTemplate.query(
			"SELECT id, login, password, privileges FROM app_users WHERE login = ?",
			(rs, rowNum) -> mapUser(rs),
			login
		);
		return users.stream().findFirst();
	}

	public List<User> findAll() {
		return jdbcTemplate.query(
			"SELECT id, login, password, privileges FROM app_users ORDER BY id",
			(rs, rowNum) -> mapUser(rs)
		);
	}

	public User add(User newUser) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection
				.prepareStatement(
					"INSERT INTO app_users (login, password, privileges) VALUES (?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS
				);
			statement.setString(1, newUser.getLogin());
			statement.setString(2, newUser.getPassword());
			statement.setInt(3, newUser.getPrivileges());
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (generatedId == null) {
			throw new IllegalStateException(
				"Database did not return the generated user id"
			);
		}
		newUser.setId(generatedId.intValue());
		return newUser;
	}
}
