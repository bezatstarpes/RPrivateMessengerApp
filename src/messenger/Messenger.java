package messenger;

import java.io.IOException;

public interface Messenger {

    void start();

    void stop() throws IOException;

    void send();
}
