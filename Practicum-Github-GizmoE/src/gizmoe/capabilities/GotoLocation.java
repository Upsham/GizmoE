package gizmoe.capabilities;

import java.awt.*;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class GotoLocation extends CapabilityBase{

	private static class MyImage extends Frame {
	    
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		static int xPixel = 20;
	    static int yPixel = 20; 
	    
	    Image myImage, offScreenImage;
	    Graphics offScreenGraphics;
	    String location;
	    public MyImage(String loc) {
	        location = loc;
	        try {
	          myImage = ImageIO.read(new File("/Users/upsham/Pictures/cobot.png"));; 
	        } 
	        catch(Exception e) {}
	        JLabel textLabel = new JLabel("I'm a label in the window",SwingConstants.CENTER); 
	        textLabel.setPreferredSize(new Dimension(300, 100)); 
	        setSize(800,600);
	        setVisible(true);
	        moveImage();
	        
	    }
	    
	    public void update(Graphics g) {
	        paint(g);
	    }
	    
	    public void paint(Graphics g) {
	        
	        int width  = getWidth();
	        int height = getHeight();
	        
	        if (offScreenImage == null) {
	            offScreenImage    = createImage(width, height);
	            offScreenGraphics = offScreenImage.getGraphics();
	        }
	        
	        // clear the off screen image
	        offScreenGraphics.clearRect(0, 0, width + 1, height + 1);
	        
	        // draw your image off screen
	        offScreenGraphics.drawImage(myImage, xPixel, yPixel, this);
	        
	        // show the off screen image
	        g.drawImage(offScreenImage, 0, 0, this);
	        	   
	        g.drawString(location, 750, 300);

	    }
	    
	    void moveImage() {
	        
	        for ( int i = 0 ; i < 500 ; i++ ){
	            	                        
	            xPixel +=1;
	            yPixel = 0;
	            repaint();
	            
	            // then sleep for a bit for your animation
	            try { Thread.sleep(20); }   /* this will pause for 20 milliseconds */
	            catch (InterruptedException e) { System.err.println("sleep exception"); }
	            
	        }
	        this.dispose();
	    }
	    
	    public static void main(String location){
	    
	        MyImage me = new MyImage(location);	
	        me.dispose();
	    }
	    
	}
	
	private static final long serialVersionUID = 1L;
	private final String tag = "GotoLocation, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String location = null;
		if(inputs.containsKey("location")){
			/*
			 * Input Section
			 */
			location = (String) inputs.get("location");
//			System.out.println(tag+"Received input location = "+location);
			System.out.println(tag+"Cobot3 is moving to "+location);
			MyImage.main(location);
			/*
			 * Operation Section
			 */
			//Go to location sim
		}else{
			System.err.println(tag+"Input location not found");
		}
		System.out.println(tag+"Cobot3 is now at "+location);
//		System.out.println(tag+"At location = "+location);
		return outputs;
	}

}
