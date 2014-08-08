//
//  ImageResize.h
//  ImageResizer PhoneGap / Cordova Plugin
//
//  Created by Raanan Weber on 02.01.12.
// 
//  The software is open source, MIT Licensed.
//  Copyright (c) 2012-2013 webXells GmbH , http://www.webxells.com. All rights reserved.
//
//  Using the following Libraries (Big thanks to the developers!)
//  Image Scaling : http://iphonedevelopertips.com/graphics/how-to-scale-an-image-using-an-objective-c-category.html . Source is added with respected copyright.
//  NSData Base64 : NSData Base64 extension by Dave Winer. http://colloquy.info/project/browser/trunk/NSDataAdditions.h?rev=1576, Source is added with original copyright.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>

@interface ImageResize : CDVPlugin {
    
    NSString* callbackID;  
    
}

@property (nonatomic, copy) NSString* callbackID;

//Instance Method  

- (void) resizeImage:(CDVInvokedUrlCommand*)command;
- (void) imageSize:(CDVInvokedUrlCommand*)command;
- (void) storeImage:(CDVInvokedUrlCommand*)command;
- (bool) saveImage:(UIImage *)image withOptions:(NSDictionary *) options;
- (void) imageSavedToPhotosAlbum:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)none;
- (UIImage*) getImageUsingOptions:(NSDictionary*)options;

@end
