package jdroidcoder.ua.taxi_bishkek_client.events;

/**
 * Created by jdroidcoder on 01.05.17.
 */

public class ConnectionErrorEvent {
    private boolean isShow;

    public ConnectionErrorEvent(boolean isShow) {
        this.isShow = isShow;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
