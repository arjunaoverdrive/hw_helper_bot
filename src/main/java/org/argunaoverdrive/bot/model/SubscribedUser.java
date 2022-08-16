package org.argunaoverdrive.bot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "subscribed_user")
public class SubscribedUser {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    private Timestamp subscribedAt;
    private Timestamp lastNotified;
    private boolean isNotified;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }


    public Timestamp getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(Timestamp subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public Timestamp getLastNotified() {
        return lastNotified;
    }

    public void setLastNotified(Timestamp lastNotified) {
        this.lastNotified = lastNotified;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }


    @Override
    public String toString() {
        return "SubscribedUser{" +
                "subscribedAt=" + subscribedAt +
                ", lastNotified=" + lastNotified +
                ", isNotified=" + isNotified +
                '}';
    }
}
