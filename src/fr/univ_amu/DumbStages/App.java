package fr.univ_amu.DumbStages;


import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

// Application JavaFX appeller dans la class main
public class App extends Application{

    //Creation et configuration de la fenetre
    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/Icon.png")));
        stage.setTitle("IUT Stage");
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("home.fxml"))));
        stage.setResizable(false);
        stage.show();
    }
}
