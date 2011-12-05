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

public class ImageStrip {
	
	private PixelColumn left;
	
	private PixelColumn right;
	
	private BufferedImage image;
	
	private boolean used;
	
	private int position;
	
	public ImageStrip(int position, BufferedImage stripImage) {
		this.position = position;
		this.image = stripImage;
		
		int height = stripImage.getHeight();
		
		this.left = new PixelColumn(height);
		this.right = new PixelColumn(height);
		
		int lastX = image.getWidth() - 1;
		for(int y = 0; y < image.getHeight(); y++) {
			this.left.setRGB(y, new RGB(image.getRGB(0, y)));
			this.right.setRGB(y, new RGB(image.getRGB(lastX, y)));
		}
	}

	public void setLeft(int y, RGB rgb) {
		this.left.setRGB(y, rgb);
	}
	
	public void setRight(int y, RGB rgb) {
		this.right.setRGB(y, rgb);
	}

	public PixelColumn getLeft() {
		return left;
	}

	public PixelColumn getRight() {
		return right;
	}

	public BufferedImage getImage() {
		return image;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public int getPosition() {
		return position;
	}
}
