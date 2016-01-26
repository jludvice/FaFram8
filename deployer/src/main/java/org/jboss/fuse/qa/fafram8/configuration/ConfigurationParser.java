package org.jboss.fuse.qa.fafram8.configuration;

import org.apache.commons.lang.ObjectUtils;
import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ChildContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.SshContainerType;
import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.cluster.xml.ClusterModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.ContainerModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.FrameworkConfigurationModel;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Fafram8 XML configuration parser class.
 * 
 * Created by mmelko on 9/8/15.
 */
@Slf4j
public class ConfigurationParser {
	
    //Parsed object cluster representation.
	private static ClusterModel clusterModel;
    
    //Unique name incrementer.
	private static int uniqueNameIncrement = 0;

	@Setter
	private ContainerBuilder containerBuilder;

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
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws JAXBException
     */
	public void parseConfigurationFile(String path) throws ParserConfigurationException, IOException, SAXException, JAXBException {
		log.info("Configuration parser started.");

		log.info("Creating unmarshaller.");
		JAXBContext jaxbContext = JAXBContext.newInstance(ClusterModel.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		log.info("Unmarshalling cluster model from " + path);
		clusterModel = (ClusterModel) jaxbUnmarshaller.unmarshal(new File(path));
		
		//TODO(ecervena): provisional debug logging 
		for (ContainerModel containerModel: clusterModel.getContainerModelList()) {
			log.info(containerModel.toString());
		}
	}

    /**
     * Call of this method will set parsed framework configuration and build container
     * objects parsed from Fafram8 XML configuration.
     * 
     */
	public void buildContainers() {
		log.info("Building containers.");
		setFrameworkConfiguration(clusterModel.getFrameworkConfigurationModel());
		
		for (ContainerModel containerModel: clusterModel.getContainerModelList()) {
			for (int i = 1; i <= containerModel.getInstances(); i++) {
				
				Container container = new Container(returnUniqueName(containerModel));

				switch (containerModel.getContainerType()) {
					case "root": {
						RootContainerType rootContainerType = new RootContainerType(container);
						if (containerModel.getUsername() != null)
							rootContainerType.setUsername(containerModel.getUsername());
						if (containerModel.getPassword() != null)
							rootContainerType.setPassword(containerModel.getPassword());
						container.setContainerType(rootContainerType);
                        //TODO(ecervena): Should be port 22 hardcoded??? Maybe init it in Node class.
						Node node = Node.builder().host(containerModel.getNode().getHost())
													.username(containerModel.getNode().getUsername())
													.password(containerModel.getNode().getPassword())
													.port(22)
													.build();
						container.setHostNode(node);
						break;
					}
					case "ssh": {
						SshContainerType containerType = new SshContainerType();
						containerType.setContainer(container);
						container.setContainerType(containerType);
						
						Node node = Node.builder().host(containerModel.getNode().getHost())
								.username(containerModel.getNode().getUsername())
								.password(containerModel.getNode().getPassword())
								.port(22)
								.build();
						container.setHostNode(node);
						
						try {
							Container parentContainer = getContainerByName(containerModel.getParentContainer());
							if (parentContainer == null) throw new NullPointerException();
							log.info("Assigning parrent container " + parentContainer.getName() + " to container " + container.getName());
							container.setParentContainer(parentContainer);
						} catch (NullPointerException npe) {
							throw new FaframException("Parent container does not exists.",npe);
						}
						break;
					}
					case "child": {
						ChildContainerType containerType = new ChildContainerType();
						containerType.setContainer(container);
						container.setContainerType(containerType);

						try {
							Container parentContainer = getContainerByName(containerModel.getParentContainer());
							if (parentContainer == null) throw new NullPointerException();
							container.setParentContainer(parentContainer);
						} catch (NullPointerException npe) {
							throw new FaframException("Parent container does not exists.",npe);
						}
						break;
					}
				}
				Fafram.addContainer(container);
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
     * specification in XML configuration. E.g. <container instances=5><name>xxx</name></container> will
     * results into 5 containers named xxx-1,xxx-2,xxx-3,xxx-4,xxx-5.
     * 
     * @param containerModel XML container mapping object
     * @return
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
	
	//TODO:ecervena remove this after upstream merge (functionality shuld be already implemented in Fafram class).
	/**
	 * Uses future Fafram.getContainerByName() and remove this.
	 */
	private Container getContainerByName(String name) {
		log.debug("Searching for container name: " + name);
        return Fafram.getContainer(name);
	}
	
}

