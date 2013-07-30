package gizmoe.capabilities;

import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class DisplayPhoto extends DemoBaseCapability{

	private static class ShowImage extends Panel{
		private static final long serialVersionUID = 1L;

		  private BufferedImage image;

		  public ShowImage(String filename) {
		    try {
		      image = ImageIO.read(new File(filename));
		    } catch (IOException ie) {
		      ie.printStackTrace();
		    }
		  }

		  public void paint(Graphics g) {
		    g.drawImage(image, 0, 0, null);
		  }

		  static public void doShow(String args, long secs) throws Exception {
		    JFrame frame = new JFrame("Photo");
		    Panel panel = new ShowImage(args);
		    frame.getContentPane().add(panel);
		    frame.setSize(560, 560);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
	        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
	        int x = (int) rect.getMaxX() - frame.getWidth();
	        int y = 0;
	        frame.setLocation(x, y);
		    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    frame.setVisible(true);
		    //Simulate operation
			if(secs > 0){
				try {
					Thread.sleep(secs * 1000);
				} catch (Exception e) {
				    frame.dispose();
					return;
				}
			}else{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					frame.dispose();
					return;
				}
			}
		    frame.dispose();
		  }
	}
	private final String tag = "DisplayPhoto, thread"+this.hashCode()+":: ";
	ConcurrentHashMap<String, Object> ioMap;

	public void run() {
		String handle = null;
		if(ioMap.containsKey("queryHandle")){
			/*
			 * Input Section
			 */
			handle = (String) ioMap.get("queryHandle");
			System.out.println(tag+"Displaying "+handle+"'s photo now.");
			//System.out.println(tag+"Received input queryHandle = "+handle);
			
			/*
			 * Operation Section
			 */
			try {
				ShowImage.doShow(handle, seconds);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else{
			System.err.println(tag+"Input queryHandle not found");
		}
		ioMap.clear();
}
	
	public DisplayPhoto(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}

}
