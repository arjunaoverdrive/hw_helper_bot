package org.argunaoverdrive.bot.service;

import org.argunaoverdrive.bot.model.User;
import org.argunaoverdrive.bot.DAO.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> findById(Long chatId) {
        return repository.findById(chatId);
    }

    public void save(User user) {
        repository.save(user);
    }
}
