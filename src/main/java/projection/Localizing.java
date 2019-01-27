package projection;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Locale;

public class Localizing {

    public static void loadView(Locale locale, Pane root) {
        try {
            Scene scene = root.getScene();
            ((Stage) scene.getWindow()).close();
            TrajectoryProjectionApp app = new TrajectoryProjectionApp();
            app.createScene(new Stage(), locale);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
