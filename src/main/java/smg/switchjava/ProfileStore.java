package smg.switchjava;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class ProfileStore {
    private final Path file;

    public ProfileStore(Path file) { this.file = file; }

    public List<JdkProfile> load() throws IOException {
        if (!Files.exists(file)) return List.of();
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { data.load(reader); }
        try {
            int count = Integer.parseInt(data.getProperty("count", "0"));
            if (count < 0 || count > 10000) throw new IllegalArgumentException("Invalid count");
            List<JdkProfile> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                result.add(new JdkProfile(data.getProperty(i + ".name"), data.getProperty(i + ".home")));
            }
            return result;
        } catch (IllegalArgumentException e) { throw new IOException("配置损坏，请检查 " + file, e); }
    }

    public void save(List<JdkProfile> profiles) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Properties data = new Properties();
        data.setProperty("count", Integer.toString(profiles.size()));
        for (int i = 0; i < profiles.size(); i++) {
            data.setProperty(i + ".name", profiles.get(i).name());
            data.setProperty(i + ".home", profiles.get(i).home());
        }
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(), "profiles-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) { data.store(writer, "SwitchJava profiles"); }
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }

    public List<JdkProfile> readLegacy(Path legacy) throws IOException {
        List<JdkProfile> result = new ArrayList<>();
        for (String line : Files.readAllLines(legacy, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] parts = line.split(",", 2);
            if (parts.length != 2) throw new IOException("旧配置格式错误，未导入任何条目");
            try { result.add(new JdkProfile(parts[0], parts[1])); }
            catch (IllegalArgumentException e) { throw new IOException("旧配置包含无效条目", e); }
        }
        return result;
    }
}
