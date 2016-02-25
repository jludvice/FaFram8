package org.jboss.fuse.qa.fafram8.junit;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.Version;

/**
 * Fafram JUnit Test runner class.
 * Created by avano on 29.10.15.
 */
@Slf4j
public class FaframTestRunner extends BlockJUnit4ClassRunner {
	/**
	 * Constructor.
	 *
	 * @param klass class
	 * @throws InitializationError initialization error
	 */
	public FaframTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public void runChild(FrameworkMethod method, RunNotifier notifier) {
		final Jira jira = method.getAnnotation(Jira.class);
		if (jira == null) {
			log.info("Starting " + method.getName());
			super.runChild(method, notifier);
			return;
		}

		final JiraClient jiraClient = createClient();

		Issue issue;

		try {
			issue = jiraClient.getIssue(jira.value());
		} catch (JiraException e) {
			log.error("Jira Exception caught: " + e.getLocalizedMessage());
			notifier.fireTestFailure(new Failure(describeChild(method), e));
			return;
		}

		if ((StringUtils.equalsIgnoreCase(issue.getStatus().toString(), "resolved")
				|| StringUtils.equalsIgnoreCase(issue.getStatus().toString(), "closed"))) {
			if (matchVersion(issue)) {
				log.info("Starting " + method.getName() + String.format(" (JIRA %s is %s)", jira.value().toUpperCase(), issue.getStatus().toString()));
				super.runChild(method, notifier);
			} else {
				log.info(String.format("Skipping %s (JIRA %s is %s, but major fix version is not a current version %s)", method.getName(),
						jira.value()
						.toUpperCase(), issue.getStatus().toString(), SystemProperty.getFuseVersion()));
				notifier.fireTestIgnored(describeChild(method));
			}
		} else {
			log.info(String.format("Skipping %s (JIRA %s is %s)", method.getName(), jira.value().toUpperCase(), issue.getStatus().toString()));
			notifier.fireTestIgnored(describeChild(method));
		}
	}

	/**
	 * Checks if the JIRA fix version is the current version. It first parses the major version from the fixVersion list and checks if the
	 * major version is substring of current version.
	 *
	 * @param issue issue to check
	 * @return boolean true if the fixed version is current version, false otherwise
	 */
	private boolean matchVersion(Issue issue) {
		// Additional check if fixed version == current version
		final List<Version> fixVersions = issue.getFixVersions();
		// Resolved as wont fix for example
		if (fixVersions.size() == 0) {
			return true;
		}

		final String currentShortVersion = StringUtils.substringBefore(SystemProperty.getFuseVersion(), ".redhat");

		for (Version v : fixVersions) {
			final String versionShort = StringUtils.substringAfterLast(v.toString(), "-");
			if (currentShortVersion.compareTo(versionShort) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the jira client instance.
	 * @return jira client instance
	 */
	private JiraClient createClient() {
		if (SystemProperty.getExternalProperty(FaframConstant.JIRA_USER) == null
				|| SystemProperty.getExternalProperty(FaframConstant.JIRA_PASSWORD) == null) {
			return new JiraClient(SystemProperty.getExternalProperty(FaframConstant.JIRA_URL));
		} else {
			final BasicCredentials credentials = new BasicCredentials(SystemProperty.getExternalProperty(FaframConstant.JIRA_USER),
					SystemProperty.getExternalProperty(FaframConstant.JIRA_PASSWORD));
			return new JiraClient(SystemProperty.getExternalProperty(FaframConstant.JIRA_URL), credentials);
		}
	}
}
