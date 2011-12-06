package com.sangupta.image.unshred;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageShred {
	
	/**
	 * Command line function to shred the given image into the given number of shreds.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 0 || args.length > 2) {
			System.out.println("Usage: $ java -jar image-unshred.jar <image> <shreds>");
			System.out.println("");
			System.out.println("    <image>    the path of the image that needs to be shredded");
			System.out.println("    <shreds>   the number of shreds to be created");
			System.out.println("");
			System.out.println("For an original image as original.png the shredded image is created as");
			System.out.println("original.shredded.png. Supported image formats are GIF, JPG, and PNG.");
			return;
		}
		
		File image = new File(args[0]);
		if(!image.exists()) {
			System.out.println("The given image file does not exists.");
			return;
		}
		
		if(image.isDirectory()) {
			System.out.println("The given image is a directory and not a file.");
			return;
		}
		image = image.getAbsoluteFile();
		
		int shredWidth = Integer.parseInt(args[1]);
		
		final long start = System.currentTimeMillis();
		
		try {
			shredImage(image, shredWidth);
		} catch (IOException e) {
			System.out.println("Unable to read image from disk." + e);
		}
		
		final long end = System.currentTimeMillis();
		
		System.out.println("Time taken " + (end - start) + " ms.");
	}
	
	public static void shredImage(File imageFile, int shredWidth) throws IOException {
		BufferedImage originalImage = ImageIO.read(imageFile);
		
		final int width = originalImage.getWidth();
		final int height = originalImage.getHeight();
		
		if(width % shredWidth != 0) {
			System.out.println("Shred width will not create slices equally. Image width: " + width);
			return;
		}
		
		// create slices
		int slices = width / shredWidth;
		List<BufferedImage> images = new ArrayList<BufferedImage>(slices);
		for(int index = 0; index < slices; index++) {
			int startX = index * shredWidth;
			BufferedImage subImage = originalImage.getSubimage(startX, 0, shredWidth, height);
			images.add(index, subImage);
		}
		
		// shuffle
		Collections.shuffle(images);
		
		// create the shredded image in memory
		int destX = 0;
		BufferedImage reconstructedImage = new BufferedImage(width, height, originalImage.getType());
		for(int index = 0; index < images.size(); index++) {
			BufferedImage im = images.get(index);
			for(int x = 0; x < im.getWidth(); x++) {
				for(int y = 0; y < height; y++) {
					reconstructedImage.setRGB(destX, y, im.getRGB(x, y));
				}
				destX++;
			}
		}
		
		// write it to disk
		String sim = getShreddedImageName(imageFile.getName());
		String path = imageFile.getParentFile().getAbsolutePath() + File.separator + sim;
		System.out.println("Writing reconstructed image as " + path);
		
		int index = path.lastIndexOf('.');
		String extension = path.substring(index + 1);
		ImageIO.write(reconstructedImage, extension, new File(path));
		
		System.out.println("Done shredding image.");
	}

	private static String getShreddedImageName(String filename) {
		int index = filename.lastIndexOf('.');
		String name = filename.substring(0, index - 1);
		String extension = filename.substring(index + 1);
		return name + ".shredded." + extension;
	}

}
