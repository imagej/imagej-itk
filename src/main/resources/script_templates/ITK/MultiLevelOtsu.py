# @org.itk.simple.Image image
# @OUTPUT Dataset output

from org.itk.simple import OtsuMultipleThresholdsImageFilter

otsu = OtsuMultipleThresholdsImageFilter()

# call itk rl using simple itk wrapper
output = otsu.execute(image, 2, 0, 255, True)