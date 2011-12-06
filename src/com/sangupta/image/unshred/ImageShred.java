/**
 *
 * Image Unshredder
 * Copyright (c) 2011, Sandeep Gupta
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.sangupta.image.unshred;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class ImageShred {
	
	private File originalImageFile;
	
	private BufferedImage originalImage = null;
	
	private BufferedImage reconstructedImage = null;
	
	private int width;
	
	private int height;
	
	private int shredWidth = -1;
	
	/**
	 * Command line function to shred the given image into the given number of shreds.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 0 || args.length > 2) {
			System.out.println("Usage: $ java -jar image-unshred.jar shred <image> <shreds>");
			System.out.println("");
			System.out.println("    <image>    the path of the image that needs to be shredded");
			System.out.println("    <width>    the width of the shred strip");
			System.out.println("");
			System.out.println("For an original image as original.png the shredded image is created as");
			System.out.println("original.shredded.png. Supported image formats are GIF, JPG, and PNG.");
			return;
		}
		
		File image = new File(args[0]);
		if(!image.exists()) {
			System.out.println("The given image file does not exists.");
			return;
		}
		
		if(image.isDirectory()) {
			System.out.println("The given image is a directory and not a file.");
			return;
		}
		image = image.getAbsoluteFile();
		
		int shredWidth = Integer.parseInt(args[1]);
		
		final long start = System.currentTimeMillis();
		
		try {
			shredImage(image, shredWidth);
		} catch (Exception e) {
			System.out.println("Unable to read image from disk." + e);
		}
		
		final long end = System.currentTimeMillis();
		
		System.out.println("Time taken " + (end - start) + " ms.");
	}
	
	/**
	 * Shred the given image and write the reconstructed image to disk with the given filename.
	 * 
	 * @param shreddedImage
	 * @param writeToFile
	 * @return
	 */
	public static boolean shredImage(File shreddedImage, String writeToFile) {
		int sliceWidth = -1;
		try {
			BufferedImage originalImage = ImageIO.read(shreddedImage);
			final int width = originalImage.getWidth();
			if(width % 4 == 0) {
				sliceWidth = new Random().nextInt(5) * 4;
				if(sliceWidth == 0) {
					sliceWidth = 4;
				}
			} else if(width % 10 == 0) {
				sliceWidth = 10;
			} else if(width % 7 == 0) {
				sliceWidth = new Random().nextInt(4) * 7;
				if(sliceWidth == 0) {
					sliceWidth = 7;
				}
			}
			
			if(sliceWidth == -1) {
				return false;
			}
			
			ImageShred shred = new ImageShred();
			shred.originalImage = originalImage;
			shred.shredWidth = sliceWidth;
			shred.loadImage(shreddedImage);
			shred.shredImage();
			shred.writeToDisk(writeToFile);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Load the given image from disk in memory.
	 * 
	 * @param imageFile
	 * @throws Exception
	 */
	private void loadImage(File imageFile) throws Exception {
		this.originalImageFile = imageFile;

		if(this.originalImage == null) {
			this.originalImage = ImageIO.read(imageFile);
		}
		
		width = originalImage.getWidth();
		height = originalImage.getHeight();
	}
	
	/**
	 * Shred the loaded image and create the split image in memory.
	 * 
	 * @throws Exception
	 */
	private void shredImage() throws Exception {
		// create slices
		int slices = width / shredWidth;
		List<BufferedImage> images = new ArrayList<BufferedImage>(slices);
		for(int index = 0; index < slices; index++) {
			int startX = index * shredWidth;
			BufferedImage subImage = originalImage.getSubimage(startX, 0, shredWidth, height);
			images.add(index, subImage);
		}
		
		// shuffle
		Collections.shuffle(images);
		
		// create the shredded image in memory
		int destX = 0;
		reconstructedImage = new BufferedImage(width, height, originalImage.getType());
		for(int index = 0; index < images.size(); index++) {
			BufferedImage im = images.get(index);
			for(int x = 0; x < im.getWidth(); x++) {
				for(int y = 0; y < height; y++) {
					reconstructedImage.setRGB(destX, y, im.getRGB(x, y));
				}
				destX++;
			}
		}
	}
	
	/**
	 * Write the reconstructed image to disk with the given name. If name is <code>null</code>
	 * a default shredded name is constructed using {@link #getShreddedImageName(String)}.
	 * 
	 * @param fileToWriteTo
	 * @throws Exception
	 */
	private void writeToDisk(String fileToWriteTo) throws Exception {
		if(fileToWriteTo == null) {
			// write it to disk
			String sim = getShreddedImageName(this.originalImageFile.getName());
			String path = this.originalImageFile.getParentFile().getAbsolutePath() + File.separator + sim;
			System.out.println("Writing reconstructed image as " + path);
			fileToWriteTo = path;
		}
		
		int index = fileToWriteTo.lastIndexOf('.');
		String extension = fileToWriteTo.substring(index + 1);
		ImageIO.write(reconstructedImage, extension, new File(fileToWriteTo));
		
		System.out.println("Done shredding image.");
	}
	
	/**
	 * Shred the given image file for the given shred width.
	 * 
	 * @param imageFile
	 * @param shredWidth
	 * @throws Exception
	 */
	public static void shredImage(File imageFile, int shredWidth) throws Exception {
		ImageShred shred = new ImageShred();
		shred.shredWidth = shredWidth;
		shred.loadImage(imageFile);
		
		if(shred.width % shredWidth != 0) {
			System.out.println("Shred width will not create slices equally. Image width: " + shred.width);
			return;
		}
		shred.shredImage();
		
		shred.writeToDisk(null);
	}

	/**
	 * Create a shredded image file name. If the image name is original.png
	 * the shredded image name is constructed as original.shredded.png
	 * 
	 * @param filename
	 * @return
	 */
	private static String getShreddedImageName(String filename) {
		int index = filename.lastIndexOf('.');
		String name = filename.substring(0, index);
		String extension = filename.substring(index + 1);
		return name + ".shredded." + extension;
	}

}
