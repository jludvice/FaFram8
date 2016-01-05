package org.jboss.fuse.qa.fafram8.test;

/**
 * Fafram test base class.
 * Created by avano on 5.1.16.
 */
public class FaframTestBase {
	public static final String CURRENT_URL = "http://download.eng.rdu2.redhat.com/brewroot/repos/jb-fuse-6.2-build/" +
			"latest/maven/org/jboss/fuse/jboss-fuse-full/" + System.getProperty("fuse.version") + "/jboss-fuse-full-"
			+ System.getProperty("fuse.version") + ".zip";

	/**
	 * Returns the url to the specified version.
	 * @param version version (e.g. 6.2.1-)
	 * @return url to the specified version
	 */
	public static String getVersion(String version) {
		return "http://download.eng.rdu2.redhat.com/brewroot/repos/jb-fuse-6.2-build/latest/maven/org/jboss/fuse/jboss-fuse-full/" + version
				+ "/jboss-fuse-full-" + version + ".zip";
	}
}
