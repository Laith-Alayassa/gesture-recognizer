# gesture-recognizer

![gesture-recognizer](https://user-images.githubusercontent.com/60319236/173670785-cccf92e1-f0b6-45cf-8b89-0cf3f45dc089.png)

An app that learns user input gestures like (circle, arrow) and recognizes them when drawn again by the user
<div align="center">
</div>

![gesture-recognizer gif](https://user-images.githubusercontent.com/60319236/173671375-466b2d1a-35f2-4270-a093-7bb54b1cdcf0.gif)


### The app:
1. Allows user to draw a gesture
2. Stores gesture as a template
   - Templates are stored after Resmapling path to a set number of points
   - Rotating points to desired angle
   - Scaling points into a container of fixed size
   - transforming points into the center of the canvas
4. if user draws again
   - the app applies the same transformations to the drawing as to the templates
   - calculates the closest template by calculating the distance between new drawing points and template points
   - Displays the closest template with score of how cloes it is to user drawing.



## What I learned:
1. Reading and understanding research paper
2. Using listeners (for mouse and keyboard input)
3. Applying abstraction by refactoring difficult and long operations into small methods


## Info:
This app is based on a research paper titled [_Gestures without libraries, toolkits or training: a $1 recognizer for user interface prototypes_](https://dl.acm.org/doi/10.1145/1294211.1294238) and was assigned as homework for a data structures class where I completed this assignment.

Test files were written by the class intructor.

#### To use app, Run the `GestureApp.java` file || App might reuire the college's graphics library and might not run on your machine
