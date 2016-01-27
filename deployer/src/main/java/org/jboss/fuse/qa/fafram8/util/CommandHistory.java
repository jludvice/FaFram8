package org.jboss.fuse.qa.fafram8.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Command history singleton. Holds the executed commands and responses and it is able to dump all the data into a file.
 * Created by avano on 24.11.15.
 */
public class CommandHistory {
	private static CommandHistory instance = null;
	private static Map<String, String> history = null;

	/**
	 * Constructor.
	 */
	protected CommandHistory() {
	}

	/**
	 * Instance getter.
	 *
	 * @return instance
	 */
	public static CommandHistory getInstance() {
		if (instance == null) {
			instance = new CommandHistory();
			history = new LinkedHashMap<>();
		}

		return instance;
	}

	/**
	 * Adds the cmd-response pair to the map.
	 *
	 * @param cmd command
	 * @param res response
	 */
	public static void add(String cmd, String res) {
		// Force initialization
		CommandHistory.getInstance();

		history.put(cmd, res);
	}

	/**
	 * Cleans the map.
	 */
	public static void clean() {
		history.clear();
	}

	/**
	 * Dumps the command-response values into a string.
	 *
	 * @return command-response values in string
	 */
	public static String dumpCommands() {
		// Force initialization
		CommandHistory.getInstance();

		final StringBuilder sb = new StringBuilder();
		for (String key : history.keySet()) {
			sb.append("Command:  ");
			sb.append(key);
			sb.append("\n");
			sb.append("Response: ");
			sb.append(history.get(key));
			sb.append("\n");
			sb.append("---------------------------------------------------------------");
			sb.append("\n");
		}

		return sb.toString();
	}
}
