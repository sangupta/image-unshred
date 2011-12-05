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

public class RGB {
	
	private int red;
	
	private int green;
	
	private int blue;
	
	public RGB(int value) {
		this.red = (value >> 16) & 0xff;
		this.green = (value >> 8) & 0xff;
		this.blue = value & 0xff;
	}
	
	@Override
	public String toString() {
		return "[" + this.red + ", " + this.green + ", " + this.blue + "]";
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

}
