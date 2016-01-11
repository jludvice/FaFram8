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
	 */
	public static void executeModifiers() {
		executeModifiers(null);
	}

	/**
	 * Executes the modifiers before the fuse starts.
	 *
	 * @param executor executor
	 */
	public static void executeModifiers(Executor executor) {
		executeModifiersFromCollection(executor, modifiers);
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
	public static void executePostModifiers(Executor executor) {
		executeModifiersFromCollection(executor, postModifiers);
	}

	/**
	 * Executes the modifiers from the given collection.
	 *
	 * @param executor executor
	 * @param col collection
	 */
	private static void executeModifiersFromCollection(Executor executor, Collection<Modifier> col) {
		for (Modifier c : col) {
			try {
				if (executor != null) {
					c.setExecutor(executor);
				}
				log.debug("Executing modifier {}.", c);
				c.execute();
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
