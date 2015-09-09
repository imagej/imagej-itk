# @org.itk.simple.Image image
# @OUTPUT Dataset output

from org.itk.simple import SmoothingRecursiveGaussianImageFilter

itkGauss = SmoothingRecursiveGaussianImageFilter();

# call itk rl using simple itk wrapper
output = itkGauss.execute(image, 3.0, False);