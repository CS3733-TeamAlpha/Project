package ui;

import java.awt.image.BufferedImage;


public class ProxyImage implements Image {

	private RealImage realImage;
	private String fileName;

	public ProxyImage (String fileName)
	{
		this.fileName = fileName;
		realImage = new RealImage(fileName);
	}

	public Image getRealImage()
	{
		return this.realImage;
	}

	public javafx.scene.image.Image getFXImage()
	{
		return this.realImage.getFXImage();
	}

	// Display image from associated file, create object for RealImage if necessary
	@Override
	public void display() {
		if(realImage == null) {
			realImage = new RealImage(fileName);
		}
		realImage.display();
	}
}
