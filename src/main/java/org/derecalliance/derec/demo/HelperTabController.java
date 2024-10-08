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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.derecalliance.derec.demo.state.State;
import org.derecalliance.derec.lib.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelperTabController {
    @FXML
    private Button pairWithSharerButton;

    @FXML
    private VBox helperTabMiddleArea;

    ScrollPane notificationsAccordionScrollPane;
    Accordion sharersAccordion = new Accordion();
    Accordion notificationsAccordion = new Accordion();
    Map<DeRecHelper.Notification.StandardHelperNotificationType, String> notifText = Map.of(
            DeRecHelper.Notification.StandardHelperNotificationType.PAIR_INDICATION, "Paired with",
            DeRecHelper.Notification.StandardHelperNotificationType.PAIR_INDICATION_RECOVERY,
                    "Paired for recovery with",
            DeRecHelper.Notification.StandardHelperNotificationType.UNPAIR_INDICATION, "Unpaired with",
            DeRecHelper.Notification.StandardHelperNotificationType.UPDATE_INDICATION, "Received share from",
            DeRecHelper.Notification.StandardHelperNotificationType.VERIFY_INDICATION, "Received verification from",
            DeRecHelper.Notification.StandardHelperNotificationType.LIST_SECRETS_INDICATION, "List secrets req from",
            DeRecHelper.Notification.StandardHelperNotificationType.RECOVER_SECRET_INDICATION,
                    "Sent share for " + "recovery to");

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @FXML
    private void initialize() {
        // Initialize the tab if needed
        initLibForHelper();
        initTestData();
        State.getInstance()
                .sharerStatuses
                .addListener((ListChangeListener.Change<? extends DeRecHelper.SharerStatus> change) -> {
                    System.out.println("Updating sharerStatuses");
                    updateSharersInUI();
                });
        State.getInstance().stateShares.addListener((ListChangeListener.Change<? extends DeRecHelper.Share> change) -> {
            System.out.println("Updating sharers");
            updateSharersInUI();
        });
        updateSharersInUI();

        State.getInstance().helperNotifications.addListener((ListChangeListener<? super DeRecHelper.Notification>)
                change -> {
                    updateNotificationsInUI();
                });
        updateNotificationsInUI();

        // since the Sharer is the default tab, simulate a click on it
        handleSharers();
    }

    void initLibForHelper() {
        // Create a helper in the library
        ServiceLoader<HelperFactory> loader = ServiceLoader.load(HelperFactory.class);
        HelperFactory factory = loader.findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + "HelperFactory implementation found"));
        DeRecHelper helper = factory.createHelper(
                State.getInstance().getUserSelections().getName(),
                State.getInstance().getUserSelections().getUri(),
                State.getInstance().getUserSelections().getUri());

        helper.setListener(c -> {
            return helperListener((DeRecHelper.Notification) c);
        });

        State.getInstance().setHelper(helper);
        logger.debug("Created helper with public encryption key"
                + State.getInstance().getHelper().getPublicEncryptionKey());
        logger.debug("Created helper with private encryption key"
                + State.getInstance().getHelper().getPrivateEncryptionKey());
        logger.debug("Created helper with public signature key"
                + State.getInstance().getHelper().getPublicSignatureKey());
        logger.debug("Created helper with private signature key"
                + State.getInstance().getHelper().getPrivateSignatureKey());
    }

    public DeRecHelper.NotificationResponse helperListener(DeRecHelper.Notification derecNotification) {
        System.out.println("In helper listener");

        DeRecHelper.Notification notification = derecNotification;

        Platform.runLater(() -> {
            State.getInstance().helperNotifications.add(notification);
        });

        if (derecNotification.getType()
                == DeRecHelper.Notification.StandardHelperNotificationType.PAIR_INDICATION_RECOVERY) {
            System.out.println("SHARER "
                    + (derecNotification.getSharerId().getName() != null
                            ? "unknown"
                            : derecNotification.getSharerId().getName())
                    + " is trying to recover");

            System.out.println("about to call askUserToReconcileIdentities(" + derecNotification.getType() + ")");
            CompletableFuture<List<DeRecHelper.SharerStatus>> resultFuture =
                    askUserToReconcileIdentities(derecNotification);
            System.out.println("Got out of askUserToReconcileIdentities");
            System.out.println(resultFuture);

            List<DeRecHelper.SharerStatus> userRespSharerStatuses = null;
            try {
                System.out.println("waiting for the derecidentity future to complete");
                userRespSharerStatuses = resultFuture.get();
            } catch (Exception ex) {
                System.out.println("Exception in getting result from the future");
                ex.printStackTrace();
            }

            System.out.println("Got the response from the user after completing the future: " + userRespSharerStatuses);
            DeRecHelper.NotificationResponse response = State.getInstance()
                    .getHelper()
                    .newNotificationResponse(true, "dummy response", userRespSharerStatuses);
            System.out.println("Constructed the response:");
            return response;
        } else {
            System.out.println("notification types did not match");
        }

        String str = "Helper Notification <type: " + notification.getType() + ">";
        if (notification.getSharerId() != null) {
            str += " from " + notification.getSharerId().getName();
        }
        if (notification.getVersion() != -1) {
            str += " Version # " + notification.getVersion();
        }
        if (notification.getSecretId() != null) {
            str += " Secret: " + notification.getSecretId().toString();
        }

        System.out.println(str);

        DeRecHelper.NotificationResponse response =
                State.getInstance().getHelper().newNotificationResponse(true, "dummy response", null);
        return response;
    }

    public CompletableFuture<List<DeRecHelper.SharerStatus>> askUserToReconcileIdentities(
            DeRecHelper.Notification derecNotification) {
        CompletableFuture<List<DeRecHelper.SharerStatus>> resultFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            List<DeRecHelper.SharerStatus> originalSharerStatuses = getOriginalIdentity(derecNotification);
            System.out.println("getOriginalIdentity responded with " + originalSharerStatuses + ", Completing future");
            resultFuture.complete(originalSharerStatuses);
        });
        return resultFuture;
    }

    public void updateSharersInUI() {
        HashMap<DeRecHelper.SharerStatus, HashMap<DeRecSecret.Id, ArrayList<DeRecHelper.Share>>> sharerSharesMap =
                new HashMap<>();

        sharersAccordion.getPanes().clear();

        if (State.getInstance().sharerStatuses.isEmpty()) {
            System.out.println("sharerStatuses is empty - returning");
            return;
        }

        System.out.println("In updateSharersInUI, there are "
                + State.getInstance().stateShares.size() + " shares");
        for (DeRecHelper.Share ss : State.getInstance().stateShares) {
            System.out.println("Sharer: " + ss.getSharer() + "secret: "
                    + Base64.getEncoder().encodeToString(ss.getSecretId().getBytes())
                    + " versions: "
                    + ss.getVersions());
        }

        // Populate sharerSharesMap (SharerStatus -> SecretId -> Shares)
        for (DeRecHelper.Share derecShare : State.getInstance().stateShares) {
            DeRecHelper.Share share = derecShare;
            System.out.println("IN share: sharer is: "
                    + share.getSharer().getId().getName() + " Sharer-Obj: " + share.getSharer() + "secret: "
                    + Base64.getEncoder().encodeToString(share.getSecretId().getBytes())
                    + " versions: "
                    + share.getVersions());
            if (!sharerSharesMap.containsKey(share.getSharer())) {
                sharerSharesMap.put(share.getSharer(), new HashMap<>());
            }
            if (!sharerSharesMap.get(share.getSharer()).containsKey(share.getSecretId())) {
                sharerSharesMap.get(share.getSharer()).put(share.getSecretId(), new ArrayList<>());
            }
            sharerSharesMap.get(share.getSharer()).get(share.getSecretId()).add(share);
        }
        System.out.println("sharerSharesMap");
        for (DeRecHelper.SharerStatus sharerStatus : sharerSharesMap.keySet()) {
            System.out.println("  Sharer: " + sharerStatus.getId().getName() + ", sharerStatusObj: " + sharerStatus);
            for (DeRecSecret.Id secretId : sharerSharesMap.get(sharerStatus).keySet()) {
                System.out.println("     SecretId: " + secretId + " count: "
                        + sharerSharesMap.get(sharerStatus).get(secretId).size());
            }
        }

        // Populate secretsPerSharer (Sharer -> count of secrets)
        HashMap<DeRecHelper.SharerStatus, Integer> secretsPerSharer = new HashMap<>();
        for (DeRecHelper.SharerStatus sharerStatus : State.getInstance().sharerStatuses) {
            if (!secretsPerSharer.containsKey(sharerStatus)) {
                secretsPerSharer.put(sharerStatus, 0);
            }

            secretsPerSharer.put(sharerStatus, secretsPerSharer.get(sharerStatus) + 1);
        }
        System.out.println("Secrets Per Sharer");
        for (DeRecHelper.SharerStatus sharerStatus : secretsPerSharer.keySet()) {
            System.out.println(
                    "  Sharer: " + sharerStatus.getId().getName() + " Count: " + secretsPerSharer.get(sharerStatus));
        }

        for (DeRecHelper.SharerStatus sharerStatus : secretsPerSharer.keySet()) {
            // If the sharer is recovering, don't show them since it just shows an empty pane that doesn't have any
            // shares, and we don't know when to remove this when the recovery is complete
            DeRecHelper.SharerStatus ss = sharerStatus;
            System.out.println("secretsPerSharer processing sharer: " + ss);
            if (ss.isRecovering()) {
                System.out.println("SharerStatus is recovering = continuing" + "Name: "
                        + sharerStatus.getId().getName() + " : " + sharerStatus);
                continue;
            }

            int knownSecretsCount = sharerSharesMap.containsKey(sharerStatus)
                    ? sharerSharesMap.get(sharerStatus).keySet().size()
                    : 0;
            try {
                System.out.println("knownSecretsCount = " + knownSecretsCount + " for sharerStatus " + sharerStatus
                        + " " + "sharerSharesMap keyset "
                        + sharerSharesMap.get(sharerStatus).keySet());
            } catch (Exception ex) {
            }
            if (knownSecretsCount > 0) {
                for (DeRecSecret.Id secretId : sharerSharesMap.get(sharerStatus).keySet()) {
                    TitledPane pane = new TitledPane();
                    pane.setText(sharerStatus.getId().getName() + " SecretId: " + secretId);

                    ScrollPane contentScrollPane = new ScrollPane();
                    contentScrollPane.setFitToWidth(true);
                    VBox contentBox = new VBox();

                    for (DeRecHelper.Share share : State.getInstance().stateShares.stream()
                            .filter(s -> s.getSharer()
                                    .getId()
                                    .getPublicEncryptionKey()
                                    .equals(sharerStatus.getId().getPublicEncryptionKey()))
                            .filter(s -> s.getSecretId().equals(secretId))
                            .toList()) {
                        VBox box = new VBox();
                        box.setSpacing(5);
                        box.setStyle("-fx-border-color: black; -fx-padding: 10px; -fx-background-color: lightgrey;");
                        VBox.setMargin(box, new Insets(10, 10, 10, 10));

                        if (share.getSharer()
                                        .getId()
                                        .getPublicEncryptionKey()
                                        .compareTo(sharerStatus.getId().getPublicEncryptionKey())
                                == 0) {
                            List<Integer> versions = share.getVersions();
                            if (versions.size() > 0) {
                                HBox hbox = new HBox();
                                VBox inner = new VBox();
                                Label l1 = new Label("Secret id: " + share.getSecretId());
                                Label l2 = new Label("");
                                Label l3 = new Label(" Versions: " + versions.get(0));
                                Label l4 = new Label("");
                                inner.getChildren().addAll(l1, l2, l3, l4);

                                Region filler = new Region();
                                HBox.setHgrow(filler, Priority.ALWAYS);

                                Button deleteButton = new Button();
                                ImageView delbuttonView = new ImageView(
                                        new Image(getClass().getResourceAsStream("images/trashcan-icon.png")));
                                delbuttonView.setFitWidth(20);
                                delbuttonView.setFitHeight(20);
                                deleteButton.setGraphic(delbuttonView);
                                deleteButton.setStyle("-fx-background-radius: 10px; ");
                                {
                                    deleteButton.setOnAction(e -> {
                                        State.getInstance()
                                                .getHelper()
                                                .removeVersion(sharerStatus, share.getSecretId(), versions.get(0));
                                    });
                                }
                                hbox.getChildren().addAll(inner, filler, deleteButton);
                                box.getChildren().add(hbox);
                            }
                        }
                        contentBox.getChildren().add(box);
                    }
                    contentScrollPane.setContent(contentBox);
                    pane.setContent(contentScrollPane);
                    sharersAccordion.getPanes().add(pane);
                }
            } else {
                System.out.println("Not adding sharer status to screen " + sharerStatus);
            }

            int panesToBeCreated = secretsPerSharer.get(sharerStatus) - knownSecretsCount;
            for (int i = 0; i < panesToBeCreated; i++) {
                TitledPane pane = new TitledPane();
                pane.setText(sharerStatus.getId().getName() + " SecretId: unknown");
                ScrollPane contentScrollPane = new ScrollPane();
                contentScrollPane.setFitToWidth(true);
                VBox contentBox = new VBox();

                contentScrollPane.setContent(contentBox);
                pane.setContent(contentScrollPane);
                sharersAccordion.getPanes().add(pane);
            }
        }
    }

    public void updateNotificationsInUI() {
        notificationsAccordionScrollPane = new ScrollPane();
        notificationsAccordion.getPanes().clear();
        HBox.setHgrow(notificationsAccordion, Priority.ALWAYS);

        for (DeRecHelper.Notification notification : State.getInstance().helperNotifications) {
            TitledPane pane = new TitledPane();

            String str = "Notification <type: " + notification.getType() + ">";

            if (notification.getSharerId() != null) {
                str += " from " + notification.getSharerId().getName();
            }
            if (notification.getVersion() != -1) {
                str += " Version # " + notification.getVersion();
            }
            if (notification.getSecretId() != null) {
                str += " Secret: " + notification.getSecretId().toString();
            }

            pane.setText((notifText.containsKey(notification.getType())
                            ? notifText.get(notification.getType())
                            : "Unknown notification from")
                    + " " + notification.getSharerId().getName());

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("card-box.fxml"));
                VBox cardContentBox = loader.load();
                CardBoxController cardController = loader.getController();
                String labelText = "Type: " + notification.getType().name() + "\n";

                if (notification.getSharerId() != null) {
                    labelText += " from " + notification.getSharerId().getName() + "\n";
                }
                if (notification.getSecretId() != null) {
                    labelText += " Secret: " + notification.getSecretId().toString() + "\n";
                }
                if (notification.getVersion() != -1) {
                    labelText += " Version # " + notification.getVersion() + "\n";
                }
                cardController.setLabelText(labelText);
                cardController.setImage("images/trashcan-icon.png");
                cardController.getCardButton().setUserData(notification);
                cardController.setCardButtonAction(event -> {
                    Button sourceButton = (Button) event.getSource();
                    DeRecHelper.Notification toBeDeleted = (DeRecHelper.Notification) sourceButton.getUserData();
                    System.out.println("Deleting delete notification " + toBeDeleted.getType());
                    State.getInstance().helperNotifications.remove(toBeDeleted);
                });
                pane.setContent(cardContentBox);
                notificationsAccordion.getPanes().add(pane);
                notificationsAccordionScrollPane.setContent(notificationsAccordion);
            } catch (Exception ex) {
                System.out.println("Exception in Card");
                ex.printStackTrace();
            }
        }
        notificationsAccordionScrollPane.setFitToWidth(true);
    }

    private List<DeRecHelper.SharerStatus> getOriginalIdentity(DeRecHelper.Notification deRecNotification) {
        System.out.println("in getOriginalIdentity, derecNotification = " + deRecNotification.getType());
        Dialog<List<DeRecHelper.SharerStatus>> dialog = new Dialog<>();
        dialog.setTitle("Select Original Sharer");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10, 10, 10, 10));

        Label instructions = new Label("Looks like you are pairing with somebody to help them\nrecover their secrets.\n"
                + "From the list below, select all the names that you are\n"
                + "helping to recover, so they can recover all their secrets\n");
        dialogContent.getChildren().add(instructions);

        ArrayList<String> addedPublicEncryptionKeys = new ArrayList<>();
        List<Pair<CheckBox, DeRecHelper.SharerStatus>> checkBoxes = new ArrayList<>();

        for (DeRecHelper.SharerStatus ss : State.getInstance().sharerStatuses) {
            if (ss.isRecovering()
                    || addedPublicEncryptionKeys.contains(ss.getId().getPublicEncryptionKey())) {
                continue;
            }
            addedPublicEncryptionKeys.add(ss.getId().getPublicEncryptionKey());

            CheckBox checkBox = new CheckBox(ss.getId().getName());
            checkBoxes.add(new Pair<>(checkBox, ss));
            dialogContent.getChildren().add(checkBox);
        }

        dialog.getDialogPane().setContent(dialogContent);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Handle user selection
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return checkBoxes.stream()
                        .filter(pair -> pair.getKey().isSelected())
                        .map(Pair::getValue)
                        .collect(Collectors.toList());
            }
            return null; // User canceled or no selection
        });

        dialog.initOwner(MainApp.primaryStage);

        // Show the dialog
        Optional<List<DeRecHelper.SharerStatus>> result = dialog.showAndWait();
        return result.orElse(new ArrayList<>());
    }

    @FXML
    private void handleSharers() {
        helperTabMiddleArea.getChildren().clear();
        pairWithSharerButton.setVisible(true);
        helperTabMiddleArea.getChildren().addAll(sharersAccordion);
        updateSharersInUI();
        System.out.println("State sharerStatuses");
        for (DeRecHelper.SharerStatus sharerStatus : State.getInstance().sharerStatuses) {
            System.out.println("  Sharer: " + sharerStatus.getId().getName() + " Obj: " + sharerStatus);
        }
    }

    @FXML
    private void handleNotificationTab() {
        helperTabMiddleArea.getChildren().clear();
        pairWithSharerButton.setVisible(false);
        helperTabMiddleArea.getChildren().addAll(notificationsAccordionScrollPane);
    }

    @FXML
    private void handleAddSharer() {
        System.out.println("In handleAddSharer");

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pair with a Sharer");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10, 10, 10, 10));

        Label instructions = new Label("Please scan this code on the sharer's app");

        ImageView qrCodeView = new ImageView();
        // Assume generateQRCodeImage returns an Image object containing the QR code
        qrCodeView.setImage(new QRCode().qrCodeForContact());

        dialogContent.getChildren().addAll(instructions, qrCodeView);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(dialogContent);
        dialogPane.getButtonTypes().addAll(ButtonType.FINISH);

        State.getInstance().sharerStatuses.addListener((ListChangeListener<? super DeRecHelper.SharerStatus>)
                change -> {
                    System.out.println("In listener for sharerstatus change");
                    while (change.next()) {
                        System.out.printf("in next");
                        if (change.wasAdded()) {
                            System.out.println("Change was added");
                            dialog.close();
                            break;
                        }
                    }
                });

        dialog.initOwner(MainApp.primaryStage);
        Optional<String> result = dialog.showAndWait();
    }

    private void initTestData() {}
}
