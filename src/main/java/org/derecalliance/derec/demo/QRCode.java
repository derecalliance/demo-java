package org.derecalliance.derec.demo;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.derecalliance.derec.lib.api.ContactFactory;
import org.derecalliance.derec.lib.api.DeRecContact;
import org.derecalliance.derec.demo.state.State;
import org.derecalliance.derec.lib.api.DeRecSharer;
import org.derecalliance.derec.lib.api.SharerFactory;
//import org.derecalliance.derec.lib.utils.ContactMsgUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Base64;
import java.util.ServiceLoader;

//import static org.derecalliance.derec.lib.utils.ContactMsgUtils.parseContactMessage;

public class QRCode {
    private double startX, startY, endX, endY;
    private Pane root;
    private Scene scene;


    /*
     * ------------------------------------------------------------------------
     * QR code reading
     * ------------------------------------------------------------------------
     */
    // Show the pane that can read/capture the QR code image
    public void createHelperPane(CaptureCallback callback) {
        int[] rectDimensions = {300, 300};
        int[] sceneDimensions = {rectDimensions[0] + 50,
                rectDimensions[1] + 100};
        Stage initStage = new Stage();
        initStage.setOpacity(0.4);

        Rectangle transparentArea = new Rectangle(rectDimensions[0],
                rectDimensions[1]);
        transparentArea.setFill(Color.TRANSPARENT);
        transparentArea.setStroke(Color.BLACK); // For visibility
        transparentArea.opacityProperty().set(0.1);

        VBox box = new VBox();
        Button captureButton = new Button("Capture");
//        captureButton.setOnAction(e -> capture(null, transparentArea));
        captureButton.setOnAction(e -> {
            DeRecContact decodedContact = capture(null, transparentArea);
            initStage.close();
            callback.onCaptureComplete(decodedContact);
        });
        captureButton.setStyle(
                "-fx-background-color: limegreen; " +  // Bright green background
                        "-fx-text-fill: black; " +             // White font color
                        "-fx-opacity: 1.0;");
//        captureButton.setStyle("-fx-background-color:rgba(ff,0,0,1)");
//        VBox innerBox = new VBox();
        Label dragLabel = new Label("Drag this transparent box onto the helper's QR code.");
        dragLabel.setFont(new Font(14));

//        innerBox.getChildren().add(label);
        box.getChildren().addAll(transparentArea, captureButton, dragLabel);
        final Scene scene2 = new Scene(box, sceneDimensions[0],
                sceneDimensions[1]);
        scene2.setFill(Color.TRANSPARENT);
//        initStage.initOwner(MainApp.primaryStage);
        Screen screen = Screen.getScreensForRectangle(MainApp.primaryStage.getX(), MainApp.primaryStage.getY(), 1, 1).get(0);
        Rectangle2D bounds = screen.getVisualBounds();
        initStage.setX(MainApp.primaryStage.getX() + 25);
        initStage.setY(MainApp.primaryStage.getY() + 150);

        initStage.setScene(scene2);

        initStage.show();
    }

    // Capture the image
    private DeRecContact capture(Pane helperPane, Rectangle transparentArea) {
        try {
            // Calculate the bounds of the transparent area in screen coordinates
            Bounds bounds = transparentArea.localToScreen(transparentArea.getBoundsInLocal());

            // Capture the screen portion
            Robot robot = new Robot();
            System.out.println("Capturing: (" + bounds.getMinX() + ", " +
                    bounds.getMinY() + ", " + bounds.getWidth() + ", " + bounds.getHeight() + ")");
            Rectangle2D captureRect = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
//            Rectangle2D captureRect = new Rectangle2D(0, 0, 500, 500);
            WritableImage screenshot = robot.getScreenCapture(null, captureRect);

            try {
                String b64str = decodeQRCode(screenshot);
                System.out.println("SCANNED b64 str: " + b64str);

                    byte[] originalData = Base64.getDecoder().decode(b64str);
                    System.out.println("SCANNED b64 RESULT: " + originalData.toString());

                ServiceLoader<ContactFactory> loader = ServiceLoader.load(ContactFactory.class);
                ContactFactory factory = loader.findFirst().orElseThrow(() -> new IllegalStateException("No " +
                        "ContactFactory implementation found"));
                DeRecContact dummyContact = factory.createContact();
                DeRecContact decodedContact = dummyContact.parseContactMessage(originalData);
                    System.out.println("SCANNED contact: ");
                    return decodedContact;
            } catch(Exception ex) {
//                File file = new File("captured_image.png");
//                ImageIO.write(SwingFXUtils.fromFXImage(screenshot, null), "png", file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Scan failed");
                alert.setHeaderText(null); // No header
                alert.setContentText("Could not capture the QR code. " +
                        "Please try again");

                // Show the alert and wait for the user to close it
                alert.showAndWait();
                alert.initOwner(MainApp.primaryStage);
                return null;
            }

            // Save the image
//            File file = new File("captured_image.png");
//            ImageIO.write(SwingFXUtils.fromFXImage(screenshot, null), "png", file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public BufferedImage simpleTransform(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage correctedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = correctedImage.createGraphics();

        // Example: Simple scaling and rotation
        AffineTransform transform = new AffineTransform();
        transform.scale(1.0, 1.0); // Scale
        transform.rotate(Math.toRadians(0), width / 2.0, height / 2.0); // Rotate

        g2d.transform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return correctedImage;
    }

//    public DeRecContact parseQRCode(byte[] data) {
//        return parseContactMessage(data);
//    }
    public String decodeQRCode(WritableImage writableImage) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

//            float scaleFactor = 2.0f; // Adjust this value as needed
//            RescaleOp op = new RescaleOp(scaleFactor, 0, null);
//            bufferedImage = op.filter(bufferedImage, null);
            bufferedImage = simpleTransform(bufferedImage);

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

//            return result.getRawBytes();
        } catch (Exception e) {
            System.out.println("Exception in decodeQRCode");
            e.printStackTrace();
            return null;
        }
    }


    /*
     * ------------------------------------------------------------------------
     * QR code generation
     * ------------------------------------------------------------------------
     */

    public Image qrCodeForContact () {
        long nonce = 1234L; // State.getInstance().getHelper().createAndStoreNewNonce();
        ServiceLoader<ContactFactory> loader = ServiceLoader.load(ContactFactory.class);
        ContactFactory factory = loader.findFirst().orElseThrow(() -> new IllegalStateException("No " +
                "ContactFactory implementation found"));
        DeRecContact dummyContact = factory.createContact();
        byte[] data = dummyContact.createContactMessage(
                State.getInstance().getHelper().getPublicEncryptionKeyId(),
                State.getInstance().getHelper().getPublicEncryptionKey(),
                nonce,
                State.getInstance().getUserSelections().getUri());

        System.out.println("in assembleQRCode: estr = " + Base64.getEncoder().encodeToString(data));
        return genericQrCode(Base64.getEncoder().encodeToString(data), "UTF-8", 250, 250).getImage();
    }

    // Geenerate QR Code and return the stackpane with that QR code
    public ImageView genericQrCode(String data,
                                String charset,
                                int height, int width) {

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    new String(data.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, width, height);

            // Convert BitMatrix to BufferedImage
            BufferedImage bufferedImage = toBufferedImage(bitMatrix);

            // Convert BufferedImage to WritableImage
            WritableImage writableImage = fromBufferedImage(bufferedImage);

            // Use an ImageView to display the WritableImage
            ImageView imageView = new ImageView(writableImage);

//            StackPane root = new StackPane(imageView);
            return imageView;
        } catch (Exception e) {
            System.out.println("Exception in createQR");
        }
        return null;
    }

    // Utility functions
    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ?
                        java.awt.Color.BLACK.getRGB() : java.awt.Color.WHITE.getRGB());
            }
        }
        return image;
    }

    // Utility functions
    private WritableImage fromBufferedImage(BufferedImage bufferedImage) {
        WritableImage writableImage = null;
        if (bufferedImage != null) {
            writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
            javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, writableImage);
        }
        return writableImage;
    }



//    public void showCaptureImageDialog(Pane currentPane) {
//        // Create the custom dialog.
//        Dialog<Pair<String, String>> dialog = new Dialog<>();
//        dialog.setTitle("Scan QR code");
//        dialog.setHeaderText("Scan QR code");
//
//        // Set the button types.
//        ButtonType saveButtonType = new ButtonType("Capture",
//                ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
//
//        captureImage(currentPane);
//        // Create the name and secret text fields.
////        GridPane grid = new GridPane();
////        grid.setHgap(10);
////        grid.setVgap(10);
////        grid.setPadding(new Insets(20, 10, 10, 10));
////
////        TextField secretName = new TextField();
////        secretName.setPromptText("Name");
////        TextArea secretText = new TextArea();
////        secretText.setPromptText("Secret Text");
////
////        grid.add(new Label("Name:"), 0, 0);
////        grid.add(secretName, 1, 0);
////        grid.add(new Label("Text:"), 0, 1);
////        grid.add(secretText, 1, 1);
////
////        dialog.getDialogPane().setContent(grid);
//
//        // Set the size of the dialog to match the size of the current pane.
//        dialog.getDialogPane().setPrefSize(currentPane.getWidth(), currentPane.getHeight());
//
//        // Request focus on the name field by default.
////        Platform.runLater(secretName::requestFocus);
//
//        // Convert the result to a name-secretText-pair when the save button is clicked.
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == saveButtonType) {
//                System.out.println("Taking a screeenshot now");
////                return new Pair<>(secretName.getText(), secretText.getText());
//                return new Pair<>("Abc", "Def");
//            }
//            return null;
//        });
//
//        // Show the dialog and capture the result.
//        Optional<Pair<String, String>> result = dialog.showAndWait();
//        result.ifPresent(secretData -> {
//            System.out.println("Name=" + secretData.getKey() + ", Text=" + secretData.getValue());
////            // Here, you can add code to handle the secret data
////            Secret secret = new Secret(secretData.getKey(),
////                    secretData.getValue());
////            State.getInstance().addSecret(secret);
////
////            System.out.println("SecretDrop down has items count: " +
////                    secretDropdown.getItems().size());
////            System.out.println("State secrets has items count: " +
////                    State.getInstance().getSecrets().size());
////            paintSharerMainTab();
//        });
//    }

//    public void captureImage(Pane pane) {
//        root = new Pane();
//        scene = new Scene(root, 600, 400);
//
//        scene.setOnMousePressed(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                startX = event.getX();
//                startY = event.getY();
//            }
//        });
//
//        scene.setOnMouseDragged(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                endX = event.getX();
//                endY = event.getY();
//                drawRectangle();
//            }
//        });
//
//        scene.setOnMouseReleased(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                endX = event.getX();
//                endY = event.getY();
//                captureAndSaveScreenshot();
//                root.getChildren().clear(); // Clear the rectangle after capturing the screenshot
//            }
//        });
//
//        pane.getChildren().add(scene.getRoot());
////        primaryStage.setTitle("Screenshot App");
////        primaryStage.setScene(scene);
////        primaryStage.show();
//    }
//
//    private void drawRectangle() {
//        root.getChildren().clear(); // Clear previous rectangles
//        root.getChildren().add(new javafx.scene.shape.Rectangle(startX, startY, endX - startX, endY - startY));
//    }
//
//    private void captureAndSaveScreenshot() {
////        WritableImage writableImage = new WritableImage((int) scene.getWidth(), (int) scene.getHeight());
//        SnapshotParameters params = new SnapshotParameters();
//        params.setFill(Color.TRANSPARENT); // making white background go away
//
//        // Translate viewport to scene coordinates
//        Rectangle2D viewport = new Rectangle2D(Math.min(startX, endX), Math.min(startY, endY),
//                Math.abs(endX - startX), Math.abs(endY - startY));
//        params.setViewport(viewport);
//
//        javafx.scene.robot.Robot robot = new Robot();
//        WritableImage imgReturn = robot.getScreenCapture(null, viewport);
//
////        root.snapshot(params, writableImage);
//
//        File directory = new File("scannedqrs");
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//
//        File file = new File("scannedqrs/screenshot.png");
//        try {
////            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
//            ImageIO.write(SwingFXUtils.fromFXImage(imgReturn, null), "png", file);
//            System.out.println("Screenshot saved: " + file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}