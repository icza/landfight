# LandFight #
## A 10 KB Java game created for the Sun developer event: _Java technology turns 10_ ##

|Title:|LandFight|
|:-----|:--------|
|Competition category:|“10 éves a Java” 10 KB special category|
|Author:|András Belicza|
|Application category:|Game/Entertainment|
|Requirements:| Java 5.0 runtime environment, 2.0 GHz CPU (2.5 GHz or better recommended), 1024x768 screen resolution, traditional keyboard (not laptop) recommended|
|(Jar) size:|10,225 byte = 9.985 KB|
|Project status:|completed/stable as of 2005|

## Screenshot ##
![http://lh3.ggpht.com/_jDMClHrENz8/SRAY1nZAfiI/AAAAAAAAGhU/LPmdqRHfelo/s800/LandFight%20usage%2002.JPG](http://lh3.ggpht.com/_jDMClHrENz8/SRAY1nZAfiI/AAAAAAAAGhU/LPmdqRHfelo/s800/LandFight%20usage%2002.JPG)

[More screenshots...](../wiki/Screenshots.md)

## Description ##
Foreword: this might not be a short description, but LandFight has a lot of functionality and feature.

LandFight is a 2D-3D skill game. You can see the level/land of the game from above. LandFight is a 2-player game played on one computer. Each player has an aircraft to control. The players are opponents, the game is against each other. The players simple goal is to eliminate their opponent.

To achieve this, they can move their aircraft over the land in 3D, and they can fire using their primary (gun) and secondary (rocket launcher) weapon.

The players can turn to left/right, can accelerate forward/backward and they can ascend and descend with their aircraft. They have to be well aware of their height and the relation between their height and the land height below them. Crashing to the land bears the penalty of death. There is a colored height indicator for each player. Since the land is a rich, colorful relief map, knowing the map-color of our height gives us confidence and knowledge where and how to make safe manoeuvres over the land. Beyond the colored height indicator, in the players status area we can see the relations of heights in preciser numbers. Picturing 3D is done by zooming the objects over the land.

Players have shields which protects them against the shots. Once the shield is out, it no longer protects its owner, and the player dies – since being an aircraft – explodes. Shields can be destroyed by hitting the players with shots. Crashing the 2 players into each other results in the deaths of both player.

The targeting and the displaying of the game is in 2D. We just have to target the opponent in 2D, firing the gun (the primary weapon) comes with automated targeting in the 3rd dimension. If target doesn’t move, bullet will hit him if the 2D aiming was right. Of course we can predict the 2D moving, and aim in front of the player if he’s moving. Our secondary weapon is a fine weapon. The rockets are target followers. That means basically we can launch our rocket in any direction, the rocket tries to turn to its target, which is the opponent of the owner. However, the target of the rocket is always the opponent, but if we are in the  way of the rocket, and we have approximately the same height, it will hit us. Turning (of rockets) to the target includes all 3 dimensions: the horizontal plane just like the vertical height. However if any of the shots hits the land, it will be destroyed – bullets simply disappear, rockets explode. Simply disappearing and exploding into the land means leaving marks on the land – which will disappear never. This means we have to make manouvers to avoid the rockets, make them hit the land.

The land is randomly generated after every game for more fun. The landscape is a perfectly colored relief map. Orientation in the land is assisted by a fine minimap. It shows us the whole land and the location of the players in the land. The visible part of the land around a player is just a slice of the whole land, and is scrolled with the player in the way that the player shall be in the center of it as far as possible. All player has an own window, they all can see and scroll their own environment.  Players are not allowed to leave the land, on the boundaries they will simple be stopped, however, shots will be destroyed.

Needless to say, moving in 3D and handling 2 weapons requires lots of control keys – especially for 2 players. Therefore a traditional/standard keyboard is required (recommended) for a comfortable playing with the default keyboard settings. Control keys are described in the game in the help section. At any time during the game, just press F1 to get help.

I’m not saying it will be easy… I’m just saying it will be good.
