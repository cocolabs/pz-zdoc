package io.yooksi.pz.luadoc;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings("unused")
public class Logger {

	private static final org.apache.logging.log4j.Logger logger;

	static
	{
		String logProperty = System.getProperty("ld.logger");
		Type type = logProperty != null && !logProperty.isEmpty() ?
				Type.get(logProperty, Type.INFO) : Type.INFO;

		logger = LogManager.getLogger(type.name);
	}

	/* Make the constructor private to disable instantiation */
	private Logger() {
		throw new UnsupportedOperationException();
	}

	public static org.apache.logging.log4j.Logger get() {
		return logger;
	}

	/*
	 * Short-hand methods to print logs to console. For more methods
	 * use the static getter method to get a hold of a Logger instance.
	 */
	public static void info(String log) {
		logger.info(log);
	}

	public static void info(String log, Object... params) {
		logger.info(log, params);
	}

	public static void error(String log) {
		logger.error(log);
	}

	public static void error(String log, Object... args) {
		logger.printf(Level.ERROR, log, args);
	}

	public static void error(String log, Throwable t) {
		logger.error(log, t);
	}

	public static void warn(String log) {
		logger.warn(log);
	}

	public static void warn(String format, Object... params) {
		logger.printf(Level.WARN, format, params);
	}

	public static void debug(String log) {
		logger.debug(log);
	}

	public static void debug(String format, Object... args) {
		logger.debug(format, args);
	}

	public static void debug(String log, Throwable t) {
		logger.debug(log, t);
	}

	public static void printf(Level level, String format, Object... params) {
		logger.printf(level, format, params);
	}

	enum Type {

		INFO("info", "StandardLogger"),
		DEBUG("debug", "DebugLogger"),
		DEV("dev", "DevLogger");

		private final String key;
		private final String name;

		Type(String key, String name) {
			this.key = key;
			this.name = name;
		}

		static Logger.Type get(String key, Type defaultType) {

			return Arrays.stream(Type.values()).filter(l ->
					l.key.equals(key)).findFirst().orElse(defaultType);
		}
	}
}
