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

public class PixelColumn {
	
	private int height;
	
	private int[] red;
	
	private int[] green;
	
	private int[] blue;
	
	private long sumRed = 0;
	
	private long sumGreen = 0;
	
	private long sumBlue = 0;
	
	private int valuesRead = 0;
	
	public PixelColumn(int height) {
		this.height = height;
		
		this.red = new int[height + 1];
		this.green = new int[height + 1];
		this.blue = new int[height + 1];
	}

	public void setRGB(int y, RGB rgb) {
		this.red[y] = rgb.getRed();
		this.green[y] = rgb.getGreen();
		this.blue[y] = rgb.getBlue();
		
		sumRed += this.red[y];
		sumGreen += this.green[y];
		sumBlue += this.blue[y];
		
		valuesRead++;
	}
	
	public double averageRGB() {
		long sum = this.sumRed + this.sumGreen + this.sumBlue;
		return ((double) sum) / this.valuesRead;
	}

	public int getHeight() {
		return height;
	}

}
