# @org.itk.simple.Image image
# @OUTPUT Dataset output

from org.itk.simple import SmoothingRecursiveGaussianImageFilter
import org.itk.simple as sitk

#itkGauss = SmoothingRecursiveGaussianImageFilter();
edges = sitk.CannyEdgeDetection(image, lowerThreshold=0.0, 
                                upperThreshold=200.0, variance = (5.0,5.0,5.0))


# call itk rl using simple itk wrapper
#output = itkGauss.execute(image, 3.0, False);