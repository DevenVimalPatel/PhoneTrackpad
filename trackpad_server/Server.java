import java.lang.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.net.*;
import java.io.*;

public class Server  {

    private Robot rbt; 
    private ServerSocket ssocket;

    public Server(int port) {
      	try {
	    rbt = new Robot();
	    ssocket = new ServerSocket(port);
	} catch (AWTException e) {
	    System.err.println("Issues creating Robot; quitting...");
	    System.exit(1);
	} catch (IOException e) {
	    System.err.println("Issue creating socket; quitting...");
	    System.exit(2);
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

    public void acceptAndRead() {
	try ( Socket phone = ssocket.accept();
              PrintWriter out =
	      new PrintWriter(phone.getOutputStream(), true);
	      BufferedReader in = new BufferedReader(
	      new InputStreamReader(phone.getInputStream()));
	      ) {
		int x, y;
		String inline;
		while((inline = in.readLine()) != null) {
		    System.out.println(inline);
		    if (inline.equals("quit"))
			break;
		    String[] parts = inline.split(",");
		    if(parts.length != 2)
			continue;
		    try {
			x = Integer.parseInt(parts[0]);
			y = Integer.parseInt(parts[1]);
			System.out.println("Moving to " + x + "," + y);
			rbt.mouseMove(x,y);
			rbt.waitForIdle();
		    } catch (NumberFormatException e) {
		    } catch (IllegalThreadStateException e) {
			break;
		    }
		}
	    
	} catch (IOException e) {
	    System.err.println("Issues accepting or reading from client; print stack trace...");
	    e.printStackTrace();	    
	}   
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
	server.acceptAndRead();
	server.close();
    }
}