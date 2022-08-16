package org.argunaoverdrive.bot.service;

import org.argunaoverdrive.bot.DAO.DayToNotifyRepository;
import org.argunaoverdrive.bot.DAO.SubscribedUserRepository;
import org.argunaoverdrive.bot.model.DayOfWeek;
import org.argunaoverdrive.bot.model.DayToNotify;
import org.argunaoverdrive.bot.model.SubscribedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final SubscribedUserRepository subscribedUserRepository;
    private final DayToNotifyRepository dayToNotifyRepository;

    @Autowired
    public NotificationService(SubscribedUserRepository subscribedUserRepository, DayToNotifyRepository dayToNotifyRepository) {
        this.subscribedUserRepository = subscribedUserRepository;
        this.dayToNotifyRepository = dayToNotifyRepository;
    }

    public SubscribedUser subscribe(long chatId) {
        Optional <SubscribedUser> subscriber = subscribedUserRepository.findById(chatId);
        if(subscriber.isPresent()){
            return subscriber.get();
        }
        SubscribedUser user = new SubscribedUser();
        user.setNotified(false);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        user.setLastNotified(now);
        user.setSubscribedAt(now);
        user.setChatId(chatId);
        return subscribedUserRepository.save(user);
    }

    @Transactional
    public void unsubscribe(long chatId) {
        dayToNotifyRepository.deleteAllBySubscribedUser_ChatId(chatId);
        subscribedUserRepository.deleteById(chatId);

    }

    public void notifyOn(long chatId, DayOfWeek dayToNotify) {
        DayToNotify day = new DayToNotify();
        day.setDay(dayToNotify);
        SubscribedUser subscribedUser = subscribedUserRepository.findById(chatId).orElse(subscribe(chatId));
        if (dayToNotifyRepository.findByDayAndChatId(day.getDay(),chatId) == null ) {
            day.setSubscribedUser(subscribedUser);
        } else {
            return;
        }
        dayToNotifyRepository.save(day);
    }

    public List<Long> getSubscriptionsList(DayOfWeek day) {
        List<DayToNotify> days = dayToNotifyRepository.findAllByDay(day);
        List<Long>subscribers = days.stream()
                .map(DayToNotify::getSubscribedUser)
                .map(SubscribedUser::getChatId)
                .collect(Collectors.toList());
        return subscribers;
    }

}
