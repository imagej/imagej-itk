/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2015 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.itk;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.itk.simple.Image;
import org.itk.simple.VectorUInt32;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default {@link SimpleITKService} implementation.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultSimpleITKService extends AbstractService implements
	SimpleITKService
{

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LogService logService;

	@Override
	public Image getImage(final Dataset dataset) {
		final int numDimensions = dataset.numDimensions();

		final VectorUInt32 itkDimensions = new VectorUInt32(numDimensions);

		for (int i = 0; i < numDimensions; i++) {
			itkDimensions.set(i, dataset.dimension(i));
		}

		final Image image =
			new Image(itkDimensions, org.itk.simple.PixelIDValueEnum.sitkFloat32);

		final LocalizingZeroMinIntervalIterator i =
			new LocalizingZeroMinIntervalIterator(dataset);
		final RandomAccess<RealType<?>> s = dataset.randomAccess();

		final VectorUInt32 index = new VectorUInt32(numDimensions);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			final float pix = s.get().getRealFloat();

			image.setPixelAsFloat(index, pix);
		}

		return image;
	}

	@Override
	public Dataset getDataset(final Image image) {
		final VectorUInt32 itkDimensions = image.getSize();
		final int numDimensions = (int) itkDimensions.size();

		// assume 3 dimensions
		final long[] dims = new long[numDimensions];

		for (int d = 0; d < numDimensions; d++) {
			dims[d] = itkDimensions.get(d);
		}

		final String name = "ITK image";

		final AxisType[] axes = new AxisType[numDimensions];

		// TODO: copy axis info from itk
		for (int i = 0; i < axes.length; i++) {
			if (i == 0) axes[i] = Axes.X;
			else if (i == 1) axes[i] = Axes.Y;
			else axes[i] = Axes.get("Unknown " + (i - 2), false);
		}

		final Dataset dataset =
			datasetService.create(new FloatType(), dims, name, axes);

		final Img<FloatType> output =
			(Img<FloatType>) dataset.getImgPlus().getImg();

		// get an iterator
		final LocalizingZeroMinIntervalIterator i =
			new LocalizingZeroMinIntervalIterator(output);

		final RandomAccess<FloatType> s = output.randomAccess();

		final VectorUInt32 index = new VectorUInt32(3);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			final float pix = image.getPixelAsFloat(index);

			s.get().setReal(pix);
		}

		return dataset;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// Try to load the native SimpleITK library from java.library.path
		System.loadLibrary("SimpleITKJava");

		// Register known data type aliases for use in script @parameters
		scriptService.addAlias("itkImage", Image.class);
	}

}
