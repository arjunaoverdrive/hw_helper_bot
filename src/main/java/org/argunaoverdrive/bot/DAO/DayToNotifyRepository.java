package org.argunaoverdrive.bot.DAO;

import org.argunaoverdrive.bot.model.WeekDays;
import org.argunaoverdrive.bot.model.DayToNotify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayToNotifyRepository extends JpaRepository<DayToNotify, Integer> {
    DayToNotify findByDay(WeekDays day);
    @Query("select d from DayToNotify d where d.day = :day and d.subscribedUser.chatId = :chatId")
    DayToNotify findByDayAndChatId(WeekDays day, long chatId);

    void deleteAllBySubscribedUser_ChatId(long chatId);

    List<DayToNotify> findAllByDay(WeekDays day);
}
