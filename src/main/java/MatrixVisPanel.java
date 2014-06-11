import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

// TODO Ignore NaNs
// TODO Show statistics for scatterplots
// TODO Show statistics for tuples
// TODO Separate scatterplots and connect points to tuples.

public class MatrixVisPanel extends JPanel implements ComponentListener,
		MouseListener, MouseMotionListener, MouseWheelListener {

	protected ArrayList variableNames = new ArrayList();
	protected ArrayList variableData = new ArrayList();
	protected ArrayList variableRanges = new ArrayList();
	protected double correlation_matrix[][];
	protected int numValues = 0;
	protected boolean antialiasEnabled = true;

	private boolean doLayout = true;

	private int cell_dimension = 0;
	private int cell_padding = 6;
	private int cell_padding_half = cell_padding / 2;
	private int offset_x = 0;
	private int offset_y = 0;
	private int grid_width = 0;
	private int grid_height = 0;
	private int cell_x_origins[] = null;
	private int cell_y_origins[] = null;
	private double significant_correlation_threshold = 0.5;
	private ArrayList scatterplotPoints[][] = null;

	public MatrixVisPanel() {
		setDoubleBuffered(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	public void componentResized(ComponentEvent event) {
		doLayout = true;
	}

	public void componentMoved(ComponentEvent event) {
	}

	public void componentShown(ComponentEvent event) {
	}

	public void componentHidden(ComponentEvent event) {
	}

	public void setAntialiasEnabled(boolean enabled) {
		antialiasEnabled = enabled;
		repaint();
	}

	public void removeVariable(String name) {

	}

	public void removeAllVariables() {
		variableNames.clear();
		variableData.clear();
	}

	public void addVariable(String name, double[] values) {
		variableNames.add(name);
		variableData.add(values);
		double minmax[] = new double[2];
		minmax[0] = minmax[1] = values[0];
		for (int i = 0; i < values.length; i++) {
			if (values[i] < minmax[0])
				minmax[0] = values[i];
			if (values[i] > minmax[1])
				minmax[1] = values[i];
		}
		variableRanges.add(minmax);
	}

	public void addVariables(ArrayList names, ArrayList values) {
		for (int i = 0; i < names.size(); i++) {
			double[] varData = (double[]) values.get(i);
			numValues = varData.length;
			addVariable((String) names.get(i), varData);
		}

		correlation_matrix = StatisticsToolkit
				.calculatePearsonCorrelationCoefficientMatrix(variableData,
						names.size(), numValues);

		for (int i = 0; i < correlation_matrix.length; i++) {
			for (int j = 0; j < correlation_matrix[i].length; j++) {
				System.out.print(correlation_matrix[i][j] + " ");
			}
			System.out.print("\n");
		}

		doLayout = true;
		repaint();
	}

	public void layoutPanel() {
		if (correlation_matrix != null) {
			if (getWidth() < getHeight()) {
				cell_dimension = (getWidth() - (offset_x * 2) - (correlation_matrix.length * cell_padding))
						/ correlation_matrix.length;
			} else {
				cell_dimension = (getHeight() - (offset_y * 2) - (correlation_matrix.length * cell_padding))
						/ correlation_matrix.length;
			}

			grid_width = (cell_dimension + cell_padding)
					* correlation_matrix.length;
			grid_height = (cell_dimension + cell_padding)
					* correlation_matrix.length;

			cell_x_origins = new int[correlation_matrix.length + 1];
			cell_y_origins = new int[correlation_matrix.length + 1];

			for (int row = 0; row < cell_x_origins.length; row++) {
				cell_y_origins[row] = offset_y + cell_padding_half
						+ (row * (cell_dimension + cell_padding));
			}
			for (int col = 0; col < cell_x_origins.length; col++) {
				cell_x_origins[col] = offset_x + cell_padding_half
						+ (col * (cell_dimension + cell_padding));
			}
		}
	}

	private Color getColorForCorrelationCoefficient(double corrcoef) {
		Color c;
		if (Double.isNaN(corrcoef)) {
			return getBackground();
		}

		if (corrcoef > 0.f) {
			float norm = 1.f - (float) Math.abs(corrcoef);
			Color c0 = new Color(211, 37, 37); // high pos. corr.
			Color c1 = new Color(240, 240, 240); // low pos. corr.

			if (Math.abs(corrcoef) > significant_correlation_threshold) {
				c = c0;
			} else {
				int r = c0.getRed()
						+ (int) (norm * (c1.getRed() - c0.getRed()));
				int green = c0.getGreen()
						+ (int) (norm * (c1.getGreen() - c0.getGreen()));
				int b = c0.getBlue()
						+ (int) (norm * (c1.getBlue() - c0.getBlue()));
				c = new Color(r, green, b);
			}
		} else {
			float norm = 1.f - (float) Math.abs(corrcoef);
			Color c0 = new Color(44, 110, 211/* 177 */); // high neg. corr.
			Color c1 = new Color(240, 240, 240);// low neg. corr.

			if (Math.abs(corrcoef) > significant_correlation_threshold) {
				c = c0;
			} else {
				int r = c0.getRed()
						+ (int) (norm * (c1.getRed() - c0.getRed()));
				int green = c0.getGreen()
						+ (int) (norm * (c1.getGreen() - c0.getGreen()));
				int b = c0.getBlue()
						+ (int) (norm * (c1.getBlue() - c0.getBlue()));
				c = new Color(r, green, b);
			}
		}
		return c;
	}

	private void calculateScatterplotPoints() {
		scatterplotPoints = new ArrayList[variableData.size()][variableData
				.size()];

		for (int ix = 0; ix < variableData.size(); ix++) {
			double xData[] = (double[]) variableData.get(ix);
			double xMinMax[] = (double[]) variableRanges.get(ix);

			for (int iy = 0; iy < variableData.size(); iy++) {
				ArrayList points = new ArrayList();
				double yData[] = (double[]) variableData.get(iy);
				double yMinMax[] = (double[]) variableRanges.get(iy);

				for (int ipt = 0; ipt < xData.length; ipt++) {

					if (Double.isNaN(yData[ipt]) || Double.isNaN(xData[ipt])) {
						System.out.println("Skipped a NaN");
						continue;
					}

					Point point = new Point();
					point.y = (int) (cell_dimension * (yData[ipt] - yMinMax[0]) / (yMinMax[1] - yMinMax[0]));
					point.x = (int) (cell_dimension * (xData[ipt] - xMinMax[0]) / (xMinMax[1] - xMinMax[0]));

					point.y = (cell_y_origins[iy] + cell_dimension) - point.y;
					point.x += cell_x_origins[ix];
					points.add(point);
				}
				scatterplotPoints[ix][iy] = points;
			}
		}
	}

	public void paint(Graphics g) {
		if (doLayout) {
			layoutPanel();
			calculateScatterplotPoints();
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(new Font("Dialog", Font.BOLD, 8));
		FontMetrics fm = getFontMetrics(g2.getFont());

		if (antialiasEnabled) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}

		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		// g2.setColor(Color.DARK_GRAY);
		// g2.drawRect(offset_x, offset_y, grid_width, grid_height);

		if (correlation_matrix != null) {
			/*
			 * // draw column and row name headings
			 * g2.setColor(Color.DARK_GRAY);
			 * 
			 * int v = g2.getFontMetrics(getFont()).getHeight()/2;
			 * 
			 * for (int i = 0; i < variableNames.size(); i++) { String name =
			 * (String)variableNames.get(i); g2.drawString(name,
			 * cell_x_origins[i], offset_y); int x = 1; int y =
			 * cell_y_origins[i]+v;
			 * 
			 * //g2.drawString(name, 1, cell_y_origins[i]); int k =
			 * name.length(); for (int j = 0; j < k+1; j++) { if (y+(j*v) >
			 * y+cell_dimension) { break; }
			 * 
			 * if (j == k) { g2.drawString(name.substring(j), x, y+(j*v)); }
			 * else { g2.drawString(name.substring(j, j+1), x, y+(j*v)); } } }
			 */

			// draw scatterplots

			for (int ix = 0; ix < variableData.size(); ix++) {
				double xData[] = (double[]) variableData.get(ix);
				double xMinMax[] = (double[]) variableRanges.get(ix);
				for (int iy = 0; iy < variableData.size(); iy++) {
					double yData[] = (double[]) variableData.get(iy);
					double yMinMax[] = (double[]) variableRanges.get(iy);

					if (ix == iy) {
						String name = (String) variableNames.get(ix);
						g2.setColor(Color.DARK_GRAY);
						int x;
						if (fm.stringWidth(name) > cell_dimension) {
							x = cell_x_origins[ix];
						} else {
							x = cell_x_origins[ix] + (cell_dimension / 2)
									- (fm.stringWidth(name) / 2);
						}
						int y = cell_y_origins[iy] + 3 * (cell_dimension / 4);
						g2.drawString(name, x, y);
						continue;
					} else if (ix > iy) {
						continue;
					}

					g2.setColor(getColorForCorrelationCoefficient(correlation_matrix[ix][iy]));
					g2.fillRect(cell_x_origins[ix] - cell_padding_half,
							cell_y_origins[iy] - cell_padding_half,
							cell_dimension + cell_padding, cell_dimension
									+ cell_padding);
					g2.setColor(getBackground());
					g2.fillRect(cell_x_origins[ix], cell_y_origins[iy],
							cell_dimension, cell_dimension);
					g2.setColor(Color.gray);
					g2.drawRect(cell_x_origins[ix], cell_y_origins[iy],
							cell_dimension, cell_dimension);

					ArrayList points = scatterplotPoints[ix][iy];
					Iterator iter = points.iterator();
					while (iter.hasNext()) {
						Point pt = (Point) iter.next();
						g2.setColor(Color.black);
						g2.drawLine(pt.x, pt.y, pt.x, pt.y);
					}
					// for (int ipt = 0; ipt < xData.length; ipt++) {
					// int pt_y = (int)(cell_dimension*(yData[ipt] -
					// yMinMax[0])/(yMinMax[1]-yMinMax[0]));
					// int pt_x = (int)(cell_dimension*(xData[ipt] -
					// xMinMax[0])/(xMinMax[1] - xMinMax[0]));
					//
					// pt_y = (cell_y_origins[iy]+cell_dimension) - pt_y;
					// pt_x += cell_x_origins[ix];
					// g2.setColor(Color.black);
					// g2.drawLine(pt_x, pt_y, pt_x, pt_y);
					// }

					g2.setColor(Color.gray);
					g2.drawRect(cell_x_origins[ix], cell_y_origins[iy],
							cell_dimension, cell_dimension);
				}
			}

			/*
			 * // draw star coordinates plot int nTuples = -1; if
			 * (!variableData.isEmpty()) { nTuples =
			 * ((double[])variableData.get(0)).length; }
			 * 
			 * int origin_x = (grid_width / 2); int origin_y = (grid_height /
			 * 2);
			 * 
			 * g2.setColor(Color.gray); //g2.setStroke(new BasicStroke(1.f));
			 * 
			 * g2.drawRect(0, 0, grid_width, grid_height); g2.drawLine(origin_x,
			 * 0, origin_x, grid_height); g2.drawLine(0, origin_y, grid_width,
			 * origin_y); for (int i = 0; i < nTuples; i++) { double tuple[] =
			 * new double[variableData.size()]; for (int ii = 0; ii <
			 * tuple.length; ii++) { tuple[ii] =
			 * ((double[])variableData.get(ii))[i]; }
			 * 
			 * Point pt = tupleToStarCoordinates(tuple);
			 * //System.out.println("point is " + pt.toString());
			 * 
			 * pt.x = pt.x + origin_x; pt.y = grid_height - (pt.y + origin_y);
			 * //System.out.println("  point is " + pt.toString());
			 * 
			 * //pt.x = pt.x + origin_x; //pt.y = pt.y + origin_y; if (pt.x >
			 * grid_width) { System.out.println("WWWWW");
			 * g2.setColor(Color.blue); } else { g2.setColor(Color.black); }
			 * //g2.setStroke(new BasicStroke(14.f));
			 * 
			 * g2.drawLine(pt.x, pt.y, pt.x, pt.y); }
			 * System.out.println("grid_width = " + grid_width +
			 * " grid_height = " + grid_height);
			 */
			// double tuple[] = new double[4];
			// tuple[0] = tuple[1] = tuple[2] = tuple[3] = 2.;
			// Point pt = tupleToStarCoordinates(tuple);
			// System.out.println("star coordinate: " + pt.toString());
		}
	}

	private Point tupleToStarCoordinates(double tuple[]) {
		int width_half = grid_width / 2;
		int height_half = grid_height / 2;
		int max_length = width_half;

		// if (width_half < height_half) {
		// max_length = width_half;
		// } else {
		// max_length = height_half;
		// }
		double current_x = 0.;
		double current_y = 0.;

		// double minmax[] = new double[2];
		// minmax[0] = 0.;
		// minmax[1] = 4.;

		double angle_increment = (2. * Math.PI) / tuple.length;
		for (int i = 0; i < tuple.length; i++) {
			double minmax[] = (double[]) variableRanges.get(i);
			double norm_value = (tuple[i] - minmax[0])
					/ (minmax[1] - minmax[0]);
			double vector_length = norm_value * (double) max_length;

			double angle = (Math.PI / 2.) - (angle_increment * i);

			// find vector components
			double v_x = vector_length * Math.cos(angle);
			double v_y = vector_length * Math.sin(angle);

			// add vector to current point
			current_x += v_x;
			current_y += v_y;
		}

		Point pt = new Point((int) (current_x + 0.5), (int) (current_y + 0.5));
		return pt;
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
	}
}
