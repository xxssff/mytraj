package graphics;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import simplification.Simplification;

import com.vividsolutions.jts.geom.Coordinate;

public class DrawLineSimp extends Applet implements MouseListener, ActionListener {

	Button switchButton;
	Button simpButton;
	TextField nameField;
	ArrayList<Integer> xcoords = new ArrayList<Integer>();
	ArrayList<Integer> ycoords = new ArrayList<Integer>();
	ArrayList<int[]> qcoords = new ArrayList<int[]>();
	boolean isP = true;
	boolean doSimp = false;

	int radius = 5;
	int qpointct = 0;
	int ppointct = 0;

	int mx, my; // the mouse coordinates
	Simplification s;

	public void init() {
		resize(800, 800);
		setLayout(new FlowLayout());
		switchButton = new Button("Draw Q");
		simpButton = new Button("Simplify Line");
		nameField = new TextField("35", 35);
		// setBackground(Color.black);

		add(switchButton);
		add(simpButton);
		add(nameField);
		// Attach actions to the components
		switchButton.addActionListener(this);
		simpButton.addActionListener(this);
		addMouseListener(this);
		// addMouseMotionListener(this);

		s = new Simplification();

	}

	public void mouseEntered(MouseEvent e) {
		// called when the pointer enters the applet's rectangular area
	}

	public void mouseExited(MouseEvent e) {
		// called when the pointer leaves the applet's rectangular area
	}

	public void mouseClicked(MouseEvent e) {
		// called after a press and release of a mouse button
		// with no motion in between
		// (If the user presses, drags, and then releases, there will be
		// no click event generated.)
	}

	public void mousePressed(MouseEvent e) {
		if (isP) {
			// called after a button is pressed down
			xcoords.add(e.getX());
			ycoords.add(e.getY());
			ppointct++;
		} else {
			qcoords.add(new int[] { e.getX(), e.getY() });
			qpointct++;
		}
		repaint();
	}

	public void mouseReleased(MouseEvent e) { // called after a button is
												// released
												// isButtonPressed = false;
		// setBackground(Color.black);
		// repaint();
		// e.consume();
	}

	// public void mouseMoved(MouseEvent e) { // called during motion when no
	// // buttons are down
	// mx = e.getX();
	// my = e.getY();
	// showStatus("Mouse at (" + mx + "," + my + ")");
	// repaint();
	// e.consume();
	// }

	public void mouseDragged(MouseEvent e) { // called during motion with
												// buttons down
												// mx = e.getX();
		// my = e.getY();
		// showStatus("Mouse at (" + mx + "," + my + ")");
		// repaint();
		// e.consume();
	}

	public void paint(Graphics g) {
		// g.drawPolygon(xcoords, ycoords, pointct); // Draws Triangle
		double plength = 0;
		double qlength = 0;

		g.setColor(Color.green);
		for (int i = 0; i < xcoords.size(); i++) {
			int x = xcoords.get(i);
			int y = ycoords.get(i);
			g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
			g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
			g.drawString("(" + x + "," + y + ")", x, y);
		}
		// g.setColor(Color.black);
		for (int i = 0; i < xcoords.size() - 1; i++) {
			g.drawLine(xcoords.get(i), ycoords.get(i), xcoords.get(i + 1),
					ycoords.get(i + 1));
			plength += Math.sqrt(Math.pow(
					(xcoords.get(i) - xcoords.get(i + 1)), 2)
					+ Math.pow((ycoords.get(i) - ycoords.get(i + 1)), 2));
		}

		// draw q
		int[] temp1, temp2;
		g.setColor(Color.red);
		for (int i = 0; i < qcoords.size(); i++) {
			temp1 = qcoords.get(i);
			g.drawOval(temp1[0] - radius, temp1[1] - radius, radius * 2,
					radius * 2);
			g.fillOval(temp1[0] - radius, temp1[1] - radius, radius * 2,
					radius * 2);
		}

		for (int i = 0; i < qcoords.size() - 1; i++) {
			temp1 = qcoords.get(i);
			temp2 = qcoords.get(i + 1);
			g.drawLine(temp1[0], temp1[1], temp2[0], temp2[1]);

			qlength += Math.sqrt(Math.pow((temp1[0] - temp2[0]), 2)
					+ Math.pow((temp1[1] - temp2[1]), 2));
		}

		g.drawString("P Points =" + xcoords.size(), 0, 400);
		g.drawString("P Length =" + plength, 0, 425);

		g.drawString("Q Points =" + qcoords.size(), 0, 450);
		g.drawString("Q Length =" + qlength, 0, 475);

		if (doSimp) {
			// do line simp
			double tolenrance = Double.parseDouble(nameField.getText());
			Coordinate[] pcurve = new Coordinate[xcoords.size()];
			Coordinate[] qcurve = new Coordinate[qcoords.size()];
			for (int i = 0; i < xcoords.size(); i++) {
				pcurve[i] = new Coordinate(xcoords.get(i), ycoords.get(i));
			}

			for (int i = 0; i < qcoords.size(); i++) {
				qcurve[i] = new Coordinate(qcoords.get(i)[0], qcoords.get(i)[1]);
			}

			s.setRouteCoords(pcurve);
			Integer[] indices = s.simplifyFromArr(0, tolenrance);

			for (int i : indices) {
				System.out.println(i);
			}
			// draw simplified trajectory
			g.setColor(Color.blue);
			g.drawLine((int) pcurve[0].x, (int) pcurve[0].y,
					(int) pcurve[indices[0]].x, (int) pcurve[indices[0]].y);

			for (int i = 0; i < indices.length; i++) {
				System.out.println(pcurve[indices[i]].toString());
			}

			for (int i = 0; i < indices.length - 1; i++) {
				g.drawLine((int) pcurve[indices[i]].x,
						(int) pcurve[indices[i]].y,
						(int) pcurve[indices[i + 1]].x,
						(int) pcurve[indices[i + 1]].y);
			}
			g.drawLine((int) pcurve[indices[indices.length-1]].x,
					(int) pcurve[indices[indices.length-1]].y,
					(int) pcurve[pcurve.length - 1].x,
					(int) pcurve[pcurve.length - 1].y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == switchButton) {
			// So it was the okButton, then let's perform his actions
			// Let the applet perform Paint again.
			// That will cause the aplet to get the text out of the textField
			// again and show it.
			isP = false;
		} else if (evt.getSource() == simpButton) {
			doSimp = true;
		}
		repaint();
	}
}