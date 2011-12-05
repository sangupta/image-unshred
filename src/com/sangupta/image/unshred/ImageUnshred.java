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
import java.util.Date;

import javax.imageio.ImageIO;

public class ImageUnshred {
	
	private BufferedImage image;
	
	private BufferedImage reconstructed;
	
	private int stripWidth;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String imageUrl;
		
		if(args.length == 1) {
			imageUrl = args[0];
		} else {
			imageUrl = "shredded.png";
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

	private void writeReconstructed(String fileName) throws IOException {
		ImageIO.write(this.reconstructed, "png", new File(fileName));
	}

	private void unshred() {
		log("unshredding starts...");
		
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		// number of columns in the image
		final int columns = width / this.stripWidth;
		final int[] sequence = new int[columns + 1];
		for(int i = 0; i < sequence.length; i++) {
			sequence[i] = -1;
		}
		
		log("total columns: " + columns);
		
		// read each strip
		log("reading strips...");
		ImageStrip[] strips = new ImageStrip[columns];
		for(int column = 0; column < columns; column++) {
			strips[column] = new ImageStrip(height);
			
			int leftX = column * stripWidth;
			int rightX = leftX + stripWidth - 1;
			for(int y = 0; y < height; y++) {
				int value = image.getRGB(leftX, y);
				RGB rgb = new RGB(value);
				strips[column].setLeft(y, rgb);
				
				value = image.getRGB(rightX, y);
				rgb = new RGB(value);
				strips[column].setRight(y, rgb);
			}
		}
		
		// now starting read 
		log("start finding left most strip...");
		
		// left most strip is the one that has the highest difference from the right most edge of all strips
		double diffRGB = 0;
		int leftColumn = -1;
		for(int c1 = 0; c1 < columns; c1++) {
			double leftRGB = strips[c1].getLeft().averageRGB();
			for(int c2 = 0; c2 < columns; c2++) {
				if(c1 == c2) {
					continue;
				}
				
				double rightRGB = strips[c2].getRight().averageRGB();
				double diff = Math.abs(leftRGB - rightRGB);
				if(diff > diffRGB) {
					diffRGB = diff;
					leftColumn = c1;
				}
			}
		}
		
		log("found leftmost column as: " + leftColumn);
		sequence[0] = leftColumn;
		
		// now start finding the columns to the right of it
		// and this we have to do for every strip
		// so one less than total strips
		for(int currentColumnToBeFound = 1; currentColumnToBeFound < columns; currentColumnToBeFound++) {
			double minDiff = Double.MAX_VALUE;
			int columnFound = -1;
			double rightRGB = strips[currentColumnToBeFound - 1].getRight().averageRGB();
			
			// now find the least diff on all left edges
			for(int column = 0; column < columns; column++) {
				// check if j is in sequence - skip it
				boolean alreadyUsed = false;
				for(int sequenceIndex = 0; sequenceIndex <= currentColumnToBeFound; sequenceIndex++) {
					if(column == sequence[sequenceIndex]) {
						alreadyUsed = true;
						break;
					}
				}
				if(alreadyUsed) {
					continue;
				}
				
				
				// this strip has not been used
				double leftRGB = strips[column].getLeft().averageRGB();
				double diff = Math.abs(leftRGB - rightRGB);
				if(diff < minDiff) {
					minDiff = diff;
					columnFound = column;
				}
			}
			
			// set it in sequence
			sequence[currentColumnToBeFound] = columnFound;
			log("Found column " + currentColumnToBeFound + " as column num " + columnFound + " with absolute diff of " + minDiff);
		}
		log("unshredding ends.");
		
		log("reconstructing the image...");
		
		reconstructed = new BufferedImage(width, height, this.image.getType());
		for(int index = 0; index < columns; index++) {
			int column = sequence[index];
			
			// transfer all the pixels to the new reconstructed image
			final int sourceStartX = column * stripWidth;
			final int sourceEndX = sourceStartX + stripWidth;
			
			int destStartX = index * stripWidth;
			for(int x = sourceStartX; x < sourceEndX; x++) {
				for(int y = 0; y < height; y++) {
					reconstructed.setRGB(destStartX, y, image.getRGB(x, y));
				}
				destStartX++;
			}
		}
		
		log("reconstruction ends.");
	}
	
	private void findStripWidth() {
		log("finding strip width...");
		
		// for now we know the strip width
		this.stripWidth = 32;
		
		log("strip width=" + this.stripWidth);
	}

	private void loadImage(String imageUrl) throws MalformedURLException, IOException {
		log("loading image...");
		image = ImageIO.read(new File(imageUrl));
		log("image loaded.");
	}

	private static void log(String logMessage) {
		System.out.print(new Date().toString());
		System.out.print(": ");
		System.out.println(logMessage);
	}

}
