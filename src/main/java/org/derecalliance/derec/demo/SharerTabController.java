package org.derecalliance.derec.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
// import org.derecalliance.derec.api.*;
import javafx.util.Duration;
import org.derecalliance.derec.demo.state.State;
// import org.derecalliance.derec.lib.Version;
import org.derecalliance.derec.lib.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharerTabController {
    class SecretDataInput {
        public String name;
        public String stringToProtect;
        public boolean isRecovering;

        public SecretDataInput(String name, String stringToProtect) {
            this.name = name;
            this.stringToProtect = stringToProtect;
        }
    }

    public class NameAndContactInfo {
        public String name;
        public String contactInfo;

        public NameAndContactInfo(String name, String contactInfo) {
            this.name = name;
            this.contactInfo = contactInfo;
        }
    }

    @FXML
    private ComboBox<String> secretsDropdown;

    @FXML
    private Button versionsButton;

    @FXML
    private Button helpersButton;

    @FXML
    private Button createANewVersionButton;

    @FXML
    private Button pairWithHelperButton;

    @FXML
    private VBox middleArea;

    @FXML
    private Label selectedSecretLabel;

    @FXML
    private Button addSecretButton;

    Accordion helpersAccordion = new Accordion();
    Accordion versionsAccordion = new Accordion();
    Accordion notificationsAccordion = new Accordion();

    ScrollPane notificationsAccordionScrollPane;

    // For showing recoveryCompleteAlert
    private boolean isShowRecoveryCompleteScheduled = false;
    private Timeline showRecoveryCompleteTimeline;
    ArrayList<DeRecStatusNotification> recoveryCompleteNotifications = new ArrayList<>();
    final String recoverySecretName = "RecoverySecret";

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @FXML
    private void initialize() {
        System.out.println("In SharerTabController - initialize");
        initLibForSharer(
                State.getInstance().getUserSelections().getName(),
                State.getInstance().getUserSelections().getUri());
        System.out.println("InitLibForSharer done");
        State.getInstance()
                .observableSharerTabSecretsUpdateCounter
                .addListener((observable, oldValue, newValue) -> secretsListUpdated());
        //        State.getInstance().versionsForSelectedSecret.addListener((ListChangeListener<DeRecVersion>) v ->
        // updateVersionsAccordion());
        State.getInstance()
                .observableSharerTabVersionsUpdateCounter
                .addListener((observable, oldValue, newValue) -> updateVersionsAccordion());

        System.out.println("Added listerner");

        secretsDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
            secretsDropdownChanged(newValue);
            //            System.out.println("Listerner called with newValue: " + newValue);
            //            if (newValue != null) {
            //                DeRecSecret secret =
            //                        (DeRecSecret) State.getInstance().getSharer().getSecrets().stream()
            //                                .filter(s -> newValue.equals(((DeRecSecret) s).getDescription()))
            //                                .findFirst()
            //                                .orElse(null);
            //                if (secret != null) {
            //                    State.getInstance().getUserSelections().setSecret(secret);
            //                    updateVersionsAccordion();
            //                    pairWithHelperButton.setDisable(false);
            //
            //                    createANewVersionButton.setDisable(secret.isRecovering() ? true: false);
            //                    addSecretButton.setDisable(secret.isRecovering() ? true : false);
            //                    System.out.println("Selecting secret " + secret.getDescription() + " isRecovering: " +
            // secret.isRecovering());
            //                    State.getInstance().getUserSelections().setRecovering(secret.isRecovering());
            //                }
            //            }
        });

        //        State.getInstance().helperStatusesForSelectedSecret.addListener((ListChangeListener<? super
        // DeRecHelperStatus>) change -> {
        //            System.out.println("Updating helpers in UI");
        //            updateHelpersInUI();
        //        });
        //        updateHelpersInUI();
        State.getInstance().observableSharerTabHelpersUpdateCounter.addListener((obs, oldVal, newVal) -> {
            System.out.println("-------------------- ************************************************** Updating "
                    + "helper in UI OldVal: " + oldVal + ", newVal: " + newVal);
            updateHelpersInUI();
        });
        updateHelpersInUI();

        State.getInstance().sharerNotifications.addListener((ListChangeListener<? super DeRecStatusNotification>)
                change -> {
                    updateNotificationsInUI();
                    updateHelpersInUI();
                });
        updateNotificationsInUI();

        State.getInstance().observableSharerTabVersionsUpdateCounter.addListener((obs, oldVal, newVal) -> {
            System.out.println("-------------------- ************************************************** Updating "
                    + "version in UI OldVal: " + oldVal + ", newVal: " + newVal);
            updateVersionsAccordion();
        });
        updateVersionsAccordion();

        handleVersions(); // Set the versions tab
    }

    void initLibForSharer(String name, String uri) {
        //        Platform.runLater(() -> { });

        // Create a helper in the library
        //        DeRecSharer sharer = new Sharer(name, uri);
        //        ServiceLoader<SharerFactory> loader = ServiceLoader.load(SharerFactory.class);
        ServiceLoader<SharerFactory> loader =
                ServiceLoader.load(SharerFactory.class, getClass().getClassLoader());

        System.out.println("loader initialized");
        SharerFactory factory = loader.findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + "SharerFactory implementation found"));
        System.out.println("Factory created");
        DeRecSharer sharer = factory.createSharer(name, uri, uri);
        System.out.println("Sharer created");

        sharer.setListener((DeRecStatusNotification notification) -> sharerListener(notification));
        State.getInstance().setSharer(sharer);

        // If a sharer has started in recovery mode, create a dummy recovery secret for them to pair, only if it
        // hasn't already been created.
        if (State.getInstance().getUserSelections().isRecovering()) {

            Optional<? extends DeRecSecret> existingRecoverySecret =
                    State.getInstance().getSharer().getSecrets().stream()
                            .filter(s -> s.getDescription() == recoverySecretName)
                            .findFirst();
            if (!existingRecoverySecret.isPresent()) {
                DeRecSecret secret =
                        (DeRecSecret) State.getInstance().getSharer().newSecret(recoverySecretName, new byte[0], true);
                State.getInstance().getUserSelections().setSecret(secret);
                secretsDropdown.setValue(recoverySecretName);
                secretsDropdownChanged(recoverySecretName);
            }
        }
    }

    void sharerListener(DeRecStatusNotification derecNotification) {
        DeRecStatusNotification notification = derecNotification;

        Platform.runLater(() -> {
            if (derecNotification.getType() == DeRecStatusNotification.StandardNotificationType.RECOVERY_COMPLETE) {
                scheduleShowRecoveryCompleteDialog(derecNotification);
                if (State.getInstance()
                        .getUserSelections()
                        .getSecret()
                        .getSecretId()
                        .equals(derecNotification.getSecret().getSecretId())) {
                    System.out.println("Recovery complete: setting secret.isRecovering to false");
                    State.getInstance().getUserSelections().setRecovering(false);
                    //                    State.getInstance().getUserSelections().getSecret().setRecovering(false);
                }
            }
            State.getInstance().sharerNotifications.add(notification);
        });

        String str = "Notification <type: " + notification.getType() + ", Sev: " + notification.getSeverity() + ">";
        if (notification.getHelper().isPresent()) {
            str += " from " + notification.getHelper().get().getId().getName();
        }
        if (!notification.getMessage().isEmpty()) {
            str += " Message " + notification.getMessage();
        }
        if (notification.getSecret() != null) {
            str += " Secret: " + notification.getSecret().getSecretId().toString();
        }
        if (notification.getVersion().isPresent()) {
            str += " Version: " + notification.getVersion().get().getVersionNumber();
        }

        System.out.println(str);
    }
    //
    //    public void old_updateHelpersInUI() {
    //        helpersAccordion.getPanes().clear();
    //
    //
    //        for (State.DisplayEntry entry : State.getInstance().sharerTabHelpersContents) {
    //            TitledPane pane = new TitledPane();
    //            DeRecHelperStatus helperStatus = (DeRecHelperStatus) entry.associatedObj;
    ////            pane.getStyleClass().add(helperStatus.getStatus() == DeRecPairingStatus.PairingStatus.PAIRED ?
    // "green-header" : "red-header");
    //            if (helperStatus.getStatus() == DeRecPairingStatus.PairingStatus.PAIRED) {
    //                pane.getStyleClass().add("green-header");
    //            } else if (helperStatus.getStatus() == DeRecPairingStatus.PairingStatus.REFUSED) {
    //                pane.getStyleClass().add("yellow-header");
    //            } else {
    //                pane.getStyleClass().add("red-header");
    //            }
    //
    //            pane.setText(entry.title);
    //            try {
    //                FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
    //                VBox cardContentBox = loader.load();
    //                CardBoxController cardController = loader.getController();
    //                cardController.setLabelText(entry.contents);
    //                cardController.setImage("images/trashcan-icon.png");
    //                cardController.getCardButton().setUserData(helperStatus);
    //                cardController.setCardButtonAction(event -> {
    //                    Button sourceButton = (Button) event.getSource();
    //                    DeRecHelperStatus toBeDeleted = (DeRecHelperStatus) sourceButton.getUserData();
    //                    System.out.println("Should unpair helper " + toBeDeleted.getId().getName());
    //                    ArrayList<DeRecIdentity> idList = new ArrayList<>();
    //                    idList.add(toBeDeleted.getId());
    //                    State.getInstance().getUserSelections().getSecret().removeHelpersAsync(idList);
    //                });
    //                pane.setContent(cardContentBox);
    //                helpersAccordion.getPanes().add(pane);
    //            } catch (Exception ex) {
    //                System.out.println("Exception in Card");
    //                ex.printStackTrace();
    //            }
    //        }
    //    }

    public void updateHelpersInUI() {
        Platform.runLater(() -> {
            if (State.getInstance().sharerTabHelpersContents.size()
                    != helpersAccordion.getPanes().size()) {
                helpersAccordion.getPanes().clear();
                for (int i = 0; i < State.getInstance().sharerTabHelpersContents.size(); i++) {
                    TitledPane pane = new TitledPane();
                    helpersAccordion.getPanes().add(pane);
                }
            }

            for (int i = 0; i < State.getInstance().sharerTabHelpersContents.size(); i++) {
                //            TitledPane pane = new TitledPane();
                TitledPane titledPane = helpersAccordion.getPanes().get(i);
                DeRecHelperStatus helperStatus = (DeRecHelperStatus)
                        State.getInstance().sharerTabHelpersContents.get(i).associatedObj;

                //            pane.getStyleClass().add(helperStatus.getStatus() ==
                // DeRecPairingStatus.PairingStatus.PAIRED ? "green-header" : "red-header");
                if (helperStatus.getStatus() == DeRecPairingStatus.PairingStatus.PAIRED) {
                    titledPane.getStyleClass().removeAll("yellow-header", "red-header");
                    titledPane.getStyleClass().add("green-header");
                } else if (helperStatus.getStatus() == DeRecPairingStatus.PairingStatus.REFUSED) {
                    titledPane.getStyleClass().removeAll("green-header", "red-header");
                    titledPane.getStyleClass().add("yellow-header");
                } else {
                    System.out.println("Helper status is: " + helperStatus.getStatus());
                    titledPane.getStyleClass().removeAll("green-header", "yellow-header");
                    titledPane.getStyleClass().add("red-header");
                }

                titledPane.setText(State.getInstance().sharerTabHelpersContents.get(i).title);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
                    VBox cardContentBox = loader.load();
                    CardBoxController cardController = loader.getController();
                    cardController.setLabelText(
                            State.getInstance().sharerTabHelpersContents.get(i).contents);
                    cardController.setImage("images/trashcan-icon.png");
                    cardController.getCardButton().setUserData(helperStatus);
                    cardController.setCardButtonAction(event -> {
                        Button sourceButton = (Button) event.getSource();
                        DeRecHelperStatus toBeDeleted = (DeRecHelperStatus) sourceButton.getUserData();
                        System.out.println(
                                "Should unpair helper " + toBeDeleted.getId().getName());
                        ArrayList<DeRecIdentity> idList = new ArrayList<>();
                        idList.add(toBeDeleted.getId());
                        State.getInstance().getUserSelections().getSecret().removeHelpersAsync(idList);
                    });
                    titledPane.setContent(cardContentBox);
                    //                helpersAccordion.getPanes().add(titledPane);

                    // Refresh the helpers screen, if the user is on the Helpers sub-tab
                    if (State.getInstance().getUserSelections().getSharerSelectedTab()
                            == State.Selections.SharerSelectedTab.Helpers) {
                        middleArea.getChildren().clear();
                        createANewVersionButton.setVisible(false);
                        pairWithHelperButton.setVisible(true);
                        middleArea.getChildren().addAll(helpersAccordion);
                    }

                } catch (Exception ex) {
                    logger.error("Exception in Card", ex);
                }
            }
        });
    }

    public void updateNotificationsInUI() {
        notificationsAccordionScrollPane = new ScrollPane();
        notificationsAccordion.getPanes().clear();
        HBox.setHgrow(notificationsAccordion, Priority.ALWAYS); // Allow accordion to expand horizontally

        notificationsAccordion.getPanes().clear();
        for (DeRecStatusNotification notification : State.getInstance().sharerNotifications) {
            TitledPane pane = new TitledPane();
            pane.setText(notification.getMessage());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
                VBox cardContentBox = loader.load();
                CardBoxController cardController = loader.getController();
                String labelText =
                        "Type: " + notification.getType() + "\n" + "Severity: " + notification.getSeverity() + "\n";
                if (!notification.getMessage().isEmpty()) {
                    labelText += " Message " + notification.getMessage() + "\n";
                }
                if (notification.getHelper().isPresent()) {
                    labelText +=
                            " Helper: " + notification.getHelper().get().getId().getName() + "\n";
                }
                if (notification.getSecret() != null) {
                    labelText +=
                            " Secret: " + notification.getSecret().getSecretId().toString() + "\n";
                }
                if (notification.getVersion().isPresent()) {
                    labelText += " Version: " + notification.getVersion().get().getVersionNumber() + "\n";
                }
                cardController.setLabelText(labelText);
                cardController.setImage("images/trashcan-icon.png");
                cardController.getCardButton().setUserData(notification);
                cardController.setCardButtonAction(event -> {
                    Button sourceButton = (Button) event.getSource();
                    DeRecStatusNotification toBeDeleted = (DeRecStatusNotification) sourceButton.getUserData();
                    System.out.println("Deleting delete notification " + toBeDeleted.getMessage());
                    State.getInstance().sharerNotifications.remove(toBeDeleted);
                });
                pane.setContent(cardContentBox);
                notificationsAccordion.getPanes().add(pane);
                notificationsAccordionScrollPane.setContent(notificationsAccordion);
            } catch (Exception ex) {
                System.out.println("Exception in Card");
                ex.printStackTrace();
            }
        }
        notificationsAccordionScrollPane.setFitToWidth(true); // Ensure accordion fits the width of the scroll pane
    }

    private void secretsListUpdated() {
        ObservableList<String> descriptions = FXCollections.observableArrayList(State.getInstance().secrets.stream()
                .map(DeRecSecret::getDescription) // Assuming getDescription returns a String
                .collect(Collectors.toList()));

        secretsDropdown.setItems(descriptions);

        if (State.getInstance().getUserSelections().getSecret() != null) {
            createANewVersionButton.setDisable(
                    State.getInstance().getUserSelections().getSecret().isRecovering() ? true : false);
            addSecretButton.setDisable(
                    State.getInstance().getUserSelections().getSecret().isRecovering() ? true : false);
        }
    }

    @FXML
    private void handleAddSecret() {
        handleAddOrEditSecret(false, "", "");
    }

    @FXML
    void handleEditSecret() {
        DeRecSecret secret = State.getInstance().getUserSelections().getSecret();
        if (!secret.getVersions().isEmpty()) {
            DeRecVersion latestVersion =
                    (DeRecVersion) secret.getVersions().lastEntry().getValue();
            handleAddOrEditSecret(
                    true,
                    secret.getDescription(),
                    new String(latestVersion.getProtectedValue(), StandardCharsets.UTF_8));
        } else {
            handleAddOrEditSecret(true, secret.getDescription(), "");
        }
    }

    @FXML
    private void handleAddOrEditSecret(boolean isEdit, String description, String data) {
        System.out.println("in  handleAddSecret");
        // Open dialog to add a secret
        try {
            // Load the FXML file for the edit secret dialog
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/org/derecalliance/derec/demo/edit-secret-dialog.fxml"));
            VBox dialogContent = loader.load();
            EditSecretDialogController controller = loader.getController();

            // Create a dialog and set the content
            Dialog<SecretDataInput> dialog = new Dialog<>();
            if (isEdit) {
                dialog.setTitle("Edit Secret");
                //                controller.getNormalModeLabel().setVisible(false);
                //                controller.getRecoveryModeToggleSwitch().setVisible(false);
                //                controller.getRecoveryModeLabel().setVisible(false);
                controller.getSecretNameTextField().setVisible(false);
                controller.getSecretNameLabel().setVisible(true);
                controller.getSecretNameLabel().setText(description);
                controller.getSecretDataField().setText(data);
            } else {
                dialog.setTitle("Add Secret");
                //                controller.getNormalModeLabel().setVisible(true);
                //                controller.getRecoveryModeToggleSwitch().setVisible(true);
                //                controller.getRecoveryModeLabel().setVisible(true);
                controller.getSecretNameTextField().setVisible(true);
                controller.getSecretNameLabel().setVisible(false);
            }
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setPrefWidth(MainApp.primaryStage.getWidth() * 0.8);
            // Define how the dialog result is processed
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String name = controller.getSecretNameLabel().textProperty().get();
                    if (name == null || name.length() == 0) {
                        name = controller
                                .getSecretNameTextField()
                                .textProperty()
                                .get();
                    }
                    System.out.println("-----after label/textfield, debug " + "name is: " + name + " and data is: "
                            + controller.getSecretDataField().textProperty().get());

                    return new SecretDataInput(
                            name, controller.getSecretDataField().textProperty().get());
                }
                return null;
            });

            dialog.setDialogPane(dialogPane);
            dialog.initOwner(MainApp.primaryStage);

            // Show the dialog and wait for the user to close it
            Optional<SecretDataInput> result = dialog.showAndWait();
            result.ifPresent(secretDataInput -> {
                // Handle the secret data entered by the user
                try {
                    if (isEdit) {
                        System.out.println("Adding a new version to the secret");
                        DeRecSecret secret =
                                State.getInstance().getUserSelections().getSecret();
                        secret.updateAsync(controller
                                .getSecretDataField()
                                .textProperty()
                                .get()
                                .getBytes(StandardCharsets.UTF_8));
                        //                        System.out.println("Now, versions are: " + secret.debugStr());
                    } else {
                        DeRecSecret secret = (DeRecSecret) State.getInstance()
                                .getSharer()
                                .newSecret(
                                        secretDataInput.name, secretDataInput.stringToProtect.getBytes("UTF-8"), false);
                        secretsDropdown.setValue(secretDataInput.name);
                        secretsDropdownChanged(secretDataInput.name);
                        System.out.println("After adding secret to lib");
                        //                        System.out.println("Now, versions are: " + secret.debugStr());

                        if (secretsDropdown.getValue() == null) {
                            System.out.println(
                                    "Setting the value of the combobox: isRecovering = " + (secret.isRecovering()));
                            State.getInstance().getUserSelections().setRecovering(secret.isRecovering());
                            secretsDropdown.setValue(secret.getDescription());
                            secretsDropdownChanged(secret.getDescription());
                        }
                    }
                    updateVersionsAccordion();
                    handleVersions();

                } catch (Exception ex) {
                    System.err.println("Exception in adding a secret");
                    ex.printStackTrace();
                }
            });

            // If you need to do something after the dialog is closed, handle it here
            // For example, updating UI or processing input data

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInitialMessage(VBox middleArea, String labelText) {
        StackPane emptyPane = new StackPane();
        StackPane.setAlignment(emptyPane, Pos.CENTER);
        Region fillerAbove = new Region();
        Region fillerBelow = new Region();
        VBox.setVgrow(fillerAbove, Priority.ALWAYS);
        VBox.setVgrow(fillerBelow, Priority.ALWAYS);
        Label noSecretLabel = new Label(labelText);
        noSecretLabel.setFont(new Font(20));
        emptyPane.getChildren().add(noSecretLabel);
        middleArea.getChildren().addAll(fillerAbove, emptyPane, fillerBelow);
    }

    @FXML
    private void handleVersions() {
        State.getInstance().getUserSelections().setSharerSelectedTab(State.Selections.SharerSelectedTab.Versions);
        middleArea.getChildren().clear();
        createANewVersionButton.setVisible(true);
        pairWithHelperButton.setVisible(false);
        if (State.getInstance().getUserSelections().getSecret() == null) {
            //            StackPane noSecretPane = new StackPane();
            //            StackPane.setAlignment(noSecretPane, Pos.CENTER);
            //            Region fillerAbove = new Region();
            //            Region fillerBelow = new Region();
            //            VBox.setVgrow(fillerAbove, Priority.ALWAYS);
            //            VBox.setVgrow(fillerBelow, Priority.ALWAYS);
            //
            //            Label noSecretLabel = new Label("You have not created a secret.\n\nPlease create a secret to
            // start.");
            //            noSecretLabel.setFont(new Font(20));
            //            noSecretPane.getChildren().add(noSecretLabel);
            //
            //            middleArea.getChildren().addAll(fillerAbove, noSecretPane, fillerBelow);
            showInitialMessage(middleArea, "You have not created a secret.\n\nPlease create a secret to start.");
        } else {
            middleArea.getChildren().addAll(versionsAccordion);
        }
    }

    @FXML
    private void handleHelpers() {
        State.getInstance().getUserSelections().setSharerSelectedTab(State.Selections.SharerSelectedTab.Helpers);
        middleArea.getChildren().clear();
        createANewVersionButton.setVisible(false);
        pairWithHelperButton.setVisible(true);

        System.out.println("In handleHelpers HelperStatuses:");
        for (DeRecHelperStatus helperStatus : State.getInstance().helperStatusesForSelectedSecret) {
            System.out.println(helperStatus.toString());
        }
        System.out.println("----");

        updateHelpersInUI();
        DeRecSecret secret = State.getInstance().getUserSelections().getSecret();
        if (secret == null) {
            showInitialMessage(middleArea, "You have not created a secret.\n\nPlease create a secret to start.");
        } else if (secret != null && secret.getHelperStatuses().isEmpty()) {
            showInitialMessage(middleArea, "You have no helpers yet.\n\nPlease pair with some helpers.");
        } else {
            middleArea.getChildren().addAll(helpersAccordion);
        }
    }

    @FXML
    private void handleNotificationTab() {
        middleArea.getChildren().clear();
        createANewVersionButton.setVisible(false);
        pairWithHelperButton.setVisible(false);
        middleArea.getChildren().addAll(notificationsAccordionScrollPane);
    }

    @FXML
    private void showPairWithHelperDialog() {
        QRCode qrcode = new QRCode();
        qrcode.createHelperPane(scannedContact -> {
            if (scannedContact != null) {
                NameAndContactInfo nameAndContactInfo = showGetNameAndContactInfoDialog();

                DeRecSecret secret = State.getInstance().getUserSelections().getSecret();
                DeRecIdentity helperId = new DeRecIdentity(
                        nameAndContactInfo.name,
                        nameAndContactInfo.contactInfo,
                        scannedContact.getTransportUri(),
                        scannedContact.getPublicEncryptionKeyId(),
                        scannedContact.getPublicEncryptionKey(),
                        null);
                logger.debug("Created DeRecIdentity: " + helperId);

                logger.debug("showPairWithHelperDialog creating contact with helperId: " + helperId.toString());
                ArrayList<DeRecIdentity> helperIdList = new ArrayList<DeRecIdentity>();
                helperIdList.add(helperId);
                secret.addHelpersAsync(helperIdList);
                // Refresh the helpers screen
                handleHelpers();
            } else {
                System.out.println("Capture result: is NULL");
            }
        });
    }

    private NameAndContactInfo showGetNameAndContactInfoDialog() {
        // Create the custom dialog
        Dialog<NameAndContactInfo> dialog = new Dialog<>();
        dialog.setTitle("Contact Info");
        dialog.setHeaderText("Enter the details for the person whose QR code you just scanned");

        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(MainApp.primaryStage.getWidth() * 0.8);

        // Create the labels and fields for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Info");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Info:"), 0, 1);
        grid.add(contactField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new NameAndContactInfo(nameField.getText(), contactField.getText());
            }
            return null;
        });

        dialog.initOwner(MainApp.primaryStage);
        Optional<NameAndContactInfo> result = dialog.showAndWait();
        return result.orElse(null);
    }

    //    @FXML
    //    private void createANewVersionButtonClicked() {
    //        System.out.println("createANewVersionButtonClicked");
    //    }

    //    public void old_updateVersionsAccordion() {
    //        versionsAccordion.getPanes().clear();
    //        DeRecSecret secret = State.getInstance().getUserSelections().getSecret();
    //        Collection<DeRecVersion> versions = (Collection<DeRecVersion>) secret.getVersions().values();
    //        for (DeRecVersion version : versions) {
    //            TitledPane pane = new TitledPane();
    //            pane.getStyleClass().add(version.isProtected() ? "green-header" : "red-header");
    //            pane.setText("Version: " + version.getVersionNumber() + "\n");
    //
    //            try {
    //                FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
    //                VBox contentBox = loader.load();
    //                CardBoxController cardController = loader.getController();
    //                cardController.setLabelText("Protected Data: " + new String(version.getProtectedValue(),
    // StandardCharsets.UTF_8) +
    //                        "\nProtected: " + (version.isProtected() ? "Yes" : "No")
    //                );
    //                cardController.setImage("images/trashcan-icon.png");
    //                cardController.getCardButton().setUserData(version);
    //                cardController.setCardButtonAction(event -> {
    //                    Button sourceButton = (Button) event.getSource();
    //                    DeRecVersion associatedVersion = (DeRecVersion) sourceButton.getUserData();
    //                    System.out.println("Button clicked for version: " + associatedVersion.getVersionNumber());
    //                    System.out.println("Delete is not implemented in the library API");
    //                });
    //
    //                pane.setContent(contentBox);
    //                versionsAccordion.getPanes().add(pane);
    //            } catch (Exception ex) {
    //                System.out.println("Exception in Card");
    //                ex.printStackTrace();
    //            }
    //
    //        }
    //    }
    public void updateVersionsAccordion() {
        versionsAccordion.getPanes().clear();
        //        DeRecSecret secret = State.getInstance().getUserSelections().getSecret();
        for (State.DisplayEntry entry : State.getInstance().sharerTabVersionsContents) {
            TitledPane pane = new TitledPane();
            DeRecVersion version = (DeRecVersion) entry.associatedObj;
            pane.getStyleClass().add(version.isProtected() ? "green-header" : "red-header");
            pane.setText(entry.title);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
                VBox contentBox = loader.load();
                CardBoxController cardController = loader.getController();
                cardController.setLabelText(entry.contents);
                cardController.setImage("images/trashcan-icon.png");
                cardController.getCardButton().setUserData(version);
                cardController.setCardButtonAction(event -> {
                    Button sourceButton = (Button) event.getSource();
                    DeRecVersion associatedVersion = (DeRecVersion) sourceButton.getUserData();
                    System.out.println("Button clicked for version: " + associatedVersion.getVersionNumber());
                    System.out.println("Delete is not implemented in the library API");
                });

                pane.setContent(contentBox);
                versionsAccordion.getPanes().add(pane);
            } catch (Exception ex) {
                System.out.println("Exception in Card");
                ex.printStackTrace();
            }
        }
    }

    public void secretsDropdownChanged(String newValue) {
        System.out.println("Listerner called with newValue: " + newValue);
        if (newValue != null) {
            DeRecSecret secret = (DeRecSecret) State.getInstance().getSharer().getSecrets().stream()
                    .filter(s -> newValue.equals(((DeRecSecret) s).getDescription()))
                    .findFirst()
                    .orElse(null);
            if (secret != null) {
                State.getInstance().getUserSelections().setSecret(secret);
                updateVersionsAccordion();
                pairWithHelperButton.setDisable(false);

                createANewVersionButton.setDisable(secret.isRecovering() ? true : false);
                addSecretButton.setDisable(secret.isRecovering() ? true : false);
                System.out.println(
                        "Selecting secret " + secret.getDescription() + " isRecovering: " + secret.isRecovering());
                State.getInstance().getUserSelections().setRecovering(secret.isRecovering());
            }
        }
    }

    private void scheduleShowRecoveryCompleteDialog(DeRecStatusNotification recoveryNotification) {
        recoveryCompleteNotifications.add(recoveryNotification);

        if (isShowRecoveryCompleteScheduled) {
            showRecoveryCompleteTimeline.stop();
        } else {
            isShowRecoveryCompleteScheduled = true;
        }

        showRecoveryCompleteTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            Platform.runLater(() -> {
                // Your code to run after 3 seconds
                System.out.println("Executed after 3 seconds");
                System.out.println(recoveryCompleteNotifications);
                StringBuilder recoveredSecretsVersionsListString = new StringBuilder();
                for (DeRecStatusNotification notif : recoveryCompleteNotifications) {
                    if (notif.getVersion().isPresent()) {
                        DeRecVersion v = notif.getVersion().get();
                        recoveredSecretsVersionsListString.append("Secret: "
                                + notif.getSecret().getDescription() + ", " + "version " + v.getVersionNumber() + "\n");
                    }
                }

                if (!recoveredSecretsVersionsListString.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.NONE);
                    alert.setTitle("Recovery Confirmation");
                    alert.setHeaderText("Exit recovery mode?");
                    alert.setContentText("We have successfully recovered the following secrets:\n\n"
                            + recoveredSecretsVersionsListString + "\nDo you want to exit recovery mode?");
                    alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    alert.initOwner(MainApp.primaryStage);

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // delete the recovering secret
                            // switch view to the first recovered secret from recoveredSecretsVersionsList
                            System.out.println("About to close recoverysecret");
                            Optional<? extends DeRecSecret> recoverySecret =
                                    State.getInstance().getSharer().getSecrets().stream()
                                            .filter(s -> s.getDescription().equals(recoverySecretName))
                                            .findFirst();
                            if (recoverySecret.isPresent()) {
                                State.getInstance()
                                        .getSharer()
                                        .recoveryComplete(recoverySecret.get().getSecretId());

                                Optional<? extends DeRecSecret> secretToSelect =
                                        State.getInstance().getSharer().getSecrets().stream()
                                                .findFirst();
                                if (secretToSelect.isPresent()) {
                                    secretsDropdown.setValue(
                                            secretToSelect.get().getDescription());
                                    secretsDropdownChanged(secretToSelect.get().getDescription());
                                }
                            }

                            //                            Optional<? extends DeRecSecret> secretToSelect =
                            //
                            // State.getInstance().getSharer().getSecrets().stream().filter(s ->
                            // !s.getDescription().equals(recoverySecretName)).findFirst();
                            //                            if (recoverySecret.isPresent() && secretToSelect.isPresent())
                            // {
                            //                                System.out.println("Selecting secret " +
                            // secretToSelect.get().getDescription());
                            //
                            // secretsDropdown.setValue(secretToSelect.get().getDescription());
                            //
                            // secretsDropdownChanged(secretToSelect.get().getDescription());
                            //
                            //                                // Delete the recovery secret
                            //                                System.out.println("Closing secret " +
                            // recoverySecret.get().getDescription());
                            //                                recoverySecret.get().close();
                            //
                            //                            }
                        }
                    });
                }
                recoveryCompleteNotifications = new ArrayList<>();
                isShowRecoveryCompleteScheduled = false;
            });
        }));
        showRecoveryCompleteTimeline.setCycleCount(1);
        showRecoveryCompleteTimeline.play();
    }
}
