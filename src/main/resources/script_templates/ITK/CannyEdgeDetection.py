# @itkImage image
# @OUTPUT Dataset output

from org.itk.simple import CannyEdgeDetectionImageFilter
from org.itk.simple import VectorDouble

import org.itk.simple as sitk

canny=CannyEdgeDetectionImageFilter()

vec1=VectorDouble()
vec2=VectorDouble()

vec1.push_back(0.0)
vec1.push_back(200.0)
vec2.push_back(0.2)
vec2.push_back(0.5)

# call itk rl using simple itk wrapper
output = canny.execute(image);
#output = canny.execute(image, 0.0, 200.0, vec1, vec2);
