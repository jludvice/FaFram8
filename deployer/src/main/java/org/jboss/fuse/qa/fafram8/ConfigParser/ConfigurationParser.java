package org.jboss.fuse.qa.fafram8.ConfigParser;

import org.jboss.fuse.qa.fafram8.manager.Container;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

/**
 * TODO(ecervena): this is only test implementation.
 * <p/>
 * Created by ecervena on 9/8/15.
 */
public class ConfigurationParser {

	@Getter
	private List<Container> containerList = new LinkedList<Container>();

	/**
	 * Constructor.
	 */
	public ConfigurationParser() {
	}

	/**
	 * Parses the configuration file.
	 *
	 * @param path path
	 */
	public void parseConfigurationFile(String path) {
		//TODO(ecervena): implement
		final Container container = new Container("node3", "172.16.116.22");
		containerList.add(container);
	}
}