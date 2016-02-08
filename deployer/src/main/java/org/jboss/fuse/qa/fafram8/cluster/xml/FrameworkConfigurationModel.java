package org.jboss.fuse.qa.fafram8.cluster.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * FrameworkConfigurationModel class represents XML configuration to object model mapping.
 * FrameworkConfigurationModel object should hold all fafram8 XML configuration possibilities.
 * <p/>
 * Created by ecervena on 1/11/16.
 */
//TODO(ecervena): Finish implementation.
@XmlRootElement(name = "framework", namespace = "org.jboss.fuse.qa")
@XmlAccessorType(XmlAccessType.FIELD)
@Slf4j
public class FrameworkConfigurationModel {

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String patchVersion;

	/**
	 * Set framework system properties from unmarshalled fields from XML configuration.
	 */
	public void applyFrameworkConfiguration() {
		log.info("Setting unmarshalled framework properties.");
		//do the magic
		//test whether field is null. when not set system property.
	}

	/**
	 * This method logs formated properties unmarshaled from Fafram8 XML configuration file.
	 */
	public void logUnmarshalledProperties() {
		log.info("Framework properties:"
				+ "  Patch version: " + patchVersion);
	}
}
