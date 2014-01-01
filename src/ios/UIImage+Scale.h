//
//  UIImage+Scale.h
//  ImageResizer for PhoneGap
//
// Based on http://iphonedevelopertips.com/graphics/how-to-scale-an-image-using-an-objective-c-category.html

#import <Foundation/Foundation.h>

@interface UIImage (scale)

-(UIImage*)scaleToSize:(CGSize)size;

@end
