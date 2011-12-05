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
	
	private double y, u, v;
	
	public RGB(int value) {
		this.red = (value >> 16) & 0xff;
		this.green = (value >> 8) & 0xff;
		this.blue = value & 0xff;
		
		y = this.red *  .299000 + this.green *  .587000 + this.blue *  .114000;
		u = this.red * -.168736 + this.green * -.331264 + this.blue *  .500000 + 128;
		v = this.red *  .500000 + this.green * -.418688 + this.blue * -.081312 + 128;
	}
	
	public double distance(RGB other) {
		double diff = ((this.u - other.u) * (this.u - other.u)) + ((this.v - other.v) * (this.v - other.v));
		return Math.sqrt(diff);
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

	public double getY() {
		return y;
	}

	public double getU() {
		return u;
	}

	public double getV() {
		return v;
	}

}
