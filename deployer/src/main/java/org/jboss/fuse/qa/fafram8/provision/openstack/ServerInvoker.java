package org.jboss.fuse.qa.fafram8.provision.openstack;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread worker class. OpenStack client is created with shared session from OpenStackClient singleton. Worker purpose
 * is spawn one single server per thread, wait for "active" status and register created server to OpenStackProvisionProvider.
 * <p/>
 * Created by ecervena on 28.9.15.
 */
@Slf4j
public class ServerInvoker implements Runnable {

	private static final int BOOT_TIMEOUT = 300000;

	//Name of the node
	private String nodeName;

	/**
	 * Constructor for thread worker.
	 *
	 * @param nodeName name of the node
	 */
	public ServerInvoker(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * Method executed in thread.
	 */
	//@Override
	public void run() {
		// TODO(ecervena): why is this duplicated in OpenStackProvisionProvider.spawnNewServer()?
		log.info("Creating server inside thread for container: " + nodeName);
		final OSClient os = OSFactory.clientFromAccess(OpenStackClient.getInstance().getAccess());
		final ServerCreate serverCreate = os
				.compute()
				.servers()
				.serverBuilder()
				.image(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_IMAGE))
				.name(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX) + "-" + nodeName)
				.flavor(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_FLAVOR))
				.keypairName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_KEYPAIR))
				.networks(Arrays.asList(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NETWORKS).split(",")))
				.build();
		//TODO(ecervena): do something smarter with server boot timeout
		final Server server = os.compute().servers().bootAndWaitActive(serverCreate, BOOT_TIMEOUT);
		OpenStackProvisionProvider.registerServer(server);
		OpenStackProvisionProvider.addServerToPool(server);
//		waitForRunningServer(server);
	}

	private void waitForRunningServer(Server server) {
		final int step = 3;
		final long timeout = step * 1000L;
		boolean online = false;

		int elapsed = 0;
		log.info("Waiting for the server " + server.getName() + " to be in \"running\" state");

		String status;
		while (!online) {
			// Check if the time is up
			if (elapsed > SystemProperty.getOpenstackWaitTime()) {
				log.error("Openstack machine wasn't running after " + SystemProperty.getOpenstackWaitTime() + " seconds");
				// todo (avano): throw excepion - InstanceNotRunningException
				break;
			}

			status = server.getPowerState();

			// "1" should be running, didn't get any docs about this
			if (!"1".equals(status)) {
				log.debug("Remaining time: " + (SystemProperty.getOpenstackWaitTime() - elapsed) + " seconds. "
						+ ("".equals(status) ? "" : ("(" + status + ")")));
				elapsed += step;
			} else {
				online = true;
				log.info("Server online");
			}

			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
