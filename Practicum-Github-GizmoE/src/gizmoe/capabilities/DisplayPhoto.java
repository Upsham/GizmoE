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

public class DisplayPhoto extends CapabilityBase{

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

		  static public void main(String args) throws Exception {
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
		    try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    frame.dispose();
		  }
	}
	private static final long serialVersionUID = 1L;
	private final String tag = "DisplayPhoto, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String handle = null;
		if(inputs.containsKey("queryHandle")){
			/*
			 * Input Section
			 */
			handle = (String) inputs.get("queryHandle");
			//System.out.println(tag+"Received input queryHandle = "+handle);
			
			/*
			 * Operation Section
			 */
			try {
				ShowImage.main(handle);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return outputs;
		}else{
			System.err.println(tag+"Input queryHandle not found");
		}
		return outputs;
	}

}
