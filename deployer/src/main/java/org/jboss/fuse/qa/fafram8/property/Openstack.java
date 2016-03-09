package org.jboss.fuse.qa.fafram8.property;

import lombok.Getter;

/**
 * Openstack class. It is just enum of possible javas on Openstack.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public enum Openstack {
	OPENJDK7("/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.75-2.5.4.2.el7_0.x86_64"), OPENJDK8("not defined"), JDK7("/qa/tools/opt/jdk1.7.0_last"), JDK8("/qa/tools/opt/jdk1.8.0_71");

	@Getter
	private String path;

	/**
	 * Constructor.
	 *
	 * @param path path to java home
	 */
	Openstack(String path) {
		this.path = path;
	}
}
