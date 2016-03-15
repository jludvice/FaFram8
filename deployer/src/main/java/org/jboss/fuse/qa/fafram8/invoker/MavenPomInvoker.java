package org.jboss.fuse.qa.fafram8.invoker;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used for uploading bundles to fabric maven proxy.
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
 * Created by ecervena on 1/13/15.
 */
@Slf4j
public class MavenPomInvoker {

	@Setter
	private String projectPath = null;

	@Setter
	private String uploadUrl = null;

	/**
	 * Constructor.
	 *
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
	 *
	 * @throws URISyntaxException exception
	 * @throws MavenInvocationException exception
	 */
	public void installFile() throws URISyntaxException, MavenInvocationException {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(projectPath));

		final Properties props = new Properties();
		props.setProperty("mvn.proxy.upload.url", uploadUrl);

		request.setProperties(props);
		request.setGoals(Arrays.asList("clean", "package", "deploy"));

		final Invoker invoker = new DefaultInvoker();
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
	 * @throws URISyntaxException exception
	 * @throws MavenInvocationException exception
	 */
	public void installFile(String projectPath, String uploadUrl) throws URISyntaxException, MavenInvocationException {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(projectPath));

		final Properties props = new Properties();
		props.setProperty("mvn.proxy.upload.url", uploadUrl);

		request.setProperties(props);
		request.setGoals(Arrays.asList("clean", "package", "deploy"));

		final Invoker invoker = new DefaultInvoker();
		log.info("Invoking maven target " + projectPath);
		invoker.execute(request);
	}

	/**
	 * Invokes maven execution on specified project with specified goals.
	 * There is no upload or anything else done with the specified project regarding to running Fuse.
	 *
	 * @param projectPath absolute or relative (to root project pom) path of target project
	 * @param goals list of maven goals that should be executed on target project
	 * @throws URISyntaxException exception
	 * @throws MavenInvocationException exception
	 */
	public static void buildMvnProject(String projectPath, Map<String, String> properties, List<String> goals) throws URISyntaxException, MavenInvocationException {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(projectPath));

		if (properties != null) {
			final Properties props = new Properties();
			for (String property : properties.keySet()) {
				props.setProperty(property, properties.get(property));
			}
			request.setProperties(props);
		}

		request.setGoals(goals);

		final Invoker invoker = new DefaultInvoker();
		log.info("Invoking maven target " + projectPath);
		invoker.execute(request);
	}

	/**
	 * Invokes maven execution on specified project with specified goals.
	 * There is no upload or anything else done with the specified project regarding to running Fuse.
	 *
	 * @param projectPath absolute or relative (to root project pom) path of target project
	 * @param goals maven goals that should be executed on target project
	 * @throws URISyntaxException exception
	 * @throws MavenInvocationException exception
	 */
	public static void buildMvnProject(String projectPath, String... goals) throws URISyntaxException, MavenInvocationException {
		buildMvnProject(projectPath, null, Arrays.asList(goals));
	}

	/**
	 * Invokes maven execution on specified project with specified goals.
	 * There is no upload or anything else done with the specified project regarding to running Fuse.
	 *
	 * @param projectPath absolute or relative (to root project pom) path of target project
	 * @param goals maven goals that should be executed on target project
	 * @throws URISyntaxException exception
	 * @throws MavenInvocationException exception
	 */
	public static void buildMvnProject(String projectPath, Map<String, String> properties, String... goals) throws URISyntaxException, MavenInvocationException {
		buildMvnProject(projectPath, properties, Arrays.asList(goals));
	}

	/**
	 * Invokes maven execution on specified project with specified goals.
	 * There is no upload or anything else done with the specified project regarding to running Fuse.
	 *
	 * @param project object containing information about maven project to be executed
	 * @throws MavenInvocationException exception
	 * @throws URISyntaxException exception
	 */
	public static void buildMvnProject(MavenProject project) throws MavenInvocationException, URISyntaxException {
		buildMvnProject(project.getProjectPath(), project.getProperties(), project.getGoals());
	}
}
