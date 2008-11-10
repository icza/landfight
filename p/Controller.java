package p;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This is the main class of LandFight.<br>
 * <br>
 * This program has been created for 'Java technology turns 10!'<br>
 * <br>
 * This is the control layer in the MVC architecture. Creates and makes the main frame visible,
 * and controls the games, connects the model and the view.
 * 
 * @author Andras Belicza
 */
public class Controller extends Thread {

	/**
	 * This is the entry point of the program.<br>
	 * Creates the main frame of the game and makes it visible.
	 * @param args used to take arguments from the running environment - not used yet
	 */
	public static void main( final String[] args ) {

		// We create and initialize a frame for the application
		final JFrame mainFrame = new JFrame( "LandFight" );
		mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		mainFrame.setResizable( false );
		
		
		// We create the controller of the game
		final Controller controller = new Controller();
		// We create the model of the game
		final Model      model      = new Model();
		// We create the view of the game
		final View       view       = new View( model, controller );

		mainFrame.getContentPane().add( view );
		mainFrame.getContentPane().add( controller.statusLabel, BorderLayout.SOUTH );
		mainFrame.pack();
		mainFrame.setVisible( true );
		
		controller.start();  // We start the iteration timer
		
		// My apologies for not dividing these to methods, but methods consume extra byes, I would have run out of the 10KB limit. 
		
		// All set, we can play now
		while ( true ) {
			// We play the game. We start the game, we and control it.
			controller.statusLabel.setText( "Generating land, please wait..." );
			model.newGame();
			view .newGame();
			
			view .repaint();
			
			controller.statusLabel.setText( "Go! Press F1 for help" );
			
			controller.paused = false;
			while ( true ) {
				// This is where key inputs go, focus must be owned
				view.requestFocusInWindow();

				if ( !controller.paused ) while ( true ) {
					      // We use this cycle to nothing!! If we calculated the next iteration or if we get to know that next iteration shell not be computed, we simply break from this cycle
					      // We would have no problem, if this would be a method, we would just simply return
					      // Or we could replace this cycle with exception handling but we would consume more space.

					
					// Now we calclulates the next game iteration.
					// My apologies again for using these lame constants instead of enumeration, but Enum as a new (internal) class
					// would have consumed at least 300 more bytes from the very little 10 KB.
					// As you can see, I could not afford that.
					final int KEY_LEFT   = 0;           // Constant value for the control key turning left
					final int KEY_RIGHT  = 1;           // Constant value for the control key turning right
					final int KEY_ACC    = 2;           // Constant value for the control key accelerating
					final int KEY_BREAK  = 3;           // Constant value for the control key break
					final int KEY_ASCEN  = 4;           // Constant value for the control key ascending
					final int KEY_DESCEN = 5;           // Constant value for the control key descending
					final int KEY_FIRE1  = 6;           // Constant value for the control key fire1
					final int KEY_FIRE2  = 7;           // Constant value for the control key fire2

					final float ROCKET_VELOCITY = 4.0f; // 1-step horizontal velcity of the rockets
					final float PI = 3.1415f;           // Using Math.PI is much space...
					
					
					// We animate the explosions
					final Vector< ShapeObject > deadExplosions = new Vector< ShapeObject >();
					for ( final ShapeObject explosion : model.explosions )
						if ( ( explosion.explosionRadius -= 2.3f ) < 0.0f )
							deadExplosions.add( explosion );
					model.explosions.removeAll( deadExplosions );
					
					final String PRESS_SPACE_STRING = "Press SPACE for a new game";
					// If game is over, next iteration is equals to the previous one, except the explosion of the dead player(s)
					if ( model.players[ 0 ].explosionRadius != null || model.players[ 1 ].explosionRadius != null ) {
						controller.statusLabel.setText( PRESS_SPACE_STRING );
						break;
					}
						
					// If players crashed into each other, game over...
					if ( Math.abs( model.players[ 0 ].p[ 2 ] - model.players[ 1 ].p[ 2 ] ) < 200.0f
							&& model.players[ 0 ].shape.intersects( model.players[ 1 ].shape.getBounds2D() ) ) {
						for ( final ShapeObject player : model.players ) {
							player.explosionRadius = 50.0f;
							player.shield          =  0.0f;
							model.explosions.add( player );
						}
						controller.statusLabel.setText( PRESS_SPACE_STRING );
						break; // If there's a dead player, game's over
					}
					
					// If one of the players has no more shield or hits the land, game over...
					for ( final ShapeObject player : model.players )
						if ( player.shield < 0.0f || model.land[ player.getY() ][ player.getX() ] > player.getHeight() ) {
							player.explosionRadius = 50.0f;
							model.explosions.add( player );
							if ( player.shield >= 0.0f )
								view.registerObjectMark( player );
							player.shield          =  0.0f;
							break;
						}

					
					// We clone the array of control key states because view can modify it during
					// the calculation of next iteration. We would see different states!!
					final boolean[][] keyStatess = view.keyStates.clone();
					
					// We check and step and handle players
					for ( int i = 0; i < model.players.length; i++ ) {
						final ShapeObject player = model.players[ i ];
						
						final boolean[] keyStates = keyStatess[ i ];
						
						if ( keyStates[ KEY_RIGHT  ] )
							player.direction += 0.1f;
						if ( keyStates[ KEY_LEFT   ] )
							player.direction -= 0.1f;

						// We keep direction between -PI and PI
						if ( player.direction < -PI )
							player.direction += 2.0f*PI;
						if ( player.direction > PI )
							player.direction -= 2.0f*PI;
						
						player.v[ 2 ] = 0f;                  // There is no vertical acceleration, vertical movement is 3-phased: up, down, or none
						if ( keyStates[ KEY_ASCEN  ] ) {
							player.v[ 2 ] += 30.0f;
						}
						if ( keyStates[ KEY_DESCEN ] ) {
							player.v[ 2 ] -= 40.0f;
						}
						if ( !keyStates[ KEY_ASCEN ] && !keyStates[ KEY_DESCEN ] ) {// If we dont accelerate, we slow down
							player.v[ 0 ] *= 0.95f;
							player.v[ 1 ] *= 0.95f;
						}

							
						final float ACCELERATION = 0.4f;    // Value of acceleration of the aircrafts.
						if ( keyStates[ KEY_ACC    ] ) {
							player.v[ 0 ] += ACCELERATION * Math.cos( player.direction );
							player.v[ 1 ] += ACCELERATION * Math.sin( player.direction );
						}
						if ( keyStates[ KEY_BREAK  ] ) {
							player.v[ 0 ] -= ACCELERATION * Math.cos( player.direction );
							player.v[ 1 ] -= ACCELERATION * Math.sin( player.direction );
						}
						
						// We make sure that the players velocity doesnt grow beyond a certain value
						while ( Math.hypot( player.v[ 0 ], player.v[ 1 ] ) > 9.0 )
							for ( int j = 0; j < 2; j++ )   // We decrease the velocity while the direction must not change
								player.v[ j ] *= 0.93f;
						
						// Now we step the player
						// (We have to do this before making new shots and moving them outside the player,
						// or else stepping the player could cause stepping onto his own shot and resulting in hurting himslef!)
						player.step();

						if ( keyStates[ KEY_FIRE1  ] && player.reloadings[ 0 ] == 1.0f ) {
							final float BULLET_VELOCITY = 10.0f; // 1-step horizontal velcity of the bullets
							
							final MovingObject bullet    = new MovingObject();
							final float[]      targetPos = model.players[ 1 - i ].p; // We want the bullet to target the other player

							// We initialize the bullet starter position
							for ( int j = 0; j < 3; j++ )
								bullet.p[ j ] = player.p[ j ];

							final float dy = targetPos[ 2 ] - bullet.p[ 2 ];
							// Vertical component of bullet velocity is determined: if target doesn't move, bullet must hit it.
							bullet.v[ 2 ] = BULLET_VELOCITY * dy / (float) Point2D.distance( bullet.p[ 0 ], bullet.p[ 1 ], targetPos[ 0 ], targetPos[ 1 ] );
							bullet.v[ 0 ] = BULLET_VELOCITY * (float) Math.cos( player.direction );
							bullet.v[ 1 ] = BULLET_VELOCITY * (float) Math.sin( player.direction );
							
							// We move the bullet outside the player, we dont want it to hit his owner (but we ensure we won't stuck inside)
							for ( int j = 0; player.shape.contains( bullet.p[ 0 ], bullet.p[ 1 ] ) && j < 50; j++ ) {
								bullet.step(); bullet.step(); bullet.step(); bullet.step();
							}
							
							player.reloadings[ 0 ] = 0.0f;
							model.shots.add( bullet );
						}

						if ( keyStates[ KEY_FIRE2  ] && player.reloadings[ 1 ] == 1.0f ) {
							final ShapeObject rocket = new ShapeObject( false );

							// We initialize the bullet starter position
							for ( int j = 0; j < 3; j++ )
								rocket.p[ j ] = player.p[ j ];

							rocket.v[ 0 ]    = ROCKET_VELOCITY * (float) Math.cos( player.direction );
							rocket.v[ 1 ]    = ROCKET_VELOCITY * (float) Math.sin( player.direction );
							rocket.targetPos = model.players[ 1 - i ].p; // The rocket targets the other player
							rocket.shape.translate( (int) rocket.p[ 0 ], (int) rocket.p[ 1 ] );

							// We move the rocket outside the player, we dont want it to hit his owner (but we ensure we won't stuck inside)
							for ( int j = 0; player.shape.intersects( rocket.shape.getBounds2D() ) && j < 50; j++ )
								rocket.step();

							player.reloadings[ 1 ] = 0.0f;
							model.shots.add( rocket );
						}

						player.reloadings[ 0 ] = Math.min( 1.0f, player.reloadings[ 0 ] + 0.1f   );
						player.reloadings[ 1 ] = Math.min( 1.0f, player.reloadings[ 1 ] + 0.007f );
						
					}
					
					// Now we step the bullets
					final Vector< MovingObject > deadShots = new Vector< MovingObject >();
					for ( final MovingObject shot : model.shots ) {
						final boolean     isRocket = shot instanceof ShapeObject;
						final ShapeObject rocket   = isRocket ? (ShapeObject) shot : null;
						
						stepCycle:
						for ( int i = 0; i < 4; i++ ) { // Bullets' step are more phased, we check hitting objects inside a "real step" too
							
							// The rocket follows its target
							if ( isRocket ) {
								shot.v[ 2 ] = shot.p[ 2 ] < rocket.targetPos[ 2 ] ? 3.0f : -3.0f;
								// If difference between rocket direction and the direction to the target is less than PI, we have to turn more left, else more right
								float rocketDirection = (float) Math.atan2( rocket.v[ 1 ], rocket.v[ 0 ] );
								float differenceDir   = rocketDirection - (float) Math.atan2( rocket.targetPos[ 1 ] - shot.p[ 1 ], rocket.targetPos[ 0 ] - shot.p[ 0 ] );
								differenceDir   += differenceDir < -PI ? 2.0f * PI : ( differenceDir > PI ? -2.0f * PI : 0.0f );
								rocketDirection += differenceDir < 0.0f ? 0.02f : -0.02f;
								rocket.v[ 0 ]    = ROCKET_VELOCITY * (float) Math.cos( rocketDirection );
								rocket.v[ 1 ]    = ROCKET_VELOCITY * (float) Math.sin( rocketDirection );
							}

							boolean deadShot = false;
							final boolean hitsLand = model.land[ shot.getY() ][ shot.getX() ] > shot.getHeight() || shot.getHeight() == 0;
							if ( shot.step() || hitsLand )
								deadShot = true;
							else // If shot is still in "play", we check whether it hits a player
								for ( final ShapeObject player : model.players )
									// If shot is a bullet, it hits the player if it's center point hits it. In case of a rocket, we have to examine polygon intersection.
									if ( Math.abs( shot.getHeight() - player.getHeight() ) < 200 
											&& ( isRocket ? player.shape.intersects( rocket.shape.getBounds2D() ) : player.shape.contains( shot.p[ 0 ], shot.p[ 1 ] ) ) ) {
										deadShot       = true;
										// We want the explosion in the center of the player being hit
										shot.p[ 0 ]    = player.p[ 0 ];
										shot.p[ 1 ]    = player.p[ 1 ];
										player.shield -= isRocket ? 0.3f : 0.05f;
									}
							
							if ( deadShot ) {
								if ( isRocket ) {
									rocket.explosionRadius = new Float( 30.0f );
									model.explosions.add( rocket );
								}
								if ( hitsLand )
									view.registerObjectMark( shot );
								deadShots.add( shot );
								break;
							}
						}
					}
					model.shots.removeAll( deadShots );

					
					// Next iteration is computed now. We have to break out from the fake cycle
					break;
					
					
					
				}
				else // If game is over, new game is required now
					if ( model.players[ 0 ].explosionRadius != null || model.players[ 1 ].explosionRadius != null )
						break;

				view.repaint();
				
				try {
					while ( controller.waitForNextIteration ) // We wait until timing say so.
						Thread.sleep( 0l, 100 );
					controller.waitForNextIteration = true;
					controller.waitForNextTiming    = false;  // We finished calculating and displaying the previous iteration, we're ready for the next one. We can start the next timing.
				}
				catch ( final Exception ie ) { // Exception is shorter than InterruptedException
				}
			}
			
		}
		
	}

	
	

	/** Status label for displaying messages.                 */
	public  final    JLabel  statusLabel          = new JLabel( " ", JLabel.CENTER );
	/** Tells whether game is paused.                         */
	public  volatile boolean paused;

	/** Tells whether next iteration can begin.               */
	private volatile boolean waitForNextIteration = true;
	/** Tells whether timing of next iteration can begin.
	 *  It's a double handshake: if program is ready for
	 *  next iteration, next iteration countdown shall begin.
	 *  If not, no reason to begin countdown, CPU is slow
	 *  or it's loaded, won't be able to process next
	 *  iteration in time. Fore more information,
	 *  see the development documentation.                    */
	private volatile boolean waitForNextTiming    = true;
	
	
	
	public void run() {
		while ( true )
			try {
				sleep( 40l ); // 25 iterations per seconds exactly if CPU is capable of computing it. If not, the most it can computje.
				waitForNextIteration = false;
				while ( waitForNextTiming )
					Thread.sleep( 0l, 100 ); 
				waitForNextTiming = true;
			}
			catch ( final Exception ie ) { // Exception is shorter than InterruptedException
			}
	}
	
}
