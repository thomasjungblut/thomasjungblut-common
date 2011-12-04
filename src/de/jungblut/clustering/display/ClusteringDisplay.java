package de.jungblut.clustering.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
    protected static Dimension res; // screen resolution

    static final Color[] COLORS = { Color.RED, Color.BLUE, Color.ORANGE,
	    Color.YELLOW, Color.GREEN, Color.MAGENTA };

    private final HashMap<ClusterCenter, ArrayList<Vector>> centerMap = new HashMap<ClusterCenter, ArrayList<Vector>>();

    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;

    double scaleFactorX = 1;
    double scaleFactorY = 1;

    double offsetX = 0;
    double offsetY = 0;

    public ClusteringDisplay() throws IOException {
	initialize();
	this.setTitle("K-Means Visualizer");
    }

    public void initialize() throws IOException {
	// Get screen resolution
	res = Toolkit.getDefaultToolkit().getScreenSize();

	Path out = new Path("files/clustering/out");
	Configuration conf = new Configuration();
	FileSystem fs = FileSystem.get(conf);
	this.setSize(res);
	this.setExtendedState(Frame.MAXIMIZED_BOTH);
	// this.setUndecorated(true);
	getMaxAndMinFromFile(fs, out, conf);
	caclulcateScaling();
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
	caclulcateScaling();
	try {
	    drawVectors(g2);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void drawVectors(Graphics2D g2) throws IOException {
	g2.setTransform(AffineTransform.getScaleInstance(scaleFactorX,
		scaleFactorY));

	int count = 0;
	for (Entry<ClusterCenter, ArrayList<Vector>> vmap : centerMap
		.entrySet()) {
	    g2.setColor(Color.BLACK);
	    plotEllipse(g2, vmap.getKey().getCenter(), 10);
	    g2.setColor(COLORS[count]);
	    for (Vector v : vmap.getValue()) {
		plotEllipse(g2, v, 3);
	    }
	    count++;
	    if (count > COLORS.length) {
		count = 0;
	    }
	}
    }

    private final void getMaxAndMinFromFile(FileSystem fs, Path out,
	    Configuration conf) throws IOException {
	FileStatus[] stati = fs.listStatus(out);
	for (FileStatus status : stati) {
	    if (!status.isDir()) {
		Path path = status.getPath();
		LOG.debug("FOUND " + path.toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
			conf);
		ClusterCenter key = new ClusterCenter();
		Vector v = new Vector();
		int count = 0;
		while (reader.next(key, v)) {
		    ArrayList<Vector> arrayList = centerMap.get(key);
		    if (arrayList == null) {
			arrayList = new ArrayList<Vector>();
			centerMap.put(new ClusterCenter(key.getCenter()),
				arrayList);
		    }
		    double x = v.getVector()[0];
		    double y = v.getVector()[1];
		    arrayList.add(new Vector(x, y));
		    if (x < minX) {
			minX = x;
		    } else if (x > maxX) {
			maxX = x;
		    }
		    if (y < minY) {
			minY = y;
		    } else if (y > maxY) {
			maxY = y;
		    }

		    if (count++ % 1000 == 0) {
			LOG.info("Reading " + count);
		    }
		}
		LOG.info("finished! " + count);
		LOG.info("Found minX: " + minX + " maxX: " + maxX + " minY: "
			+ minY + " maxY: " + maxY);
		reader.close();
	    }
	}
    }

    protected void caclulcateScaling() {
	scaleFactorX = (this.getWidth() - 2 * offsetX) / (maxX - minX);
	scaleFactorY = (this.getHeight() - 2 * offsetY) / (maxY - minY);
	LOG.info("Found scale factor of: " + scaleFactorX + " / "
		+ scaleFactorY);
    }

    protected void plotEllipse(Graphics2D g2, Vector v2, int radius) {
	double x = v2.getVector()[0];
	double y = v2.getVector()[1];
	LOG.debug("Plotting point " + x + "/" + y + " scaled from "
		+ v2.getVector()[0] + " / " + v2.getVector()[1]);
	g2.fill(new Ellipse2D.Double(x, y, radius, radius));
    }

    public static void main(String[] args) throws Exception {
	new ClusteringDisplay();
    }

}
