# Humans-VS-Zombies
A 2D Humans vs. Zombies simulator made in Processing.

Design & Implementation
•	This version of HvZ features human and zombie sprites with bounding circles.
•	It utilizes a more front-facing camera, as opposed to top-down.
•	Obstacles are trees and the enclosed area is shown by a black outlined box which humans and zombies steer away from.
•	The UI at the bottom of the window allows for debugging. 
•	When the Zombie Add button is selected, the user can click somewhere in the window to add a zombie (likewise for humans and the human add button)
•	Debug lines are activated when the red button is clicked:
•	The red line is the forward of the character, the green is the right.
•	Yellow lines connecting the zombies and humans’ future position  are drawn when targeting (they turn blue when in pursuit)
•	A grey circle is shown around trees to represent its bounding circle.
•	The blue lines show the pursuit and evasion future positions
•	Both humans and zombies have animated sprites taken from a sprite sheet in their respective classes
•	In their draw methods, the angle of human and zombie’s velocity is used to determine the direction of motion, thus used to draw out the correct facing sprite
•	Humans flee from zombies when their safe zones are intersected, while zombies in the Zombie class seek and pursue humans.
•	Humans and Zombies wander when not targeting or being targeted.
•	Humans max force is greater than the max speed of zombies.
•	When zombies intersect with humans, the human is removed from its array and a new zombie replaces it.
Bugs
•	The direction detection used in relation to the sprite sheets do not draw the correct sprites.
•	Zombies wander around the center, and when there are no humans, some seek the center
•	Humans flee but do not evade
•	Humans velocity rapidly changes direction when fleeing (viewable in debug mode)
Unimplemented Ideas (due to time constraints)
•	Different types of zombies and humans, such as children versus adults which would have different max forces to simulate reality. They would be randomly assigned in the array of humans/zombies every time the program is started.
•	Self-drawn assets.

Asset Sources:
http://www.die2nite.net/viewtopic.php?f=51&t=962

http://s872.photobucket.com/user/Mollombo/library/?sort=3&page=1 

