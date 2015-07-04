//
//  ImageResize.m
//  ImageResizer PhoneGap / Cordova Plugin
//
//  Created by Raanan Weber on 02.01.12.
//
//  The software is open source, MIT Licensed.
//  Copyright (c) 2012-2013 webXells GmbH , http://www.webxells.com. All rights reserved.
//
// Using the following Libraries (Big thanks to the developers!)
// Image Scaling : http://iphonedevelopertips.com/graphics/how-to-scale-an-image-using-an-objective-c-category.html . Source is added with respected copyright.
// NSData Base64 : NSData Base64 extension by Dave Winer. http://colloquy.info/project/browser/trunk/NSDataAdditions.h?rev=1576,  Source is added with original copyright.
//

#import "ImageResize.h"
#import "UIImage+Scale.h"
#import "NSData+Base64.h"
#import "CDVFile.h"

@implementation ImageResize

@synthesize callbackID;

- (void) resizeImage:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    CGFloat width = [[options objectForKey:@"width"] floatValue];
    CGFloat height = [[options objectForKey:@"height"] floatValue];
    NSInteger quality = [[options objectForKey:@"quality"] integerValue];
    NSString *format =  [options objectForKey:@"format"];
    NSString *resizeType = [options objectForKey:@"resizeType"];
    bool storeImage = [[options objectForKey:@"storeImage"] boolValue];
    NSString *filename = [options objectForKey:@"filename"];
    bool accountForPixelDensity = [[options objectForKey:@"pixelDensity"] boolValue];
    
    //Load the image
    UIImage *img = [self getImageUsingOptions:options];
    
    UIImage *scaledImage = nil;
    CGFloat newHeight;
    CGFloat newWidth;
    if ([resizeType isEqualToString:@"factorResize"] == YES) {
        newWidth = img.size.width * width;
        newHeight = img.size.height * height;
    } else if ([resizeType isEqualToString:@"minPixelResize"] == YES) {
        CGFloat widthFactor = width/img.size.width;
        CGFloat heightFactor = height/img.size.height;
        CGFloat scaleFactor = 0.0;
        if (widthFactor > heightFactor && widthFactor <= 1.0) {
            scaleFactor = widthFactor;
        } else if (heightFactor <= 1.0) {
            scaleFactor = heightFactor;
        } else {
            scaleFactor = 1.0;
        }
        newWidth = img.size.width * scaleFactor;
        newHeight = img.size.height * scaleFactor;
    } else {
        CGFloat widthFactor = width / img.size.width;
        CGFloat heightFactor = height / img.size.height;
        CGFloat scaleFactor = 0.0;
        if (widthFactor == 0.0) {
        	   scaleFactor = heightFactor;
        } else if (heightFactor == 0.0) {
        	   scaleFactor = widthFactor;
        } else if (widthFactor > heightFactor) {
            scaleFactor = heightFactor; // scale to fit height
        } else {
            scaleFactor = widthFactor; // scale to fit width
        }
        newWidth = img.size.width * scaleFactor;
        newHeight = img.size.height * scaleFactor;
    }
    
    //Double size for retina if option set to true
    if (accountForPixelDensity && [[UIScreen mainScreen] respondsToSelector:@selector(scale)] && [[UIScreen mainScreen] scale] == 2.0) {
        if (img.size.width > newWidth * 2 && img.size.height > newHeight * 2) {
            newWidth = newWidth * 2;
            newHeight = newHeight * 2;
        } else {
            newWidth = img.size.width;
            newHeight = img.size.height;
        }
    }
    
    scaledImage = [img scaleToSize:CGSizeMake(newWidth, newHeight)];
    NSNumber *newWidthObj = [[NSNumber alloc] initWithFloat:newWidth];
    NSNumber *newHeightObj = [[NSNumber alloc] initWithFloat:newHeight];
    
    CDVPluginResult* pluginResult = nil;
    if (storeImage) {
        bool written = [self saveImage:scaledImage withOptions:options];
        if (written) {
            NSDictionary* result = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:filename, newWidthObj, newHeightObj, nil] forKeys:[NSArray arrayWithObjects: @"filename", @"width", @"height", nil]];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
    } else {
        NSData* imageDataObject = nil;
        if ([format isEqualToString:@"jpg"]) {
            imageDataObject = UIImageJPEGRepresentation(scaledImage, (quality/100.f));
        } else {
            imageDataObject = UIImagePNGRepresentation(scaledImage);
        }
        
        NSString *encodedString = [imageDataObject base64EncodingWithLineLength:0];
        NSDictionary* result = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:encodedString, newWidthObj, newHeightObj, nil] forKeys:[NSArray arrayWithObjects: @"imageData", @"width", @"height", nil]];
        
        if (encodedString != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (UIImage*) getImageUsingOptions:(NSDictionary*)options {
    NSString *imageData = [options objectForKey:@"data"];
    NSString *imageDataType = [options objectForKey:@"imageDataType"];
    
    //Load the image
    UIImage *img = nil;
    if([imageDataType isEqualToString:@"base64Image"]==YES) {
        img = [[UIImage alloc] initWithData:[NSData dataWithBase64EncodedString:imageData]];
    } else {
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfFile:[self getUrl:imageData]]];
    }
    return img;
}

- (NSString *)getUrl:(NSString *)urlString
{
    NSString *path = nil;
    id filePlugin = [self.commandDelegate getCommandInstance:@"File"];
    if (filePlugin != nil) {
        CDVFilesystemURL* url = [CDVFilesystemURL fileSystemURLWithString:urlString];
        path = [filePlugin filesystemPathForURL:url];
    }
    if (path == nil) {
        if ([urlString hasPrefix:@"file:"]) {
            path = [[NSURL URLWithString:urlString] path];
        }
    }
    return path;
}

- (void) imageSize:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    UIImage * img = [self getImageUsingOptions:options];
    NSNumber *width = [[NSNumber alloc] initWithInt:img.size.width];
    NSNumber *height = [[NSNumber alloc] initWithInt:img.size.height];
    NSDictionary* dic = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:width,height,nil] forKeys:[NSArray arrayWithObjects: @"width", @"height", nil]];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) storeImage:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    UIImage * img = [self getImageUsingOptions:options];
    [self saveImage:img withOptions:options];
}

- (bool) saveImage:(UIImage *)img withOptions:(NSDictionary *) options {
    NSString *format =  [options objectForKey:@"format"];
    NSString *filename =  [options objectForKey:@"filename"];
    NSString *directory =  [options objectForKey:@"directory"];
    directory = [self getUrl:directory];
    NSInteger quality = [[options objectForKey:@"quality"] integerValue];
    bool photoAlbum = [[options objectForKey:@"photoAlbum"] boolValue];
    if (photoAlbum == YES) {
        UIImageWriteToSavedPhotosAlbum(img, self, @selector(imageSavedToPhotosAlbum:didFinishSavingWithError:contextInfo:), nil);
        return true;
    } else {
        NSData* imageDataObject = nil;
        if ([format isEqualToString:@"jpg"]) {
            imageDataObject = UIImageJPEGRepresentation(img, (quality/100.f));
        } else {
            imageDataObject = UIImagePNGRepresentation(img);
        }
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = [paths objectAtIndex:0];
        
        NSMutableString* fullFileName;
        if (![directory isEqualToString:@""]) {
            fullFileName = [NSMutableString stringWithString: directory];
            if (![[NSFileManager defaultManager] fileExistsAtPath:fullFileName]) {
                NSError *error = nil;
                [[NSFileManager defaultManager] createDirectoryAtPath:fullFileName withIntermediateDirectories:NO attributes:nil error:&error];
            }
        } else {
            fullFileName = [NSMutableString stringWithString: documentsDirectory];
        }
        
        [fullFileName appendString:@"/"];
        [fullFileName appendString:filename];
        NSRange r = [filename rangeOfString:format options:NSCaseInsensitiveSearch];
        if (r.location == NSNotFound) {
            [fullFileName appendString:@"."];
            [fullFileName appendString:format];
        }
        NSError *error = nil;
        bool written = [imageDataObject writeToFile:fullFileName options:NSDataWritingAtomic error:&error];
        if (!written) {
            NSLog(@"Write returned error: %@", [error localizedDescription]);
        }
        return written;
    }
}

- (void) imageSavedToPhotosAlbum:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo {
    NSString *message;
    NSString *title;
    if (!error) {
        title = NSLocalizedString(@"Image Saved", @"");
        message = NSLocalizedString(@"The image was placed in your photo album.", @"");
    }
    else {
        title = NSLocalizedString(@"Error", @"");
        message = [error description];
    }
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:title
                                                    message:message
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}

@end
