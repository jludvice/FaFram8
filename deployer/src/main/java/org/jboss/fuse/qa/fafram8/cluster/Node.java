package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class representing node.
 * Created by mmelko on 09/10/15.
 */
@ToString
@Builder
public class Node implements Cloneable {
	@Getter
	@Setter
	private String nodeId;

	@Getter
	@Setter
	private boolean live;

	@Getter
	@Setter
	private String host;

	@Getter
	@Setter
	private int port = SystemProperty.getHostPort();

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private String password;

	@Getter
	@Setter
	private String privateKey;

	@Getter
	@Setter
	private String passPhrase;

	/**
	 * Constructor.
	 */
	public Node() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param hostNode node which will be cloned
	 */
	public Node(Node hostNode) {
		this.nodeId = hostNode.getNodeId();
		this.host = hostNode.getHost();
		this.port = hostNode.getPort();
		this.username = hostNode.getUsername();
		this.password = hostNode.getPassword();
		this.privateKey = hostNode.getPrivateKey();
		this.passPhrase = hostNode.getPassPhrase();
	}

	/**
	 * All args constructor.
	 *
	 * @param nodeId ID of node
	 * @param live is live
	 * @param host host name
	 * @param port port
	 * @param username username
	 * @param password password
	 * @param privateKey private key
	 * @param passPhrase pass phrase
	 */
	@java.beans.ConstructorProperties({"nodeId", "live", "host", "port", "username", "password", "privateKey", "passPhrase"})
	public Node(String nodeId, boolean live, String host, int port, String username, String password, String privateKey, String passPhrase) {
		this.nodeId = nodeId;
		this.live = live;
		this.host = host;
		if (port == 0) {
		} else {
			this.port = port;
		}
		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
		this.passPhrase = passPhrase;
	}
}
