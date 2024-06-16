package org.derecalliance.derec.demo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.ToggleSwitch;

public class EditSecretDialogController {

    @FXML
    private Label secretNameLabel;
    @FXML
    private TextField secretNameTextField;

    @FXML
    private TextArea secretDataField;

    @FXML
    private ToggleSwitch recoveryModeToggleSwitch;

    @FXML
    private Label normalModeLabel;

    @FXML
    private Label recoveryModeLabel;


    @FXML
    private void initialize() {
        recoveryModeToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Recovery is " + (newValue ? "TRUE" : "FALSE"));
            secretDataField.setVisible(!newValue);

            if (recoveryModeToggleSwitch.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.setTitle("Recovery Confirmation");
                alert.setHeaderText("Are you sure that you want to proceed with recovery?");
                alert.setContentText("This operation will erase any data you have on this device and attempt recovery from your helpers.");
                alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                alert.initOwner(MainApp.primaryStage);
//                alert.initOwner(alert.getOwner());

                alert.showAndWait().ifPresent(response -> {
                    if (response != ButtonType.OK) {
                        System.out.println("User does not want to recover, switching to normal mode for secret");
                        recoveryModeToggleSwitch.setSelected(false);
                    }
                });
            }
        });
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

    public ToggleSwitch getRecoveryModeToggleSwitch() {
        return recoveryModeToggleSwitch;
    }

    public Label getNormalModeLabel() {
        return normalModeLabel;
    }

    public Label getRecoveryModeLabel() {
        return recoveryModeLabel;
    }

}
