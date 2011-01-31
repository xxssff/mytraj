package graphics;

import java.applet.*;
import java.awt.*;

import com.vividsolutions.jts.geom.Coordinate;

// The applet's class name must be identical to the filename.
public class DrawingLines extends Applet {

	// Declare two variables of type "int" (integer).
	int width, height;

	// This gets executed when the applet starts.
	public void init() {

		// Store the height and width of the applet for future reference.
		width = getSize().width;
		height = getSize().height;

		// Make the default background color black.
		setBackground(Color.black);
	}

	// This gets executed whenever the applet is asked to redraw itself.
	public void paint(Graphics g) {

		// Set the current drawing color to green.
		g.setColor(Color.green);

		// Draw ten lines using a loop.
		// We declare a temporary variable, i, of type "int".
		// Note that "++i" is simply shorthand for "i=i+1"
		// for ( int i = 0; i < 10; ++i ) {
		//
		// // The "drawLine" routine requires 4 numbers:
		// // the x and y coordinates of the starting point,
		// // and the x and y coordinates of the ending point,
		// // in that order. Note that the cartesian plane,
		// // in this case, is upside down (as it often is
		// // in 2D graphics programming): the origin is at the
		// // upper left corner, the x-axis increases to the right,
		// // and the y-axis increases downward.
		// g.drawLine( width, height, i * width / 10, 0 );
		// }
//		g.drawLine(x1, y1, x2, y2);

		
//		Coordinate c1 = new Coordinate(0, 0);
//		Coordinate c2 = new Coordinate(1, 1);
//		Coordinate c3 = new Coordinate(2, 1);
//		Coordinate c4 = new Coordinate(2, 2);

		g.drawLine(0, 0, 100, 100);
		g.drawLine(100, 100, 200, 100);
		g.drawLine(200, 100, 200, 200);
		
//		Coordinate c5 = new Coordinate(1, 1.5);
//		Coordinate c6 = new Coordinate(3, 1.5);
		g.setColor(Color.yellow);
		g.drawLine(100, 200, 300, 300);
	}
}
