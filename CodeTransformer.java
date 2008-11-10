import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * This program transfroms java source code for they can be compiled into smaller class byte codes.<br>
 * All that the program does is renaming strings which are java identifiers. Does not build up a document object model.
 * The renamable identifiers are built into the program too.<br>
 * 
 * The program takes the package p, and reads all java files inside it, 
 * and transforms them into the transformedcode directory.
 * 
 * @author Andras Belicza
 */
public class CodeTransformer {

	
	/**
	 * This is the entry point of the program.
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		new CodeTransformer().transformCodes();
	}

	
	
	/** The list (array) of renamable identifiers. */
	private static final String[] RENAMABLE_IDENTIFIERS = new String[] {

		// Class names:
		"Controller", // This must be the first, this is the main class, jar manifest has note of this
		"View",
		"Model",
		"ShapeObject",
		"MovingObject",
		
		// Identifiers from Model.java
		"LAND_BASE_POINTS",
		"SECTOR_SIZE",
		"LAND_SIZE",
		"LAND_MIN",
		"LAND_MAX",
		"players",
		"land",
		"shots",
		"explosions",
		"generateLand",
		"interpolate",
		
		// Identifiers from View.java
		"SCENE_SIZE",
		"MINIMAP_SIZE",
		"SEPARATOR_WIDTH",
		"VIEW_DIMENSION",
		"CONTROL_KEYS",
		"landscape",
		"miniMap",
		"storedTransform",
		"keyStates",
		"getRGBOfHeight",
		"interpolateColors",
		"registerObjectMark",
		"prepareContextForDrawing",
		"restoreContext",
		
		// Identifiers from Controller.java
		"paused",
		"statusLabel",
		"waitForNextIteration",
		"waitForNextTiming",

		// Identifiers from ShapeObject.java
		"shape",
		"targetPos",
		"shield",
		"reloadings",
		"direction",
		"explosionRadius",
		"isPlayer",
		
		// Identifiers from MovingObject.java
		"BOUNDARIES",
		"getX",
		"getY",
		"getHeight",

		// From multiple sources:
		"controller",
		"model",
		"view",
		"newGame",
		"step"
	};

	/**
	 * This method transforms the codes.
	 */
	public void transformCodes() {
		final String[][] idertifierPairs = buildIdentifierPairs();
		try {
			final File[] files = new File( "p" ).listFiles();
			
			for ( final File file : files )
				if ( file.getName().endsWith( ".java" ) ) {
					final StringBuffer sourceCode = readFile( file );
					
					transformCode( sourceCode, idertifierPairs );
					
					final StringBuffer outputFileName = new StringBuffer( file.getName() );
					transformCode( outputFileName, idertifierPairs );
					writeFile( "transformedcode/p/" + outputFileName.toString(), sourceCode );
				}
		}
		catch ( final Exception e ) {
			e.printStackTrace();
		}
				
	}

	/**
	 * Builds the identifier pairs 2 dimensional String array and returns it.<br>
	 * A pair is a renamable identifier and its new name.
	 * @return
	 */
	private String[][] buildIdentifierPairs() {
		// We just assign the shortest available identifier to the renamable ones.

		final String[] SHORTEST_AVAILABLE_IDENTIFIERS = new String[] {
			// Can (may) not be used:
			//     i, j, k    - cycle variables
			//     a, r, g, b - rgb components  (g also graphics context)
		    //     x, y, z    - coordinates
			//     p, v       - position and velocity
				"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
				"c", "d", "e", "f", "h", "l", "m", "n", "o", "q", "s", "t", "u", "w",
				"_A", "_B", "_C", "_D", "_E", "_F", "_G", "_H", "_I", "_J", "_K", "_L", "_M", "_N", "_O", "_P", "_Q", "_R", "_S", "_T", "_U", "_V", "_W", "_X", "_Y", "_Z"
			};

		if ( RENAMABLE_IDENTIFIERS.length > SHORTEST_AVAILABLE_IDENTIFIERS.length ) {
			System.out.println( "Error! Not supplied enough new identifiers!" );
			try {
				Thread.sleep( 3000 );
			} catch (InterruptedException e) {}
			System.exit( 0 );
		}
		
		final String[][] identifierPairs = new String[ RENAMABLE_IDENTIFIERS.length ][ 2 ];
		
		for ( int i = 0; i < identifierPairs.length; i++ ) {
			identifierPairs[ i ][ 0 ] = RENAMABLE_IDENTIFIERS[ i ];
			identifierPairs[ i ][ 1 ] = SHORTEST_AVAILABLE_IDENTIFIERS[ i ];
		}
		
		return identifierPairs;
	}
	
	/**
	 * Reads the content of a text file into a string buffer, and returns it.
	 * 
	 * @param file file to be read
	 * @return a string buffer holding the content of the specified file
	 * @throws Exception if error occurs during the operation 
	 */
	private StringBuffer readFile( final File file ) throws Exception {
		final StringBuffer content = new StringBuffer();
		
		final BufferedReader input = new BufferedReader( new FileReader( file ) );
		while ( input.ready() ) {
			content.append( input.readLine() );
			content.append( '\n' );
		}
		input.close();
		
		return content;
	}
	
	/**
	 * Transforms the given source code.<br>
	 * Replaces the renamable identifiers to shorter ones.
	 * 
	 * @param sourceCode      source code to be transformed
	 * @param identifierPairs pairs of renamable identifiers and their new shorter name
	 */
	private void transformCode( final StringBuffer sourceCode, final String[][] idertifierPairs ) {
		
		for ( final String[] identifierPair : idertifierPairs ) {
			int lastIndex = 0;
			int index;

			while ( ( index = sourceCode.indexOf( identifierPair[ 0 ], lastIndex ) ) >= 0 ) {
				
				// We examine whether the occurance found must be replaced
				boolean makeReplace = true;
				
				// We can rename only complete, full identifiers
				if ( index > 0 )
					if ( Character.isJavaIdentifierStart( sourceCode.charAt( index - 1 ) ) || Character.isJavaIdentifierPart( sourceCode.charAt( index - 1 ) ) )
						makeReplace = false;
				if ( index + identifierPair[ 0 ].length() < sourceCode.length() )
					if ( Character.isJavaIdentifierPart( sourceCode.charAt( index + identifierPair[ 0 ].length() ) ) )
						makeReplace = false;

				// We do not replace strings inside quotation maks
				if ( isQuotationAtIndex( sourceCode, index ) )
					makeReplace = false;
				
				if ( makeReplace )
					sourceCode.replace( index, index + identifierPair[ 0 ].length(), identifierPair[ 1 ] );
				else
					lastIndex = index + 1;
			}
		}
	}
	
	/**
	 * Tells whether a specified position of a source is inside of quotation marks.<br>
	 * Note: counts quotation marks inside comments too, so this works well only if number of quotation marks inside
	 * comments is even (which is in our case)!
	 * 
	 * @param source source in which to test
	 * @param index  index to be tested
	 * @return true if the specified index in the source is inside of quotation marks; false otherwise
	 */
	private boolean isQuotationAtIndex( final StringBuffer source, int index ) {
		boolean insideQuotation = false;
		
		for ( index--; index >= 0; index-- )
			if ( source.charAt( index ) == '\"' )
				insideQuotation = !insideQuotation;
		
		return insideQuotation;
	}
	
	/**
	 * Writes the given content to a file.
	 * 
	 * @param fileName name of file to write to
	 * @param content  content to be written
	 * @throws Exception if error occurs during the operation
	 */
	private void writeFile( final String fileName, final StringBuffer content ) throws Exception {
		final PrintWriter output = new PrintWriter( new FileWriter( fileName ) );
		
		output.write( content.toString() );
		
		output.flush();
		output.close();
	}
	
}
