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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardBoxController {

    @FXML
    private Label cardLabel;

    @FXML
    private Button cardButton;

    @FXML
    private ImageView cardImage;

    @FXML
    public void setCardButtonAction(EventHandler<ActionEvent> eventHandler) {
        cardButton.setOnAction(eventHandler);
    }

    public void setLabelText(String text) {
        cardLabel.setText(text);
    }

    public Button getCardButton() {
        return cardButton;
    }

    public void setImage(String filename) {
        Image img = new Image(getClass().getResourceAsStream(filename));
        cardImage.setImage(img);
    }
}
