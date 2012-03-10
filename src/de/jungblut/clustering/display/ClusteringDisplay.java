package de.jungblut.clustering.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
  // resolution

  private static final Color[] COLORS = { Color.RED, Color.BLUE, Color.GREEN,
      Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.BLACK, Color.CYAN,
      Color.PINK };

  private final HashMap<ClusterCenter, ArrayList<Vector>> centerMap = new HashMap<>();

  private double minX = Double.MAX_VALUE;
  private double minY = Double.MAX_VALUE;
  private double maxX = Double.MIN_VALUE;
  private double maxY = Double.MIN_VALUE;

  private double scaleFactorX = 1;
  private double scaleFactorY = 1;

  private final double offsetX = 50;
  private final double offsetY = 100;

  private Point clickPoint;
  private boolean zoomed = false;
  private int zoomFactor = 1;

  private boolean inDrag = false;
  private int startX = -1;
  private int startY = -1;
  private int curX = -1;
  private int curY = -1;

  private ClusteringDisplay() throws IOException {
    initialize();
    this.setTitle("K-Means Visualizer");
  }

  void initialize() throws IOException {
    // Get screen resolution
    Dimension res = Toolkit.getDefaultToolkit().getScreenSize();

    Path out = new Path("files/clustering/out");
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    this.setSize(res);
    this.setExtendedState(Frame.MAXIMIZED_BOTH);
    // this.setUndecorated(true);
    getMaxAndMinFromFile(fs, out, conf);
    caclulcateScaling();
    this.setVisible(true);
    this.addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseMoved(MouseEvent e) {
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        curX = p.x;
        curY = p.y;
        if (inDrag) {
          repaint();
        }
      }
    });
    this.addMouseListener(new MouseListener() {

      @Override
      public void mouseReleased(MouseEvent e) {
        inDrag = false;
        clickPoint = null;
      }

      @Override
      public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        startX = p.x;
        startY = p.y;
        inDrag = true;
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        int button = e.getButton();
        if (e.getClickCount() == 2) {
          clickPoint = e.getPoint();
          // left click zoom in
          if (button == 1) {
            zoomed = true;
            zoomFactor += 1;
          } else if (button == 3) { // right click zoom out
            zoomed = true;
            if (zoomFactor > 1) {
              zoomFactor -= 1;
            }
          }

        } else if (e.getClickCount() == 1) {
          if (button == 2) { // middle click reset
            zoomed = false;
            zoomFactor = 1;
          }
        }
        LOG.info("zoomed=" + zoomed + " factor=" + zoomFactor);
        repaint();
      }
    });

    // Window listener to terminate program.
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    caclulcateScaling();
    g2.setTransform(AffineTransform
        .getScaleInstance(scaleFactorX, scaleFactorY));
    g2.translate(offsetX, offsetY);
    drawVectors(g2);
  }

  void drawVectors(Graphics2D g2) {
    if (inDrag) {
      int w = curX - startX;
      int h = curY - startY;
      g2.translate(offsetX + w, offsetY + h);
    }
    if (zoomed && clickPoint != null) {
      g2.scale(zoomFactor, zoomFactor);
      if (!inDrag) {
        g2.translate(-clickPoint.x, -clickPoint.y);
      } else {
        int w = curX - startX;
        int h = curY - startY;
        g2.translate(offsetX + w - clickPoint.x, offsetY + h - clickPoint.y);
      }
    }

    // g2.drawLine(0, 0, Integer.MAX_VALUE, 0);
    // g2.drawLine(0, 0, 0, Integer.MAX_VALUE);

    int count = 0;
    for (Entry<ClusterCenter, ArrayList<Vector>> vmap : centerMap.entrySet()) {
      g2.setColor(COLORS[count]);
      plotEllipse(g2, vmap.getKey().getCenter(), 16);
      for (Vector v : vmap.getValue()) {
        plotEllipse(g2, v, 6);
      }
      count++;
      if (count >= COLORS.length) {
        count = 0;
      }
    }
  }

  private void getMaxAndMinFromFile(FileSystem fs, Path out, Configuration conf)
      throws IOException {
    FileStatus[] stati = fs.listStatus(out);
    for (FileStatus status : stati) {
      if (!status.isDir()) {
        Path path = status.getPath();
        LOG.debug("FOUND " + path.toString());
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        ClusterCenter key = new ClusterCenter();
        Vector v = new Vector();
        int count = 0;
        while (reader.next(key, v)) {
          ArrayList<Vector> arrayList = centerMap.get(key);
          if (arrayList == null) {
            arrayList = new ArrayList<>();
            centerMap.put(new ClusterCenter(key.getCenter()), arrayList);
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
        LOG.info("Found minX: " + minX + " maxX: " + maxX + " minY: " + minY
            + " maxY: " + maxY);
        reader.close();
      }
    }
  }

  void caclulcateScaling() {
    scaleFactorX = (this.getWidth() - offsetX) / (maxX - minX);
    scaleFactorY = (this.getHeight() - offsetY) / (maxY - minY);
    LOG.debug("Found scale factor of: " + scaleFactorX + " / " + scaleFactorY);
  }

  void plotEllipse(Graphics2D g2, Vector v2, int radius) {
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
