Image Unshredder
================

image-unshred is a Java application to unshred a given image. This is a solution to the Instagram Engineering's Coding Challenge posted at, http://instagram-engineering.tumblr.com/post/12651721845/instagram-engineering-challenge-the-unshredder
	
The application also includes an image shredder, a test suite and a common interface that can be executed via the command line for executing the respective tools.

ImageMain
----------
A command line interface to execute each individual tool.

    $ java -jar image-unshred.jar <function> <arguments>
	
	<function>		Keyword for the respective tool. Valid values are shred/unshred/test
	<arguments>		Arguments that need to be passed to the function

ImageShred
----------
Shred a given image into stripes of the given width, shuffles the stripes and creates the shredded image.

    $ java -jar image-unshred.jar shred <image> <width>
	
	<image>    the path of the image that needs to be shredded
	<width>    the width of the shred strip
	
	For an original image as original.png the shredded image is created as original.shredded.png. 
	Supported image formats are GIF, JPG, and PNG.
	
ImageUnshred
------------
Unshreds the given image from the previously shredded image.

    $ java -jar image-unshred.jar unshred <image> <width>
	
	<image>    the path of the image that needs to be shredded
	<width>    the width of the shred strip, if known. If the width is not known, 
	the code will try and attempt to automatically find out the shred width

ImageUnshredderTest
-------------------
A test suite that reads all images from a given folder, shreds the images randomly. It then goes ahead and unshreds each image using automatic detection mode for strip width. Once the image has been reconstructed, it compares the original image to the reconstructed image. The test passes if all images are reconstructed perfectly.

    $ java -jar image-unshred.jar test <folder>
	
	<folder>    folder containing a set of original images. Supported image formats are GIF, JPG and PNG
	
Further Improvements Areas
--------------------------
The code is not 100% perfect and needs a lot of improvement. I have noticed the following areas where code may be improved:

1. Auto detection of strip width fails when the image has not many front objects - specifically between the strips which have a similar background. For example, two strips of a clear sky with very little clouds.

2. On similar lines, unshredding may result in a shifted image. It may look like the whole image was shifted to right and the spilled over part moved to the left side of the image. This again is due to presence of very similar strips which have a very low eucledian distance between them. I tried computing a value as,

    Math.sqrt( ((r1 - r2) ^ 2) + (g1 - g2) ^ 2) + (b1 - b2) ^ 2) )

which resulted in identifying of such confusions in the algorithm - though not 100%

Spolier Alert
-------------
The solution is posted here for my reference only. I suggest that you do not use the code below for solving the challenge. It would however be interesting to compare and know more, once you have solved the challenge yourself. Best of luck!

License
-------

Copyright (c) 2011, Sandeep Gupta

For more details on the project refer to link,
http://www.sangupta.com/projects/image-unshred

The project uses various other libraries that are subject to their
own license terms. See the distribution libraries or the project
documentation for more details.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the LICENSE
as included in the distribution. You may obtain a copy of the 
License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
