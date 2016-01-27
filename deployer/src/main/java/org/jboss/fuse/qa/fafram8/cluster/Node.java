package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
	 * Constructor.
	 */
	public Node() {
		super();
	}
}
