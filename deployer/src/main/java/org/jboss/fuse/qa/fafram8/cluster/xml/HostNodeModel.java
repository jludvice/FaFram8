package org.jboss.fuse.qa.fafram8.cluster.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * HostNodeModel class represents XML configuration to object model mapping.
 * Holds information parsed from <node> tag.
 * <p/>
 * Created by ecervena on 1/13/16.
 */
@XmlRootElement(name = "node", namespace = "org.jboss.fuse.qa")
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
public class HostNodeModel {

	@Getter
	@Setter
	@XmlAttribute(name = "host")
	private String host;

	@Getter
	@Setter
	@XmlAttribute(name = "username")
	private String username;

	@Getter
	@Setter
	@XmlAttribute(name = "password")
	private String password;
}
