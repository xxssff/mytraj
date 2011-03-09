package graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class PointPlot extends JApplet {

	// Declare two variables of type "int" (integer).
	final static BasicStroke stroke = new BasicStroke(2.0f);

	public void init() {
		// Store the height and width of the applet for future reference.
		setBackground(Color.white);
		setForeground(Color.white);
	}

	// This gets executed whenever the applet is asked to redraw itself.
	public void paint(Graphics g) {

		// Set the current drawing color to green.
		// g.setColor(Color.black);

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
		// g.drawLine(x1, y1, x2, y2);

		// Coordinate c1 = new Coordinate(0, 0);
		// Coordinate c2 = new Coordinate(1, 1);
		// Coordinate c3 = new Coordinate(2, 1);
		// Coordinate c4 = new Coordinate(2, 2);

		// file handling
		String dataFile = "E:\\weka\\datasets-UCI_20051003\\UCI\\test.arff";
		ArrayList<double[]> coords = new ArrayList<double[]>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith("@") || line.trim().length() == 0) {
					// continue;
				} else {
					StringTokenizer st = new StringTokenizer(line, ",");
					double[] temp = { Double.parseDouble(st.nextToken()),
							Double.parseDouble(st.nextToken()) };
					coords.add(temp);
				}
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("array size: "+coords.size());
		Graphics2D g2 = (Graphics2D) g;
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawLine(0, 0, 100, 100);
		g.drawLine(100, 100, 200, 100);
		g.drawLine(200, 100, 200, 200);

//		g2.setPaint(Color.gray);
		g2.setColor(Color.blue);
		
		for (double[] coord : coords){
			double x = coord[0];
			double y = coord[1];
			Ellipse2D e = new Ellipse2D.Double(x, y, 10, 10);
			g2.draw(e);
			g2.fill(e);
		}
		

		// g2.setStroke(stroke);

		g2.drawString("Ellipse2D", 100, 250);

	}

	public static void main(String s[]) {
		JFrame f = new JFrame("");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		JApplet applet = new PointPlot();

		f.getContentPane().add("Center", applet);
		applet.init();
		f.pack();
		f.setSize(new Dimension(300, 300));
		f.show();
	}
}
