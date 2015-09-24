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
public class RecursiveGaussianImageOp<T extends RealType<T>, S extends RealType<S>>
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
