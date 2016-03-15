package org.jboss.fuse.qa.fafram8.invoker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class encapsulating attributes of maven project that can be invoked with MavenPomInvoker.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@ToString
public class MavenProject {

	// Absolute path to pom.xml of the maven project
	@Setter
	@Getter
	private String projectPath;

	// Properties that should be added to execution of the maven project
	@Setter
	@Getter
	private Map<String, String> properties;

	// List of goals that should be executed on the maven project (e.g. install, clean, etc.)
	@Setter
	@Getter
	private List<String> goals;

	/**
	 * Constructor.
	 *
	 * @param projectPath absolute path to pom.xml of the maven project
	 * @param properties properties that should be added to execution of the maven project
	 * @param goals list of goals that should be executed on the maven project (e.g. install, clean, etc.)
	 */
	public MavenProject(String projectPath, Map<String, String> properties, List<String> goals) {
		this.goals = goals;
		this.projectPath = projectPath;
		this.properties = properties;
	}

	/**
	 * Constructor.
	 *
	 * @param projectPath absolute path to pom.xml of the maven project
	 * @param properties properties that should be added to execution of the maven project
	 * @param goals list of goals that should be executed on the maven project (e.g. install, clean, etc.)
	 */
	public MavenProject(String projectPath, Map<String, String> properties, String... goals) {
		this.goals = Arrays.asList(goals);
		this.projectPath = projectPath;
		this.properties = properties;
	}
}
