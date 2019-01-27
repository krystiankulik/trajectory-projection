package projection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class TrajectoryProjectionApp extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        Locale locale = new Locale("pl", "PL");
        createScene(stage, locale);
    }

    public void createScene(Stage stage, Locale locale) throws Exception{
        ResourceBundle bundle = ResourceBundle.getBundle("bundle", locale);
        Messages.setBundle(bundle);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("stage.fxml"),bundle);
        String style= getClass().getClassLoader().getResource("styles.css").toExternalForm();
        root.getStylesheets().add(style);
        stage.setTitle(Messages.getString("projection.application.title"));
        stage.setScene(new Scene(root,800,600));
        stage.setResizable(false);
        stage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
    }

}
