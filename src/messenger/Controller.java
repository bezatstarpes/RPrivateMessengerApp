package messenger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller extends Thread implements Initializable {

    @FXML
    private TextField inputMsg;

    @FXML
    private TextField addressField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea textArea;

    @FXML
    private Button buttonSend;

    @FXML
    private Button buttonConnect;

    @FXML
    private Button buttonDisconnect;

    private Messenger messenger = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addressField.setText("127.0.0.1");
        addressField.setEditable(false);
        inputMsg.setEditable(false);
        nameField.setPromptText("User");
        nameField.requestFocus();
        buttonSend.setDisable(true);
        buttonDisconnect.setDisable(true);
        buttonConnect.setDisable(true);


        inputMsg.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER") & !(buttonSend.isDisable())) {
                actionSend();
            }
        });
    }

    @FXML
    public void actionSend() {
        messenger.send();
        inputMsg.requestFocus();
    }

    @FXML
    public void actionConnect() {
        UITask ui = (UITask) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{UITask.class},
                new EDTInvocationHandler(new UITaskImpl()));
        if (addressField.getText().trim().equals("localhost") || addressField.getText().trim().equals("127.0.0.1")) {
            messenger = new MessengerImpl(nameField.getText(), ui, 51515);
        } else {
            messenger = new MessengerImpl(addressField.getText().trim(), nameField.getText(), 51515, ui);
        }

        Platform.runLater(() -> {
            messenger.start();
        });

        inputMsg.setEditable(true);
        inputMsg.requestFocus();
        buttonSend.setDisable(false);
        buttonConnect.setDisable(true);
        buttonDisconnect.setDisable(false);
        addressField.setDisable(true);
        nameField.setDisable(true);
    }

    @FXML
    public void actionDisconnect() throws IOException {
        messenger.stop();

        buttonSend.setDisable(true);
        buttonConnect.setDisable(false);
        buttonDisconnect.setDisable(true);
        addressField.setDisable(false);
        nameField.setDisable(false);
        nameField.requestFocus();
    }

    class UITaskImpl implements UITask {
        @Override
        public String getMessage() {
            String message = inputMsg.getText();
            inputMsg.clear();
            return message;
        }

        @Override
        public void setText(String message) {
            textArea.appendText(message + "\n");
        }
    }

    @FXML
    private void disableButtonConnect() {
        if (!nameField.getText().trim().isEmpty()) {
            buttonConnect.setDisable(false);
            addressField.setEditable(true);
        }
    }
}