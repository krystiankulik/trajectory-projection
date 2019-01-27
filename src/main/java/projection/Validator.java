package projection;

import javafx.scene.control.TextField;

public class Validator {


    public static void validateTextFieldInput(TextField textField) {
        textField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(!newValue.matches(Messages.getString("projection.validator.number.matcher"))) {
                        textField.setText(oldValue);
                    }
                }
        );
    }
}
