package org.jboss.fuse.qa.fafram8.configuration;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Created by mmelko on 9/8/15.
 */
@Slf4j
public class ConfigurationParser {

	private static final String CLUSTER_ELEMENT = "cluster";
	private static final String FRAMEWORK_ELEMENT = "framework";

	@Setter
	private ContainerBuilder containerBuilder;

	private boolean autoName = false;
	private int containerCount = -1;

	@Setter
	private String path;

	@Getter
	private List<Container> containerList = new LinkedList<Container>();

	private Container globalContainerConf;

	/**
	 * Constructor.
	 */
	public ConfigurationParser() {
	}

	/**
	 * Constructor.
	 *
	 * @param path to configuration file
	 */
	public ConfigurationParser(String path) {
		super();
		this.path = path;
	}

	/**
	 * Parses the configuration file.
	 *
	 * @param path path to confifuration file
	 * @throws IOException exception
	 * @throws SAXException exception
	 * @throws ParserConfigurationException exception
	 */
	public void parseConfigurationFile(String path) throws IOException, SAXException, ParserConfigurationException {
		log.info("Start parsing the configuration file");
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(true);

		final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		final Document document = builder.parse(new File(path));

		parseCluster(document.getDocumentElement().getElementsByTagName(CLUSTER_ELEMENT));
		parseFrameworkConfiguration(document.getDocumentElement().getElementsByTagName(FRAMEWORK_ELEMENT).item(0));
	}

	/**
	 * Parse the configuration file.
	 *
	 * @throws ParserConfigurationException exception
	 * @throws SAXException exception
	 * @throws IOException exception
	 */
	public void parseConfigurationFile() throws ParserConfigurationException, SAXException, IOException {
		parseConfigurationFile(this.path);
	}

	/**
	 * Method for parsing framework configuration such a Host, ssh credentials etc.
	 *
	 * @param conf XML configuration node
	 */
	private void parseFrameworkConfiguration(Node conf) {
		final NodeList values = conf.getChildNodes();
		for (int i = 0; i < values.getLength(); i++) {
			final Node n = values.item(i);

			if (n.getNodeType() == Node.ELEMENT_NODE) {
				final Element element = (Element) n;
				SystemProperty.set(element.getNodeName(), element.getTextContent());
			}
		}
	}

	/**
	 * Parse cluster element.
	 *
	 * @param clusterNodeList clusterNodeList element
	 */
	private void parseCluster(NodeList clusterNodeList) {
		for (int i = 0; i < clusterNodeList.getLength(); i++) {
			final Node n = clusterNodeList.item(i);

			if (n.getNodeType() == Node.ELEMENT_NODE) {
				final Element element = (Element) n;
				if ("global".equals(element.getNodeName())) {
					parseContainer(element, true);
				} else if (("containers").equals(element.getNodeName())) {
					parseContainers(element);
				}
			}
		}
	}

	/**
	 * Parse containers element.
	 *
	 * @param containers element which contains containers and some metadata about containers
	 */
	private void parseContainers(Element containers) {
		//TODO(mmelko): finish this
	}

	/**
	 * Parse.
	 *
	 * @param container container xml element
	 * @param isGlobal if true, container configuration is global and it isn't set to builder
	 */
	private void parseContainer(Element container, boolean isGlobal) {
		//TODO(mmelko): finish
	}

	private Map<String, String> parseSimpleElement(Node n) {
		return null;
	}
}

