package org.argunaoverdrive.bot.model;

import javax.persistence.*;

@Entity
@Table(name = "day_to_notify")

public class DayToNotify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private WeekDays day;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private SubscribedUser subscribedUser;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public WeekDays getDay() {
        return day;
    }

    public void setDay(WeekDays day) {
        this.day = day;
    }

    public SubscribedUser getSubscribedUser() {
        return subscribedUser;
    }

    public void setSubscribedUser(SubscribedUser subscribedUser) {
        this.subscribedUser = subscribedUser;
    }
}
