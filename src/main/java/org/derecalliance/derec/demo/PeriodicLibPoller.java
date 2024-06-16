package org.derecalliance.derec.demo;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import org.derecalliance.derec.demo.state.State;

public class PeriodicLibPoller {
    private Timer timer;

    PeriodicLibPoller() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    State.getInstance().updateHelperFromLib();
                    State.getInstance().updateSharerFromLib();
                });
            }
        }, 0, 1000); // TODO make this more frequent
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
