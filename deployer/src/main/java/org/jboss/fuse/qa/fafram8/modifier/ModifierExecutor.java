package org.jboss.fuse.qa.fafram8.modifier;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Modifier Executor class. This class manages the (post-)modifiers.
 * Created by avano on 5.10.15.
 */
@Slf4j
public class ModifierExecutor {
	private static ModifierExecutor instance = null;
	private static List<Modifier> modifiers = null;
	private static List<Modifier> postModifiers = null;

	/**
	 * Constructor.
	 */
	protected ModifierExecutor() {
	}

	/**
	 * Gets the instance.
	 *
	 * @return instance
	 */
	public static ModifierExecutor getInstance() {
		if (instance == null) {
			instance = new ModifierExecutor();
			modifiers = new ArrayList<>();
			postModifiers = new ArrayList<>();
		}

		return instance;
	}

	/**
	 * Adds the modifier into the modifier list.
	 *
	 * @param modifier modifiers
	 */
	public static void addModifiers(Modifier... modifier) {
		// Force the initialization
		ModifierExecutor.getInstance();

		addModifiersToCollection(modifiers, modifier);
	}

	/**
	 * Add modifiers to post modifiers.
	 *
	 * @param modifier modifiers
	 */
	public static void addPostModifiers(Modifier... modifier) {
		// Force the initialization
		ModifierExecutor.getInstance();

		addModifiersToCollection(postModifiers, modifier);
	}

	/**
	 * Add modifiers to the collection.
	 *
	 * @param col collection
	 * @param modifier modifiers
	 */
	private static void addModifiersToCollection(Collection<Modifier> col, Modifier... modifier) {
		Collections.addAll(col, modifier);
	}

	/**
	 * Executes the modifiers before the fuse starts.
	 *
	 * @param host host to execute on
	 */
	public static void executeModifiers(String host) {
		executeModifiers(host, null);
	}

	/**
	 * Executes the modifiers before the fuse starts.
	 *
	 * @param executor executor
	 * @param host host to execute on
	 */
	public static void executeModifiers(String host, Executor executor) {
		executeModifiersFromCollection(host, executor, modifiers);
	}

	/**
	 * Executes the post modifiers.
	 */
	public static void executePostModifiers() {
		executePostModifiers(null);
	}

	/**
	 * Executes the post modifiers on remote.
	 *
	 * @param executor executor
	 */
	//TODO(avano): figure out the null
	public static void executePostModifiers(Executor executor) {
		executeModifiersFromCollection(null, executor, postModifiers);
	}

	/**
	 * Executes the modifiers from the given collection.
	 *
	 * @param executor executor
	 * @param col collection
	 */
	private static void executeModifiersFromCollection(String host, Executor executor, Collection<Modifier> col) {
		for (Modifier c : col) {
			try {
				// If the host in the modifier is null, it is applicable for all containers
				// If c.getHost() != host, then this modifier does not belong to that container, so skip it
				if ((c.getHost() == null) || c.getHost().equals(host)) {
					// If executor is not null, then set the executor to the modifier so that it will know it should do it on remote
					if (executor != null) {
						c.setExecutor(executor);
					}
					log.debug("Executing modifier {}.", c);
					c.execute();
				}
			} catch (Exception e) {
				log.error("Failed to execute modifiers.", e);
				throw new FaframException(e);
			}
		}
	}

	/**
	 * Clears the modifiers.
	 */
	public static void clearAllModifiers() {
		// Clear all the modifiers at the end so that they will not stay here when executing multiple tests
		modifiers.clear();
		postModifiers.clear();
	}
}
