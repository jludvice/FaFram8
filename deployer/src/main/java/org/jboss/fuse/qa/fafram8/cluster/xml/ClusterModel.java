package org.jboss.fuse.qa.fafram8.cluster.xml;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ecervena on 1/11/16.
 *
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
