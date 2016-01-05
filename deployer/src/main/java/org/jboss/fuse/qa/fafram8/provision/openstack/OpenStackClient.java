package org.jboss.fuse.qa.fafram8.provision.openstack;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

/**
 * OpenStack client entry point holding authenticated token. This class should be instatied only once.
 * <p/>
 * Created by ecervena on 28.9.15.
 */
public final class OpenStackClient {

	//static property holding created OpenStackClient singleton
	private static OpenStackClient openStackClient = null;

	//Authenticated openstack4j api client
	private static OSClient os = null;

	/**
	 * Constructor with OpenStack client factory authentication.
	 */
	private OpenStackClient() {
		os = OSFactory.builder()
				.endpoint(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_URL))
				.credentials(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_USER), SystemProperty.getExternalProperty(FaframConstant
						.OPENSTACK_PASSWORD))
				.tenantName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_TENANT))
				.authenticate();
	}

	/**
	 * Singleton access method.
	 *
	 * @return OpenStackClient instance
	 */
	public static OSClient getInstance() {
		if (openStackClient == null) {
			openStackClient = new OpenStackClient();
		}
		return os;
	}
}
