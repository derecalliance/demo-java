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
