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

import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.derecalliance.derec.demo.state.State;

public class SigninController {

    @FXML
    private ImageView imageView;

    @FXML
    private TextField nameField;

    @FXML
    private TextField uriField;

    @FXML
    private ComboBox<String> modeSelectionDropdown;

    @FXML
    private Button submitButton;

    @FXML
    private VBox deleteMe;

    private Runnable onSignedIn;

    @FXML
    private VBox signInScreenMain;

    class TestUser {
        public String uri;
        public String role;

        public TestUser(String uri, String role) {
            this.uri = uri;
            this.role = role;
        }
    }

    Map<String, TestUser> testUsers = Map.of(
            "alice", new TestUser("http://localhost:8001", "Sharer"),
            "bob", new TestUser("http://localhost:8002", "Helper"),
            "carol", new TestUser("http://localhost:8003", "Helper"),
            "dave", new TestUser("http://localhost:8004", "Helper"),
            "eve", new TestUser("http://localhost:8005", "Helper"),
            "frank", new TestUser("http://localhost:8006", "Helper"));

    @FXML
    private void initialize() {
        modeSelectionDropdown.getItems().addAll("Normal Mode", "Recovery Mode");
        modeSelectionDropdown.getSelectionModel().select("Normal Mode");
        State.getInstance().getUserSelections().setRecovering(false);
    }

    @FXML
    private void handleSigninSubmit() {
        String name = nameField.getText();
        String uri = uriField.getText();

        if (onSignedIn != null) {
            State.getInstance().getUserSelections().setName(name);
            State.getInstance().getUserSelections().setUri(uri);

            onSignedIn.run();
        }
    }

    public void setOnSignedIn(Runnable onSignedInCallback) {
        this.onSignedIn = onSignedInCallback;
    }

    @FXML
    private void handleModeSelectionChanges() {
        System.out.println("In handleModeSelectionChanges");
        String mode = modeSelectionDropdown.getValue();
        System.out.println("Mode: " + mode);
        if ("Recovery Mode".equals(mode)) {
            // Make the color red
            System.out.println("Setting color of the sign in screen to red");
            signInScreenMain.getStyleClass().remove("signinscreen");
            signInScreenMain.getStyleClass().add("signinscreen-recovery");
        } else {
            System.out.println("Setting color of the sign in screen to normal");
            signInScreenMain.getStyleClass().remove("signinscreen-recovery");
            signInScreenMain.getStyleClass().add("signinscreen");
        }

        if ("Recovery Mode".equals(mode)) {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Recovery Confirmation");
            alert.setHeaderText("Are you sure that you want to proceed with recovery?");
            alert.setContentText(
                    "This operation will erase any data you have on this device and attempt recovery from your helpers.");
            alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            alert.initOwner(MainApp.primaryStage);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    State.getInstance().getUserSelections().setRecovering(true);

                } else {
                    modeSelectionDropdown.getSelectionModel().select("Normal Mode");
                    State.getInstance().getUserSelections().setRecovering(false);
                }
            });
        }
    }

    @FXML
    void handleNameKeyPressed(KeyEvent event) {
        String newValue = nameField.textProperty().getValue().toLowerCase();
        if (testUsers.containsKey(newValue.toLowerCase())) {
            uriField.setText(testUsers.get(newValue).uri);
            State.getInstance().testUserRole = testUsers.get(newValue).role;
        }
    }
}
