package p;

import static p.Model.LAND_SIZE;
import static p.Model.LAND_MAX;

/**
 * This class represents a moving object over the land.<br>
 * A moving object has a 3 dimensional position and a 3D velocity.<br>
 * Moving objects are Comparable, which helps the right 3D rendering.
 * The order is the order of the moving object's height. It's kinda z-buffer.
 * 
 * @author Andras Belicza
 */
class MovingObject implements Comparable {

	/** The upper boundaries (limits) of the coordiantes of a moving object. */
	public static final int[] BOUNDARIES = new int[] { LAND_SIZE - 1, LAND_SIZE - 1, (int) ( LAND_MAX * 1.2f ) };

	/** Position of the moving object. */
	public float[]   p = new float[ 3 ];
	/** Velocity of the moving object. */
	public float[]   v = new float[ 3 ];

	/**
	 * Returns the x coordinate of the moving object in int precision.
	 * @return the x coordinate of the moving object in int precision
	 */
	public int getX() {
		return (int) p[ 0 ];
	}
	
	/**
	 * Returns the y coordinate of the moving object in int precision.
	 * @return the y coordinate of the moving object in int precision
	 */
	public int getY() {
		return (int) p[ 1 ];
	}
	
	/**
	 * Returns the height of the moving object in int precision.<br>
	 * The height (above sea-level) is the z coordiante of the moving objects position.
	 * @return the height of the moving object in int precision
	 */
	public int getHeight() {
		return (int) p[ 2 ];
	}

	/**
	 * Steps the moving object.<br>
	 * Stepping a moving object means adding the velocity to the position
	 * and checking if it left the land. If it did, we cut the step so it can stay inside,
	 * but we return that step has been cut.
	 * @return true if step has been cut in order to stay inside the land, false otherwise
	 */
	public boolean step() {
		boolean cut = false;
		
		for ( int i = 0; i < 3; i++ ) {
			p[ i ] += v[ i ];

			// Now we check if player left the land
			if ( p[ i ] < 0.0f ) {
				p[ i ] = 0.0f;
				if ( i < 2 )
					cut = true;
			}
			if ( p[ i ] >= BOUNDARIES[ i ] ) {
				p[ i ] = BOUNDARIES[ i ];
				if ( i < 2 )
					cut = true;
			}
		}
		
		return cut;
	}

	/**
	 * Compares this MovingObject to another one.<br>
	 * WARNING! We use moving objects in SortedSet, this cannot give back 0,
	 * adding a moving object when this returns 0 would cause ignoring the new object.
	 * (Set does not (cannot) containt equal elements!)
	 * By the way, if heights are equals, displaying order is unimportant.
	 *  
	 * @param object the other moving object what to compare this one to
	 * @return 1 if this one is higher, -1 otherwise
	 */
	public int compareTo( final Object object ) {
		final MovingObject anotherMovingObject = (MovingObject) object;
		return p[ 2 ] > anotherMovingObject.p[ 2 ] ? 1 : -1;
	}
	
}
