package jdroidcoder.ua.taxi_bishkek.events;

import android.location.Location;

/**
 * Created by jdroidcoder on 07.04.17.
 */

public class ChangeLocationEvent {
    private Location location;

    public ChangeLocationEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
