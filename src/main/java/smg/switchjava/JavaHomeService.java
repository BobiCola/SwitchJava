package smg.switchjava;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class JavaHomeService {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    static List<String> command(JdkProfile profile, boolean system) {
        return system ? List.of("setx.exe", "JAVA_HOME", profile.home(), "/M")
                : List.of("setx.exe", "JAVA_HOME", profile.home());
    }

    public String switchTo(JdkProfile profile, boolean system) throws IOException, InterruptedException {
        if (!isWindows()) throw new IOException("环境变量切换仅支持 Windows；当前平台可管理 JDK 配置。");
        profile.validate();
        if (profile.home().length() >= 1024) throw new IOException("路径过长，拒绝调用可能截断值的 setx");
        Process process = new ProcessBuilder(command(profile, system)).redirectErrorStream(true).start();
        try {
            if (!process.waitFor(15, TimeUnit.SECONDS)) throw new IOException("切换超时，未确认环境变量是否更新");
            String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset()).strip();
            if (process.exitValue() != 0) throw new IOException("切换失败 (" + process.exitValue() + "): " + output);
            return "已写入" + (system ? "系统" : "用户") + " JAVA_HOME：" + profile.home()
                    + "\n请重新打开终端。当前进程和已打开的终端不会更新。";
        } finally { process.destroyForcibly(); }
    }
}
