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

import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Circle myCircle;

    @FXML
    protected void onHelloButtonClick() {
        int rand = new Random().nextInt(100);
        if (rand < 30) {
            myCircle.setFill(Paint.valueOf("#00ff00"));
        } else if (rand < 60) {
            myCircle.setFill(Paint.valueOf("#0000ff"));
        } else {
            myCircle.setFill(Paint.valueOf("#ff0000"));
        }
        welcomeText.setText("Random number is: " + rand);
    }
}
