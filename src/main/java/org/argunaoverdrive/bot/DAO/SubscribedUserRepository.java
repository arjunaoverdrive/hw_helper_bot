package org.argunaoverdrive.bot.DAO;

import org.argunaoverdrive.bot.model.SubscribedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscribedUserRepository extends JpaRepository<SubscribedUser, Long> {
    SubscribedUser findByChatId(long chatId);
}
