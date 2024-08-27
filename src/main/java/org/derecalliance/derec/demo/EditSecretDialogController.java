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

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EditSecretDialogController {

    @FXML
    private Label secretNameLabel;

    @FXML
    private TextField secretNameTextField;

    @FXML
    private TextArea secretDataField;

    //    @FXML
    //    private ToggleSwitch recoveryModeToggleSwitch;

    //    @FXML
    //    private Label normalModeLabel;

    //    @FXML
    //    private Label recoveryModeLabel;

    @FXML
    private void initialize() {
        //        recoveryModeToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
        //            System.out.println("Recovery is " + (newValue ? "TRUE" : "FALSE"));
        //            secretDataField.setVisible(!newValue);
        //
        //            if (recoveryModeToggleSwitch.isSelected()) {
        //                Alert alert = new Alert(Alert.AlertType.NONE);
        //                alert.setTitle("Recovery Confirmation");
        //                alert.setHeaderText("Are you sure that you want to proceed with recovery?");
        //                alert.setContentText("This operation will erase any data you have on this device and attempt
        // recovery from your helpers.");
        //                alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        //                alert.initOwner(MainApp.primaryStage);
        ////                alert.initOwner(alert.getOwner());
        //
        //                alert.showAndWait().ifPresent(response -> {
        //                    if (response != ButtonType.OK) {
        //                        System.out.println("User does not want to recover, switching to normal mode for
        // secret");
        //                        recoveryModeToggleSwitch.setSelected(false);
        //                    }
        //                });
        //            }
        //        });
    }

    public Label getSecretNameLabel() {
        return secretNameLabel;
    }

    public TextField getSecretNameTextField() {
        return secretNameTextField;
    }

    public TextArea getSecretDataField() {
        return secretDataField;
    }

    //    public ToggleSwitch getRecoveryModeToggleSwitch() {
    //        return recoveryModeToggleSwitch;
    //    }

    //    public Label getNormalModeLabel() {
    //        return normalModeLabel;
    //    }
    //
    //    public Label getRecoveryModeLabel() {
    //        return recoveryModeLabel;
    //    }

}
