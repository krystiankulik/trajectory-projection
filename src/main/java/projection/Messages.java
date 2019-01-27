package projection;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private static ResourceBundle bundle;

    public static void setBundle(ResourceBundle bundle) {
        Messages.bundle = bundle;
    }

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }

    }

    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
