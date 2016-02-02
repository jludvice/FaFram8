package org.jboss.fuse.qa.fafram8.configuration;

import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.xml.ClusterModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.ContainerModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.FrameworkConfigurationModel;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Fafram8 XML configuration parser class.
 * <p/>
 * Created by mmelko on 9/8/15.
 */
@Slf4j
public class ConfigurationParser {
	//Parsed object cluster representation.
	private ClusterModel clusterModel;

	//Unique name incrementer.
	private int uniqueNameIncrement = 0;

	@Setter
	private String path;

	/**
	 * Constructor.
	 */
	public ConfigurationParser() {
	}

	/**
	 * Parse referenced Fafram8 XML configuration.
	 *
	 * @param path path to Fafram8 XML configuration
	 * @throws JAXBException if an error was encountered while creating the Unmarshaller object.
	 */
	public void parseConfigurationFile(String path) throws JAXBException {
		log.info("Configuration parser started.");

		log.info("Creating unmarshaller.");
		final JAXBContext jaxbContext = JAXBContext.newInstance(ClusterModel.class);
		final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		log.info("Unmarshalling cluster model from " + path);
		clusterModel = (ClusterModel) jaxbUnmarshaller.unmarshal(new File(path));

		//TODO(ecervena): provisional debug logging
		for (ContainerModel containerModel : clusterModel.getContainerModelList()) {
			log.info(containerModel.toString());
		}
	}

	/**
	 * Call of this method will set parsed framework configuration and build container
	 * objects parsed from Fafram8 XML configuration.
	 */
	public void buildContainers() {
		log.info("Building containers.");
		setFrameworkConfiguration(clusterModel.getFrameworkConfigurationModel());

		for (ContainerModel containerModel : clusterModel.getContainerModelList()) {
			for (int i = 1; i <= containerModel.getInstances(); i++) {

				Container container = null;

				switch (containerModel.getContainerType()) {
					case "root": {
						final RootContainer.RootBuilder builder = RootContainer.builder().name(returnUniqueName(containerModel));
						if (containerModel.getUsername() != null) {
							builder.user(containerModel.getUsername());
						}
						if (containerModel.getPassword() != null) {
							builder.password(containerModel.getPassword());
						}

						final Node node = Node.builder().host(containerModel.getNode().getHost())
								.username(containerModel.getNode().getUsername())
								.password(containerModel.getNode().getPassword())
								.build();
						builder.node(node);
						container = builder.build();
						break;
					}
					case "ssh": {
						final SshContainer.SshBuilder builder = SshContainer.builder().name(returnUniqueName(containerModel));
						final Node node = Node.builder().host(containerModel.getNode().getHost())
								.username(containerModel.getNode().getUsername())
								.password(containerModel.getNode().getPassword())
								.build();
						builder.node(node);
						final Container parentContainer = ContainerManager.getContainer(containerModel.getParentContainer());
						if (parentContainer == null) {
							throw new FaframException("Parent container does not exists.");
						}
						builder.parent(parentContainer);
						container = builder.build();
						break;
					}
					case "child": {
						final ChildContainer.ChildBuilder builder = ChildContainer.builder().name(returnUniqueName(containerModel));
						final Container parentContainer = ContainerManager.getContainer(containerModel.getParentContainer());
						if (parentContainer == null) {
							throw new FaframException("Parent container does not exists.");
						}
						builder.parent(parentContainer);
						container = builder.build();
						break;
					}
					default:
						break;
				}

				if (container != null) {
					ContainerManager.getContainerList().add(container);
				}
			}
			resetUniqueNameIncrement();
		}
	}

	/**
	 * Set unmarshaled framework properties.
	 *
	 * @param frameworkConfigurationModel XML configuration mapping object
	 */
	public void setFrameworkConfiguration(FrameworkConfigurationModel frameworkConfigurationModel) {
		//do the magic
	}

	/**
	 * Return unique container name for container mapped by containerModel. This method enables multiple instances
	 * specification in XML configuration. E.g. &lt;container instances=5&gt;&lt;name&gt;xxx&lt;/name&gt;&lt;/container&gt;
	 * will results into 5 containers named xxx-1,xxx-2,xxx-3,xxx-4,xxx-5.
	 *
	 * @param containerModel XML container mapping object
	 * @return unique name
	 */
	private String returnUniqueName(ContainerModel containerModel) {
		if (containerModel.getInstances() <= 1) {
			return containerModel.getName();
		}
		uniqueNameIncrement++;
		return containerModel.getName() + "-" + uniqueNameIncrement;
	}

	/**
	 * Private method reseting uniqueNameIncrement field.
	 */
	private void resetUniqueNameIncrement() {
		uniqueNameIncrement = 0;
	}
}

