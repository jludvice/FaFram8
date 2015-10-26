package org.jboss.fuse.qa.fafram8.ConfigParser;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * <p/>
 * Created by mmelko on 9/8/15.
 */
public class ConfigurationParser {

	@Setter
	private String path;

	@Getter
	private List<Container> containerList = new LinkedList<Container>();

	private Container globalContainerConf;

	/**
	 * Constructor.
	 */
	public ConfigurationParser(String path) {
		this.path = path;
	}

	/**
	 * Parses the configuration file.
	 *
	 * @param path path
	 */
	public void parseConfigurationFile(String path) throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document document = builder.parse(new File(path));

	//	document.
	}

	public void parseConfigurationFile() throws ParserConfigurationException, SAXException, IOException {
		parseConfigurationFile(this.path);
	}


	private void parseFrameworkConfiguration(){
		//TODO (mmelko)
	}

	private void parseCluster(){}

	private void parseContainers(){}
}
