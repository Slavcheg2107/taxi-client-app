package jdroidcoder.ua.taxi_bishkek_client.events;

/**
 * Created by jdroidcoder on 07.04.17.
 */

public class ErrorMessageEvent {
    private String message;

    public ErrorMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
