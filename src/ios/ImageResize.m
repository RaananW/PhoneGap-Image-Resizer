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

@implementation ImageResize

@synthesize callbackID;

-(void)resizeImage:(CDVInvokedUrlCommand*)command 
{
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
        
    CGFloat width = [[options objectForKey:@"width"] floatValue];  
    CGFloat height = [[options objectForKey:@"height"] floatValue];
    
    NSInteger quality = [[options objectForKey:@"quality"] integerValue];  
    
    NSString *format =  [options objectForKey:@"format"] ?: @"jpg";
    
    NSString *resizeType = [options objectForKey:@"resizeType"];
    
    //Load the image
    UIImage * img = [self getImageUsingOptions:options];   

    UIImage *scaledImage = nil;
    if([resizeType isEqualToString:@"factorResize"]==YES) {
        scaledImage = [img scaleToSize:CGSizeMake(img.size.width * width, img.size.height * height)];
    } else {
        scaledImage = [img scaleToSize:CGSizeMake(width, height)];
    }
    
    NSData* imageDataObject = nil;
    if([format isEqualToString:@"png"]==YES) {
        imageDataObject = UIImagePNGRepresentation(scaledImage);
    } else {
        imageDataObject = UIImageJPEGRepresentation(scaledImage, (quality/100));
    }
    
    NSString *encodedString = [imageDataObject base64EncodingWithLineLength:0];
    
    NSNumber *newwidth = [[NSNumber alloc] initWithInt:scaledImage.size.width];
    NSNumber *newheight = [[NSNumber alloc] initWithInt:scaledImage.size.height];
    NSDictionary* result = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:encodedString,newwidth,newheight,nil] forKeys:[NSArray arrayWithObjects: @"imageData", @"width", @"height", nil]];

    CDVPluginResult* pluginResult = nil;
        
    if(encodedString != nil)
    {
        //Call  the Success Javascript function
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    }else
    {    
        //Call  the Failure Javascript function
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (UIImage*) getImageUsingOptions:(NSDictionary*)options {
    NSString *imageData = [options objectForKey:@"data"];
    NSString *imageDataType = [options objectForKey:@"imageDataType"] ?: @"base64Image";
    
    //Load the image
    UIImage * img = nil;
    if([imageDataType isEqualToString:@"base64Image"]==YES) {
        img = [[UIImage alloc] initWithData:[NSData dataWithBase64EncodedString:imageData]];
    } else {
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageData]]];
    }
    return img;
}

-(void)imageSize:(CDVInvokedUrlCommand*)command
{
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    UIImage * img = [self getImageUsingOptions:options];   
    NSNumber *width = [[NSNumber alloc] initWithInt:img.size.width];
    NSNumber *height = [[NSNumber alloc] initWithInt:img.size.height];
    NSDictionary* dic = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:width,height,nil] forKeys:[NSArray arrayWithObjects: @"width", @"height", nil]];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
   
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)storeImage:(CDVInvokedUrlCommand*)command {
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    UIImage * img = [self getImageUsingOptions:options];
    NSString *format =  [options objectForKey:@"format"] ?: @"jpg";
    NSString *filename =  [options objectForKey:@"filename"] ?: @"jpg";
    NSInteger quality = [[options objectForKey:@"quality"] integerValue] ?: 70; 
    BOOL photoAlbum = [[options objectForKey:@"photoAlbum"] boolValue] ?: YES;
    if(photoAlbum==YES) {
        UIImageWriteToSavedPhotosAlbum(img, self, @selector(imageSavedToPhotosAlbum:didFinishSavingWithError:contextInfo:), nil);
    } else {
        NSData* imageData = nil;
        if([format isEqualToString:@"jpg"]==YES) {
            imageData = UIImageJPEGRepresentation(img, (quality/100));
        } else {
            imageData = UIImagePNGRepresentation(img);
        }
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = [paths objectAtIndex:0];
        
        NSMutableString* fullFileName = [NSMutableString stringWithString: documentsDirectory];
        
        [fullFileName appendString:@"/"];
        [fullFileName appendString:filename];
        NSRange r = [filename rangeOfString:format options:NSCaseInsensitiveSearch];
        if(r.location == NSNotFound) {
            [fullFileName appendString:@"."];
            [fullFileName appendString:format];
        }
        NSLog(@"%@", fullFileName);
        [imageData writeToFile:fullFileName atomically:YES];
    }
}

- (void)imageSavedToPhotosAlbum:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo {
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
