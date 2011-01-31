package graphics;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class CircleDraw extends Frame {
	Shape circle = new Ellipse2D.Float(100.0f, 100.0f, 100.0f, 100.0f);
	Shape square = new Rectangle2D.Double(100, 100, 100, 100);
	Shape p1 = new Rectangle2D.Double(80, 80, 5, 5);
	Shape p2 = new Rectangle2D.Double(80, 100, 5, 5);
	
	public void paint(Graphics g) {
		Graphics2D ga = (Graphics2D) g;
		ga.draw(circle);
		ga.setPaint(Color.green);
		ga.fill(circle);
		ga.setPaint(Color.red);
		ga.draw(square);
		
		ga.setColor(Color.blue);
		ga.draw(p1);
		ga.drawString("p1", 80, 80);
		ga.draw(p2);
		ga.drawString("p2", 80, 100);
	}

	public static void main(String args[]) {
		Frame frame = new CircleDraw();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		frame.setSize(300, 250);
		frame.setVisible(true);
	}
}
