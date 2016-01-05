package org.jboss.fuse.qa.fafram8.junit;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import lombok.extern.slf4j.Slf4j;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

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

		JiraClient jiraClient;
		if (SystemProperty.getExternalProperty(FaframConstant.JIRA_USER) == null
				|| SystemProperty.getExternalProperty(FaframConstant.JIRA_PASSWORD) == null) {
			jiraClient = new JiraClient(SystemProperty.getExternalProperty(FaframConstant.JIRA_URL));
		} else {
			final BasicCredentials credentials = new BasicCredentials(SystemProperty.getExternalProperty(FaframConstant.JIRA_USER),
					SystemProperty.getExternalProperty(FaframConstant.JIRA_PASSWORD));
			jiraClient = new JiraClient(SystemProperty.getExternalProperty(FaframConstant.JIRA_URL), credentials);
		}

		Issue issue;

		try {
			issue = jiraClient.getIssue(jira.value());
		} catch (JiraException e) {
			log.error("Jira Exception caught: " + e.getLocalizedMessage());
			notifier.fireTestFailure(new Failure(describeChild(method), e));
			return;
		}

		if (StringUtils.equalsIgnoreCase(issue.getStatus().toString(), "resolved")
				|| StringUtils.equalsIgnoreCase(issue.getStatus().toString(), "closed")) {
			log.info("Starting " + method.getName() + String.format(" (JIRA %s is %s)", jira.value().toUpperCase(),
					issue.getStatus().toString()));
			super.runChild(method, notifier);
		} else {
			log.info("Skipping " + method.getName() + String.format(" (JIRA %s is %s)", jira.value().toUpperCase(),
					issue.getStatus().toString()));
			notifier.fireTestIgnored(describeChild(method));
		}
	}
}
