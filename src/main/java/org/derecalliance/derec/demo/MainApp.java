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

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.derecalliance.derec.demo.state.State;

public class MainApp extends Application {
    public static Scene mainScene;
    public static Stage primaryStage;

    static {
        // Format the current time as MM-dd-HH-mm-ss
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-HH-mm-ss");
        String formattedDate = sdf.format(new Date());

        // Set the instanceId property with the formatted date
        System.setProperty("instanceId", System.getenv("username") + "-" + formattedDate);

        // Debug print to verify
        System.out.println("Instance ID: " + System.getProperty("instanceId"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        // Load the Signin Screen first
        FXMLLoader signinLoader =
                new FXMLLoader(getClass().getResource("/org/derecalliance/derec/demo/SigninScreen.fxml"));
        Parent signinRoot = signinLoader.load();
        Scene signinScene = new Scene(signinRoot, State.getInstance().appWidth, State.getInstance().appHeight);
        primaryStage.setScene(signinScene);

        // Set up a callback or event listener in SigninController
        // to transition to the main application view upon successful signin
        SigninController signinController = signinLoader.getController();
        signinController.setOnSignedIn(() -> {
            // Load the Main Application View
            try {
                FXMLLoader mainLoader =
                        new FXMLLoader(getClass().getResource("/org/derecalliance/derec/demo/MainView.fxml"));
                Parent mainRoot = mainLoader.load();
                Scene mainScene = new Scene(mainRoot, State.getInstance().appWidth, State.getInstance().appHeight);
                this.mainScene = mainScene;
                primaryStage.setScene(mainScene);
                primaryStage.setTitle("DeRec Demo Application");
            } catch (Exception ex) {
                System.err.println("Exception in Mainapp\n");
                ex.printStackTrace();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
