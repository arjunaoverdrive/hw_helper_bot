package org.argunaoverdrive.bot.DAO;

import org.argunaoverdrive.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

}
