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
