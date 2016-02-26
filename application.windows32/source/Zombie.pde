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
  
  float CIRC_DISTANCE = 20.0;
  float CIRC_RADIUS = 10.0;
  float ANGLE_CHANGE = 5.0;
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
  
  PVector pursue(){
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
  PVector wander()
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
    wanderAngle += random(1) * 10 - 10*.5;
      
    PVector wanderForce;
    wanderForce = circ.add(displacement);
    return wanderForce;
  }
  
  void setAngle(PVector vec, float wanAng){
     float len = vec.mag();
     vec.x = cos(radians(wanAng)*len);
     vec.y = sin(radians(wanAng)*len);
  }
    
  
  /// Arrive Method
  // human is killed (removed from array) upon reach
  void arrive(){
    for (int i = 0; i < humans.size(); i++){
      PVector vecToCen2 = PVector.sub(humans.get(i).position.copy(), position);
      float dist = vecToCen2.mag();
      // get location of human in order for it to change to a zombie in the same place
      PVector newPosition = humans.get(i).position.copy();
      if (abs(dist) <= 15){
        humans.remove(i);
        zombies.add(new Zombie(newPosition.x, newPosition.y, 32, 1, .4, zomSpriteSheet));
      }
    }
  }
  
  void calcSteeringForces() {
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
  
  void display() {
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