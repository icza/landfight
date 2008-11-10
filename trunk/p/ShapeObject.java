package p;

import java.awt.Polygon;

/**
 * Represents a(n moving) object which has a shape
 * (which is not used in the game, for space matters we merged with its inheriter classes!).<br>
 * 
 * The class also represents the rocket, if instances are created by callin the public constructor
 * with false parameter.<br>
 * 
 * The class also represents the player, if instances are created by callin the public constructor
 * with true parameter.
 * 
 * The class also used to represent an explosion of a rocket or an aircraft, or rather to
 * represent the explosion itself.
 * 
 * @author Andras Belicza
 */
public class ShapeObject extends MovingObject {

	
	//==================================================================================================================
    // ShapeObject attributes
	//==================================================================================================================

	// This is here and not at the view because decisions of crashing and hitting (with bullets or aircrafts)
	// uses the polygon too (not the boundary rectangle only)
	/** Polygon of the object.                                                             */
	public final Polygon shape      = new Polygon();
	
	
	//==================================================================================================================
    // Rocket attributes
	//==================================================================================================================

	/** Reference to the position of the rockets target. (Rockets are "target followers".) */
	public float[]       targetPos;
	

	//==================================================================================================================
    // Rocket attributes
	//==================================================================================================================

	/** State of the shield of the player. 0 means gone, 1 means undamaged.                */
	public float         shield     = 1.0f;
	/** Array of reloading states of the weapons.
	 *  0 means out, 1 means fully operational.
	 *  Length of the array is 2.                                                          */
	public float[]       reloadings = new float[] { 1.0f, 1.0f };
	/** Direction of the player. This is an angle between -PI and PI rad.                  */
	public float         direction;


	//==================================================================================================================
    // Explosion attributes
	//==================================================================================================================

	/** Radius of the explosion or null if this object does not represents an explosion.    */
	public Float         explosionRadius;
	
	

	/** Tells whether the shape object is a player or a rocket.                            */
	public final boolean isPlayer;
	
	
	/**
	 * Creates a ShapeObject, a rocket.
	 * @param isPlayer tells whether the creatable shape object will be a player or a rocket
	 */
	public ShapeObject( final boolean isPlayer ) {
		this.isPlayer = isPlayer;
		
		// Trick: we gives the coordinates with int numbers not in double, compiling and storing doubles in classes would consume much more space
		int[] xs;   // x coordinates of the shape of the object in the model coordiante system which is a 1000-length square
		int[] ys;   // y coordinates of the shape of the object in the model coordiante system which is a 1000-length square
		int   size; // size of the shape of the object
		// The shape faces the 0 direction.

		if ( isPlayer ) {
			// The the original coordinates are scaled to 90% and moved by 10%,
			// because I wanted to move the rotate center point by 10 percent to left (looks much better during moving in case of this shape)
			// This is done by constants here and manually not with cycle. These manual constant operations are not compiled into classes, only the results (more free space...)
			final int    DELTA = 100;
			final double SCALE = 0.9;
			xs   = new int[] { DELTA   + (int) ( SCALE * 1000 ), DELTA   + (int) ( SCALE * 800 ), DELTA   + (int) ( SCALE * 600 ), DELTA   + (int) ( SCALE * 466 ), DELTA   + (int) ( SCALE * 333 ), DELTA   + (int) ( SCALE * 400 ), DELTA   + (int) ( SCALE * 160 ), DELTA   + (int) ( SCALE * 0   ), DELTA   + (int) ( SCALE * 0   ), DELTA   + (int) ( SCALE * 160 ), DELTA   + (int) ( SCALE * 400 ), DELTA   + (int) ( SCALE * 333  ), DELTA   + (int) ( SCALE * 466 ), DELTA   + (int) ( SCALE * 600 ), DELTA   + (int) ( SCALE * 800 ) };
		    ys   = new int[] { DELTA/2 + (int) ( SCALE * 500  ), DELTA/2 + (int) ( SCALE * 433 ), DELTA/2 + (int) ( SCALE * 433 ), DELTA/2 + (int) ( SCALE * 33  ), DELTA/2 + (int) ( SCALE * 0   ), DELTA/2 + (int) ( SCALE * 433 ), DELTA/2 + (int) ( SCALE * 440 ), DELTA/2 + (int) ( SCALE * 233 ), DELTA/2 + (int) ( SCALE * 766 ), DELTA/2 + (int) ( SCALE * 560 ), DELTA/2 + (int) ( SCALE * 566 ), DELTA/2 + (int) ( SCALE * 1000 ), DELTA/2 + (int) ( SCALE * 966 ), DELTA/2 + (int) ( SCALE * 566 ), DELTA/2 + (int) ( SCALE * 566 ) };
			size = 70;
		}
		else {
			xs   = new int[] { 1000, 875, 625, 500, 500, 125, 0  , 0  , 125, 500, 500, 625, 875 };
		    ys   = new int[] { 500 , 438, 438, 313, 438, 438, 313, 687, 562, 562, 687, 562, 562 };
			size = 40;
		}
		
		for ( int i = 0; i < xs.length; i++ )
			shape.addPoint( (int) ( xs[ i ] * size / 1000.0 ) - size/2, (int) ( ys[ i ] * size / 1000.0 ) - size/2 );
	}

	/**
	 * Steps the object.<br>
	 * Stepping an shapeObject means not only adding the velocity to the position
	 * but also recalculating the points of its polygon.
	 * @return true if step has been cut in order to stay inside the land, false otherwise
	 */
	public boolean step() {
		final float[] oldPos = p.clone();  // We need this because we "recalculate" the aircrafts polygon only by simply translate it with the value of step
		
		final boolean wasCut = super.step();
		
		// Now we "recalculate" the points of the aircrafts polygon
		shape.translate( (int) p[ 0 ] - (int) oldPos[ 0 ], (int) p[ 1 ] - (int) oldPos[ 1 ] );
		return wasCut;
	}

}
