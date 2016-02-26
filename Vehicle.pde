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
  
  abstract void calcSteeringForces();
  abstract void display();
 
  void update() {
    calcSteeringForces();
    velocity.add(acceleration);
    velocity.limit(maxSpeed);
    position.add(velocity);
    
    forward = velocity.copy();
    forward = forward.normalize();
    right = new PVector(forward.y, -forward.x);
    
    acceleration = new PVector(0, 0);
  }

  void applyForce(PVector force) {
    acceleration.add(PVector.div(force, mass));
  }
  
  
  PVector avoidObstacle(Obstacle ob, float safeDistance){
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