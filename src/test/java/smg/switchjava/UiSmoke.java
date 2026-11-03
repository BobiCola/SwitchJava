package smg.switchjava;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.nio.file.Files;

/** Runs against the packaged JAR on a Windows desktop runner without changing JAVA_HOME. */
public final class UiSmoke {
    public static void main(String[] args) throws Exception {
        System.setProperty("user.home", Files.createTempDirectory("switchjava-smoke-").toString());
        Platform.startup(() -> {
            try {
                Stage stage = new Stage();
                new SwitchJavaApplication().start(stage);
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                if (!stage.isShowing() || stage.getScene().getRoot().lookup(".primary") == null) {
                    throw new IllegalStateException("Main window did not render");
                }
                System.out.println("SWITCHJAVA_SMOKE_OK");
                Platform.runLater(() -> { stage.close(); Platform.exit(); });
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
        });
    }
}
