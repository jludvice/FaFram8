package org.jboss.fuse.qa.fafram8.invoker;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used for uploading bundles to fabric maven proxy.
 * Created by ecervena on 1/13/15.
 * <p/>
 * Object of this class can invoke maven execution with clean,package and deploy target. It is intended for building
 * specified project and uploading its package to maven proxy service. You have to specify relative project path, maven
 * upload url and repository to upload to in target project distributionManagement tag. e.g.<br>
 * &lt;distributionManagement&gt;<br>
 * &lt;repository&gt;<br>
 * &lt;tid&gt;fuse-maven-proxy&lt;/id&gt;<br>
 * &lt;tname&gt;fuse-maven-proxy&lt;/name&gt;<br>
 * &lt;turl&gt;${mvn.proxy.upload.url}&lt;/url&gt;<br>
 * &lt;/repository&gt;<br>
 * &lt;/distributionManagement&gt;
 * <p/>
 * Check jbossqe-fuse/fuse-tests/fabric-related-tests/fabric-maven-proxy-test for test example.
 */
@Slf4j
public class MavenPomInvoker {

	@Setter
	private String projectPath = null;

	@Setter
	private String uploadUrl = null;

	/**
	 * @param projectPath absolute or relative (to root project pom) path of target project
	 * @param uploadUrl full url of maven proxy upload service. Use credentials if needed.
	 * e.g. http://admin:admin@host:port/maven/upload
	 */
	public MavenPomInvoker(String projectPath, String uploadUrl) {
		this.projectPath = projectPath;
		this.uploadUrl = uploadUrl;
	}

	/**
	 * Invokes maven execution with clean,package and deploy target. You have to specify
	 * repository to upload in target projects distributionManagement tag. e.g.<br>
	 * &lt;distributionManagement&gt;<br>
	 * &lt;repository&gt;<br>
	 * &lt;tid&gt;fuse-maven-proxy&lt;/id&gt;<br>
	 * &lt;tname&gt;fuse-maven-proxy&lt;/name&gt;<br>
	 * &lt;turl&gt;${mvn.proxy.upload.url}&lt;/url&gt;<br>
	 * &lt;/repository&gt;<br>
	 * &lt;/distributionManagement&gt;
	 */
	public void installFile() throws URISyntaxException, MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(projectPath));

		Properties props = new Properties();
		props.setProperty("mvn.proxy.upload.url", uploadUrl);

		request.setProperties(props);
		request.setGoals(Arrays.asList("clean", "package", "deploy"));

		Invoker invoker = new DefaultInvoker();
		log.info("Invoking maven target " + projectPath);
		invoker.execute(request);
	}

	/**
	 * Invokes maven execution with clean,package and deploy target. You have to specify
	 * repository to upload in target projects distributionManagement tag. e.g.<br>
	 * &lt;distributionManagement&gt;<br>
	 * &lt;repository&gt;<br>
	 * &lt;tid&gt;fuse-maven-proxy&lt;/id&gt;<br>
	 * &lt;tname&gt;fuse-maven-proxy&lt;/name&gt;<br>
	 * &lt;turl&gt;${mvn.proxy.upload.url}&lt;/url&gt;<br>
	 * &lt;/repository&gt;<br>
	 * &lt;/distributionManagement&gt;
	 *
	 * @param projectPath absolute or relative (to root project pom) path of target project
	 * @param uploadUrl full url of maven proxy upload service. Use credentials if needed.
	 * e.g. http://admin:admin@host:port/maven/upload
	 */
	public void installFile(String projectPath, String uploadUrl) throws URISyntaxException, MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(projectPath));

		Properties props = new Properties();
		props.setProperty("mvn.proxy.upload.url", uploadUrl);

		request.setProperties(props);
		request.setGoals(Arrays.asList("clean", "package", "deploy"));

		Invoker invoker = new DefaultInvoker();
		log.info("Invoking maven target " + projectPath);
		invoker.execute(request);
	}
}
