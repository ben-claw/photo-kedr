package com.example.demo.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;

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
}
