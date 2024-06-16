package org.derecalliance.derec.demo;


        import javafx.application.Application;
        import javafx.fxml.FXML;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.Parent;
        import javafx.scene.Scene;
        import javafx.scene.layout.Region;
        import javafx.scene.layout.VBox;
        import javafx.stage.Stage;
        import org.derecalliance.derec.demo.state.State;

public class MainApp extends Application {
    public static Scene mainScene;
    public static Stage primaryStage;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        // Load the Signin Screen first
        FXMLLoader signinLoader = new FXMLLoader(getClass().getResource(
                "/org/derecalliance/derec/demo/SigninScreen.fxml"));
        Parent signinRoot = signinLoader.load();
        Scene signinScene = new Scene(signinRoot,
                State.getInstance().appWidth, State.getInstance().appHeight);
        primaryStage.setScene(signinScene);

        // Set up a callback or event listener in SigninController
        // to transition to the main application view upon successful signin
        SigninController signinController = signinLoader.getController();
        signinController.setOnSignedIn(() -> {
            // Load the Main Application View
            try {
                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource(
                        "/org/derecalliance/derec/demo/MainView.fxml"));
                Parent mainRoot = mainLoader.load();
                Scene mainScene = new Scene(mainRoot, State.getInstance().appWidth, State.getInstance().appHeight);
                this.mainScene = mainScene;
                primaryStage.setScene(mainScene);
                primaryStage.setTitle("DeRec Demo Application");
            } catch (Exception ex) {
                System.err.println("Exception in Mainapp\n");
                ex.printStackTrace();
            }


//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(getClass().getResource("/org/derecalliance/derec/demo/MainView.fxml"));
//            Parent root = loader.load();
//
//            Scene scene = new Scene(root, State.getInstance().appWidth,
//                    State.getInstance().appHeight);
//            primaryStage.setScene(scene);


        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}
