package de.jungblut.math.intersection;

public class Intersection {

	public static Point intersect(Line line1, Line line2) {
		// first check the slopes for parallel
		if (line1.slope == line2.slope) {
			// if they are, just return null
			return null;
		} else {
			// check the y for beeing the intersection
			if (line1.getY() == line2.getY()) {
				return new Point(0, line1.y);
			} else {
				// do the algebraic stuff
				/*
				 * y = m*x + b <br>
				 * 
				 * m1*x + b1 = m2*x + b2 <br>
				 * 
				 * (m1 - m2) * x = b2 - b1 <br>
				 * 
				 * x = (b2 - b1) / (m1 - m2)
				 */
				double x = (line2.getY() - line1.getY())
						/ (line1.getSlope() - line2.getSlope());
				return new Point(x, line1.getSlope() * x + line1.getY());
			}
		}
	}

	static final class Point {
		private final double x;
		private final double y;

		public Point(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		/**
		 * @return the y
		 */
		public double getY() {
			return y;
		}

		/**
		 * @return the x
		 */
		public double getX() {
			return x;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
	}

	static final class Line {

		private final double slope;
		private final double y;

		public Line(double slope, double y) {
			super();
			this.slope = slope;
			this.y = y;
		}

		/**
		 * @return the slope
		 */
		public double getSlope() {
			return slope;
		}

		/**
		 * @return the y
		 */
		public double getY() {
			return y;
		}
	}

	public static void main(String[] args) {
		Line l1 = new Line(-25, 12);
		Line l2 = new Line(-5, 512);

		System.out.println(intersect(l1, l2));

	}

}