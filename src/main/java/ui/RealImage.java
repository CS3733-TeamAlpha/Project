package ui;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

	javafx.scene.image.Image getFXImage(){
		return SwingFXUtils.toFXImage(img, null);
	}

	@Override
	public void display() {
		System.out.println("Displaying " + fileName);
	}

	private void loadFromDisk(String fileName) {
		if (img == null) { // Only load image if it isn't loaded yet
			// Load image from filename to img
			System.out.println("Loading " + fileName); // Let us know we're loading
			try {
				System.out.println(fileName);
				img = ImageIO.read(getClass().getResource(fileName).openStream());
			} catch (IOException e) {
				System.out.println(fileName);
				e.printStackTrace();
			}
		}

	}
}
