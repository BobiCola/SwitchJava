package smg.switchjava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProfileStoreTest {
    @TempDir Path temp;

    @Test void roundTripUnicodeCommasAndSpaces() throws Exception {
        ProfileStore store = new ProfileStore(temp.resolve("config/profiles.properties"));
        assertTrue(store.load().isEmpty());
        List<JdkProfile> profiles = List.of(new JdkProfile("开发 JDK, 21", temp.resolve("Java Home,21").toString()));
        store.save(profiles);
        assertEquals(profiles, store.load());
        store.save(List.of());
        assertTrue(store.load().isEmpty());
    }

    @Test void rejectsCorruptConfig() throws Exception {
        Path file = temp.resolve("bad.properties");
        Files.writeString(file, "count=1\n0.name=missing path\n");
        assertThrows(IOException.class, () -> new ProfileStore(file).load());
    }

    @Test void importsLegacyWithoutModifyingOriginal() throws Exception {
        Path file = temp.resolve("settings.txt");
        String original = "JDK 17," + temp.resolve("jdk,17") + "\n";
        Files.writeString(file, original);
        List<JdkProfile> result = new ProfileStore(temp.resolve("new.properties")).readLegacy(file);
        assertEquals(1, result.size());
        assertTrue(result.get(0).home().endsWith("jdk,17"));
        assertEquals(original, Files.readString(file));
    }

    @Test void malformedLegacyFailsWithoutPartialImport() throws Exception {
        Path file = temp.resolve("settings.txt");
        Files.writeString(file, "bad line");
        assertThrows(IOException.class, () -> new ProfileStore(temp.resolve("new")).readLegacy(file));
    }

    @Test void validatesJdkLayout() throws Exception {
        JdkProfile profile = new JdkProfile("test", temp.toString());
        assertThrows(IllegalArgumentException.class, profile::validate);
        Path bin = Files.createDirectory(temp.resolve("bin"));
        String suffix = JavaHomeService.isWindows() ? ".exe" : "";
        Files.createFile(bin.resolve("java" + suffix));
        Files.createFile(bin.resolve("javac" + suffix));
        assertDoesNotThrow(profile::validate);
    }

    @Test void commandPreservesPathAsSingleArgument() {
        JdkProfile profile = new JdkProfile("test", temp.resolve("JDK space & comma,21").toString());
        assertEquals(List.of("setx.exe", "JAVA_HOME", profile.home()), JavaHomeService.command(profile, false));
        assertEquals(List.of("setx.exe", "JAVA_HOME", profile.home(), "/M"), JavaHomeService.command(profile, true));
    }

    @Test void rejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new JdkProfile(" ", "test"));
        assertThrows(IllegalArgumentException.class, () -> new JdkProfile("test", " "));
    }

    @Test void nonWindowsCannotMutateEnvironment() {
        if (!JavaHomeService.isWindows()) assertThrows(IOException.class,
                () -> new JavaHomeService().switchTo(new JdkProfile("test", temp.toString()), false));
    }
}
