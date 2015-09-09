
package net.imagej.itk.ops;

import org.itk.simple.Image;
import org.scijava.ItemIO;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imglib2.type.numeric.RealType;

/**
 * An op that wraps the itk implementation of Recursive Gaussian Filter
 * 
 * @author bnorthan
 * @param <T>
 * @param <S>
 */
@Plugin(type = RecursiveGaussian.class, name = RecursiveGaussian.NAME,
	priority = Priority.HIGH_PRIORITY + 1)
public class ItkRecursiveGaussianImageOp<T extends RealType<T>, S extends RealType<S>>
	implements Op
{

	@Parameter
	protected Image itkImage;

	@Parameter
	protected float sigma = 3.0f;

	@Parameter(type = ItemIO.OUTPUT, required = false)
	protected Image output;

	org.itk.simple.RecursiveGaussianImageFilter itkGauss;

	public void run() {

		org.itk.simple.SmoothingRecursiveGaussianImageFilter itkGauss =
			new org.itk.simple.SmoothingRecursiveGaussianImageFilter();

		// call itk rl using simple itk wrapper
		output = itkGauss.execute(itkImage, sigma, false);

	}
}
