package ui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Sam on 2/15/2017.
 */
public class RealImage implements Image {
	private String fileName;
	private BufferedImage img;

	public RealImage(String fileName) {
		this.fileName = fileName;
		this.img = null;
		loadFromDisk(fileName);
	}

	// Getter for image in this class
	BufferedImage getImg () {
		return img;
	}

	@Override
	public void display() {
		System.out.println("Displaying " + fileName);
		// TODO: Make display return the required format to update the map image
	}

	private void loadFromDisk(String fileName) {
		System.out.println("Loading " + fileName);

		// Load image from filename to img
		try {
			img = ImageIO.read(new File(fileName));
		} catch (IOException e) {
		}

	}
}
