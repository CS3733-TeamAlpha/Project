package ui;

public class ProxyImage implements Image
{

	private RealImage realImage;
	private String fileName;

	public ProxyImage (String fileName)
	{
		this.fileName = fileName;
	}

	public javafx.scene.image.Image getFXImage()
	{
		lazyLoad();
		return this.realImage.getFXImage();
	}

	private void lazyLoad()
	{
		if(realImage == null)
			realImage = new RealImage(fileName);
	}
}
