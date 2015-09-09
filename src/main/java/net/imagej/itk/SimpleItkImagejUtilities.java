
package net.imagej.itk;

import org.itk.simple.Image;
import org.itk.simple.VectorUInt32;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.type.numeric.RealType;

/**
 * Utility class to transfer images between itk and imagej2
 * 
 * @author bnorthan
 */
public class SimpleItkImagejUtilities {

	/**
	 * convert a 3D RandomAccessibleInterval to an itk Image
	 * 
	 * @param interval
	 * @return
	 */
	public static <T extends RealType<T>> Image simple3DITKImageFromInterval(
		RandomAccessibleInterval<T> interval)
	{
		int numDimensions = interval.numDimensions();

		VectorUInt32 itkDimensions = new VectorUInt32(numDimensions);

		for (int i = 0; i < numDimensions; i++) {
			itkDimensions.set(i, interval.dimension(i));
		}

		Image itkImage = new Image(itkDimensions,
			org.itk.simple.PixelIDValueEnum.sitkFloat32);

		LocalizingZeroMinIntervalIterator i = new LocalizingZeroMinIntervalIterator(
			interval);
		RandomAccess<T> s = interval.randomAccess();

		VectorUInt32 index = new VectorUInt32(numDimensions);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			float pix = s.get().getRealFloat();

			itkImage.setPixelAsFloat(index, pix);
		}

		return itkImage;
	}

	/**
	 * Convert a 3D simple itk "Image" to a imagej2 "Img"
	 * 
	 * @param itkImage - the simple itk Image to convert
	 * @param imgFactory - a factory to create the Img
	 * @param type - type of the image
	 * @return
	 */

	public static <T extends RealType<T>> Img<T> simple3DITKImageToImg(
		Image itkImage, ImgFactory<T> imgFactory, T type)
	{
		VectorUInt32 itkDimensions = itkImage.getSize();
		int numDimensions = (int) itkDimensions.size();

		// assume 3 dimensions
		long[] dims = new long[numDimensions];

		for (int d = 0; d < numDimensions; d++) {
			dims[d] = itkDimensions.get(d);
		}

		// create the ImageJ2 "Img"
		Img<T> output = imgFactory.create(dims, type);

		// get an iterator
		LocalizingZeroMinIntervalIterator i = new LocalizingZeroMinIntervalIterator(
			output);
		RandomAccess<T> s = output.randomAccess();

		VectorUInt32 index = new VectorUInt32(3);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			float pix = itkImage.getPixelAsFloat(index);

			s.get().setReal(pix);
		}

		return output;
	}
}
