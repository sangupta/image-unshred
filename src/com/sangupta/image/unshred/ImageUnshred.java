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
	
	private int stripWidth;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String imageUrl;
		
		if(args.length == 1) {
			imageUrl = args[0];
		} else {
			imageUrl = "tree40.jpg";
		}

		ImageUnshred unshredder = new ImageUnshred();
		
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
		unshredder.writeReconstructed("reconstructed.png");
		
		log("Reconstructed image written to disk.");
	}

	/**
	 * Write the reconstructed image to the given file on disk.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private void writeReconstructed(String fileName) throws IOException {
		if(this.reconstructed != null) {
			ImageIO.write(this.reconstructed, "png", new File(fileName));
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
		log("finding strip width...");
		
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
		double average = sum / width;
		double ratio = maxDiff / minDiff;
		
		for(int index = 0; index < width; index++) {
			if(distances[index] > average) {
				double currentRatio = distances[index] / minDiff;
				if((currentRatio / ratio) > 0.5) {
					this.stripWidth = index + 1;
					break;
				}
			}
		}

		if(this.stripWidth == 0) {
			stripWidth = 32;
		}
		
		log("Strip width found as " + this.stripWidth);
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
		image = ImageIO.read(new File(imageUrl));
		log("image loaded.");
	}
	
	/**
	 * Log the given message and add a new line at the end.
	 * 
	 * @param logMessage
	 */
	private static void log(String logMessage) {
		log(logMessage, true);
	}

	private static void log(String logMessage, boolean newLine) {
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

}
