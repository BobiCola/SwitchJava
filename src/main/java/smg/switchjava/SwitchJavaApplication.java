package smg.switchjava;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SwitchJavaApplication extends Application {

    private List<String> names = new ArrayList<>();
    private List<String> paths = new ArrayList<>();
    private ChoiceBox<String> choiceBox;
    private TextArea outputArea; // 添加输出区域

    @Override
    public void start(Stage primaryStage) {
        // 从文件加载设置
        loadSettings();

        // 主界面布局
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // ChoiceBox
        choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(names); // 加载名称到 ChoiceBox
        choiceBox.setValue("选择名称"); // 默认值

        // 让 ChoiceBox 宽度适应窗口
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setMinHeight(30);

        // 按钮
        Button button1 = new Button("设置");
        Button button2 = new Button("切换");
        Button button3 = new Button("删除"); // 添加删除按钮

        // 让按钮宽度匹配窗口
        button1.setMinWidth(81);
        button1.setMinHeight(30);
        button2.setMinWidth(81);
        button2.setMinHeight(30);
        button3.setMinWidth(81);
        button3.setMinHeight(30);

        // 按钮区域布局
        HBox buttonBox = new HBox(10, button2, button1, button3); // 将删除按钮加入按钮区域
        buttonBox.setSpacing(10);

        // 按钮事件
        button1.setOnAction(e -> openSettingsWindow());
        button2.setOnAction(e -> switchAction());
        button3.setOnAction(e -> deleteSelectedItem()); // 删除按钮事件

        // 输出区域
        outputArea = new TextArea();
        outputArea.setMinHeight(215);
        outputArea.setEditable(false); // 设置为只读
        outputArea.setPrefHeight(100); // 设置高度

        // 将控件加入主界面
        root.getChildren().addAll(choiceBox, buttonBox, outputArea); // 添加输出区域到布局

        // 设置主场景
        Scene scene = new Scene(root, 300, 350); // 窗口大小
        primaryStage.setTitle("Switch Java");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/image/switch_d.png")));

        // 固定窗口大小并去除最大化按钮
        primaryStage.setResizable(false); // 禁止调整窗口大小
        primaryStage.setWidth(300); // 设置窗口宽度
        primaryStage.setHeight(350); // 设置窗口高度

        // 重定向 System.out 到 outputArea
        redirectSystemOut();

        primaryStage.show();
    }

    private void redirectSystemOut() {
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // 将字节转换为字符并添加到输出区域，使用 UTF-8 编码
                outputArea.appendText(String.valueOf((char) b));
                outputArea.setScrollTop(Double.MAX_VALUE); // 滚动到最底部
            }

            @Override
            public void write(byte[] b, int off, int len) {
                // 将字节数组转换为字符串并添加到输出区域
                String str = new String(b, off, len);
                outputArea.appendText(str);
                outputArea.setScrollTop(Double.MAX_VALUE); // 滚动到最底部
            }
        });
        System.setOut(printStream); // 重定向 System.out
        System.setErr(printStream); // 也重定向 System.err
    }

    private void openSettingsWindow() {
        // 创建弹出窗口
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("设置");

        // 布局
        VBox dialogVbox = new VBox(10);
        dialogVbox.setPadding(new Insets(10));

        // 输入字段
        TextField nameField = new TextField();
        nameField.setPromptText("请输入名称");

        TextField pathField = new TextField();
        pathField.setPromptText("请输入路径");

        // 确认按钮
        Button saveButton = new Button("保存");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        saveButton.setOnAction(e -> {
            String name = nameField.getText();
            String path = pathField.getText();
            if (!name.isEmpty() && !path.isEmpty()) {
                names.add(name);
                paths.add(path);
                choiceBox.getItems().add(name); // 更新 ChoiceBox
                saveSettings(); // 保存到文件
                nameField.clear(); // 清空输入框
                pathField.clear(); // 清空输入框
                dialog.close();
                System.out.println("已添加: " + name); // 输出到输出区域
            } else {
                // 提示用户输入不完整
                Alert alert = new Alert(Alert.AlertType.WARNING, "名称和路径不能为空！");
                alert.showAndWait();
            }
        });

        // 将控件加入布局
        dialogVbox.getChildren().addAll(new Label("Name:"), nameField, new Label("Path:"), pathField, saveButton);

        // 设置弹出窗口场景
        Scene dialogScene = new Scene(dialogVbox, 300, 200); // 窗口大小
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void switchAction() {
        outputArea.clear();
        int selectedIndex = choiceBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedPath = paths.get(selectedIndex);
            System.out.println("切换到路径: " + selectedPath);
            util.setJavaHome(selectedPath); // 假设这是您设置JAVA_HOME的逻辑
        } else {
            // 提示用户没有选择
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先选择一个版本！");
            alert.showAndWait();
        }
    }

    private void deleteSelectedItem() {
        int selectedIndex = choiceBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String item = choiceBox.getSelectionModel().getSelectedItem();
            // 从列表中删除
            names.remove(selectedIndex);
            paths.remove(selectedIndex);

            choiceBox.getItems().remove(selectedIndex); // 更新 ChoiceBox
            saveSettings(); // 保存到文件
            System.out.println("已删除选中的项: " + item );
        } else {
            // 提示用户没有选择
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先选择一个版本！");
            alert.showAndWait();
        }
    }

    private void loadSettings() {
        // 从用户的主目录加载 settings.txt 文件
        File file = new File(System.getProperty("user.home"), "settings.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    names.add(parts[0]);
                    paths.add(parts[1]);
                }
            }
            System.out.println("设置已从: " + file.getAbsolutePath() + " 加载"); // 输出加载路径
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        // 假设您将文件保存到用户的主目录
        File file = new File(System.getProperty("user.home"), "settings.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < names.size(); i++) {
                bw.write(names.get(i) + "," + paths.get(i));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
