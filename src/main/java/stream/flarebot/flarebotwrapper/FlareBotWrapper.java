package stream.flarebot.flarebotwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlareBotWrapper {

    private static final Logger LOGGER;
    private static final String FLAREBOT_JAR_FILE = "FlareBot-jar-with-dependencies.jar";

    static {
        LOGGER = LoggerFactory.getLogger(FlareBotWrapper.class);
    }

    public static void main(String[] args) {
        new File("wrapper.log").delete();
        Spark.port(8080);
        Spark.get("/update", Routes.getUpdateRoute());



    }

    public static void run() throws IOException, InterruptedException {
        ProcessBuilder flarebot = new ProcessBuilder("java", "-jar", FLAREBOT_JAR_FILE);
        Process p = flarebot.start();
        p.
        p.waitFor();
        switch (p.exitValue()) {
            case ExitValues.CLEAN_EXIT:
                LOGGER.info("Exiting cleanly!");
        }
    }

    public static void update() throws IOException, InterruptedException {
        File git = new File("FlareBot" + File.separator);
        if (!(git.exists() && git.isDirectory())) {
            LOGGER.info("Cloning git!");
            ProcessBuilder clone =
                    new ProcessBuilder("git", "clone", "https://github.com/FlareBot/FlareBot.git", git
                            .getAbsolutePath());
            clone.redirectErrorStream(true);
            Process p = clone.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out = "";
            String line;
            if ((line = reader.readLine()) != null) {
                out += line + '\n';
            }
            p.waitFor();
            if (p.exitValue() != 0) {
                updateError(out);
            }
        } else {
            LOGGER.info("Pulling git!");
            ProcessBuilder builder = new ProcessBuilder("git", "pull");
            builder.directory(git);
            Process p = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out = "";
            String line;
            if ((line = reader.readLine()) != null) {
                out += line + '\n';
            }
            p.waitFor();
            if (p.exitValue() != 0) {
                updateError(out);
            }
        }
        LOGGER.info("Building!");
        ProcessBuilder maven = new ProcessBuilder("mvn", "clean", "package", "-e", "-U");
        maven.directory(git);
        Process p = maven.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String out = "";
        String line;
        if ((line = reader.readLine()) != null) {
            System.out.println(line);
            out += line + '\n';
        }
        p.waitFor();
        if (p.exitValue() != 0) {
            updateError(out);
            return;
        }
        LOGGER.info("Replacing jar!");
        File current = new File(FLAREBOT_JAR_FILE);
        Files.copy(current.toPath(), Paths
                .get(current.getPath().replace(".jar", ".backup.jar")), StandardCopyOption.REPLACE_EXISTING);
        File built = new File(git, "target" + File.separator + FLAREBOT_JAR_FILE);
        Files.copy(built.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void updateError(String out) throws IOException {
        File file = new File(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (!file.exists()) {
            file.createNewFile();
        }
        try (Writer w = new FileWriter(file)) {
            w.write(out);
        }
        LOGGER.error("Could not update! Log: {}", file.getName());
        return;
    }

    public class ExitValues {

        public static final int CLEAN_EXIT = 0;
        public static final int RESTART = 200;
        public static final int UPDATE = 210;

    }

}
