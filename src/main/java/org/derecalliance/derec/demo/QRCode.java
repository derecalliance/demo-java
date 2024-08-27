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

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.ServiceLoader;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.derecalliance.derec.demo.state.State;
import org.derecalliance.derec.lib.api.ContactFactory;
import org.derecalliance.derec.lib.api.DeRecContact;

public class QRCode {
    /*
     * ------------------------------------------------------------------------
     * QR code reading
     * ------------------------------------------------------------------------
     */
    public void createHelperPane(CaptureCallback callback) {
        // Show the pane that can read/capture the QR code image
        int[] rectDimensions = {300, 300};
        int[] sceneDimensions = {rectDimensions[0] + 50, rectDimensions[1] + 100};
        Stage initStage = new Stage();
        initStage.setOpacity(0.4);

        Rectangle transparentArea = new Rectangle(rectDimensions[0], rectDimensions[1]);
        transparentArea.setFill(Color.TRANSPARENT);
        transparentArea.setStroke(Color.BLACK);
        transparentArea.opacityProperty().set(0.1);

        VBox box = new VBox();
        Button captureButton = new Button("Capture");
        captureButton.setOnAction(e -> {
            DeRecContact decodedContact = capture(null, transparentArea);
            initStage.close();
            callback.onCaptureComplete(decodedContact);
        });
        captureButton.setStyle("-fx-background-color: limegreen; " + "-fx-text-fill: black; " + "-fx-opacity: 1.0;");
        Label dragLabel = new Label("Drag this transparent box onto the helper's QR code.");
        dragLabel.setFont(new Font(14));
        box.getChildren().addAll(transparentArea, captureButton, dragLabel);

        final Scene scene2 = new Scene(box, sceneDimensions[0], sceneDimensions[1]);
        scene2.setFill(Color.TRANSPARENT);
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
            System.out.println("Capturing: (" + bounds.getMinX() + ", " + bounds.getMinY() + ", " + bounds.getWidth()
                    + ", " + bounds.getHeight() + ")");
            Rectangle2D captureRect =
                    new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            WritableImage screenshot = robot.getScreenCapture(null, captureRect);

            try {
                String b64str = decodeQRCode(screenshot);
                System.out.println("SCANNED b64 str: " + b64str);

                byte[] originalData = Base64.getDecoder().decode(b64str);
                System.out.println("SCANNED b64 RESULT: " + originalData.toString());

                ServiceLoader<ContactFactory> loader = ServiceLoader.load(ContactFactory.class);
                ContactFactory factory = loader.findFirst()
                        .orElseThrow(() -> new IllegalStateException("No " + "ContactFactory implementation found"));
                DeRecContact dummyContact = factory.createContact();
                DeRecContact decodedContact = dummyContact.parseContactMessage(originalData);
                System.out.println("SCANNED contact: ");
                return decodedContact;
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Scan failed");
                alert.setHeaderText(null);
                alert.setContentText("Could not capture the QR code. " + "Please try again");

                // Show the alert and wait for the user to close it
                alert.showAndWait();
                alert.initOwner(MainApp.primaryStage);
                return null;
            }
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

        AffineTransform transform = new AffineTransform();
        transform.scale(1.0, 1.0); // Scale
        transform.rotate(Math.toRadians(0), width / 2.0, height / 2.0); // Rotate

        g2d.transform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return correctedImage;
    }

    public String decodeQRCode(WritableImage writableImage) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            bufferedImage = simpleTransform(bufferedImage);

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
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

    public Image qrCodeForContact() {
        long nonce = 1234L; // TODO: generate a real nonce
        ServiceLoader<ContactFactory> loader = ServiceLoader.load(ContactFactory.class);
        ContactFactory factory = loader.findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + "ContactFactory implementation found"));
        DeRecContact dummyContact = factory.createContact();
        byte[] data = dummyContact.createContactMessage(
                State.getInstance().getHelper().getPublicEncryptionKeyId(),
                State.getInstance().getHelper().getPublicEncryptionKey(),
                nonce,
                State.getInstance().getUserSelections().getUri());

        System.out.println("in assembleQRCode: estr = " + Base64.getEncoder().encodeToString(data));
        return genericQrCode(Base64.getEncoder().encodeToString(data), "UTF-8", 250, 250)
                .getImage();
    }

    // Geenerate QR Code and return the stackpane with that QR code
    public ImageView genericQrCode(String data, String charset, int height, int width) {

        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = toBufferedImage(bitMatrix);
            WritableImage writableImage = fromBufferedImage(bufferedImage);
            ImageView imageView = new ImageView(writableImage);
            return imageView;
        } catch (Exception e) {
            System.out.println("Exception in createQR");
        }
        return null;
    }

    /*
     * ------------------------------------------------------------------------
     * Utility functions
     * ------------------------------------------------------------------------
     */
    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? java.awt.Color.BLACK.getRGB() : java.awt.Color.WHITE.getRGB());
            }
        }
        return image;
    }

    private WritableImage fromBufferedImage(BufferedImage bufferedImage) {
        WritableImage writableImage = null;
        if (bufferedImage != null) {
            writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
            javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, writableImage);
        }
        return writableImage;
    }
}
