package p;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import static p.Model.LAND_SIZE;
import static p.Model.LAND_MIN;
import static p.Model.LAND_MAX;
import static java.awt.image.BufferedImage.TYPE_INT_BGR;

/**
 * The view layer in the MVC architectrue.<br>
 * The view consists of:
 * <ul>
 *     <li>2 game scene parts, which is a view of the players and their environment including the land,
 *     <li>2 player status parts where we can see heights, our shield, and our weapon reloading status,
 *     <li>one minimap of the whole land.
 * </ul>
 * 
 * @author Andras Belicza
 */
class View extends JComponent implements KeyListener {

	/** Size of the game scene of each scene parts of the players.                   */
	private static final int       SCENE_SIZE      = 500;
	/** Size of the minimap.                                                         */
	private static final int       MINIMAP_SIZE    = 150;
	/** Width of the separator of the parts of the view (the distance between them). */
	private static final int       SEPARATOR_WIDTH = 4;
	/** The dimension of the view of the game.                                       */
	private static final Dimension VIEW_DIMENSION  = new Dimension( SCENE_SIZE * 2 + 2 * SEPARATOR_WIDTH, SCENE_SIZE + 2 * SEPARATOR_WIDTH + MINIMAP_SIZE );
	


	
	
	/** Reference to the model.                                                      */
	private final Model           model;
	/** Reference to the controller.                                                 */
	private final Controller      controller;
	/** Landscape: the relief map of the land.                                       */
	private final BufferedImage   landscape = new BufferedImage( LAND_SIZE   , LAND_SIZE   , TYPE_INT_BGR );
	/** A mini map of the whole landscape.                                           */
	private final BufferedImage   miniMap   = new BufferedImage( MINIMAP_SIZE, MINIMAP_SIZE, TYPE_INT_BGR );
	
	/** During the rendering we use this to store the affine transform of the graphics context. */
	private       AffineTransform storedTransform;
	

	/** States of the control keys of the players (public: used by the controller).  */
	public final  boolean[][]     keyStates = new boolean[ 2 ][ 8 ];
	
	
	/**
	 * Creates a new View.
	 * @param model      reference to the model
	 * @param controller reference to the controller
	 */
	public View( final Model model, final Controller controller ) {
		this.model      = model;
		this.controller = controller;
		
		setPreferredSize( VIEW_DIMENSION );
		addKeyListener  ( this           );
	}
	
	/**
	 * Does all neccessary things for starting a new game.
	 */
	public void newGame() {
		// Land has been changed, we have to redraw landscape
		for ( int y = 0; y < LAND_SIZE; y++ )
			for ( int x = 0; x < LAND_SIZE; x++ )
				landscape.setRGB( x, y, getRGBOfHeight( model.land[ y ][ x ] ) );

		// Now we can draw the minimap
		miniMap.createGraphics().drawImage( landscape, 0,0, MINIMAP_SIZE, MINIMAP_SIZE, null );
	}
	
	/**
	 * Calculates and returns the RGB components of the color of the specified height on the landscape.<br>
	 * The returned int value contains the rgb values in the right order. Blue is at the least significant bits.
	 * All of the rgb components are 8 bit precision.
	 * @param height height whose color needed to be calculated
	 * @return the RGB values of the specified height on the landscape
	 */
	private int getRGBOfHeight( final float height ) {
		// Values of heights where the relief map changes colors at.
		final float[]   HEIGHT_STONES   = new float[] { LAND_MIN                , 0.0f                    , 0.0f                   , 600.0f                    , 900.0f                    , 1300.0f                  , 1600.0f                   , LAND_MAX               , MovingObject.BOUNDARIES[ 2 ] };
		// Values of colors at the height stones to be used for landscape
		final Color[]   STONE_COLORS    = new Color[] { new Color( 39, 23, 112 ), new Color( 0, 146, 221 ), new Color( 0, 146, 63 ), new Color( 126, 195, 128 ), new Color( 184, 218, 141 ), new Color( 255, 252, 212 ), new Color( 187, 129, 92 ), new Color( 90, 58, 37 ), Color.BLACK                  };

		// We locate the height between 2 heights whose interpolatable colors will determine the searched RGB
		int i;
		for ( i = 1; height > HEIGHT_STONES[ i ]; i++ )
			;
		
		return interpolateColors( STONE_COLORS[ i - 1 ], STONE_COLORS[ i ], ( height - HEIGHT_STONES[ i - 1 ] ) / ( HEIGHT_STONES[ i ] - HEIGHT_STONES[ i - 1 ] ) );
	}
	
	/**
	 * Linear-interpolates colors. Calculates a 3rd color at a specified position between 2 boundary colors.<br>
	 * The position is specified by a ratio, whose value means:
	 * 0 => value1, 1 => value2, 0 < ratio < 1  => somewhere between value1 and value2.<br>
	 * In case of colors, interpolation means interpolation of red, green and blue components of colors.
	 * 
	 * @param value1 first value of interpolation
	 * @param value2 last value of interpolation
	 * @param ratio  where between value1 and value2 we need the interpolation
	 * @return the RGB value of the linear interpolation of value1 and value2 at a position between them specified by ratio
	 */
	private int interpolateColors( final Color value1, final Color value2, final double ratio ) {
		final int a = value1.getAlpha() + (int) ( ( value2.getAlpha() - value1.getAlpha() ) * ratio );
		final int r = value1.getRed  () + (int) ( ( value2.getRed  () - value1.getRed  () ) * ratio );
		final int g = value1.getGreen() + (int) ( ( value2.getGreen() - value1.getGreen() ) * ratio );
		final int b = value1.getBlue () + (int) ( ( value2.getBlue () - value1.getBlue () ) * ratio );
		
		return ( a << 24 ) + ( r << 16 ) + ( g << 8 ) + b;
	}
	
	/**
	 * Registers a mark of a moving object.<br>
	 * Mark of a moving object can be like a bullet hitting the land, a rocket exploding into the land
	 * or a player crashing into the land.<br>
	 * Registering means displaying it somehow on the scene.<br>
	 * Implementation simply draws to the landscape.
	 * Drawing to the landscape cannot be done directly. Landscape is a very big (n*10MB) image, drawing into it
	 * is very slow (would make the game lag). First we make a subimage of it which has a small size,
	 * and we draw into that subimage.
	 *
	 * @param object object whose mark is to be registered
	 */
	public void registerObjectMark( final MovingObject object ) {
		final Float explosionRadius = object instanceof ShapeObject ? ( (ShapeObject) object ).explosionRadius : null;
		
		final int BULLET_MARK_RADIUS = 2;
		final int SUBIMAGE_RADIUS    = explosionRadius == null ? BULLET_MARK_RADIUS : explosionRadius.intValue();
		
		final int px = (int) object.getX();
		final int py = (int) object.getY();
		
		final int LEFT_X  = Math.min( px, SUBIMAGE_RADIUS );
		final int UPPER_Y = Math.min( py, SUBIMAGE_RADIUS );
		final int RIGHT_X = Math.min( LAND_SIZE - 1 - px, SUBIMAGE_RADIUS );
		final int LOWER_Y = Math.min( LAND_SIZE - 1 - py, SUBIMAGE_RADIUS );
		
		// We make a subimage only big enough to hold the mark of the object
		final BufferedImage bi = landscape.getSubimage( px - LEFT_X, py - UPPER_Y, LEFT_X + RIGHT_X + 1, UPPER_Y + LOWER_Y + 1 );
		final Graphics2D    g2 = bi.createGraphics();
		
		g2.setColor( new Color( 0, 0, 0, 70 ) );
		if ( explosionRadius == null ) {// Bullet has a single filled oval mark
			try { // If we're near the edge of land, ArrayOutOfBoundsException is a normal thing...
				if ( model.land[ py ][ px ] > 0.0f )               // Marks cannot be in the water
					g2.fillOval( LEFT_X - BULLET_MARK_RADIUS, UPPER_Y - BULLET_MARK_RADIUS, BULLET_MARK_RADIUS*2, BULLET_MARK_RADIUS*2 );
			}
			catch ( final Exception e ) {}
		}
		else
			for ( int i = SUBIMAGE_RADIUS * 10; i > 0; i-- ) {
				final double radius = Math.random() * ( SUBIMAGE_RADIUS - 2 );
				final double alpha  = Math.random() * 6.28;             // 2*PI, a full circle
				final int    dx     = (int) ( radius*Math.cos( alpha ) );
				final int    dy     = (int) ( radius*Math.sin( alpha ) );
				try { // If we're near the edge of land, ArrayOutOfBoundsException is a normal thing...
					if ( model.land[ py + dy ][ px + dx ] > 0.0f ) // Marks cannot be in the water
						g2.fillOval( LEFT_X + dx - 2, UPPER_Y + dy - 2, 4, 4 );
				}
				catch ( final Exception e ) {}
			}
	}
	
	/**
	 * Paints the actual look of the component, paints the view of the game.<br>
	 * 
	 * I would have used paintComponent(), but paint() is shorter, less space.
	 * I draw only to the Graphics2D object, doesn't matter...
	 * I'm sorry for being space centric... (but that's one of the competition's aspect)
	 * 
	 * First draws the scenes, then the status windows, the minimap and lastly the potential messages.
	 * 
	 * @param g the graphics context in which to paint
	 */
	public void paint( final Graphics g ) {
		try {
			// Colors of the players
			final Color[] PLAYER_COLORS = new Color[] { Color.ORANGE, Color.MAGENTA };
			// Width of the status window. Note: the height is determined by MINIMAP_SIZE!
			final int     STATUS_WIDTH  = ( 2*SCENE_SIZE - SEPARATOR_WIDTH - MINIMAP_SIZE ) /2;

			
			final Graphics2D g2 = (Graphics2D) g;
			g2.setBackground( Color.BLACK );
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	
			
			// My apoligies for the millionth times now. This would have deserved several other methods, but space again...
			
			// We draw the scenes
			for ( int i = 0; i < model.players.length; i++ ) { // There is one for each player.
				prepareContextForDrawing( g2, i == 0 ? SEPARATOR_WIDTH/2 : SCENE_SIZE + SEPARATOR_WIDTH*3/2, SEPARATOR_WIDTH/2, SCENE_SIZE, SCENE_SIZE );
				
				final ShapeObject player = model.players[ i ];

				// We draw the land below and around the player.
				// We position so the player will be in the center of the scene except if player reached the edges of the land
				// Note: this algorithm assumes that LAND_SIZE>SCENE_SIZE.
				final int landPosX = Math.max( 0, Math.min( LAND_SIZE - SCENE_SIZE, player.getX() - SCENE_SIZE/2 ) );
				final int landPosY = Math.max( 0, Math.min( LAND_SIZE - SCENE_SIZE, player.getY() - SCENE_SIZE/2 ) );
				g2.drawImage( landscape, 0, 0, SCENE_SIZE , SCENE_SIZE , landPosX, landPosY, landPosX + SCENE_SIZE , landPosY + SCENE_SIZE , null );
				
				// Now we draw the moving objects in the right height order.
				// Sorted set for moving object to draw them in the right height order.
				// It's kinda like z-buffer.
				final TreeSet< MovingObject > movingObjects = new TreeSet< MovingObject >();
				movingObjects.clear();
				movingObjects.addAll( model.shots      );
				movingObjects.addAll( model.explosions );
				for ( final ShapeObject player_ : model.players )
					movingObjects.add( player_ );
				final Iterator< MovingObject >  iterator = movingObjects.iterator();
				
				while ( iterator.hasNext() ) {
					final MovingObject movingObject = iterator.next();
					// We could check and display moving object only if it's visible,
					// but that would cost lots of bytes giving the same result :) (we have clipping window)
					if ( movingObject instanceof ShapeObject ) { // It has a shape, we have to fill a polygon
						final ShapeObject shapeObject = (ShapeObject) movingObject;
						
						final AffineTransform at = g2.getTransform();
						g2.translate( shapeObject.getX() - landPosX, shapeObject.getY() - landPosY );

						if ( shapeObject.explosionRadius == null ) {
							g2.setColor( shapeObject.isPlayer ? PLAYER_COLORS[ shapeObject == model.players[ 0 ] ? 0 : 1 ] : Color.BLACK );

							// The shape is real size at maximum height and is 40% at 0 height.
							final double scaleFactor = 0.4 + 0.6 * shapeObject.getHeight() / MovingObject.BOUNDARIES[ 2 ];
							g2.scale( scaleFactor, scaleFactor );
							g2.rotate( shapeObject.isPlayer ? shapeObject.direction : Math.atan2( shapeObject.v[ 1 ], shapeObject.v[ 0 ] ) );
							shapeObject.shape.translate( -shapeObject.getX(), -shapeObject.getY() );
					
							g2.fillPolygon( shapeObject.shape ); // At last after a lots of preparation
							
							shapeObject.shape.translate(  shapeObject.getX(),  shapeObject.getY() );
						}
						else  // It's an explosion
							for ( int radius = shapeObject.explosionRadius.intValue(); radius > 0; radius -= 5 ) {
								g2.setColor( new Color( interpolateColors( new Color( 227, 225, 195, 200 ), new Color( 224, 197, 41, 130 ), radius / shapeObject.explosionRadius ), true ) );
								g2.fillOval( -radius, -radius, radius*2, radius*2 );
							}
						
						g2.setTransform( at );
					}
					else { // It's a simple bullet
						g2.setColor( Color.BLACK );
						g2.fillOval( movingObject.getX() - landPosX - 2, movingObject.getY() - landPosY - 2, 4, 4 );
					}
				}


				// We draw the colored height indicator
				g2.setColor( Color.BLACK );
				g2.fillRect( SCENE_SIZE - 62, 18, 34, 34 );
				g2.setColor( Color.WHITE );
				g2.fillRect( SCENE_SIZE - 61, 19, 32, 32 );
				g2.setColor( new Color( getRGBOfHeight( player.getHeight() ) ) );
				g2.fillRect( SCENE_SIZE - 60, 20, 30, 30 );
					
				// Now we draw the possible scene-messages
				g2.setFont( new Font( null, Font.BOLD, 13 ) );
				final FontMetrics fontMetrics     = g2.getFontMetrics();
				final String      CRITICAL_HEIGHT = "Critical height!";
				g2.setColor( Color.RED );
				if ( player.getHeight() - model.land[ player.getY() ][ player.getX() ] < 500.0f )
					g2.drawString( CRITICAL_HEIGHT, SCENE_SIZE/2 - fontMetrics.stringWidth( CRITICAL_HEIGHT )/2, 50 );
				
				restoreContext( g2 );
			}
			
			
			
			// We draw the status windows, the status infos of the players.
			g2.setFont( new Font( null, Font.PLAIN, 12 ) );
			for ( int i = 0; i < model.players.length; i++ ) {
				prepareContextForDrawing( g2, i == 0 ? SEPARATOR_WIDTH/2 : STATUS_WIDTH + SEPARATOR_WIDTH*5/2 + MINIMAP_SIZE, SCENE_SIZE + SEPARATOR_WIDTH*3/2, STATUS_WIDTH, MINIMAP_SIZE );
				
				g2.clearRect( 0, 0, STATUS_WIDTH, MINIMAP_SIZE );
				final ShapeObject player = model.players[ i ];
				
				g2.setColor( Color.WHITE );
				g2.drawString( "Your height / land height: " + player.getHeight() + " / " + (int) model.land[ player.getY() ][ player.getX() ] , 5, 20 );

				g2.setColor( Color.WHITE );
				g2.drawString( "Shield:", 5, 50 );
				g2.setColor( Color.BLUE );
				g2.fillRect( 0, 50 + 1, (int) ( player.shield * STATUS_WIDTH ) , 12 );
				
				for ( int j = 0; j < player.reloadings.length; j++ ) {
					g2.setColor( Color.WHITE );
					g2.drawString( "Weapon " + (j+1) + ":", 5, 85 + j * 25 );
					g2.setColor( Color.CYAN );
					g2.fillRect( 0, 85 + j * 25 + 1, (int) ( player.reloadings[ j ] * STATUS_WIDTH ) , 12 );
				}

				restoreContext( g2 );
			}

			
			
			// We draw the mini map
			prepareContextForDrawing( g2, STATUS_WIDTH + SEPARATOR_WIDTH*3/2, SCENE_SIZE + SEPARATOR_WIDTH*3/2, MINIMAP_SIZE, MINIMAP_SIZE );
			g2.drawImage( miniMap, 0, 0, null );
			for ( int i = 0; i < model.players.length; i++ ) {
				g2.setColor( PLAYER_COLORS[ i ] );
				g2.fillOval( model.players[ i ].getX() * MINIMAP_SIZE/LAND_SIZE - 3, model.players[ i ].getY() * MINIMAP_SIZE/LAND_SIZE - 3, 6, 6 );
			}
			restoreContext( g2 );


			
			// We draw possible window-messages
			if ( model.players[ 0 ].explosionRadius != null || model.players[ 1 ].explosionRadius != null ) {
				g2.setFont( new Font( null, Font.ITALIC | Font.BOLD, 38 ) );
				final FontMetrics fontMetrics = g2.getFontMetrics();
				String gameOverMessage = "Game Over, ";
				if ( model.players[ 0 ].explosionRadius != null && model.players[ 1 ].explosionRadius != null )
					gameOverMessage += "there is no winner!";
				for ( int i = 0; i < model.players.length; i++ )
					if ( model.players[ i ].explosionRadius == null )
						gameOverMessage += "Player " + ( i + 1 ) + " wins!";
				for ( int i = 0; i < 2; i++ ) {
					g2.setColor( i == 0 ? new Color( 0, 0, 0, 150 ) : Color.YELLOW );
					g2.drawString( gameOverMessage, SCENE_SIZE + SEPARATOR_WIDTH/2 - fontMetrics.stringWidth( gameOverMessage )/2 - i*4 , SCENE_SIZE/2 + SEPARATOR_WIDTH - i*2 );
				}
			}
		
		}
		catch ( final Exception e ) {  // Exception can occur when we modify/regenerate the model during a painting
		}
	}

	
	/**
	 * Prepares the specified graphics context for drawing in a window.
	 * @param g2     reference to the graphics context to be prepared
	 * @param x      x position of the window of drawing
	 * @param y      y position of the window of drawing
	 * @param width  width of the window of drawing
	 * @param height width of the window of drawing
	 */
	private void prepareContextForDrawing( final Graphics2D g2, final int x, final int y, final int width, final int height ) {
		storedTransform = g2.getTransform();
		g2.setClip( x, y, width, height );
		g2.translate( x, y );
	}
	
	/**
	 * Restores the specified graphics context before the state of calling prepareContextForDrawing(). 
	 * @param g2 reference to the graphics context to be restored
	 */
	private void restoreContext( final Graphics2D g2 ) {
		g2.setTransform( storedTransform                 );
		g2.setClip     ( new Rectangle( VIEW_DIMENSION ) );
	}

	/**
	 * Handles the key typed events.
	 * @param ke details of the key event
	 */
	public void keyTyped( final KeyEvent ke ) {
	}

	/**
	 * Handles the key pressed events.
	 * @param ke details of the key event
	 */
	public void keyPressed( final KeyEvent ke ) {
		// Keys which will result the actions of the players
		final int[][] CONTROL_KEYS = new int[][] {
				{ KeyEvent.VK_D      , KeyEvent.VK_G      , KeyEvent.VK_R      ,KeyEvent.VK_F      , KeyEvent.VK_S , KeyEvent.VK_X   , KeyEvent.VK_W    , KeyEvent.VK_2    },
				{ KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD8,KeyEvent.VK_NUMPAD5, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT }
			};

		for ( int i = 0; i < CONTROL_KEYS.length; i++ )
			for ( int j = 0; j < CONTROL_KEYS[ i ].length; j++ )
				if ( ke.getKeyCode() == CONTROL_KEYS[ i ][ j ] )
					keyStates[ i ][ j ] = true;

		if ( ke.getKeyCode() == KeyEvent.VK_F1 ) {
			 // We show help informations about Landfight
			final JPanel panel = new JPanel( new GridLayout( 5, 3, 5, 0 ) );

			panel.add( new JLabel( "Action:", JLabel.CENTER ) );
			panel.add( new JLabel( "Player1:", JLabel.CENTER ) );
			panel.add( new JLabel( "Player2:", JLabel.CENTER ) );

			panel.add( new JLabel( "Turn left / right", JLabel.CENTER ) );
			panel.add( new JLabel( "D / G", JLabel.CENTER ) );
			panel.add( new JLabel( "Numpad4 / Numpad6", JLabel.CENTER ) );

			panel.add( new JLabel( "Accelerate forward / backward", JLabel.CENTER ) );
			panel.add( new JLabel( "R / F", JLabel.CENTER ) );
			panel.add( new JLabel( "Numpad8 / Numpad5", JLabel.CENTER ) );

			panel.add( new JLabel( "Ascend / Descend", JLabel.CENTER ) );
			panel.add( new JLabel( "S / X", JLabel.CENTER ) );
			panel.add( new JLabel( "Up / Down", JLabel.CENTER ) );

			panel.add( new JLabel( "Fire primary / secondary", JLabel.CENTER ) );
			panel.add( new JLabel( "W / 2", JLabel.CENTER ) );
			panel.add( new JLabel( "Right / Left", JLabel.CENTER ) );

			JOptionPane.showMessageDialog( getParent(), new Object[] {
					new JLabel( "Java Technology Turns 10:", JLabel.CENTER ),
					new JLabel( "LandFight", JLabel.CENTER ),
					new JSeparator(),
					new JLabel( "Your simple goal is to eliminate your opponent.", JLabel.CENTER ),
					new JSeparator(),
					"Control keys:",
					"F1 - this help",
					"Space - pause / resume / new game",
					new JSeparator(),
					panel,
					new JSeparator(),
					new JLabel( "Created by Andr\u00e1s Belicza, Hungary 2005", JLabel.CENTER )
				}, "Help", JOptionPane.INFORMATION_MESSAGE );
			
		}
		else if ( ke.getKeyCode() == KeyEvent.VK_SPACE )
			// If game is over, we don't modify the status text, because it will be right away ('Generating...'), we don't want it to blink. We use no short cut condition evaluation (controller.paused gets new value!). 
			controller.statusLabel.setText( ( controller.paused = !controller.paused ) & ( model.players[ 0 ].explosionRadius == null && model.players[ 1 ].explosionRadius == null ) ? "Paused - SPACE to resume" : null );
	}

	/**
	 * Handles the key released events.
	 * @param ke details of the key event
	 */
	public void keyReleased( final KeyEvent ke ) {
		// Keys which will result the actions of the players
		final int[][] CONTROL_KEYS = new int[][] {
				{ KeyEvent.VK_D      , KeyEvent.VK_G      , KeyEvent.VK_R      ,KeyEvent.VK_F      , KeyEvent.VK_S , KeyEvent.VK_X   , KeyEvent.VK_W    , KeyEvent.VK_2    },
				{ KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD8,KeyEvent.VK_NUMPAD5, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT }
			};
		
		for ( int i = 0; i < CONTROL_KEYS.length; i++ )
			for ( int j = 0; j < CONTROL_KEYS[ i ].length; j++ )
				if ( ke.getKeyCode() == CONTROL_KEYS[ i ][ j ] )
					keyStates[ i ][ j ] = false;
	}
	
}
