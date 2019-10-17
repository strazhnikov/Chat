package chat.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (!message.contains(": ")) return;
            String[] messageArray = message.split(":");
            String msgSender = messageArray[0].trim();
            String msgText = messageArray[1].trim();
            String pattern = null;
            if (msgText.equalsIgnoreCase("дата")) {
                pattern = "d.MM.YYYY";
            } else if (msgText.equalsIgnoreCase("день")) {
                pattern = "d";
            } else if (msgText.equalsIgnoreCase("месяц")) {
                pattern = "MMMM";
            } else if (msgText.equalsIgnoreCase("год")) {
                pattern = "YYYY";
            } else if (msgText.equalsIgnoreCase("время")) {
                pattern = "H:mm:ss";
            } else if (msgText.equalsIgnoreCase("час")) {
                pattern = "H";
            } else if (msgText.equalsIgnoreCase("минуты")) {
                pattern = "m";
            } else if (msgText.equalsIgnoreCase("секунды")) {
                pattern = "s";
            }
            if (pattern != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                String date = dateFormat.format(Calendar.getInstance().getTime());
                String answer = "Информация для " + msgSender + ": " + date;
                sendTextMessage(answer);
            }
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }

    public static void main(String[] args) {
        new BotClient().run();
    }
}
