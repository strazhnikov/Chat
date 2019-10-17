package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static int port;
    private static ServerSocket serverSocket;
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String userName = null;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message nameRequest = connection.receive();
                if (nameRequest.getType() == MessageType.USER_NAME) {
                    userName = nameRequest.getData();
                }
            } while (userName == null || userName == "" || connectionMap.containsKey(userName));
            connectionMap.put(userName, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return userName;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Ошибка: сообщение не является текстом");
                }
            }
        }

        @Override
        public void run() {
            /*
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                ConsoleHelper.writeMessage("Установлено новое соединение с " + connection.getRemoteSocketAddress());
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
                e.printStackTrace();
            }
            if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Соединение с удаленным адресом " + socket.getRemoteSocketAddress() + " закрыто");
            */

            ConsoleHelper.writeMessage("Установлено новое соединение с " + socket.getRemoteSocketAddress().toString());
            Connection connection = null;
            String userName = null;
            try {
                connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        ConsoleHelper.writeMessage("Ошибка при закрытии клиентского соединения");
                        e.printStackTrace();
                    }
                }
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("Соединение с удаленным адресом " + socket.getRemoteSocketAddress().toString() + " закрыто");
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message); // send message to all connections from Map
            } catch (IOException e) {
                System.out.println("Не получилось отправить сообщение пользователю " + entry.getKey());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.print("Укажите номер порта: ");
        try {
            serverSocket = new ServerSocket(ConsoleHelper.readInt());
            System.out.println("Сервер запущен");
            while (true) {
                Socket newSocket = serverSocket.accept();
                new Handler(newSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Ошибка при закрытии серверного сокета");
                    e.printStackTrace();
                }
            }
        }

    }
}
