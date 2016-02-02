package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Property modifier class.
 * Created by jludvice on 20.8.15.
 */
@Slf4j
@ToString
@EqualsAndHashCode(of = {"filePath", "key"})
public final class PropertyModifier implements Modifier {
	private String filePath;
	private String key;
	private String value;
	private boolean extend;

	@Getter
	private String host;

	@Setter
	private Executor executor;

	/**
	 * Private constructor.
	 *
	 * @param filePath file path
	 * @param key key
	 * @param value value
	 * @param extend extend
	 */
	private PropertyModifier(String filePath, String key, String value, boolean extend) {
		this(null, filePath, key, value, extend);
	}

	/**
	 * Private constructor.
	 *
	 * @param ip ip to execute on
	 * @param filePath file path
	 * @param key key
	 * @param value value
	 * @param extend extend
	 */
	private PropertyModifier(String ip, String filePath, String key, String value, boolean extend) {
		this.host = ip;
		this.filePath = filePath;
		this.key = key;
		this.value = value;
		this.extend = extend;
	}

	/**
	 * Factory method - command for put/replace entry in property file.
	 *
	 * @param filePath path to property file - absolute, or relative to $FUSE_HOME
	 * @param key key in property file
	 * @param value value for key
	 * @return command instance
	 */
	public static PropertyModifier putProperty(final String filePath, final String key, final String value) {
		return new PropertyModifier(filePath, key, value, false);
	}

	/**
	 * Factory method - command for put/replace entry in property file.
	 * @param ip ip to execute on
	 * @param filePath path to property file - absolute, or relative to $FUSE_HOME
	 * @param key key in property file
	 * @param value value for key
	 * @return command instance
	 */
	public static PropertyModifier putProperty(String ip, String filePath, String key, String value) {
		return new PropertyModifier(ip, filePath, key, value, false);
	}

	/**
	 * Factory method - command to append to existing entry.
	 *
	 * @param filePath path to property file - absolute, or relative to $FUSE_HOME
	 * @param key key in property file
	 * @param value value to be appended to existing key=value
	 * @return command instance
	 */
	public static PropertyModifier extendProperty(final String filePath, final String key, final String value) {
		return new PropertyModifier(filePath, key, value, true);
	}

	@Override
	public void execute() {
		if (executor == null) {
			localExecute();
		} else {
			remoteExecute();
		}
	}

	/**
	 * Modifies properties on localhost.
	 */
	private void localExecute() {
		final Properties p = new Properties();

		Path path = Paths.get(filePath);
		if (!path.isAbsolute()) {
			path = Paths.get(SystemProperty.getFusePath() + File.separator + filePath);
		}
		// load property file if it exists
		if (Files.exists(path)) {
			try (InputStream ignored = Files.newInputStream(path)) {
				p.load(Files.newInputStream(path));
			} catch (IOException e) {
				log.error("Can't load property file {}.", filePath, e);
				throw new FaframException("Can't load property file " + filePath + ".", e);
			}
		} else {
			log.debug("Creating new property file {}", filePath);
		}

		if (extend) {
			p.put(key, p.getProperty(key, "") + value);
		} else {
			p.put(key, value);
		}

		try (OutputStream os = Files.newOutputStream(path)) {
			p.store(os, "property file edited by Fuse ModifierExecutor");
		} catch (IOException e) {
			log.error("Failed to store modified property file {}", filePath, e);
			throw new FaframException("Failed to store modified property file " + filePath + ".", e);
		}
	}

	/**
	 * Modifies properties on remote.
	 */
	private void remoteExecute() {
		final String path = SystemProperty.getFusePath() + File.separator + filePath;

		final String response = executor.executeCommand("(grep -v '[#]' " + path + " | grep -q '" + key + "' ) && sed"
				+ " -i \"s/^\\s*\\(" + StringUtils.replace(key, ".", "\\.") + "\\).*\\$/\\1=" + value + "/\" " + path
				+ " || echo '\n" + key + "=" + value + "' >> " + path);
		if (!response.isEmpty()) {
			log.error("Setting property on remote host failed. Response should be empty but was: {}.", response);
			throw new FaframException("Setting property on remote host failed (response should be empty): " + response);
		}
	}
}
