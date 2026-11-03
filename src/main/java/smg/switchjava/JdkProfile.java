package smg.switchjava;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public record JdkProfile(String name, String home) {
    public JdkProfile {
        if (name == null || name.isBlank() || home == null || home.isBlank()) {
            throw new IllegalArgumentException("名称和路径不能为空");
        }
        name = name.strip();
        home = Path.of(home.strip()).toAbsolutePath().normalize().toString();
    }

    public void validate() {
        String suffix = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows") ? ".exe" : "";
        Path bin = Path.of(home, "bin");
        if (!Files.isRegularFile(bin.resolve("java" + suffix))
                || !Files.isRegularFile(bin.resolve("javac" + suffix))) {
            throw new IllegalArgumentException("请选择包含 bin/java 和 bin/javac 的 JDK 根目录");
        }
    }

    @Override
    public String toString() { return name; }
}
