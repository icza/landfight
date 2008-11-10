package p;

import java.util.Vector;
import static p.MovingObject.BOUNDARIES;
/**
 * This is the model layer of the MVC architecture.<br>
 * 
 * @author Andras Belicza
 */
class Model {

	/** Number of land basepoints in both of the coordinate axis.
	 *  A basepoint is a determined (random) height point of the land.
	 *  Note: there are basepoints on the edges of the land.                        */
	private static final int   LAND_BASE_POINTS = 11;
	/** The base points divide the land to sectors, and this is their sizes.        */
	private static final int   SECTOR_SIZE      = 250;
	/** Size of the land (calculated from the LAND_BASE_POINTS and SECTOR_SIZE).    */
	public  static final int   LAND_SIZE        = ( LAND_BASE_POINTS - 1 ) * SECTOR_SIZE;
	
	/** Minimum height (depth) value of a land point.                               */
	public  static final float LAND_MIN         = -800.0f;
	/** Maximum height value of a land point.                                       */
	public  static final float LAND_MAX         = 2000.0f;
	
	
	
	
	/** Players of the game.                                                        */
	public ShapeObject[]          players = new ShapeObject[ 2 ];
	/** The values of the heights of the land.<br>
	 *  We want this float not doulbe, because this can contain millions of 
	 *  height values (huge size), and we don't even use fully the float precision. */
	public float[][]              land    = new float[ LAND_SIZE + 1 ][ LAND_SIZE + 1 ];  // +1 is for having +1 base point for helping the land generation
	
	/** Vector of bullets and rockets.                                              */
	public Vector< MovingObject > shots      = new Vector< MovingObject >();
	/** Vector of explosions.                                                       */
	public Vector< ShapeObject  > explosions = new Vector< ShapeObject  >();
	
	

	
	/**
	 * Reinits the game model so a new game can begin.
	 */
	public void newGame() {
		generateLand();
		
		for ( int i = 0; i < players.length; i++ ) {
			final ShapeObject player = players[ i ] = new ShapeObject( true );
			// Random position for the player
			player.p[ 0 ] = (float) Math.random() * BOUNDARIES[ 0 ];
			player.p[ 1 ] = (float) Math.random() * BOUNDARIES[ 1 ];
			player.p[ 2 ] = Math.min( BOUNDARIES[ 2 ], Math.max( 0.0f, land[ (int) player.p[ 1 ] ][ (int) player.p[ 0 ] ] + 650.0f ) );
			player.shape.translate( (int) player.p[ 0 ], (int) player.p[ 1 ] );
		}
		
		shots     .clear();
		explosions.clear();
		
	}
	
	
	/**
	 * Generates a random land.
	 */
	private void generateLand() {
		// First we generates the heights of the base points
		for ( int i = 0; i < LAND_BASE_POINTS; i++ )
			for ( int j = 0; j < LAND_BASE_POINTS; j++ )
				land[ i * SECTOR_SIZE ][ j * SECTOR_SIZE ] = LAND_MIN + (float)Math.random() * ( LAND_MAX - LAND_MIN );
		
		// Now we fill the land interpolating between the base points
		int x, y, dx, dy; // dx and dy are relative coordinates inside a sector
		for ( int i = 0; i < LAND_BASE_POINTS-1; i++ )
			for ( int j = 0; j < LAND_BASE_POINTS-1; j++ ) { // We go from sector to sector
				y = i * SECTOR_SIZE;
				x = j * SECTOR_SIZE;
				// Heights of the basepoints at the sectors corners
				final float baseHeight1 = land[ y               ][ x               ];
				final float baseHeight2 = land[ y               ][ x + SECTOR_SIZE ];
				final float baseHeight3 = land[ y + SECTOR_SIZE ][ x + SECTOR_SIZE ];
				final float baseHeight4 = land[ y + SECTOR_SIZE ][ x               ];
				for ( dy = 0; dy < SECTOR_SIZE; dy++, y++ ) {
					x = j * SECTOR_SIZE;
					final float baseHeight5 = land[ y ][ x               ] = interpolate( baseHeight1, baseHeight4, (float) dy / SECTOR_SIZE );
					final float baseHeight6 = land[ y ][ x + SECTOR_SIZE ] = interpolate( baseHeight2, baseHeight3, (float) dy / SECTOR_SIZE );
					for ( dx = 0; dx < SECTOR_SIZE; dx++, x++ ) {
						// We give a random value to the interpolated value for generating more realistic land (DISPERSION)
						land[ y ][ x ] = interpolate( baseHeight5, baseHeight6, (float) dx / SECTOR_SIZE ) + ( (float)Math.random() - 0.5f ) * 30.0f;
						land[ y ][ x ] = Math.max( LAND_MIN, Math.min( land[ y ][ x ], LAND_MAX ) );
					}
				}
			}
	}
	
	/**
	 * Interpolates. Calculates a 3rd value at a specified position between 2 boundary value.<br>
	 * The position is specified by a ratio, whose value means:
	 * 0 => value1, 1 => value2, 0 < ratio < 1  => somewhere between value1 and value2.
	 * We would like the result to be continous and nice, so 'somewhere' is determined by a
	 * function which is non-linear (linear resulted in "breaklines"). It goes from value1
	 * to value2 by the function which starts with 0 derivation and ends with 0 derivation
	 * (it has an infletion point, it has an 'S'). We use for this an 'x^4' function:<br>
	 * f(x) = a*x^4 + b*x^3 + c*x^2 + d*x^1 + e*x^0<br>
	 * We have parameters: f(0)=0, f(1)=1, f'(0)=0, f'(1)=0, and we'd like that f'(0.5)=0.5.
	 * The result is this:<br>
	 * 
	 * f(x) = 2*x^4 - 6*x^3 + 5*x^2   (my magic function!)
	 * 
	 * @param value1 first value of interpolation
	 * @param value2 last value of interpolation
	 * @param ratio  where between value1 and value2 we need the interpolation
	 * @return the interpolation of value1 and value2 at a position between them specified by ratio
	 */
	private float interpolate( final float value1, final float value2, final float ratio ) {
		final float ratio2 = ratio * ratio;
		return value1 + ( value2 - value1 ) * ( 2.0f * ratio2*ratio2 - 6.0f * ratio2 * ratio + 5.0f * ratio2 );
	}
	
}
