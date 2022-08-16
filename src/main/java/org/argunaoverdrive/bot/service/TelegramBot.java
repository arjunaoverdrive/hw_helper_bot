package org.argunaoverdrive.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.argunaoverdrive.bot.config.BotConfig;
import org.argunaoverdrive.bot.model.DayOfWeek;
import org.argunaoverdrive.bot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String HELP_TEXT = "This bot is created to help with homework management." +
            "\n\nYou can execute commands from the main menu on the left or by typing a command";
    private static final long CHECK_PERIOD = 1000l * 60 * 60 * 24;

    @Autowired
    private final BotConfig config;
    @Autowired
    private final UserService userService;
    @Autowired
    private final NotificationService notificationService;


    public TelegramBot(BotConfig config, UserService userService, NotificationService notificationService) {
        this.config = config;
        this.userService = userService;
        this.notificationService = notificationService;
        registerCommands();
        checkSubscriptions();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                Message message = update.getMessage();
                executeCommands(message);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallBack(update);
        }
    }

    private void handleCallBack(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        int data = Integer.parseInt(callbackQuery.getData());
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        SendMessage message = new SendMessage();
        if (data < 7 && data >= 0) {
            createSubscribeMessage(data, chatId, message);
        } else if (data == 7) {
            createUnsubscribeMessage(chatId, message);
        } else {
            message.setChatId(String.valueOf(chatId));
            message.setText("I'm glad you're reasonable and hard-working!");
        }
        sendMessage(message);
    }

    private void executeCommands(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        switch (messageText) {
            case "/start" -> {
                registerUser(message);
                startCommandReceived(chatId, message.getChat().getFirstName());
            }
            case "/help" -> sendMessage(chatId, HELP_TEXT);
            case "/subscribe" -> {
                notificationService.subscribe(chatId);
                attachSubscriptionKeyboard(chatId);

            }
            case "/unsubscribe" -> unsubscribeUserFromNotifications(chatId);
            default -> sendMessage(chatId, "Sorry, the command was not recognized");
        }
    }

    private void createUnsubscribeMessage(long chatId, SendMessage message) {
        notificationService.unsubscribe(chatId);
        message.setChatId(String.valueOf(chatId));
        message.setText("You have unsubscribed from all notifications. \n\n" +
                "To subscribe again, please run /subscribe");
    }

    private void createSubscribeMessage(int data, long chatId, SendMessage message) {
        DayOfWeek[] values = DayOfWeek.values();
        DayOfWeek dayToNotify = values[data];
        notifyUserOn(chatId, dayToNotify);
        message.setChatId(String.valueOf(chatId));
        message.setText("I will notify you on " + dayToNotify.getName());
    }

    private void notifyUserOn(long chatId, DayOfWeek dayToNotify) {
        notificationService.notifyOn(chatId, dayToNotify);
        log.info("Notify on " + dayToNotify.getName());
    }

    private InlineKeyboardMarkup addDaysOfWeekKeyboard() {
        DayOfWeek[] values = DayOfWeek.values();
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> rowOne = new ArrayList<>();
        buttons.add(getRowOfButtons(values, 0, 4));
        buttons.add(getRowOfButtons(values, 4, 7));
        keyboard.setKeyboard(buttons);
        return keyboard;
    }

    private List<InlineKeyboardButton> getRowOfButtons(DayOfWeek[] values, int from, int to) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = from; i < to; i++) {
            InlineKeyboardButton day = new InlineKeyboardButton();
            day.setText(values[i].name());
            day.setCallbackData(String.valueOf(i));
            row.add(day);
        }
        return row;
    }

    private InlineKeyboardMarkup addYNKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> yn = getInlineKeyboardButtons();
        buttons.add(yn);
        keyboard.setKeyboard(buttons);
        return keyboard;
    }

    private List<InlineKeyboardButton> getInlineKeyboardButtons() {
        List<InlineKeyboardButton> yn = new ArrayList<>();
        InlineKeyboardButton yes = getInlineKeyboardButton("Yes", "7");
        InlineKeyboardButton no = getInlineKeyboardButton("No", "-1");
        yn.add(yes);
        yn.add(no);
        return yn;
    }

    private InlineKeyboardButton getInlineKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void unsubscribeUserFromNotifications(long chatId) {
        InlineKeyboardMarkup keyboard = addYNKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Are you sure you don't want to receive our awesome notifications " +
                "\n\nthat enforce your productivity any more?");
        message.setReplyMarkup(keyboard);
        sendMessage(message);
    }

    private void attachSubscriptionKeyboard(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = addDaysOfWeekKeyboard();
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText("Please choose the day you want me to notify you.");
        sm.setReplyMarkup(keyboardMarkup);
        sendMessage(sm);
    }

    private void registerUser(Message message) {
        if (userService.findById(message.getChatId()).isEmpty()) {
            long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userService.save(user);
            log.info("User saved " + user);
        } else
            log.info("User already exists.");
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
        answer += HELP_TEXT;
        sendMessage(chatId, answer);
        log.info("Start conversation with user " + name + ". Sent hello message " + answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException tae) {
            log.warn(tae.getMessage());
        }
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException tae) {
            log.error(tae.getMessage());
        }
    }

    private void registerCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/subscribe", "Subscribe to homework reminders"));
        listOfCommands.add(new BotCommand("/unsubscribe", "Unsubscribe from homework reminders"));
        listOfCommands.add(new BotCommand("/help", "Info help with bot"));
        try {
            this.execute(new DeleteMyCommands(new BotCommandScopeDefault(), null));
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException tae) {
            log.error("Error setting bot's command list " + tae.getMessage());
        }
    }

    private void checkSubscriptions() {
        TimerTask repeatedTask = new TimerTask() {

            @Override
            public void run() {
                DayOfWeek today = getDayOfWeek();
                List<Long> subscriptionsList = notificationService.getSubscriptionsList(today);

                String text = "Hi! It's high time you started completing your homework!" +
                        "\n\nRemember, 10 minutes a day is better than an hour once a week";
                subscriptionsList.forEach(chatId -> {
                    sendMessage(chatId, text);
                    log.info("Sent notification to user " + chatId);
                        }
                );
            }
        };

        Date dateToWork = getDateToStart();
        startTimerThread(repeatedTask, dateToWork);
    }

    private Date getDateToStart() {
        LocalDateTime timeToWork = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 45));
        return Date.from(timeToWork.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void startTimerThread(TimerTask repeatedTask, Date dateToWork) {
        Thread t = new Thread(()->{
            new Timer().scheduleAtFixedRate(repeatedTask, dateToWork, CHECK_PERIOD);
        });
        t.start();
    }

    private DayOfWeek getDayOfWeek() {
        LocalDateTime now = LocalDateTime.now();
        java.time.DayOfWeek dayOfWeek = now.getDayOfWeek();
        String name = dayOfWeek.name();
        Map<String, DayOfWeek> nameToDay = new HashMap<>();
        Arrays.stream(DayOfWeek.values()).forEach(day -> nameToDay.put(dayOfWeek.name(), day));
        DayOfWeek today = nameToDay.get(name);
        return today;
    }
}
