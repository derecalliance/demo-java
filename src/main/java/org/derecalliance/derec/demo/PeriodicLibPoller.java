/*
 * Copyright (c) DeRec Alliance and its Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.derecalliance.derec.demo;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import org.derecalliance.derec.demo.state.State;

public class PeriodicLibPoller {
    private Timer timer;

    PeriodicLibPoller() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            State.getInstance().updateHelperFromLib();
                            State.getInstance().updateSharerFromLib();
                        });
                    }
                },
                0,
                1000); // TODO make this more frequent
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
