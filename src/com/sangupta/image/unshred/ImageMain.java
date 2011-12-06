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

public class ImageMain {
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.out.println("Usage: This class serves as an entry point to the image-unshred suite of classes." +
					"\nThe first argument is the name of the function to be invoked. Rest of the arguments are" +
					"\nas defined for the function." +
					"\n" +
					"\n$ java -jar image-unshred.jar <function> <arguments>" +
					"\n" +
					"\n    <function>    Valid values are shred/unshred/test" +
					"\n    <arguments>   Arguments to be passed to the respective function");
			return;
		}
		
		final String function = args[0].toLowerCase();
		final String[] newArgs = getNewArgs(args);

		if("shred".equals(function)) {
			ImageShred.main(newArgs);
			return;
		}
		
		if("test".equals(function)) {
			ImageUnshredderTest.main(newArgs);
			return;
		}
		
		if("unshred".equals(function)) {
			ImageUnshred.main(newArgs);
			return;
		}
		
		System.out.println("Unknown function: Valid values are shred/unshred/test");
		return;
	}

	private static String[] getNewArgs(String[] args) {
		final int length = args.length - 1;
		String[] newArgs = new String[length];
		for(int index = 1; index < args.length; index++) {
			newArgs[index - 1] = args[index];
		}
		
		return newArgs;
	}

}
