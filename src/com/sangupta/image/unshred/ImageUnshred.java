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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUnshred {
	
	private BufferedImage image = null;
	
	private BufferedImage reconstructed = null;
	
	private static boolean LOGS_ENABLED = true;
	
	private int stripWidth = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length == 0 || args.length > 2) {
			System.out.println("Usage: $ java -jar image-unshred.jar unshred <image> <width>");
			System.out.println("");
			System.out.println("    <image>    the path of the image that needs to be un-shredded");
			System.out.println("    <width>    the width of the shred strip, if known ");
			System.out.println("               if not known, the program will try and auto-detect the value ");
			System.out.println("");
			System.out.println("For an original image as original.png the reconstructed image is created as");
			System.out.println("original.reconstructed.png. Supported image formats are GIF, JPG, and PNG.");
			return;
		}
		
		String imageUrl = args[0];
		File file = new File(imageUrl);
		
		if(!file.exists()) {
			System.out.println("The given image file does not exists.");
			return;
		}

		if(file.isDirectory()) {
			System.out.println("Given image is a directory, not a file.");
			return;
		}

		ImageUnshred unshredder = new ImageUnshred();
		if(args.length == 2) {
			int stripWidth = Integer.parseInt(args[1]);
			unshredder.stripWidth = stripWidth;
		}
		
		final long start = System.currentTimeMillis(); 
		// load the image
		unshredder.loadImage(imageUrl);
		
		// find the width of the strip
		unshredder.findStripWidth();
		
		// unshred the image
		unshredder.unshred();
		final long end = System.currentTimeMillis();
		
		log("Total time taken: " + (end - start) + " ms.");
		
		// write the reconstructed image to disk
		File fileToWrite = new File(imageUrl);
		unshredder.writeReconstructed(getUnshreddedImageName(fileToWrite.getAbsolutePath()));
		
		log("Reconstructed image written to disk as " + fileToWrite.getAbsolutePath());
	}
	
	public static boolean unshredImage(String shreddedImagePath, String reconstructedImage) {
		File shreddedImage = new File(shreddedImagePath);
		try {
			LOGS_ENABLED = false;
			ImageUnshred unshredder = new ImageUnshred();
			unshredder.loadImage(shreddedImage.getAbsolutePath());
			unshredder.findStripWidth();
			unshredder.unshred();
			unshredder.writeReconstructed(reconstructedImage);
			return true;
		} catch(Exception e) {
		}
		LOGS_ENABLED = true;
		return false;
	}

	/**
	 * Write the reconstructed image to the given file on disk.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private void writeReconstructed(String fileName) throws IOException {
		int index = fileName.lastIndexOf('.');
		String extension = fileName.substring(index + 1);
		if(this.reconstructed != null) {
			ImageIO.write(this.reconstructed, extension, new File(fileName));
		}
	}

	private void unshred() {
		log("unshredding starts...");
		
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		// number of columns in the image
		final int columns = width / this.stripWidth;
		log("total columns: " + columns);
		
		// read each strip
		log("reading strips...");
		ImageStrip[] strips = new ImageStrip[columns];
		for(int column = 0; column < columns; column++) {
			final int leftX = column * stripWidth;
			ImageStrip is = new ImageStrip(column, image.getSubimage(leftX, 0, stripWidth, height));
			strips[column] = is;
		}
		
		// start matching
		log("start unshredding...");
		List<ImageStrip> sorted = new ArrayList<ImageStrip>();
		sorted.add(strips[0]);
		strips[0].setUsed(true);
		
		for(int index = 1; index < strips.length; index++) {
			int leftIndex = -1, rightIndex = -1;
			double minRightScore = Double.MAX_VALUE, minLeftScore = Double.MAX_VALUE;
			
			ImageStrip leftStrip = sorted.get(0);
			ImageStrip rightStrip = sorted.get(sorted.size() - 1);
			
			for(int testIndex = 1; testIndex < strips.length; testIndex++) {
				ImageStrip testStrip = strips[testIndex];
				if(testStrip.isUsed()) {
					continue;
				}
				
				double leftScore = leftStrip.getLeft().averageDistance(testStrip.getRight());
				double rightScore = rightStrip.getRight().averageDistance(testStrip.getLeft());
				
				if(leftScore < minLeftScore) {
					minLeftScore = leftScore;
					leftIndex = testIndex;
				}
				
				if(rightScore < minRightScore) {
					minRightScore = rightScore;
					rightIndex = testIndex;
				}
			}
			
			if(minRightScore < minLeftScore) {
				sorted.add(strips[rightIndex]);
				strips[rightIndex].setUsed(true);
			} else {
				sorted.add(0, strips[leftIndex]);
				strips[leftIndex].setUsed(true);
			}
		}
		log("Done unshredding!");
		
		// reconstruct the image
		log("Reconstructing image...");
		this.reconstructed = new BufferedImage(width, height, image.getType());
		int destX = 0;
		for(int index = 0; index < sorted.size(); index++) {
			ImageStrip is = sorted.get(index);
			BufferedImage im = is.getImage();
			for(int x = 0; x < im.getWidth(); x++) {
				for(int y = 0; y < height; y++) {
					this.reconstructed.setRGB(destX, y, im.getRGB(x, y));
				}
				destX++;
			}
		}
		log("Done reconstructing!");
	}
	
	/**
	 * Find the width of the shred strip.
	 *  
	 */
	private void findStripWidth() {
		if(this.stripWidth != -1) {
			log("Strip width already provided as " + this.stripWidth + ", skipping auto detection.");
			return;
		}
		
		log("Finding strip width...");
		
		final int height = image.getHeight();
		final int width = image.getWidth();
		
		// from the start - scan each pixel strip vertically
		// and then see if the distance has gone above a threshold
		// store the value for some of the ones
		// and then check for max mean values

		// compute the max and min
		double minDiff = Double.MAX_VALUE, maxDiff = 0, sum = 0;
		double[] distances = new double[width + 1];
		for(int index = 0; index < width - 1; index++) {
			PixelColumn left = new PixelColumn(height);
			PixelColumn right = new PixelColumn(height);
			for(int y = 0; y < height; y++) {
				left.setRGB(y, new RGB(image.getRGB(index, y)));
				right.setRGB(y, new RGB(image.getRGB(index + 1, y)));
			}
			
			double distance = left.averageDistance(right);
			if(distance < minDiff) {
				minDiff = distance;
			}
			if(distance > maxDiff) {
				maxDiff = distance;
			}
			
			sum += distance;
			distances[index] = distance;
		}
		
		// find the average and ratio of max/min
		final double average = sum / width;
		final double ratio = maxDiff / minDiff;
		
		// compute the comparison ratio
		double lastAverage = average; 
		double maxAverage = 0;
		do {
			int maxCount = 0;
			double maxSum = 0;
			for(int index = 0; index < width; index++) {
				if(distances[index] > lastAverage) {
					maxSum += distances[index];
					maxCount++;
				}
			}
			maxAverage = maxSum / maxCount;
			if((maxAverage / lastAverage) < 1.75) {
				break;
			}

			lastAverage = maxAverage;
		} while(true);
		
		boolean stripWidthComputed = false;
		double compareRatio = 2.0;
		do {
			for(int index = 0; index < width; index++) {
				double distance = distances[index];
				if(distance > average) {
					double currentRatio = distance / minDiff;
					if((ratio / currentRatio) < compareRatio) {
						int stripe = index + 1;
						
						if(width % stripe == 0) {
							this.stripWidth = stripe;
							stripWidthComputed = true;
							break;
						}
					}
				}
			}
			
			// unable to compute the strip width
			// reduce the ratio by 0.5
			compareRatio -= 0.1;
		} while(!stripWidthComputed);

		if(this.stripWidth == 0) {
			stripWidth = 32;
			log("Default strip width to 32 pixels");
		} else {
			log("Strip width found as " + this.stripWidth);
		}
	}

	/**
	 * Load the image from the given image url and contruct the {@link BufferedImage} object.
	 * 
	 * @param imageUrl
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void loadImage(String imageUrl) throws MalformedURLException, IOException {
		log("loading image...");
		File file = new File(imageUrl);
		image = ImageIO.read(file);
		log("image loaded.");
	}
	
	/**
	 * Log the given message and add a new line at the end.
	 * 
	 * @param logMessage
	 */
	private static void log(String logMessage) {
		if(!LOGS_ENABLED) {
			return;
		}
		
		log(logMessage, true);
	}

	private static void log(String logMessage, boolean newLine) {
		if(!LOGS_ENABLED) {
			return;
		}
		
		if("".equals(logMessage)) {
			System.out.println("");
			return;
		}
		
		if(newLine) {
			System.out.print(new Date().toString());
			System.out.print(": ");
			System.out.println(logMessage);			
		} else {
			System.out.print(logMessage);
		}
	}

	private static String getUnshreddedImageName(String filename) {
		int index = filename.lastIndexOf('.');
		String name = filename.substring(0, index);
		String extension = filename.substring(index + 1);
		return name + ".reconstructed." + extension;
	}
}
