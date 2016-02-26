class Obstacle{
  float radius;
  PVector position;
  PImage tree = loadImage("tree.png");
  
  Obstacle(){
    // Creates a randomly placed obstacle with a random radius
    radius = 32;
    position = new PVector(random(30, width-30),random(30, height-30));
  }
  
  void display(){
    if (debug){
      noFill();
      stroke(150);
      ellipse(position.x, position.y, radius*2, radius*2);
    }
    image(tree, position.x - radius, position.y - radius);
  }
  
}