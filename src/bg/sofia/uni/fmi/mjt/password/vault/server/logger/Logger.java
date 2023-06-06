package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import java.nio.file.Path;
import java.time.LocalDateTime;

public interface Logger {

    void log(Level level, LocalDateTime timestamp, String message);

    Path getCurrentFilePath();

}