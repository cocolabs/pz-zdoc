package io.yooksi.pzlua.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class Logger {

	private final File LOG_FILE;

	public Logger() {
		try {
			File logDir = new File("logs");
			if (!logDir.exists() && !logDir.mkdir()) {
				throw new IOException("Unable to create log directory.");
			}
			LOG_FILE = Paths.get(logDir.getPath(), "main.log").toFile();
			FileUtils.write(LOG_FILE, "", Charset.defaultCharset(), false);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(String log) throws IOException {
		FileUtils.write(LOG_FILE, log + System.lineSeparator(), Charset.defaultCharset(), true);
	}
}
