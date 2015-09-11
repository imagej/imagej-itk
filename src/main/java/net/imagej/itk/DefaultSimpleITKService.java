/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

import java.math.BigInteger;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.itk.simple.Image;
import org.itk.simple.PixelIDValueEnum;
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
 * @author Mark Hiner, Brian Northan
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

		Type<?> type = dataset.firstElement();
		PixelIDValueEnum itkType = imageJTypeToITKPixelID(type);
		Image image = new Image(itkDimensions, itkType);

		final LocalizingZeroMinIntervalIterator i =
			new LocalizingZeroMinIntervalIterator(dataset);
		final RandomAccess<RealType<?>> s = dataset.randomAccess();

		final VectorUInt32 index = new VectorUInt32(numDimensions);

		ImageJToSimpleITKPixelCopier copier = getImageJToSimpleITKPixelCopier(
			itkType);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			copier.copyPixel(s, image, index);
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

		PixelIDValueEnum itkType = image.getPixelID();
		Dataset dataset = createDataSetFromSimpleITKPixelID(itkType, dims, name,
			axes);

		// get an iterator
		LocalizingZeroMinIntervalIterator i = new LocalizingZeroMinIntervalIterator(
			dataset);

		RandomAccess<? extends RealType<?>> s = dataset.randomAccess();

		final VectorUInt32 index = new VectorUInt32(3);

		SimpleITKToImageJPixelCopier copier = getSimpleITKToImageJPixelCopier(
			itkType);

		while (i.hasNext()) {
			i.fwd();
			s.setPosition(i);

			for (int d = 0; d < numDimensions; d++) {
				index.set(d, i.getLongPosition(d));
			}

			copier.copyPixel(s, image, index);

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

	interface ImageJToSimpleITKPixelCopier {

		void copyPixel(RandomAccess<? extends RealType<?>> s, Image image,
			VectorUInt32 index);
	}

	ImageJToSimpleITKPixelCopier getImageJToSimpleITKPixelCopier(
		PixelIDValueEnum itkType)
	{
		if (itkType == PixelIDValueEnum.sitkUInt8) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					image.setPixelAsUInt8(index, (short) s.get().getRealFloat());
				}
			};
		}

		else if (itkType == PixelIDValueEnum.sitkInt8) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					image.setPixelAsInt8(index, (byte) s.get().getRealFloat());
				}
			};
		}

		else if (itkType == PixelIDValueEnum.sitkUInt16) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					image.setPixelAsUInt16(index, (int) s.get().getRealFloat());
				}
			};
		}

		else if (itkType == PixelIDValueEnum.sitkInt16) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					image.setPixelAsInt16(index, (short) s.get().getRealFloat());
				}
			};
		}

		else if (itkType == PixelIDValueEnum.sitkUInt32) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					image.setPixelAsUInt32(index, (long) s.get().getRealFloat());
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt32) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					image.setPixelAsInt32(index, (int) s.get().getRealFloat());
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkUInt64) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					float temp = s.get().getRealFloat();
					BigInteger bi = BigInteger.valueOf((long) temp);
					image.setPixelAsUInt64(index, bi);
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt64) {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					image.setPixelAsInt64(index, (int) s.get().getRealFloat());
				}
			};
		}
		else {
			return new ImageJToSimpleITKPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{

					image.setPixelAsFloat(index, s.get().getRealFloat());
				}
			};
		}
	}

	interface SimpleITKToImageJPixelCopier {

		void copyPixel(RandomAccess<? extends RealType<?>> s, Image image,
			VectorUInt32 index);
	}

	SimpleITKToImageJPixelCopier getSimpleITKToImageJPixelCopier(
		PixelIDValueEnum itkType)
	{
		if (itkType == PixelIDValueEnum.sitkUInt8) {

			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsUInt8(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt8) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsInt8(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkUInt16) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsUInt16(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt16) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsInt16(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkUInt32) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsUInt32(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt32) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsInt32(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkUInt64) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsUInt64(index).longValue());
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkInt64) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsInt64(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkFloat32) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsFloat(index));
				}
			};
		}
		else if (itkType == PixelIDValueEnum.sitkFloat64) {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsDouble(index));
				}
			};
		}
		else {
			return new SimpleITKToImageJPixelCopier() {

				@Override
				public void copyPixel(RandomAccess<? extends RealType<?>> s,
					Image image, VectorUInt32 index)
				{
					s.get().setReal(image.getPixelAsFloat(index));
				}
			};
		}
	}

	PixelIDValueEnum imageJTypeToITKPixelID(Type<?> type)
	{

		if (type.getClass() == new UnsignedByteType().getClass())
			return PixelIDValueEnum.sitkUInt8;
		else if (type.getClass() == new ByteType().getClass())
			return PixelIDValueEnum.sitkInt8;

		else if (type.getClass() == new UnsignedShortType().getClass())
			return PixelIDValueEnum.sitkUInt16;
		else if (type.getClass() == new ShortType().getClass())
			return PixelIDValueEnum.sitkInt16;

		else if (type.getClass() == new UnsignedIntType().getClass())
			return PixelIDValueEnum.sitkUInt32;
		else if (type.getClass() == new IntType().getClass())
			return PixelIDValueEnum.sitkInt32;

		else if (type.getClass() == new UnsignedLongType().getClass())
			return PixelIDValueEnum.sitkUInt64;
		else if (type.getClass() == new LongType().getClass())
			return PixelIDValueEnum.sitkInt64;

		else if (type.getClass() == new FloatType().getClass())
			return PixelIDValueEnum.sitkFloat32;
		else if (type.getClass() == new DoubleType().getClass())
			return PixelIDValueEnum.sitkFloat64;

		else return PixelIDValueEnum.sitkFloat32;

	}

	Dataset createDataSetFromSimpleITKPixelID(PixelIDValueEnum id, long[] dims,
		String name, AxisType[] axes)
	{

		if (id == PixelIDValueEnum.sitkInt8) return datasetService.create(
			new ByteType(), dims, name, axes);
		else if (id == PixelIDValueEnum.sitkUInt8) return datasetService.create(
			new UnsignedByteType(), dims, name, axes);

		else if (id == PixelIDValueEnum.sitkInt16) return datasetService.create(
			new ShortType(), dims, name, axes);
		else if (id == PixelIDValueEnum.sitkUInt16) return datasetService.create(
			new UnsignedShortType(), dims, name, axes);

		else if (id == PixelIDValueEnum.sitkInt32) return datasetService.create(
			new IntType(), dims, name, axes);
		else if (id == PixelIDValueEnum.sitkUInt32) return datasetService.create(
			new UnsignedIntType(), dims, name, axes);

		else if (id == PixelIDValueEnum.sitkInt64) return datasetService.create(
			new LongType(), dims, name, axes);
		else if (id == PixelIDValueEnum.sitkUInt64) return datasetService.create(
			new UnsignedLongType(), dims, name, axes);

		else if (id == PixelIDValueEnum.sitkFloat32) return datasetService.create(
			new FloatType(), dims, name, axes);
		else if (id == PixelIDValueEnum.sitkFloat64) return datasetService.create(
			new DoubleType(), dims, name, axes);

		else return datasetService.create(new FloatType(), dims, name, axes);
	}
}
