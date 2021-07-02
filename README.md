[![](https://github.com/imagej/imagej-itk/actions/workflows/build-main.yml/badge.svg)](https://github.com/imagej/imagej-itk/actions/workflows/build-main.yml)

# imagej-itk

This is a work in progress repository that demostrates how to expose [Insight
Toolkit](http://itk.org) functionality to [ImageJ2](http://imagej.net).

Currently, the repository demonstrates how to expose a few
[SimpleITK](http://simpleitk.org) filters as [ImageJ2 Ops](http://imagej.net/Ops),
[ImageJ2
Commands](https://github.com/imagej/imagej-tutorials/tree/master/simple-commands), and
[ImageJ2 Parameterized
Scripts](https://imagej.github.io/presentations/2015-09-04-imagej2-scripting/#/)

The project includes converters which translate ImageJ2 Datasets to the SimpleITK Image
format.  The ImageJ2 scripting framework automatically calls the converters when a script
requires a SimpleITK Image. 

Working example
---------------

Try this Jython script in ImageJ's
[Script Editor](http://imagej.net/Script_Editor)!

```python
# @org.itk.simple.Image image
# @OUTPUT Dataset output

from org.itk.simple import SmoothingRecursiveGaussianImageFilter

itkGauss = SmoothingRecursiveGaussianImageFilter();

# call itk rl using simple itk wrapper
output = itkGauss.execute(image, 3.0, False);
```


## Build Instructions ##

To get started, install [Maven 3](http://maven.apache.org/), and run:

```
mvn
```

from the repository root directory.

### Bundling Native Libraries ###

You will need to add the `SimpleITKJava` native library appropriate for your operating system and architecture to the `java.library.path`. See the [SimpleITK documentation](http://www.itk.org/Wiki/SimpleITK/GettingStarted#Build_It_Yourself) for information on custom builds.
