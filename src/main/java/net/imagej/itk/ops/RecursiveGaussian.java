package net.imagej.itk.ops;

import net.imagej.ops.Op;

/**
 * Base interface for "recursiveGaussian" operations.
 * <p>
 * Implementing classes should be annotated with:
 * </p>
 *
 * <pre>
 * @Plugin(type = RecursiveGaussian.class, name = RecursiveGaussian.NAME)
 * </pre>
 */
public interface RecursiveGaussian extends Op{
	String NAME = "filter.recursiveGaussian";
}
