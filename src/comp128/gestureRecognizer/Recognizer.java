package comp128.gestureRecognizer;


import edu.macalester.graphics.Point;

import java.time.temporal.Temporal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import javax.crypto.Mac;


/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {

    final int RESAMPLEINTERVAL = 64;
    ArrayList<Template> tempList;
    double bestScore;
    final double SIZE = 250;

    /**
     * Constructs a recognizer object
     */
    public Recognizer() {
        tempList = new ArrayList<>();
        bestScore = 0;
    }


    /**
     * Create a template to use for matching
     * 
     * @param name   of the template
     * @param points in the template gesture's path
     */
    public Template addTemplate(String name, Deque<Point> points) {

        Deque<Point> resampledPoints = resample(points, RESAMPLEINTERVAL);
        Deque<Point> rotatedPoints = rotateBy(resampledPoints, -indicativeAngle(resampledPoints));
        Deque<Point> scaledPoints = scaleTo(rotatedPoints, SIZE);
        Deque<Point> translatedPoints = translateTo(scaledPoints, new Point(0, 0));

        Template template = new Template(name, translatedPoints);

        tempList.add(template);
        return template;
    }


    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the
     * gesture and the template points.
     * 
     * @param points
     * @param templatePoints
     * @return best distance
     */
    private double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints) {
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5 * (-1.0 + Math.sqrt(5.0));// golden ratio
        double x1 = phi * thetaA + (1 - phi) * thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi) * thetaA + phi * thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while (Math.abs(thetaB - thetaA) > deltaTheta) {
            if (f1 < f2) {
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi * thetaA + (1 - phi) * thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            } else {
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1 - phi) * thetaA + phi * thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }

    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta) {
        Deque<Point> rotatedPoints = null;
        rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    /**
     * Finds the distance between the template points and gesture points
     * 
     * @param a rotated Points
     * @param b templatePoints
     * @return double: length of the original path
     */
    public double pathDistance(Deque<Point> a, Deque<Point> b) {
        Iterator<Point> aIterator = a.iterator();
        Iterator<Point> bIterator = b.iterator();
        double distance = 0;
        double avgDistance = 0;

        while (aIterator.hasNext()) {
            Point aCurrent = aIterator.next();
            Point bCurrent = bIterator.next();
            distance += aCurrent.distance(bCurrent);
        }

        avgDistance = distance / a.size();

        return avgDistance;
    }

    /**
     * finds length of path by incrementing the distance between each consecutive points
     * 
     * @param path input path
     * @return double: path length
     */
    public double pathLength(Deque<Point> path) {
        double length = 0;
        Point currentPoint; // initial point to compare
        Point nextPoint; // final point to compare (initial + 1)

        Iterator<Point> iterator = path.iterator();
        currentPoint = iterator.next();

        while (iterator.hasNext()) {
            nextPoint = iterator.next(); // Does that make them equla or one after the other?

            double deltaPoints = currentPoint.distance(nextPoint); // distance between points

            length += deltaPoints;
            currentPoint = nextPoint;
        }
        return length;
    }

    /**
     * resamples input points into evenly spaced points equal to the remaple interval
     * 
     * @param startingPoint
     * @param inPathLength
     * @param a
     */
    public Deque<Point> resample(Deque<Point> path, int n) {
        Deque<Point> resampledPoints = new ArrayDeque<>();
        double resampleInterval = pathLength(path) / (n - 1);

        double cumDistance = 0;

        resampledPoints.offer(path.peekFirst());

        Iterator<Point> iterator = path.iterator();
        Point p0 = iterator.next();
        Point p1 = iterator.next();

        while (iterator.hasNext()) {
            double segmentDistance = p0.distance(p1);
            if (segmentDistance + cumDistance >= resampleInterval) {
                double alpha = (resampleInterval - cumDistance) / segmentDistance;
                Point nextPoint = Point.interpolate(p0, p1, alpha);
                resampledPoints.offerLast(nextPoint);
                p0 = nextPoint;
                cumDistance = 0;
            } else {
                cumDistance += segmentDistance;
                p0 = p1;
                p1 = iterator.next();
            }
        }

        if (resampledPoints.size() < n) {
            resampledPoints.add(path.getLast());
        }

        return resampledPoints;
    }


    /**
     * returns the indicative angle of points path
     * 
     * @param resampled points path
     * @return indicative angle
     */
    public double indicativeAngle(Deque<Point> resampled) {

        double angle = findCentroid(resampled).subtract(resampled.peek()).angle();
        return angle;
    }

    /**
     * rotates all points in an point dequeue by specified degree amount and returns a dequeue of
     * rotated points
     * 
     * @param inputDeque points dequeue
     * @param degree     degree in radians
     * @return dequeue of rotated points
     */
    public Deque<Point> rotateBy(Deque<Point> inputDeque, double degree) {
        Deque<Point> rotatedPoints = new ArrayDeque<>();

        for (Point point : inputDeque) {
            point = point.rotate(degree, findCentroid(inputDeque));
            rotatedPoints.add(point);
        }

        return rotatedPoints;
    }


    /**
     * find centroid of a path of points
     * 
     * @param inputDeque path
     * @return centroid point
     */
    public static Point findCentroid(Deque<Point> inputDeque) {
        double xTotal = 0;
        double yTotal = 0;
        Point centroid;

        for (Point point : inputDeque) {
            xTotal += point.getX();
            yTotal += point.getY();
        }
        double centX = (xTotal / inputDeque.size());
        double centY = (yTotal / inputDeque.size());

        centroid = new Point(centX, centY);
        return centroid;
    }


    /**
     * returns a scaled version of the input points dequeue according to a set size
     * 
     * @param input input points dequeue
     * @param size  size to scale to
     * @return scaled points dequeue
     */
    public Deque<Point> scaleTo(Deque<Point> input, double size) {
        Deque<Point> scaledPoints = new ArrayDeque<>();
        double heightScale = size / findHeight(input);
        double widthScale = size / findWidth(input);

        for (Point point : input) {
            Point scaledPoint = new Point(point.getX() * widthScale, point.getY() * heightScale);
            scaledPoints.add(scaledPoint);
        }

        return scaledPoints;
    }


    /**
     * returns height of input path
     * 
     * @param input path
     * @return height
     */
    private double findHeight(Deque<Point> input) {
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;

        for (Point point : input) {
            maxY = Math.max(point.getY(), maxY);
            minY = Math.min(point.getY(), minY);
        }

        double height = maxY - minY;

        return height;
    }


    /**
     * returns width of input path
     * 
     * @param input path
     * @return width
     */
    private double findWidth(Deque<Point> input) {
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;

        for (Point point : input) {
            maxX = Math.max(point.getX(), maxX);
            minX = Math.min(point.getX(), minX);
        }

        double width = maxX - minX;

        return width;
    }

    /**
     * Translates the input dequeue around a point
     * 
     * @param input
     * @param centerPoint
     * @return
     */
    public Deque<Point> translateTo(Deque<Point> input, Point centerPoint) {
        Deque<Point> translatedPath = new ArrayDeque<>();
        Point centroid;
        Point k = new Point(0, 0);

        centroid = findCentroid(input);

        for (Point point : input) {
            point = point.add(k);
            point = point.subtract(centroid);
            translatedPath.add(point);
        }
        return translatedPath;
    }

    /**
     * finds the best matching template to the gesture and its score
     * 
     * @param input     gestrure dequeue
     * @param arrayList array of possible templates
     * @return closest template
     */
    public Template recognize(Deque<Point> input) {
        Deque<Point> resampledPath = resample(input, RESAMPLEINTERVAL);
        Deque<Point> rotatedPath = rotateBy(resampledPath, -indicativeAngle(resampledPath));
        Deque<Point> scaledPath = scaleTo(rotatedPath, SIZE);
        Deque<Point> translatedPath = translateTo(scaledPath, new Point(0, 0));

        double bestDistance = Double.MAX_VALUE;
        Template closestTemplateNew = null;

        for (Template temp : tempList) {
            double distanceAtBestAngle = distanceAtBestAngle(translatedPath, temp.getTemplate());
            if (distanceAtBestAngle < bestDistance){
                bestDistance = distanceAtBestAngle;
                closestTemplateNew = temp;
            }            
        }
        double score = 1 - (bestDistance / (0.5 * Math.sqrt(SIZE * SIZE + SIZE * SIZE)));
        closestTemplateNew.setScore(score);
        return closestTemplateNew;
    }

    public ArrayList<Template> getTempList() {
        return tempList;
    }

}