package org.jboss.fuse.qa.fafram8.cluster.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ContainerModel class represents XML to object model mapping.
 * Containers in XML configuration are mapped to this class.
 * <p/>
 * Created by ecervena on 1/11/16.
 */
@XmlRootElement(name = "container", namespace = "org.jboss.fuse.qa")
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
public class ContainerModel {

	@Getter
	@Setter
	@XmlAttribute(name = "type")
	private String containerType;

	@Getter
	@Setter
	@XmlAttribute
	private int instances;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String name;

	@Getter
	@Setter
	@XmlElement(name = "node", namespace = "org.jboss.fuse.qa", type = HostNodeModel.class)
	private HostNodeModel node;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private boolean ensemble = false;

	@Getter
	@Setter
	@XmlElement(name = "parent", namespace = "org.jboss.fuse.qa")
	private String parentContainer;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String resolver;

	@Setter
	@Getter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String envProperties;

	@Setter
	@Getter
	private ArrayList<String> profiles;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String path;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String username;

	@Getter
	@Setter
	@XmlElement(namespace = "org.jboss.fuse.qa")
	private String password;
}
