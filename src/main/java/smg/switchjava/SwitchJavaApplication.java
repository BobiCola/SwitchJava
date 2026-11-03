package smg.switchjava;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.nio.file.*;
import java.util.*;

public class SwitchJavaApplication extends Application {
    private final ObservableList<JdkProfile> profiles = FXCollections.observableArrayList();
    private final ProfileStore store = new ProfileStore(Path.of(System.getProperty("user.home"), ".switchjava", "profiles.properties"));
    private final ListView<JdkProfile> list = new ListView<>(profiles);
    private final TextArea output = new TextArea();
    private Stage stage;
    private boolean busy;

    @Override public void start(Stage stage) {
        this.stage = stage;
        Button add = new Button("+ 添加 JDK"); add.getStyleClass().add("primary");
        add.setOnAction(e -> addProfile());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(16, label("SJ / SwitchJava", "brand"), spacer, add);
        Label count = label("", "section-label");
        count.textProperty().bind(Bindings.size(profiles).asString("本地版本 / %d"));
        list.setPlaceholder(new Label("还没有 JDK\n点击右上角添加第一个版本"));
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(JdkProfile item, boolean empty) {
                super.updateItem(item, empty); setText(null);
                if (empty || item == null) { setGraphic(null); return; }
                Label home = label(item.home(), "muted"); home.setMaxWidth(230);
                Label name = label(item.name(), "profile-name"); name.setMaxWidth(230);
                setGraphic(new VBox(6, name, home));
            }
        });
        VBox versions = new VBox(12, count, list); versions.getStyleClass().add("card");
        versions.setPrefWidth(300); versions.setMinWidth(260); versions.setMaxWidth(300); VBox.setVgrow(list, Priority.ALWAYS);
        Label selected = label("选择一个 JDK", "detail-title");
        Label path = label("从左侧选择版本，或添加新的本地 JDK。", "muted"); path.setWrapText(true);
        Label current = label("启动时 JAVA_HOME\n" + System.getenv().getOrDefault("JAVA_HOME", "未设置"), "environment"); current.setWrapText(true);
        CheckBox system = new CheckBox("写入系统变量（需要管理员权限）");
        Label hint = label(JavaHomeService.isWindows() ? "默认写入用户变量，不修改 PATH。请检查用户/系统变量及 PATH 的优先级。" : "当前平台仅支持配置管理；写入 JAVA_HOME 需要 Windows。", "muted"); hint.setWrapText(true);
        Button apply = new Button("切换到此版本"); apply.getStyleClass().add("primary");
        Button delete = new Button("删除配置"); delete.getStyleClass().add("danger");
        apply.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        HBox actions = new HBox(10, apply, delete);
        apply.setOnAction(e -> {
            JdkProfile profile = list.getSelectionModel().getSelectedItem();
            boolean scope = system.isSelected();
            Task<String> task = new Task<>() {
                @Override protected String call() throws Exception { return new JavaHomeService().switchTo(profile, scope); }
            };
            busy = true; list.setDisable(true); add.setDisable(true); system.setDisable(true); actions.setDisable(true);
            output.setText("正在写入 JAVA_HOME…");
            Runnable finish = () -> { busy = false; list.setDisable(false); add.setDisable(false); system.setDisable(false); actions.setDisable(false); };
            task.setOnSucceeded(event -> { output.setText(task.getValue()); finish.run(); });
            task.setOnFailed(event -> { output.setText("未确认切换成功：\n" + task.getException().getMessage()); finish.run(); });
            Thread worker = new Thread(task, "java-home-switch"); worker.setDaemon(true); worker.start();
        });
        delete.setOnAction(e -> {
            JdkProfile profile = list.getSelectionModel().getSelectedItem();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "仅删除配置，不删除磁盘上的 JDK：" + profile.name(), ButtonType.CANCEL, ButtonType.OK);
            confirm.initOwner(stage);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                List<JdkProfile> next = new ArrayList<>(profiles); next.remove(profile); persist(next);
            }
        });
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, profile) -> {
            selected.setText(profile == null ? "选择一个 JDK" : profile.name());
            path.setText(profile == null ? "从左侧选择版本，或添加新的本地 JDK。" : profile.home());
        });
        output.setEditable(false); output.setWrapText(true); output.setPrefRowCount(5);
        output.setText("准备就绪。添加 JDK 后即可切换。\n切换后请重新打开终端，并运行 java -version 核实实际版本。");
        VBox details = new VBox(18, label("VERSION CONTROL", "section-label"), selected, path, current, system, hint, actions, new Label("操作记录"), output);
        details.getStyleClass().add("card"); details.setMinWidth(0); VBox.setVgrow(output, Priority.ALWAYS); HBox.setHgrow(details, Priority.ALWAYS);
        HBox body = new HBox(20, versions, details); VBox.setVgrow(body, Priority.ALWAYS);
        VBox root = new VBox(20, header, new VBox(8, label("你的 Java 工作空间", "title"), label("管理本地 JDK，让版本切换更清晰、更可靠。", "muted")), body);
        root.setPadding(new Insets(28));
        Scene scene = new Scene(root, 960, 680); scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        stage.setTitle("SwitchJava · JDK Manager"); stage.setScene(scene); stage.setMinWidth(850); stage.setMinHeight(640);
        stage.setOnCloseRequest(e -> { if (busy) e.consume(); }); stage.show();
        try {
            profiles.setAll(store.load()); list.getSelectionModel().selectFirst();
            Path legacy = Path.of(System.getProperty("user.home"), "settings.txt");
            if (profiles.isEmpty() && Files.isRegularFile(legacy)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "发现旧版 ~/settings.txt，是否导入？原文件不会修改。", ButtonType.CANCEL, ButtonType.OK);
                confirm.initOwner(stage);
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) persist(store.readLegacy(legacy));
            }
        } catch (Exception e) {
            add.setDisable(true);
            output.setText("配置读取失败，为防止覆盖已禁止添加。请备份并修复配置后重启：\n" + e.getMessage());
        }
    }

    private Label label(String text, String style) {
        Label label = new Label(text); label.getStyleClass().add(style); return label;
    }

    private void addProfile() {
        Dialog<Void> dialog = new Dialog<>(); dialog.initOwner(stage); dialog.setTitle("添加本地 JDK");
        TextField name = new TextField(); name.setPromptText("例如：Temurin 21 / 项目名称");
        TextField path = new TextField(); path.setPromptText("JDK 根目录（不是 bin 目录）");
        Button browse = new Button("浏览…");
        browse.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser(); chooser.setTitle("选择 JDK 根目录");
            var folder = chooser.showDialog(stage);
            if (folder != null) { path.setText(folder.getAbsolutePath()); if (name.getText().isBlank()) name.setText(folder.getName()); }
        });
        Label error = label("", "error"); error.setWrapText(true);
        VBox content = new VBox(10, new Label("版本名称"), name, new Label("安装路径"), path, browse, error); content.setPrefWidth(450);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                JdkProfile profile = new JdkProfile(name.getText(), path.getText()); profile.validate();
                if (profiles.stream().anyMatch(p -> p.name().equalsIgnoreCase(profile.name()) || p.home().equalsIgnoreCase(profile.home()))) throw new IllegalArgumentException("此名称或路径已存在");
                List<JdkProfile> next = new ArrayList<>(profiles); next.add(profile);
                store.save(next); profiles.setAll(next); list.getSelectionModel().select(profile); output.setText("已添加：" + profile.name());
            } catch (Exception e) { error.setText(e.getMessage()); event.consume(); }
        });
        dialog.showAndWait();
    }

    private void persist(List<JdkProfile> next) {
        try { store.save(next); profiles.setAll(next); list.getSelectionModel().selectFirst(); output.setText("配置已保存。"); }
        catch (Exception e) { output.setText("保存失败，保留原配置：" + e.getMessage()); }
    }

    public static void main(String[] args) { launch(args); }
}
