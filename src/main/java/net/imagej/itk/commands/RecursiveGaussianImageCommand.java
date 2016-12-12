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

package net.imagej.itk.commands;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.itk.SimpleITKService;
import net.imagej.itk.ops.RecursiveGaussian;
import net.imagej.ops.OpService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.itk.simple.Image;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A command with parameters and a menu item. The command will be added to the
 * ImageJ menu (under "Plugins", "ITK").
 *
 * @author Brian Northan
 * @param <T>
 */
@Plugin(type = Command.class, menuPath = "Plugins>ITK>ITK Gaussian")
public class RecursiveGaussianImageCommand<T extends RealType<T> & NativeType<T>>
	implements Command
{

	@Parameter
	private SimpleITKService simpleITKService;

	@Parameter
	protected DatasetService data;

	@Parameter
	protected OpService ops;

	@Parameter
	protected Image input;

	@Parameter
	protected float sigma = 3.0f;

	@Parameter(type = ItemIO.OUTPUT)
	protected Dataset output;

	@Override
	public void run() {

		// call the op
		final Image image = (Image) ops.run(RecursiveGaussian.NAME, input, sigma);

		output = simpleITKService.getDataset(image);
	}

}
