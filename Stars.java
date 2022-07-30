/*
 * Jedd Lupoy (1536884)
 * Jeffrey Luo (1535901)
 */
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

public class Stars{
    private static List<Star> stars = new ArrayList<>();
    private static List<Star> starsToEvaluate = new ArrayList<>();
    private static List<Star> starsAlreadyEvaluated = new ArrayList<>();
    private static List<Star> neighbourStars = new ArrayList<>();
    //Using stars as nodes
    private static class Star {
        private Star _previousStar = null;
        private double _x, _y, _distFromStart, _distFromEnd;

        public Star(double x, double y){
            _x = x;
            _y = y;
        }
        //Adding the distance from the start and the distance from the end
        public double getWeighedCost(){return _distFromStart + _distFromEnd;}
        public void setDistFromStart(double dist){_distFromStart = dist;}
        public double getDistFromStart(){return _distFromStart;}
        //Heuristic
        public void setDistFromEnd(double dist){_distFromEnd = dist;}
        public void setPreviousStar(Star s){_previousStar = s;}
        public Star getPreviousStar(){return _previousStar;}
        public double getX(){return _x;}
        public double getY(){return _y;}
    }

    //Returns the distance between two stars
    private static double calculateDist(Star s1, Star s2){
        return Math.sqrt(Math.pow(s1.getX() - s2.getX(), 2) + Math.pow( s1.getY() - s2.getY(), 2));
    }

    public static void main(String[] args){
        if (args.length != 4){ System.err.println("USAGE: java Stars [galaxy_csv_filename] [start_index] [end_index] [D]"); return; }
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            boolean foundPath = false;
            //Get all lines of coordinates from file
            while ((line = br.readLine()) != null){
                String[] coordinates = line.split(",");
                //Create star with x and y coordinate values
                Star s = new Star(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                stars.add(s);
                //System.out.println(String.valueOf(stars.get(stars.size()-1).getX()) + ", " + String.valueOf(stars.get(stars.size()-1).getY()));
            }
            int startIndex = Integer.parseInt(args[1]), endIndex = Integer.parseInt(args[2]);
            //Checking if entering start and end indices which corresponds to indices in stars and are not the same
            if (startIndex < 0 || endIndex >= stars.size()) { System.err.println("Please enter a valid start or end index"); br.close(); return;}
            if (startIndex < 0 || endIndex >= stars.size()) { System.err.println("Please enter a valid start or end index"); br.close(); return;}
            if (startIndex == endIndex) { System.err.println("Please enter a different start or end index"); br.close(); return;}
            //System.out.println(calculateDist(stars.get(startIndex), stars.get(endIndex)));
            double maxDist = Double.parseDouble(args[3]);
            if (maxDist < 0) { System.err.println("Please enter a distance greater than 0"); br.close(); return;}

            //Add the starting star (index) to as a star to evaluate
            starsToEvaluate.add(stars.get(startIndex));
            Star current = starsToEvaluate.get(0);
            while (starsToEvaluate.size() > 0){
                //Evaluating current star
                starsToEvaluate.remove(current);
                starsAlreadyEvaluated.add(current);
                
                //Check if the current star is the final star
                if (stars.get(endIndex) == current){
                    System.out.println("Final star reached. Previous star was (" + stars.get(endIndex).getPreviousStar().getX() + ", " + stars.get(endIndex).getPreviousStar().getY() + ")");
                    foundPath = true;
                    break;
                }
                
                //Search for possible stars, neigbouring stars, to travel to 
                System.out.println("Possible stars to travel to:");
                for (Star s: stars){
                    if (calculateDist(current, s) <= maxDist && !starsAlreadyEvaluated.contains(s)){
                        neighbourStars.add(s);
                        System.out.print("N: (" + String.valueOf(s.getX()) + ", " + String.valueOf(s.getY()) + "). ");
                    }
                }
                
                //Initialising potential neighbours
                for (Star s: neighbourStars){
                    if (!starsAlreadyEvaluated.contains(s)){
                        Double newCostToNeighbour = current.getDistFromStart() + calculateDist(current, s);
                        //New path to neighbour is shorter or neighbour has not been evaluated
                        if (newCostToNeighbour < s.getDistFromStart() || !starsToEvaluate.contains(s)){
                            //Calculating the weighted costs (implicitly)
                            s.setDistFromStart(newCostToNeighbour);
                            s.setDistFromEnd(calculateDist(s, stars.get(endIndex)));
                            //Set neighbouring star's previous star as the current star
                            s.setPreviousStar(current);
                            if (!starsToEvaluate.contains(s))
                                starsToEvaluate.add(s);
                        }
                    }
                }
                neighbourStars = new ArrayList<>();
                //Find the next star (next current) to finish evaluating
                if (starsToEvaluate.size() > 0){
                    current = starsToEvaluate.get(0);
                    //Go to the star with the lowest weighted distance cost
                    for (Star s: starsToEvaluate){
                        if (s.getWeighedCost() < current.getWeighedCost())
                            current = s;
                    }
                    System.out.println("\nStar to finish evaluating: (" + String.valueOf(current.getX()) + ", " + String.valueOf(current.getY()) + ")");
                }
            }

            //Retracing path, printing it out
            if (foundPath){
                List<Star> path = new ArrayList<>();
                //Current star should be the final star
                Star currentStarOnPath = stars.get(endIndex); 
                while (currentStarOnPath.getX() != stars.get(startIndex).getX() && currentStarOnPath.getY() != stars.get(startIndex).getY()){
                    path.add(currentStarOnPath);
                    currentStarOnPath = currentStarOnPath.getPreviousStar();
                }
                path.add(stars.get(startIndex));
                //Go from start to end star
                Collections.reverse(path);
                // Display the plot graph with shortest path.
                new Graph(path);
            }else{
                System.err.println("No valid path was found");
            }
            // Close buffered reader.
            br.close();
        }
        catch (IOException ex){
            System.err.println(ex);
        }
    }

    /*
     * Opens a GUI with the plot graph.
     */
    public static class Graph extends JFrame {
        private int _size = 800;

        public Graph(List<Star> path) {
            // Create an instance of the data visualiser.
            DataVisualiser dv = new DataVisualiser(_size, path);

            // Display the data on the GUI.
            add(dv);
            // Window properties.
            setSize(_size, _size);
            setTitle("COMPX301 Assigmment 3");
            setVisible(true);
            // Exit program when closing window.
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    /*
     * Visualises the plot data as well as the shortest path.
     */
    public static class DataVisualiser extends Canvas {
        // Store path points.
        private List<Star> _path;
        // Initialise values for GUI scaling and translation.
        private int _size = 0,
                    _margin = 0,
                    _multiplier = 0,
                    _height = 0,
                    _heightOffset = 0,
                    _textOffset = 7,
                    _pointSize = 8;
        private double _pointMultiplier = 0;

        /*
         * Constructor function for DataVisualiser.
         */
        public DataVisualiser(int size, List<Star> path) {
            // Set values according to the provided window size value.
            _size = size;
            _margin = _size / 10;
            _multiplier = size / 100;
            _pointMultiplier = _multiplier * 0.8;
            _heightOffset = _margin / 4;
            _height = _size - _margin - _heightOffset;
            _path = path;

            // DEBUG CODE
            System.out.println("Path:");
            for (int i = 0; i < path.size(); i++){
                System.out.println("Star " + String.valueOf(i+1) + ": (" + String.valueOf(path.get(i).getX()) + ", " + String.valueOf(path.get(i).getY()) + ")");
            }
        }

        /*
         * Draw the plot data.
         */
        public void paint(Graphics g) {
            // Initialise values for draw scaling and translation.
            int maxGridValue = 100,
                gridIncrement = 25,
                scale = 0,
                scaleMultiplier = 5,
                vertXOffset = 30,
                vertYOffset = 30,
                horizXOffset = 30,
                horixYOffset = 5;

            // Draw the plot graph boundary.
            g.drawRect(_margin, _margin - _heightOffset, _size - _margin * 2, _size - _margin * 2);

            // Draw numbers in the x and y axis.
            for (int i = 0; i <= maxGridValue; i += gridIncrement) {
                // Calculate an even drawing distance between numbers.
                scale++;
                int value = i - (scale * scaleMultiplier);
                // Draw the axis numbers.
                g.drawString(Integer.toString(i), _margin - vertXOffset, _height - (value * _multiplier) - vertYOffset);
                g.drawString(Integer.toString(i), _margin + (value * _multiplier) + horizXOffset, _size - _margin + horixYOffset);
            }

            // Draw all the points on the graph.
            for (int j = 0; j < stars.size(); j++) {
                Star s = stars.get(j);
                drawPoint(g, Color.DARK_GRAY, s.getX(), s.getY());
            }

            // Draw the shortest path lines.
            for (int k = 1; k < _path.size(); k++) {
                // Get previous point.
                Star s1 = _path.get(k - 1);
                // Get current point.
                Star s2 = _path.get(k);

                // Draw the path lines.
                g.setColor(Color.RED);
                drawPoint(g, Color.RED, s2.getX(), s2.getY());
                g.drawLine(_margin + (int) (s1.getX() * _pointMultiplier), _height - (int) (s1.getY() * _pointMultiplier),
                           _margin + (int) (s2.getX() * _pointMultiplier), _height - (int) (s2.getY() * _pointMultiplier));

                // Draw text to indicate the start and goal for the first and last points.
                if (k - 1 == 0) 
                    writePointText(g, Color.MAGENTA, "START", s1.getX(), s1.getY());
                if (k == _path.size() - 1)
                    writePointText(g, Color.BLUE, "GOAL", s2.getX(), s2.getY());
            }

            // Draw the start and goal points as a different colour.
            Star start = _path.get(0);
            Star goal = _path.get(_path.size() - 1);
            drawPoint(g, Color.MAGENTA, start.getX(), start.getY());
            drawPoint(g, Color.BLUE, goal.getX(), goal.getY());
            // Set background and foreground colour.
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
        }

        /*
         * Writes text at a certain plot point in the graph.
         */
        public void writePointText(Graphics g, Color c, String text, double x, double y) {
            g.setColor(c);
            g.drawString(text, _margin + (int) (x * _pointMultiplier) + _textOffset, _height - (int) (y * _pointMultiplier) - _textOffset);
        }

        /*
         * Draws a plot point in the graph.
         */
        public void drawPoint(Graphics g, Color c, double x, double y) {
            g.setColor(c);
            g.fillOval(_margin + (int) (x * _pointMultiplier) - (_pointSize / 2), _height - (int) (y * _pointMultiplier) - (_pointSize / 2), _pointSize, _pointSize);
        }
    }
}