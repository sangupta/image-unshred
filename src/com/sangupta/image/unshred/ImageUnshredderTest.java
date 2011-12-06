package com.sangupta.image.unshred;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUnshredderTest {

	/**
	 * Command line invocation handler that takes in a folder and shreds all files into equal slices
	 * and then unshreds them - once done - it compares each original image with the unshredded image
	 * and checks if the algorithm was successful or not.
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: $ java -jar image-unshred.jar test <folder>");
			System.out.println("");
			System.out.println("    <folder>    folder containing a set of original images. Supported");
			System.out.println("                image formats are GIF, JPG and PNG");
			return;
		}
		
		File folder = new File(args[0]);
		if(!folder.exists()) {
			System.out.println("Given folder does not exists.");
			return;
		}
		
		if(!folder.isDirectory()) {
			System.out.println("Given folder is not a directory.");
			return;
		}
		
		File[] files = folder.listFiles();
		if(files.length == 0) {
			System.out.println("No file found in the given folder.");
			return;
		}
		
		List<File> sampleFiles = new ArrayList<File>();
		for(File file : files) {
			file = file.getAbsoluteFile();
			String extension = getExtension(file);
			if("gif".equals(extension) || "jpg".equals(extension) || "png".equals(extension)) {
				sampleFiles.add(file);
			}
		}
		
		System.out.println("Number of files to be tested: " + sampleFiles.size());
		
		int passed = 0, failed = 0;
		
		final long start = System.currentTimeMillis(); 
		for(File file : sampleFiles) {
			final long startImage = System.currentTimeMillis(); 
			boolean success = testImage(file);
			final long endImage = System.currentTimeMillis();
			if(success) {
				System.out.println("Image " + file.getName() + "passed test in " + (endImage - startImage) + " ms.");
				passed++;
			} else {
				System.out.println("Image " + file.getName() + "failed test in " + (endImage - startImage) + " ms.");
				failed++;
			}
		}
		final long end = System.currentTimeMillis();
		
		System.out.println("Passed: " + passed + ", Failed: " + failed);
		System.out.println("Test completed in " + (end - start) + " ms.");
	}

	private static boolean testImage(final File file) {
		boolean success = false;
		
		String filename = file.getName();
		String path = file.getParentFile().getAbsolutePath();

		int index = filename.lastIndexOf('.');
		String name = filename.substring(0, index - 1);
		String extension = filename.substring(index + 1);
		
		// create a shredded image
		String shreddedImage = path + File.separator + name + ".shredded." + extension;
		success = ImageShred.shredImage(file, shreddedImage);
		if(!success) {
			return false;
		}
		
		// create the unshredded image
		String reconstructedImage = path + File.separator + name + ".reconstructed." + extension;
		success = ImageUnshred.unshredImage(shreddedImage, reconstructedImage);
		if(!success) {
			return false;
		}
		
		// compare original and unshredded image
		success = compare(file, new File(reconstructedImage));
		
		return success;
	}

	private static boolean compare(File original, File copy) {
		try {
			BufferedImage im1 = ImageIO.read(original);
			BufferedImage im2 = ImageIO.read(copy);
			final int width = im1.getWidth();
			final int height = im2.getHeight();
			
			for(int x = 0; x < width; x++) {
				for(int y = 0; y < height; y++) {
					int c1 = im1.getRGB(x, y);
					int c2 = im2.getRGB(x, y);
					
					if(c1 != c2) {
						return false;
					}
				}
			}
			
			return true;
		} catch(Exception e) {
			
		}
		return false;
	}

	private static String getExtension(File file) {
		int index = file.getName().lastIndexOf('.');
		if(index == -1) {
			return null;
		}
		
		return file.getName().substring(index + 1);
	}

}
