package com.example.photo_kedr.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.example.photo_kedr.model.User;

import lombok.extern.slf4j.Slf4j;


@Slf4j
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
    
    public Optional<User> findByLogin(String login){
        log.info("Finding user " + login);
        return jdbcTemplate.query(
            "SELECT * FROM Users WHERE login = ?", 
            rs -> rs.first()? Optional.of(mapUser(rs)): Optional.empty(),      
            login
        );
    }
    
    public List<User> findAll(){
        return jdbcTemplate.query(
            "SELECT * FROM Users", 
            (rs, _num) -> mapUser(rs)
        );
    }

    public User add(User newUser){
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Users (login, password, privileges) values (?,?,?)",
                    new String[] {"id"});
                ps.setString(1, newUser.getLogin());
                ps.setString(2, newUser.getPassword());
                ps.setInt(3, newUser.getPrivileges());
                return ps;
            },
            keyHolder
        );
        newUser.setId(keyHolder.getKey().intValue());
        return newUser;
    }
}
