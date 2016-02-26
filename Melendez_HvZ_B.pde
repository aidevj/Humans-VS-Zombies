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

void setup() {
  size(800, 600);
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
    zombies.add(new Zombie(100 + random(500), 100 + random(500), 32, 1, .4, zomSpriteSheet));
  }  
  for (int i = 0; i < hCount; i++){
    humans.add(new Human(100 + random(500), 100 + random(500), 32, 1, .8, humSpriteSheet));
  }
}

void draw() {
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

void mouseClicked() {
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
        zombies.add(new Zombie(mouseX, mouseY, 32, 1, .4, zomSpriteSheet));
      else if (addHum == true)
        humans.add(new Human(mouseX, mouseY, 32, 1, 0.8, humSpriteSheet));
    }
}

// different kinds of humans with different speeds (child vs adult)