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

	private RGB[] rgb;
	
	public PixelColumn(int height) {
		this.height = height;
		this.rgb = new RGB[height + 1];
	}
	
	public void setRGB(int y, RGB rgb) {
		this.rgb[y] = rgb;
	}
	
	public int getHeight() {
		return height;
	}

	public double averageDistance(PixelColumn other) {
		double distance = 0;
		for(int i = 0; i < this.height; i++) {
			distance += this.rgb[i].distance(other.rgb[i]);
		}
		return distance / this.height;
	}

}
