package jdroidcoder.ua.taxi_bishkek.events;

/**
 * Created by jdroidcoder on 07.04.17.
 */

public class LoginEvent {
    private String login;
    private String password;

    public LoginEvent() {
    }

    public LoginEvent(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
