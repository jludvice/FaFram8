package org.jboss.fuse.qa.fafram8.modifier;

import org.jboss.fuse.qa.fafram8.executor.Executor;

/**
 * Modifier interface.
 * Created by jludvice on 4.8.15.
 */
public interface Modifier {
	/**
	 * This method will be called before fuse is started.
	 */
	void execute();

	/**
	 * Executor setter for remote modifications.
	 *
	 * @param executor executor
	 */
	void setExecutor(Executor executor);

	/**
	 * Gets the host associated with the modifier.
	 * @return host
	 */
	String getHost();
}
