package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions.LogParserException;

import java.time.LocalDateTime;
import java.util.List;

public interface LogParser {

    List<Log> getLogs(Level level, LocalDateTime from, LocalDateTime to) throws LogParserException;

    List<Log> getLogs(LocalDateTime from, LocalDateTime to) throws LogParserException;

    List<Log> getLogsTail(int n) throws LogParserException;

}