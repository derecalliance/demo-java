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

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.derecalliance.derec.demo.state.State;
import org.derecalliance.derec.lib.impl.HelperImpl;

public class MainController {
    @FXML
    private ComboBox<String> roleDropdown;

    @FXML
    private StackPane mainContentArea;

    private Node sharerContent;
    private Node helperContent;

    @FXML
    public VBox topbarVbox;

    @FXML
    public Region bottombarRegion;

    @FXML
    private void initialize() {
        // Init library
        State.getInstance().getUserSelections().isRecovering.addListener((observable, oldValue, newValue) -> {
            System.out.println("IsRecovering Value changed: " + newValue);
            setTopAndBottomBarStyle();
        });
        setTopAndBottomBarStyle();
        roleDropdown.getItems().addAll("Sharer", "Helper");
        preloadRoleContent();

        roleDropdown.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            mainContentArea.getChildren().setAll("helper".equalsIgnoreCase(newValue) ? helperContent : sharerContent);
            mainContentArea.requestLayout();
        });
        roleDropdown.getSelectionModel().select("Sharer");

        // TODO: remove this - added for demo/testing
        if (State.getInstance().testUserRole == "Sharer") {
            roleDropdown.getSelectionModel().select("Sharer");
        } else if (State.getInstance().testUserRole == "Helper") {
            roleDropdown.getSelectionModel().select("Helper");
        }

        PeriodicLibPoller periodicLibPoller =
                new PeriodicLibPoller(); // Periodic task to copy data from the lib into app State
    }

    public void setTopAndBottomBarStyle() {
        if (State.getInstance().getUserSelections().isRecovering()) {
            topbarVbox.getStyleClass().remove("topbar-normal");
            topbarVbox.getStyleClass().add("topbar-recovery");
            bottombarRegion.getStyleClass().remove("bottombar-normal");
            bottombarRegion.getStyleClass().add("bottombar-recovery");
        } else {
            topbarVbox.getStyleClass().remove("topbar-recovery");
            topbarVbox.getStyleClass().add("topbar-normal");
            bottombarRegion.getStyleClass().remove("bottombar-recovery");
            bottombarRegion.getStyleClass().add("bottombar-normal");
        }
    }

    private void loadMainContent(String role) {
        try {
            mainContentArea.getChildren().setAll("helper".equals(role) ? helperContent : sharerContent);

            mainContentArea.requestLayout(); // Force the VBox to update its layout
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preloadRoleContent() {
        try {
            if (sharerContent == null) {
                sharerContent =
                        FXMLLoader.load(getClass().getResource("/org/derecalliance/derec/demo/sharer-tab.fxml"));
            }
            if (helperContent == null) {
                helperContent =
                        FXMLLoader.load(getClass().getResource("/org/derecalliance/derec/demo/helper-tab.fxml"));
            }
        } catch (IOException e) {
            System.out.println("Exception in preloadRoleContent");
            e.printStackTrace();
        }
    }

    @FXML
    void onHamburgerClick() {
        System.out.println("Hamburger clicked");
        Dialog dialog = new Dialog<>();
        dialog.setTitle("Settings");
        GridPane dialogContent = new GridPane();
        dialogContent.setHgap(10);
        dialogContent.setVgap(10);

        // First row: Pause Label and CheckBox
        Label pauseLabel = new Label("Pause");
        CheckBox pauseCheckBox = new CheckBox();
        pauseCheckBox.setSelected(State.getInstance().getUserSelections().isPaused());
        dialogContent.add(pauseLabel, 0, 0);
        dialogContent.add(pauseCheckBox, 1, 0);

        // Second row: Min Helpers Label and TextField
        Label minHelpersLabel = new Label("Min Helpers");
        TextField minHelpersTextField = new TextField();
        dialogContent.add(minHelpersLabel, 0, 1);
        dialogContent.add(minHelpersTextField, 1, 1);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(dialogContent);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setPrefWidth(MainApp.primaryStage.getWidth() * 0.8);
        // Set the user to paused when the checkbox is selected
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                System.out.println("button ok");
                State.getInstance().getUserSelections().setPaused(pauseCheckBox.isSelected());
                if (State.getInstance().getHelper() != null) {
                    ((HelperImpl) State.getInstance().getHelper()).setPaused(pauseCheckBox.isSelected());
                }
            }
            return null;
        });

        dialog.setDialogPane(dialogPane);
        dialog.initOwner(MainApp.primaryStage);
        Optional<SharerTabController.SecretDataInput> result = dialog.showAndWait();
    }
}
