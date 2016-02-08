package org.jboss.fuse.qa.fafram8.cluster.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ecervena on 1/11/16.
 * <p/>
 * ClusterModel class represents XML to object model mapping. Cluster XML configuration is mapped to this class.
 * Fafram containerList is initialized according to clusterModel.
 */

@XmlRootElement(name = "fafram", namespace = "org.jboss.fuse.qa")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClusterModel {

	@Getter
	@Setter
	@XmlElement(name = "framework", namespace = "org.jboss.fuse.qa")
	private FrameworkConfigurationModel frameworkConfigurationModel;

	@Getter
	@Setter
	@XmlElement(name = "container", namespace = "org.jboss.fuse.qa")
	private List<ContainerModel> containerModelList;
}
