package ui;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class RealImage implements Image
{
	private BufferedImage img;

	public RealImage(String newFileName)
	{
		img = null;
		loadFromDisk(newFileName);
	}

	/**
	 * Get a a JFX image.
	 * @return Image.
	 */
	javafx.scene.image.Image getFXImage()
	{
		return SwingFXUtils.toFXImage(img, null);
	}

	private void loadFromDisk(String fileName) {
		// Only load image if it isn't loaded yet
		if (img == null)
		{
			try
			{
				img = ImageIO.read(getClass().getResource(fileName).openStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
