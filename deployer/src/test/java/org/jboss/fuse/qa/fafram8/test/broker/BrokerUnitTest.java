package org.jboss.fuse.qa.fafram8.test.broker;

import org.jboss.fuse.qa.fafram8.cluster.broker.Broker;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by mmelko on 17/03/16.
 */
public class BrokerUnitTest {

	private static Broker broker;

	private static final String NAME = "broker";
	private static final String DATA = "/home/fuse/data";
	private static final String KIND = Broker.STANDALONE;
	private static final String PROFILE = "parentProfile";
	private static final String GROUP = "brokerGroup";

	@BeforeClass
	public static void initBroker() {
		broker = Broker.builder()
				.data(DATA)
				.name(NAME)
				.group(GROUP)
				.addNetwork("network")
				.kind(Broker.STANDALONE)
				.parentProfile(PROFILE)
				.addPid("network.consumerTTL", "1")
				.addPid("network.messageTTL", "29")
				.addPid("network.replayWhenNoConsumers", "true")
				.addPid("network.replayDelay", "1000")
				.addPid("network.initialReconnectDelay", "1500")
				.build();
	}

	@Test
	public void test() {
		for (String s : broker.getCreateCommands()) {
			System.out.println(s);
		}

		String createCommand = broker.getCreateCommands().get(0);

		Assert.assertTrue(createCommand.contains(" --group " + GROUP));
		Assert.assertTrue(createCommand.contains(" --data " + DATA));
		Assert.assertTrue(createCommand.contains(" " + NAME));
		Assert.assertTrue(createCommand.contains(" --kind " + KIND));
		Assert.assertTrue(createCommand.contains(" --parent-profile " + PROFILE));

	Broker broker2 = Broker.builder(broker)
				.name("name2").group("group2")
				.data(DATA)
				.addNetwork(GROUP, "admin", "admin")
				.kind(KIND)
				.parentProfile(PROFILE)
				.addPid("openwire-port","61616")
				.build();

		createCommand = broker2.getCreateCommands().get(0);

		Assert.assertTrue(createCommand.contains(" --group group2"));
		Assert.assertTrue(createCommand.contains(" --data " + DATA));
		Assert.assertTrue(createCommand.contains(" name2"));
		Assert.assertTrue(createCommand.contains(" --kind " + KIND));
		Assert.assertTrue(createCommand.contains(" --parent-profile " + PROFILE));
		System.out.println(broker2.getProfileName());
		Assert.assertTrue(broker2.getPids().size()>broker.getPids().size());
	}
}
