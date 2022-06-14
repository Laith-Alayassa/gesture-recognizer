

package comp128.gestureRecognizer;

import java.util.Deque;
import edu.macalester.graphics.Point;

/**
 * This is a template class that store the path of a certain shape (e.g. circle), and it contains
 * information about its score which indicates how close is it to another template when comparing
 */
public class Template {
    private String name;
    Deque<Point> template;
    double score;


    public Template(String name, Deque<Point> points) {
        this.name = name;
        this.template = points;
        this.score = 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplate(Deque<Point> template) {
        this.template = template;
    }

    public String getName() {
        return name;
    }

    public Deque<Point> getTemplate() {
        return template;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Template name: " + name + "\n" + "template points: " + template + "\n" + "Score: " + score;
    }
}
