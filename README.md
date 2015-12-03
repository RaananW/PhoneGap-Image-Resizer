* NOTE - I am sorry, I just can't find the time to fully test the plugin and develop it further. Small updates are constantly integrated. *

# Image Resizer plugin for Phonegap/Cordova 3.0+ #

This plugin resizes images natively using the phonegap / cordova architecture.

## Adding the Plugin to your project ##

The plugin conforms to the Cordova plugin specification, it can be installed
using the Cordova / Phonegap command line interface.

```
phonegap plugin add https://github.com/RaananW/PhoneGap-Image-Resizer

cordova plugin add https://github.com/RaananW/PhoneGap-Image-Resizer
```

## Using the plugin ##

The plugin creates the object `window.imageResizer`. `window.plugins.imageResizer` still works for older versions.

To use, call one of the following, available methods:

```javascript
   window.imageResizer.resizeImage(successCallBack, failCallBack, imageData, width, height, options);
   window.imageResizer.getImageSize(successCallBack, failCallBack, imageData, options);
   window.imageResizer.storeImage(successCallBack, failCallBack, imageData, options);
```

For Example:
```javascript
window.imageResizer.resizeImage(
   function(data) { 
     var image = document.getElementById('myImage');
     image.src = "data:image/jpeg;base64," + data.imageData; 
   }, function (error) {
     console.log("Error : \r\n" + error);
   }, imageDataInBase64, 0.5, 0.5, {
      resizeType: ImageResizer.RESIZE_TYPE_FACTOR,
      imageDataType: ImageResizer.IMAGE_DATA_TYPE_BASE64,
      format: ImageResizer.FORMAT_JPG
   }
);
```

### Android quirks and howto's ###

Android 2.3+ is supported

The storeImage funtion will always store the image to the device's default external storage, under the given Directory and filename. the photoAlbum property will be ignored.

### iOS quirks and howto's ###

For a full iOS integration, two extra extensions should be used:

1. Base64 Encoder/Decoder using Dave Wimer implementation (http://colloquy.info/project/browser/trunk/NSDataAdditions.h?rev=1576)
2. Image Scaling for UIImage (Using the great howto guide here : http://iphonedevelopertips.com/graphics/how-to-scale-an-image-using-an-objective-c-category.html )

The sources are added to the github.

The storeImage function in iOS saves either in the photo album, or in the application's temporary directory. 
In the first case (photo album is set to true), the filename and directory will be completly ignored. A filename will be decided by iOS itself.
In the second case, only filename will be used and directory will be ignored.

### The MIT License

Copyright (c) 2012-2015 Raanan Weber (raananw@gmail.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
