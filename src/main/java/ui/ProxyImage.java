package ui;

/**
 * Created by Sam on 2/15/2017.
 */
public class ProxyImage implements Image {

	private RealImage realImage;
	private String fileName;

	public ProxyImage (String fileName) {
		this.fileName = fileName;
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
