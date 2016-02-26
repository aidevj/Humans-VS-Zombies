import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Melendez_HvZ_B extends PApplet {

/* 
 * Isis Melendez
 * HvZ #1
 * IGME 202
 * 25 October 2015
 */

boolean debug = false;
boolean addZom = true;
boolean addHum = !addZom;

ArrayList<Obstacle> obs;
ArrayList<Human> humans;
ArrayList<Zombie> zombies;

// initial values
int hCount = 5;
int zCount = 1;
int obCount = 10;

PFont font;

PImage zomSpriteSheet;
PImage humSpriteSheet;

public void setup() {
  
  font = createFont("Calibri", 32);
  
  // Sprite sheets
  zomSpriteSheet = loadImage("ZombieSheetA.png");
  humSpriteSheet = loadImage("HumanSheetA.PNG");
  
  // Initialize and populate array lists
  obs = new ArrayList<Obstacle>(); 
  for (int i = 0; i < obCount; i++){
    obs.add(new Obstacle());
  }
  zombies = new ArrayList<Zombie>();
  humans = new ArrayList<Human>();
    for (int i = 0; i < zCount; i++){
    zombies.add(new Zombie(100 + random(500), 100 + random(500), 32, 1, .4f, zomSpriteSheet));
  }  
  for (int i = 0; i < hCount; i++){
    humans.add(new Human(100 + random(500), 100 + random(500), 32, 1, .8f, humSpriteSheet));
  }
}

public void draw() {
  background(255);
  
  // Draw field bounding box
  fill(116, 188, 136);
  rect(100, 100, 600, 400);
  
   // Display and update objects
  for (int i = 0; i < humans.size(); i++){
    humans.get(i).update();
    humans.get(i).display();
  }
  for (int i = 0; i < zombies.size(); i++){
    zombies.get(i).update();
    zombies.get(i).display();
    zombies.get(i).arrive();
  }
  for (int i = 0; i < obs.size(); i++){
    obs.get(i).display();
  }
  
  // DEBUG UI
  
  // Debug on/off button
  fill(255, 0, 0);
  rect(50, 525, 100, 50);
  fill(0);
  text("DEBUG SWTICH", 54, 555);
  
  // place zombies button
  if (addZom == true)
    fill (255, 255, 0);
  else
    fill(255);
  rect(175, 525, 100, 50);
  fill(0);
  image(zomSpriteSheet.get(32, 0, 32, 32), 180, 535);
  text("ADD", 230, 555);
  
  // place humans button
  if (addHum == true)
    fill (255, 255, 0);
  else
    fill(255);
  rect(300, 525, 100, 50);
  fill(0);
  image(humSpriteSheet.get(32, 0, 32, 32), 305, 535);
  text("ADD", 355, 555);
}

public void mouseClicked() {
  if (mouseX > 50 && mouseY > 525 && mouseX < 150 && mouseY < 575){
    debug = !debug;
    println("Debug on: " + debug);
  }
  else if (mouseX > 175 && mouseY > 525 && mouseX < 275 && mouseY < 575){
    addZom = !addZom;
    addHum = !addHum;
  }
  else if (mouseX > 300 && mouseY > 525 && mouseX < 400 && mouseY < 575){
    addZom = !addZom;
    addHum = !addHum;
  }
  else {
      if (addZom == true)
        zombies.add(new Zombie(mouseX, mouseY, 32, 1, .4f, zomSpriteSheet));
      else if (addHum == true)
        humans.add(new Human(mouseX, mouseY, 32, 1, 0.8f, humSpriteSheet));
    }
}

// different kinds of humans with different speeds (child vs adult)
class Human extends Vehicle {

  PVector steeringForce;

  PImage spritesheet;
  int currentFrame = 0;
  int numFrames = 3;
  int numAnims = 4;
  PImage[][] humanPics;
  int DIMENSION = 32; // dimension in pixels of individual sprites
  int W = 96;
  int H = 128;
  int animNum;

  boolean isFleeing = false;

  float CIRC_DISTANCE = 20.0f;
  float CIRC_RADIUS = 10.0f;
  float ANGLE_CHANGE = 5.0f;
  float wanderAngle;

  Human(float x, float y, float r, float ms, float mf, PImage ss) {

    //call the super class' constructor and pass in necessary arguments
    super(x, y, r, ms, mf);

    //instantiate steeringForce vector to (0, 0)
    steeringForce = new PVector(0, 0);

    // Get spritesheet pics
    spritesheet = ss;
    humanPics = new PImage[numFrames][numAnims];
    for (int i = 0; i < numFrames; i++) {
      for (int j = 0; j < numAnims; j++) {
        int x0 = (i * DIMENSION) % W;
        int y0 = (j * DIMENSION) % H;
        humanPics[i][j] = spritesheet.get(x0, y0, DIMENSION, DIMENSION);
      }
    }
  }

  //////////////////////////
  ////  FORCE METHODS  /////
  //////////////////////////

  public PVector seek(PVector target){
    PVector desiredVel = PVector.sub(target, position);
    desiredVel.normalize();
    desiredVel.mult(maxSpeed);
    desiredVel.sub(velocity);
    return desiredVel;
  }
  
  // FLEEING FORCE
  public PVector flee() {
    PVector fVelocity = new PVector(0, 0);
    PVector fleeingForce = new PVector(0, 0);
    PVector pursuer = new PVector(0, 0);
    float safeDistance = 500;

    // check for non-collision with all zombies
    for (int i = 0; i < zombies.size(); i++) {
      PVector vecToCen = PVector.sub(zombies.get(i).position.copy(), position);
      float d = vecToCen.mag();

      // if the absolute value of the magnitude of vecToCenter/distance is larger than the safe distance
      if (abs(d) < safeDistance) {
        safeDistance = d;
        if (safeDistance < 150) {
          isFleeing = true;
          println("Human fleeing");
          pursuer = (zombies.get(i).position.copy());   ///////////////// how do I flee from zombie's future position??????
          fVelocity = PVector.sub(pursuer, position);  ////////////////////
        } else {
          isFleeing = false;
          println("Human not fleeing");
        }
      }
    } 
    // calculate
    fVelocity.normalize();
    fVelocity.mult((-1) * maxSpeed); // steers velocity the opposite direction of the pursuer
    fleeingForce = PVector.sub(fVelocity, velocity);

    return fleeingForce;
  }
  
  public PVector evade(){
    PVector eVel = new PVector(0, 0);
    PVector eForce = new PVector(0, 0);
    PVector pursuer = new PVector(0, 0);
    PVector pursuerVel = new PVector(0, 0);
    float safeDistance = 500;
    
    // check for non-collision with all zombies
    for (int i = 0; i < zombies.size(); i++) {
      PVector vecToCen = PVector.sub(zombies.get(i).position, position);
      float d = vecToCen.mag();
      
      if (abs(d) < safeDistance) {
        safeDistance = d;
        if (safeDistance < 150) {
          pursuer = zombies.get(i).position.copy();
          pursuerVel = zombies.get(i).velocity.copy();
          pursuer.add(pursuerVel.mult(50));
          
          eVel = PVector.sub(pursuer, position);
          isFleeing = true;
        }
        else { isFleeing = false; }
      }
    }
    eVel.normalize();
    eVel.mult(-maxSpeed);
  
    eForce = PVector.sub(eVel, velocity);
    return eForce;
  }


  // Wander Method
  // Humans wanders while not fleeing

  public PVector wander()
  {
    // Calculate the circle center
    PVector circ = velocity.copy();
    circ.normalize();
    circ.setMag(CIRC_DISTANCE);

    // Calculate displacement force
    // Along Y-axis of zombie
    PVector displacement;
    displacement = new PVector(0, -1);
    displacement.setMag(CIRC_RADIUS);

    // Randomly change the vectors direction by making it its current angle
    //displacement.rotate(wanderAngle);
    //wanderAngle += (random(10) * ANGLE_CHANGE) - (ANGLE_CHANGE * .5) ; //////////////////
    setAngle(displacement, wanderAngle);
    wanderAngle += random(1) * 10 - 10*.5f;

    PVector wanderForce;
    wanderForce = circ.add(displacement);
    return wanderForce;
  }

  public void setAngle(PVector vec, float wanAng) {
    float len = vec.mag();
    vec.x = cos(radians(wanAng)*len);
    vec.y = sin(radians(wanAng)*len);
  }


  public void calcSteeringForces() {
    // Initialize over force vectors
    PVector flee = new PVector(0, 0);
    PVector avoid = new PVector(0, 0);
    PVector toCenter = new PVector(width/2, height/2);
    PVector wander = new PVector(0, 0);

    // Calculate individual forces
    flee = evade();
    toCenter.sub(position);
    for (int i = 0; i < obs.size(); i++) {
      avoid.add(avoidObstacle(obs.get(i), 40));
    }

    //add the above forces to this overall steering force   

    if (isFleeing == true) {
      steeringForce.add(flee);
    }
    if (isFleeing == false) {
      steeringForce.add(wander);
    }

    steeringForce.add(PVector.mult(avoid, 50)); //////////
    // edge detection
    if (position.x <100 || position.x>700) {
      steeringForce.add(PVector.mult(toCenter, 100));
    }
    if (position.y < 100 || position.y > 500) {
      steeringForce.add(PVector.mult(toCenter, 100));
    }

    //limit steering force to a maximum force
    steeringForce.limit(maxForce);

    //apply this steering force to the vehicle's acceleration
    super.applyForce(steeringForce);

    //reset the steering force to 0
    steeringForce.mult(0);
  }

  public void display() {

    //calculate the direction of the current velocity - this is done for you
    //float angle = velocity.heading(); 

    // get direction facing
    float angle = degrees(velocity.heading()); 


    if (angle >= PI/4 && angle < 3*PI/4) { // face down
      animNum = 0;
    } else if (angle < -PI/4 && angle > -3*PI/4) { // face up
      animNum = 1;
    } else if (angle > 3*PI/4 && angle <= 5*PI/4) { // face left
      animNum = 3;
    } else if (angle > 7*PI/4 && angle < PI/4) { // face right
      animNum = 2;
    }

    // Draw human
    currentFrame = (currentFrame + 1) % numFrames;
    pushMatrix();
    translate(position.x, position.y);
    image(humanPics[currentFrame][animNum], -DIMENSION/2, -DIMENSION/2);
    popMatrix();

    if (debug) {
      //forward
      stroke(255, 0, 0);
      line(position.x, position.y, position.x+forward.x * 15, position.y + forward.y * 15);
      //right
      stroke(0, 255, 0);
      line(position.x, position.y, position.x + right.x * 15, position.y + right.y * 15);
    }
    stroke(0);
  }
}
class Obstacle{
  float radius;
  PVector position;
  PImage tree = loadImage("tree.png");
  
  Obstacle(){
    // Creates a randomly placed obstacle with a random radius
    radius = 32;
    position = new PVector(random(30, width-30),random(30, height-30));
  }
  
  public void display(){
    if (debug){
      noFill();
      stroke(150);
      ellipse(position.x, position.y, radius*2, radius*2);
    }
    image(tree, position.x - radius, position.y - radius);
  }
  
}
abstract class Vehicle {

  PVector position;
  PVector velocity;
  PVector acceleration;

  PVector forward;
  PVector right;

  float mass;
  float radius;
  float speed;
  float maxSpeed;
  float maxForce;
  

  Vehicle(float x, float y, float r, float ms, float mf) {
    position = new PVector(x, y);
    velocity = new PVector(1, 0);
    acceleration = new PVector(0, 0);
    
    radius = r;
    mass = 1;
    maxSpeed = ms;
    maxForce = mf;
    
    forward = new PVector(0, 0);
    right = new PVector(0, 0);
  }
  
  public abstract void calcSteeringForces();
  public abstract void display();
 
  public void update() {
    calcSteeringForces();
    velocity.add(acceleration);
    velocity.limit(maxSpeed);
    position.add(velocity);
    
    forward = velocity.copy();
    forward = forward.normalize();
    right = new PVector(forward.y, -forward.x);
    
    acceleration = new PVector(0, 0);
  }

  public void applyForce(PVector force) {
    acceleration.add(PVector.div(force, mass));
  }
  
  
  public PVector avoidObstacle(Obstacle ob, float safeDistance){
    PVector steer = new PVector(0, 0);
      
    // VecToCenter vecroe
    //PVector vecToCenter = PVector.sub(ob.position, position);
    PVector vecToCenter = new PVector(ob.position.x-position.x, ob.position.y-position.y);
      
    // distance to the obstacle
    float distance = PVector.dist(position, ob.position);
      
    // Return a zero vector if the obstacle is too far to concern
    // Use safe distance to determine how large the "safe zone" is
    if (distance > safeDistance){
      return steer;
    }
      
    // Return a zero vector if the obstacle is behind us
    // If both the dot product of vecToCenter and forward are negative
    float dotProduct = vecToCenter.dot(forward);
    if (dotProduct < 0)
      return steer;
        
    // Use the dot product of the vector-to-obstacle center and the unit vector
    // to the right of the vehicle (right vector) to find the distance between the centers
    // of the vehicle and the obstacle
    // Compare this to the sum of the radii and return a zero vector if we can pass safely
    dotProduct = vecToCenter.dot(right);
    if (dotProduct > (radius + ob.radius))
      return steer;
      
    // If we get this far we are on a collision course and must steer away!
    // Use the sign of the dot product between the vector to center (vecToCenter) and the
    // vector to the right (right) to determine whether to steer left or right   
    // For each case calculate desired velocity using the right vector and maxSpeed
    PVector desiredVel = new PVector(0, 0);
    // if on the right of the vehicle, turn left
    if (dotProduct > 0)
      desiredVel = right.mult(-maxSpeed);
    // turn right
    else
      desiredVel = right.mult(maxSpeed);
      
    // Compute the force required to change current velocity to desired velocity
    steer = PVector.sub(desiredVel, velocity);
      
    // Consider multiplying this force by safeDistance/dist to increase the relative weight
    // of the steering force when obstacles are closer
    steer.mult(safeDistance / distance);
      return steer;
  }    
}
class Zombie extends Vehicle {

  PImage spritesheet;
  int currentFrame = 0;
  int numFrames = 3;
  int numAnims = 4;
  PImage[][] zomPics;
  int DIMENSION = 32; // dimension in pixels of individual sprites
  int W = 96;
  int H = 128;
  int animNum;
  
  PShape z;
  float rad;
  PVector steeringForce;
  boolean targetting;
  
  float CIRC_DISTANCE = 20.0f;
  float CIRC_RADIUS = 10.0f;
  float ANGLE_CHANGE = 5.0f;
  float wanderAngle;
      

  Zombie(float x, float y, float r, float ms, float mf, PImage ss) {
      
    //call the super class' constructor and pass in necessary arguments
    super(x, y, r, ms, mf);

    //instantiate steeringForce vector to (0, 0)
    steeringForce = new PVector(0, 0);
    rad = r;
    
    targetting = false; // initially must find a target
    
    spritesheet = ss;
    zomPics = new PImage[numFrames][numAnims];
    for(int i = 0; i < numFrames; i++){
      for (int j = 0; j < numAnims; j++){
        int x0 = (i * DIMENSION) % W;
        int y0 = (j * DIMENSION) % H;
        zomPics[i][j] = spritesheet.get(x0, y0, DIMENSION, DIMENSION);
      }
    }
  }

  // SEEK METHOD // could call seek(futurePosition) in pursue??
  /*
  PVector seek(PVector target){
    PVector desiredVel = PVector.sub(target, position);
    desiredVel.normalize();
    desiredVel.mult(maxSpeed);
    desiredVel.sub(velocity);
    return desiredVel;
  }*/
  
  public PVector pursue(){
    PVector pVel = new PVector(0, 0);
    PVector pForce = new PVector(0, 0);
    PVector pursuingTarget = new PVector(width/2, height/2); // defaults to center (if can't see a human)
    float distance = 200;
    
    for (int i = 0; i < humans.size(); i++){
      PVector vecToCen = PVector.sub(humans.get(i).position.copy(), position);
      float dist = vecToCen.mag();
      if (abs(dist) < distance){
        targetting = true;
        println("Targetting human");
        pursuingTarget = PVector.add(humans.get(i).position.copy(), humans.get(i).velocity.copy().mult(50)); //pursuing target is now futurepos
        distance = dist;
      }
      else{
        targetting = false;
        println("not targetting");
      }
    }
    pVel = PVector.sub(pursuingTarget, position);
    pVel.normalize();
    pVel.mult(maxSpeed);
    pForce = PVector.sub(pVel, velocity);
    
    if (debug) {
      // Draw to target's future position
      if(targetting == false)
        stroke(255, 255, 0 );
      if (targetting == true)
        stroke(0, 0, 255);
      line(position.x, position.y, pursuingTarget.x, pursuingTarget.y);
    }
    
    return pForce;
  }
  
  // Wander Method
  // Zombie wanders while not targeting a human
  public PVector wander()
  {
    // Calculate the circle center
    PVector circ = velocity.copy();
    circ.normalize();
    circ.setMag(CIRC_DISTANCE);
     
    // Calculate displacement force
    // Along Y-axis of zombie
    PVector displacement;
    displacement = new PVector(0, -1);
    displacement.setMag(CIRC_RADIUS);
      
    // Randomly change the vectors direction by making it its current angle
    //displacement.rotate(wanderAngle);
    //wanderAngle += (random(10) * ANGLE_CHANGE) - (ANGLE_CHANGE * .5) ; //////////////////
    setAngle(displacement, wanderAngle);
    wanderAngle += random(1) * 10 - 10*.5f;
      
    PVector wanderForce;
    wanderForce = circ.add(displacement);
    return wanderForce;
  }
  
  public void setAngle(PVector vec, float wanAng){
     float len = vec.mag();
     vec.x = cos(radians(wanAng)*len);
     vec.y = sin(radians(wanAng)*len);
  }
    
  
  /// Arrive Method
  // human is killed (removed from array) upon reach
  public void arrive(){
    for (int i = 0; i < humans.size(); i++){
      PVector vecToCen2 = PVector.sub(humans.get(i).position.copy(), position);
      float dist = vecToCen2.mag();
      // get location of human in order for it to change to a zombie in the same place
      PVector newPosition = humans.get(i).position.copy();
      if (abs(dist) <= 15){
        humans.remove(i);
        zombies.add(new Zombie(newPosition.x, newPosition.y, 32, 1, .4f, zomSpriteSheet));
      }
    }
  }
  
  public void calcSteeringForces() {
    PVector avoid = new PVector(0, 0);
    PVector toCenter = new PVector(height/2, width/2);
    PVector seekingForce = new PVector(0, 0);
    PVector wander = new PVector(0, 0);
    
    // Calculate individual forces
    seekingForce = pursue();
    wander = wander();
    toCenter.sub(position);
    for (int i = 0; i < obs.size(); i++){
      avoid.add(avoidObstacle(obs.get(i), 40));
    }

    //add the above seeking force to this overall steering force
    if (targetting == true){
      steeringForce.add(seekingForce);
    }
    if (targetting == false){
      steeringForce.add(wander);
    }
    steeringForce.add(PVector.mult(avoid, 100)); //////////////////////// 100
    // edge detection
    if (position.x <100 || position.x>700) {
      steeringForce.add(PVector.mult(toCenter, 100));
    }
    if (position.y < 100 || position.y > 500) {
      steeringForce.add(PVector.mult(toCenter, 100));
    }

    //limit this seeker's steering force to a maximum force
    steeringForce.limit(maxForce);

    //apply this steering force to the vehicle's acceleration
    super.applyForce(steeringForce);
    //acceleration.add(PVector.div(steeringForce, mass));

    //reset the steering force to 0
    steeringForce.mult(0);
  }
  
  public void display() {
    // get direction facing
    float angle = degrees(velocity.heading()); 

    if (angle >= PI/4 && angle < 3*PI/4){ // face down
      animNum = 0;
      println("down");
    }
    else if (angle < -PI/4 && angle > -3*PI/4){ // face up
      animNum = 1;
      println("up");
    }
    else if (angle > 3*PI/4 && angle <= 5*PI/4){ // face left ////ISNT WORKING?
      animNum = 2;
      println("left");
    }
    else if (angle > 7*PI/4 && angle < PI/4){ // face right////ISNT WORKING?
      animNum = 3;
      println("right");
    }  
    
    currentFrame = (currentFrame + 1) % numFrames;
    pushMatrix();
      translate(position.x, position.y);
      image(zomPics[currentFrame][animNum], -DIMENSION/2, -DIMENSION/2);
    popMatrix();
    
    if (debug == true) {
      stroke(255, 0, 0);
      line(position.x, position.y, position.x+forward.x * rad, position.y + forward.y * rad);

      stroke(0, 255, 0);
      line(position.x, position.y, position.x + right.x * rad, position.y + right.y * rad);
    }
    stroke(0);
  }

  
}
  public void settings() {  size(800, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Melendez_HvZ_B" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
