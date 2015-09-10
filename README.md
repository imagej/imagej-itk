# imagej-itk

This is a work in progress repository that demostrates how to expose [Insight
Toolkit](http://itk.org) functionality to [ImageJ2](http://imagej.net).

Currently, the repository demonstrates how to expose a few
[SimpleITK](http://simpleitk.org) filters as [ImgLib2](http://imglib2.net/)
[Ops](http://imagej.net/Ops).

## Build Instructions ##

To get started, install [Maven 3](http://maven.apache.org/), and run:

```
mvn
```

from the repository root directory.

### Bundling Native Libraries ###

You will need to add the `SimpleITKJava` native library appropriate for your operating system and architecture to the `java.library.path`. See the [SimpleITK documentation](http://www.itk.org/Wiki/SimpleITK/GettingStarted#Build_It_Yourself) for information on custom builds.
