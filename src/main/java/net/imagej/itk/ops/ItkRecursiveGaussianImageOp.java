
package net.imagej.itk.ops;

import net.imagej.ops.Op;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.itk.simple.Image;
import org.itk.simple.RichardsonLucyDeconvolutionImageFilter;
import org.itk.simple.RichardsonLucyDeconvolutionImageFilter.BoundaryConditionType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ItemIO;
import org.scijava.Priority;

import net.imagej.itk.SimpleItkImagejUtilities;

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
	protected Img<T> input;

	@Parameter
	protected float sigma = 3.0f;

	@Parameter(type = ItemIO.OUTPUT, required = false)
	protected Img<T> output;

	org.itk.simple.RecursiveGaussianImageFilter itkGauss;

	public void run() {

		// convert input to itk Images
		Image itkImage = SimpleItkImagejUtilities.simple3DITKImageFromInterval(
			input);
		// org.itk.simple.RecursiveGaussianImageFilter.

		org.itk.simple.SmoothingRecursiveGaussianImageFilter itkGauss =
			new org.itk.simple.SmoothingRecursiveGaussianImageFilter();

		// call itk rl using simple itk wrapper
		Image out = itkGauss.execute(itkImage, sigma, false);

		T inputType = Util.getTypeFromInterval(input);

		// convert output to ImageJ Img
		output = SimpleItkImagejUtilities.simple3DITKImageToImg(out, input
			.factory(), inputType);

	}
}
