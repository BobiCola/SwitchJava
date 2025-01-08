package smg.switchjava;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class util {
    public static void setJavaHome(String newValue) {
        String javaHome = System.getenv("JAVA_HOME");
        try {
            if (javaHome != null) {
                System.out.println("JAVA_HOME已存在，当前值为: " + javaHome);

                // 清空JAVA_HOME
                Process clearProcess = Runtime.getRuntime().exec("setx JAVA_HOME \"\" /M");
                clearProcess.waitFor();
            } else {
                System.out.println("JAVA_HOME环境变量未设置。");
            }

            // 设置新的JAVA_HOME值
            System.out.println("设置新的JAVA_HOME为: " + newValue);
            Process setProcess = Runtime.getRuntime().exec("setx JAVA_HOME \"" + newValue + "\" /M");
            setProcess.waitFor();
            System.out.println("JAVA_HOME已设置为: " + newValue);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
