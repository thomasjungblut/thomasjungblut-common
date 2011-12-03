package de.jungblut.clustering.display;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.Vector;

public class ClusteringDisplay extends Frame {

    private static final long serialVersionUID = 5937206456529884404L;

    private static final Log LOG = LogFactory.getLog(ClusteringDisplay.class);
    protected static final int DS = 72; // default scale = 72 pixels per inch
    protected static final int SIZE = 9; // screen size in inches
    protected static int res; // screen resolution

    static final Color[] COLORS = { Color.red, Color.orange, Color.yellow,
	    Color.green, Color.blue, Color.magenta, Color.lightGray };

    protected final ArrayList<Vector> points = new ArrayList<Vector>();

    public ClusteringDisplay() {
	initialize();
	this.setTitle("K-Means Visualizer");
    }

    public void initialize() {
	// Get screen resolution
	res = Toolkit.getDefaultToolkit().getScreenResolution();

	// Set Frame size in inches
	this.setSize(SIZE * res, SIZE * res);
	this.setVisible(true);

	// Window listener to terminate program.
	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
    }

    // Override the paint() method
    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	try {
	    drawVectors(g2);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void drawVectors(Graphics2D g2) throws IOException {
	double sx = (double) res / DS;
	g2.setTransform(AffineTransform.getScaleInstance(sx, sx));

	// plot the axes
	g2.setColor(Color.BLACK);
	Vector dv = new Vector(SIZE / 2.0, SIZE / 2.0);
	plotRectangle(g2, new Vector(2, 2), dv);
	plotRectangle(g2, new Vector(2, -2), dv);

	// plot the sample data
	g2.setColor(Color.DARK_GRAY);
	dv.add(new Vector(0.03, 0.03));

	Path out = new Path("files/clustering/out");
	Configuration conf = new Configuration();
	FileSystem fs = FileSystem.get(conf);

	FileStatus[] stati = fs.listStatus(out);
	for (FileStatus status : stati) {
	    if (!status.isDir()) {
		Path path = status.getPath();
		LOG.debug("FOUND " + path.toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
			conf);
		ClusterCenter key = new ClusterCenter();
		Vector v = new Vector();
		while (reader.next(key, v)) {
		    g2.setColor(Color.DARK_GRAY);
		    plotRectangle(g2, v, dv);
		    g2.setColor(Color.RED);
		    plotEllipse(g2, key.getCenter(), dv);
		}
		reader.close();
	    }
	}
    }

    protected static void plotRectangle(Graphics2D g2, Vector v, Vector dv) {
	Vector v2 = v.times(new Vector(new Vector(1, -1)));
	v2 = v2.minus(dv.divide(new Vector(2, 2)));
	int h = SIZE / 2;
	double x = v2.getVector()[0] + h;
	double y = v2.getVector()[1] + h;
	g2.draw(new Rectangle2D.Double(x * DS, y * DS, dv.getVector()[0] * DS,
		dv.getVector()[1] * DS));
    }

    protected static void plotEllipse(Graphics2D g2, Vector v, Vector dv) {
	double[] flip = { 1, -1 };
	Vector v2 = v.times(new Vector(flip));
	v2 = v2.minus(dv.divide(new Vector(2, 2)));
	int h = SIZE / 2;
	double x = v2.getVector()[0] + h;
	double y = v2.getVector()[1] + h;
	g2.draw(new Ellipse2D.Double(x * DS, y * DS, dv.getVector()[0] * DS, dv
		.getVector()[1] * DS));
    }

    public static void main(String[] args) throws Exception {
	new ClusteringDisplay();
    }

}
