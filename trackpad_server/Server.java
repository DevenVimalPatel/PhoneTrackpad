import java.lang.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.net.*;
import java.io.*;

public class Server  {

    private Robot rbt; 
    private ServerSocket ssocket;
    private int x, y;
    private double width, height;

    public Server(int port) {
      	try {
	    rbt = new Robot();
	    ssocket = new ServerSocket(port);
	} catch (AWTException e) {
	    System.err.println("Issue creating Robot; printing stack trace and quitting...");
	    e.printStackTrace();
	    System.exit(1);
	} catch (IOException e) {
	    System.err.println("Issue creating socket; printing stack trace and quitting...");
	    e.printStackTrace();
	    System.exit(2);
	}

	try {
	    Point pos = MouseInfo.getPointerInfo().getLocation();
	    x = (int) pos.getX();
	    y = (int) pos.getY();
	} catch (Exception e) {
	    System.err.println("Issue getting current mouse pointer; printint stack trace and quitting...");
	    e.printStackTrace();
	    System.exit(3);
	}

	try {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    width = screenSize.getWidth();
	    height = screenSize.getHeight();
	} catch (Exception e) {
	    System.err.println("Issue getting maximum screen size; printing stack trace and quitting...");
	    e.printStackTrace();
	    System.exit(4);
	}

    }

    public void close() {
	try {
	    ssocket.close();
	} catch (IOException e) {
	    System.err.println("Issues closing server socket; print stack trace...");
	    e.printStackTrace();
	}
    }

    public boolean acceptAndRead() {
	try ( Socket phone = ssocket.accept();
	      BufferedReader in = new BufferedReader(
	      new InputStreamReader(phone.getInputStream()));
	      ) {
		int cmd;
		String inline;
		while((inline = in.readLine()) != null) {
		    System.out.println(inline);
		    if (inline.equals("quit"))
			return false;
		    String[] parts = inline.split(",");
		    if(parts.length > 2)
			continue;
		    if (parts.length == 2) {
			try {
			    int incX = Math.round(Float.parseFloat(parts[0]));
			    int incY = Math.round(Float.parseFloat(parts[1]));
			    if(x + incX > 0 &&  x + incX < width)
				x += incX;
			    if(y + incY > 0 &&  y + incY < height)
				y += incY;
			    System.out.println("Moving to " + x + "," + y);
			    rbt.mouseMove(x,y);
			    rbt.waitForIdle();
			} catch (NumberFormatException e) {
			} catch (IllegalThreadStateException e) {
			    System.out.println("Something fishy with threads...");
			    break;
			}
		    }
		    else if (parts.length == 1) {
			try {
			    cmd = Integer.parseInt(parts[0]);
			    switch(cmd) {
			        case 1:
				    System.out.println("Press button");
				    rbt.mousePress(InputEvent.BUTTON1_MASK);
				    break;
			        case 2:
				    System.out.println("Release button");
				    rbt.mouseRelease(InputEvent.BUTTON1_MASK);
				    break;
			        default:
				    break;
			    }
			} catch (NumberFormatException e) {
			}
		    }
		}
	    
	} catch (IOException e) {
	    System.err.println("Issues accepting or reading from client; print stack trace...");
	    e.printStackTrace();	    
	}   
	return false;
    }



    public static void main(String[] args) {
	if(args.length != 1) {
	    System.err.println("Wrong arguments given");
	    System.exit(1);
	}
	int port = -1;
	try {
	    port = Integer.parseInt(args[0]);
	} catch (NumberFormatException e) {
	    System.err.println("Argument not a valid integer");
	    System.exit(1);
	}

	Server server = new Server(port);
	while(!server.acceptAndRead())
	    continue;
	server.close();
    }
}