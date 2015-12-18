package org.jboss.fuse.qa.fafram8.cluster.brokers;

import org.apache.commons.lang3.tuple.Pair;

import org.jboss.fuse.qa.fafram8.cluster.Container;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Class represents broker object.
 * Created by mmelko on 09/10/15.
 */
public class Broker {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private boolean ssl;

	@Setter
	@Getter
	private String kind;

	@Getter
	@Setter
	private String group = "default";

	@Getter
	@Setter
	private String data;

	@Getter
	@Setter
	private Map<String, Pair<String, String>> networks = new HashMap<String, Pair<String, String>>();

	@Getter
	@Setter
	private Container container;

	@Getter
	@Setter
	private String parentProfile;

	@Getter
	@Setter
	private Map<String, String> pids = new HashMap<>();

	/**
	 * Constructor.
	 */
	public Broker() {
		super();
	}

	/**
	 * Copy constructor.
	 * @param broker broker which should be cloned.
	 */
	public Broker(Broker broker) {
		this.name = broker.getName();
		this.ssl = broker.isSsl();
		this.kind = broker.getKind();
		this.group = broker.getGroup();
		this.data = broker.getData();
		this.networks = new HashMap<String, Pair<String, String>>(broker.getNetworks());
		this.parentProfile = broker.getParentProfile();
		this.pids = new HashMap<>(broker.getPids());
	}

	/**
	 * List of commands needed for creation of the broker.
	 *
	 * @return list of commands.
	 */
	public List<String> getCreateCommands() {
		final List<String> commands = new LinkedList<>();
		commands.add(mqCreate());

		for (Map.Entry<String, String> entry : pids.entrySet()) {
			commands.add("profile-edit " + mqServerPidModification(entry.getKey(), entry.getValue()) + getProfileName());
		}

		return commands;
	}

	/**
	 * Get name of the profile of the broker.
	 *
	 * @return profile.
	 */
	public String getProfileName() {
		return "mq-broker-" + group + "." + name;
	}

	/**
	 * Creates mq-create command.
	 *
	 * @return mq-create command.
	 */
	private String mqCreate() {
		String cmd = "mq-create ";

		if (!ssl) {
			cmd += "--no-ssl";
		}
		if (kind != null && !"".equals(kind)) {
			cmd += " --kind " + kind;
		}
		if (parentProfile != null && !"".equals(parentProfile)) {
			cmd += " --parent-profile " + parentProfile;
		}
		if (group != null && !"".equals(group)) {
			cmd += " --group " + group;
		}
		if (data != null && !"".equals(data)) {
			cmd += " --data " + data;
		}

		for (Map.Entry<String, Pair<String, String>> network : networks.entrySet()) {
			cmd += " --network " + network.getKey();
			if (network.getValue() != null) {
				if (network.getValue().getLeft() != null && !"".equals(network.getValue().getLeft())) {
					cmd += " --network-username " + network.getValue().getLeft() + " --network-password " + network.getValue().getRight();
				}
			}
		}

		cmd += " " + name;
		return cmd;
	}

	/**
	 * Creates command for broker profile.
	 *
	 * @param key key of pid
	 * @param value value of pid
	 * @return proper value.
	 */
	private String mqServerPidModification(String key, String value) {
		return "io.fabric8.mq.fabric.server-" + name + "/" + key + "=" + value + " ";
	}
}
