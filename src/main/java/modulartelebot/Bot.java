package modulartelebot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SuppressWarnings("deprecation")
public class Bot extends TelegramLongPollingBot {
    private BotModule[] modules;
    private String name;
    private String token;

    public Bot(String name, String token) {
        this.name = name;
        this.token = token;

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            Log.log("unable to register bot.. " + e.getMessage(), Log.FLAVOR.Err);
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String message = update.getMessage().getText();
            String id = Long.toString(update.getMessage().getChatId());

            for (BotModule module : modules) {
                if (message.contains(module.getCommand())) {
                    Log.log("recieved command " + module.getCommand(), Log.FLAVOR.Success);
                    new Thread(() -> {
                        try {
                            module.update(message, id);
                        } catch (TelegramApiException e) {
                            Log.log("can't send message " + e.getMessage(), Log.FLAVOR.Err);
                        }
                    }).start();
                }
            }
        }
    }

    public void addModules(BotModule[] modules) {
        this.modules = modules;
    }
}