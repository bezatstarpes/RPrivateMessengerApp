package messenger;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.time.LocalDateTime;

public class MessengerImpl implements Messenger {

    private InetAddress address;
    private int port = 51515;
    private String name;
    private UITask UI;
    private ServerSocket server;
    private Socket connection;
    private boolean canceled = false;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Messenger messenger;

    // Start server connection
    public MessengerImpl(String name, UITask UI, int port) {
        try {
            this.name = name;
            this.port = port;
            this.UI = UI;
            server = new ServerSocket(port);
            connection = server.accept();
            UI.setText("You are now connected to " + connection.getInetAddress());
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            showErrorDialog("The host is invalid or unavailable\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // Start Client connection
    public MessengerImpl(String address, String name, int port, UITask UI) {
        try {
            this.address = InetAddress.getByName(address);
            this.name = name;
            this.UI = UI;
            this.port = port;
            connection = new Socket(this.address, port);
            UI.setText("You are now connected to " + connection.getInetAddress().getHostName());
            input = new ObjectInputStream(connection.getInputStream());
            output = new ObjectOutputStream(connection.getOutputStream());

        } catch (IOException e) {
            showErrorDialog("The host is invalid or unavailable\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Receiver().start();
    }

    @Override
    public void stop() throws IOException {
        cancel();
        try {
            input.close();
            output.close();

        } catch (IOException e) {
            showErrorDialog(("Join error\n" + e.getMessage()));
        } finally {
            connection.close();
        }
    }

    @Override
    public void send() {
        new Sender().start();
    }

    private class Receiver extends Thread {
        @Override
        public void run() {
            try {
                String message;
                while (!isCanceled()) {
                    message = (String) input.readObject();
                    UI.setText(getCurrentTime() + message);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isCanceled()) {
                    showInfoDialog("Connection completed correctly!");
                } else {
                    UI.setText("Your friend terminated connection!");
                    try {
                        messenger.stop();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private class Sender extends Thread {
        @Override
        public void run() {
            try {
                String message = " <" + name + "> " + UI.getMessage();
                output.writeObject(message);
                output.flush();
                UI.setText(getCurrentTime() + message);
            } catch (IOException e) {
                showErrorDialog("Error sending data\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private synchronized String getCurrentTime() {
        LocalDateTime time = LocalDateTime.now();  // current date and time
        return ((time.getHour() < 10) ? "0" + time.getHour() : time.getHour()) + ":"
                + ((time.getMinute() < 10) ? "0" + time.getMinute() : time.getMinute()) + ":"
                + ((time.getSecond() < 10) ? "0" + time.getSecond() : time.getSecond());
    }

    private synchronized boolean isCanceled() {
        return canceled;
    }

    private synchronized void cancel() {
        canceled = true;
    }

    private static void showInfoDialog(String info) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Dialog");
            alert.setHeaderText("INFO: " + info);
            alert.setContentText(null);
            alert.setResizable(false);

        });
    }

    private static void showErrorDialog(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("ERROR: " + error);
            alert.setContentText(null);
            alert.setResizable(false);
            alert.show();
        });
    }
}

