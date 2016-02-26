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

  float CIRC_DISTANCE = 20.0;
  float CIRC_RADIUS = 10.0;
  float ANGLE_CHANGE = 5.0;
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

  PVector seek(PVector target){
    PVector desiredVel = PVector.sub(target, position);
    desiredVel.normalize();
    desiredVel.mult(maxSpeed);
    desiredVel.sub(velocity);
    return desiredVel;
  }
  
  // FLEEING FORCE
  PVector flee() {
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
  
  PVector evade(){
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

  void setAngle(PVector vec, float wanAng) {
    float len = vec.mag();
    vec.x = cos(radians(wanAng)*len);
    vec.y = sin(radians(wanAng)*len);
  }


  void calcSteeringForces() {
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

  void display() {

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