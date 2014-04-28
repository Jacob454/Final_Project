package CSET1200;

public class conwayGOL {
	
	
	public class Cell {
		  public final short col;
		  public final short row;
		  /**
		   * Number of neighbours of this cell.
		   * 
		   * Determines the next state.
		   */
		  public byte neighbour; // Neighbour is International English
		  
		  /**
		   * HASHFACTOR must be larger than the maximum number of columns (that is: the max width of a monitor in pixels).
		   * It should also be smaller than 65536. (sqrt(MAXINT)).
		   */
		  private final int HASHFACTOR = 5000; 
		  
		  /**
		   * Constructor
		   * @param col column of cell
		   * @param row row or cell
		   */
		  public Cell( int col, int row ) {
		    this.col = (short)col;
		    this.row = (short)row;
		    neighbour = 0;
		  }
		  
		  /**
		   * Compare cell-objects for use in hashtables
		   * @see java.lang.Object#equals(java.lang.Object)
		   */
		  public boolean equals(Object o) {
		    if (!(o instanceof Cell) )
		      return false;
		    return col==((Cell)o).col && row==((Cell)o).row;
		  }

		  /**
		   * Calculate hash for use in hashtables
		   * 
		   * @see java.lang.Object#hashCode()
		   */
		  public int hashCode() {
		    return HASHFACTOR*row+col;
		  }
		  
		  /**
		   * @see java.lang.Object#toString()
		   */
		  public String toString() {
		    return "Cell at ("+col+", "+row+") with "+neighbour+" neighbour"+(neighbour==1?"":"s");
		  }
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	import java.awt.Dimension;
	import java.util.Enumeration;

	/**
	 * Interface between GameOfLifeCanvas and GameOfLife.
	 * This way GameOfLifeCanvas is generic, independent of GameOfLife.
	 * It contains generic methods to operate on a cell grid.
	 *
	 * @author Edwin Martin
	 */
	public interface CellGrid {
	  /**
	   * Get status of cell (alive or dead).
	   * @param col x-position
	   * @param row y-position
	   * @return living or not
	   */
	  public boolean getCell( int col, int row );

	  /**
	   * Set status of cell (alive or dead).
	   * @param col x-position
	   * @param row y-position
	   * @param cell living or not
	   */
	  public void setCell( int col, int row, boolean cell );

	  /**
	   * Get dimension of cellgrid.
	   * @return dimension
	   */
	  public Dimension getDimension();
	  
	  /**
	   * Resize the cell grid.
	   * @param col new number of columns.
	   * @param row new number of rows.
	   */
	  public void resize( int col, int row );

	  /**
	   * Get cell-enumerator. Enumerates over all living cells (type Cell).
	   * @return Enumerator over Cell.
	   * @see Cell
	   */
	  public Enumeration getEnum();

	  /**
	   * Clears grid.
	   */
	  public void clear();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	import java.awt.Canvas;
	import java.awt.Color;
	import java.awt.Dimension;
	import java.awt.Graphics;
	import java.awt.Image;
	import java.awt.event.ComponentEvent;
	import java.awt.event.ComponentListener;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseMotionAdapter;
	import java.util.Enumeration;
	import java.util.Vector;

	/**
	 * Subclass of Canvas, which makes the cellgrid visible.
	 * Communicates via CellGrid interface.
	 * @author Edwin Martin
	 */
	public class CellGridCanvas extends Canvas {
	  private boolean cellUnderMouse;
	  /**
	   * Image for double buffering, to prevent flickering.
	   */
	  private Image offScreenImage;
	  private Graphics offScreenGraphics;
	  private Image offScreenImageDrawed;
	  /**
	   * Image, containing the drawed grid.
	   */
	  private Graphics offScreenGraphicsDrawed;
	  private int cellSize;
	  private CellGrid cellGrid;
	  private Vector listeners;
	  private int newCellSize;
	  private Shape newShape;

	  /**
	   * Constructs a CellGridCanvas.
	   * @param cellGrid the GoL cellgrid
	   * @param cellSize size of cell in pixels
	   */
	  public CellGridCanvas(CellGrid cellGrid, int cellSize) {
	    this.cellGrid = cellGrid;
	    this.cellSize = cellSize;

	    setBackground(new Color(0x999999));
	    
	    addMouseListener(
	      new MouseAdapter() {
	        public void mouseReleased(MouseEvent e) {
	          draw(e.getX(), e.getY());
	        }
	        public void mousePressed(MouseEvent e) {
	          saveCellUnderMouse(e.getX(), e.getY());
	        }
	      });

	    addMouseMotionListener(new MouseMotionAdapter() {
	      public void mouseDragged(MouseEvent e) {
	        draw(e.getX(), e.getY());
	      }
	    });
	    addComponentListener( 
	      new ComponentListener() {
	        public void componentResized(ComponentEvent e) {
	          resized();
	          repaint();
	        }
	        public void componentMoved(ComponentEvent e) {}
	        public void componentHidden(ComponentEvent e) {}
	        public void componentShown(ComponentEvent e) {}
	      }
	    );

	  }
	  
	  /**
	   * Set cell size (zoom factor)
	   * @param cellSize Size of cell in pixels
	   */
	  public void setCellSize( int cellSize ) {
	    this.cellSize = cellSize;
	    resized();
	    repaint();
	  }
	  
	  /**
	   * The grid is resized (by window resize or zooming).
	   * Also apply post-resize properties when necessary
	   */
	  public void resized() {
	    if ( newCellSize != 0 ) {
	      cellSize = newCellSize;
	      newCellSize = 0;
	    }
	    Dimension canvasDim = this.size();
	    offScreenImage = null;
	    offScreenImageDrawed = null;
	    cellGrid.resize(canvasDim.width/cellSize, canvasDim.height/cellSize);
	    if ( newShape != null ) {
	      try {
	        setShape( newShape );
	      } catch (ShapeException e) {
	        // ignore
	      }
	    }
	      
	  }

	  /**
	   * Remember state of cell for drawing.
	   * 
	   * @param x x-coordinate
	   * @param y y-coordinate
	   */
	  public void saveCellUnderMouse(int x, int y) {
	    try {
	      cellUnderMouse = cellGrid.getCell(x / cellSize, y / cellSize);
	    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
	      // ignore
	    }
	  }

	  /**
	   * Draw in this cell.
	   * 
	   * @param x x-coordinate
	   * @param y y-coordinate
	   */
	  public void draw(int x, int y) {
	    try {
	      cellGrid.setCell(x / cellSize, y / cellSize, !cellUnderMouse );
	      repaint();
	    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
	      // ignore
	    }
	  }

	  /** 
	   * Use double buffering.
	   * @see java.awt.Component#update(java.awt.Graphics)
	   */
	  public void update(Graphics g) {
	    Dimension d = getSize();
	    if ((offScreenImage == null)) {
	      offScreenImage = createImage(d.width, d.height);
	      offScreenGraphics = offScreenImage.getGraphics();
	    }
	    paint(offScreenGraphics);
	    g.drawImage(offScreenImage, 0, 0, null);
	  }

	  /**
	   * Draw this generation.
	   * @see java.awt.Component#paint(java.awt.Graphics)
	   */
	  public void paint(Graphics g) {
	    // Draw grid on background image, which is faster
	    if (offScreenImageDrawed == null) {
	      Dimension dim = cellGrid.getDimension();
	      Dimension d = getSize();
	      offScreenImageDrawed = createImage(d.width, d.height);
	      offScreenGraphicsDrawed = offScreenImageDrawed.getGraphics();
	      // draw background (MSIE doesn't do that)
	      offScreenGraphicsDrawed.setColor(getBackground());
	      offScreenGraphicsDrawed.fillRect(0, 0, d.width, d.height);
	      offScreenGraphicsDrawed.setColor(Color.gray);
	      offScreenGraphicsDrawed.fillRect(0, 0, cellSize * dim.width - 1, cellSize * dim.height - 1);
	      offScreenGraphicsDrawed.setColor(getBackground());
	      for (int x = 1; x < dim.width; x++) {
	        offScreenGraphicsDrawed.drawLine(x * cellSize - 1, 0, x * cellSize - 1, cellSize * dim.height - 1);
	      }
	      for (int y = 1; y < dim.height; y++) {
	        offScreenGraphicsDrawed.drawLine( 0, y * cellSize - 1, cellSize * dim.width - 1, y * cellSize - 1);
	      }
	    }
	    g.drawImage(offScreenImageDrawed, 0, 0, null);
	    // draw populated cells
	    g.setColor(Color.yellow);
	    Enumeration enum = cellGrid.getEnum();
	    Cell c;
	    while ( enum.hasMoreElements() ) {
	      c = (Cell)enum.nextElement();
	      g.fillRect(c.col * cellSize, c.row * cellSize, cellSize - 1, cellSize - 1);
	    }
	  }
	  
	  /**
	   * This is the preferred size.
	   * @see java.awt.Component#getPreferredSize()
	   */
	  public Dimension getPreferredSize() {
	    Dimension dim = cellGrid.getDimension();
	    return new Dimension( cellSize * dim.width,  cellSize * dim.height );
	  }

	  /**
	   * This is the minimum size (size of one cell).
	   * @see java.awt.Component#getMinimumSize()
	   */
	  public Dimension getMinimumSize() {
	    return new Dimension( cellSize,  cellSize );
	  }
	  
	  /**
	   * Settings to appy after a window-resize.
	   * @param newShape new shape
	   * @param newCellSize new cellSize
	   */
	  public void setAfterWindowResize( Shape newShape, int newCellSize ) {
	    this.newShape = newShape;
	    this.newCellSize = newCellSize;
	  }

	  /**
	   * Draws shape in grid.
	   * 
	   * @param shape name of shape
	   * @return true when shape fits, false otherwise
	   * @throws ShapeException if the shape does not fit on the canvas
	   */
	  public synchronized void setShape(Shape shape) throws ShapeException {
	    int xOffset;
	    int yOffset;
	    int[][] shapeGrid;
	    Dimension dimShape;
	    Dimension dimGrid;
	    int i;

	    // get shape properties
	    //shapeGrid = shape.getShape();
	    dimShape =  shape.getDimension();
	    dimGrid =  cellGrid.getDimension();

	    if (dimShape.width > dimGrid.width || dimShape.height > dimGrid.height)
	      throw new ShapeException( "Shape doesn't fit on canvas (grid: "+dimGrid.width+"x"+dimGrid.height+", shape: "+dimShape.width+"x"+dimShape.height+")"); // shape doesn't fit on canvas

	    // center the shape
	    xOffset = (dimGrid.width - dimShape.width) / 2;
	    yOffset = (dimGrid.height - dimShape.height) / 2;
	    cellGrid.clear();

	    // draw shape
	    Enumeration cells = shape.getCells();
	    while (cells.hasMoreElements()) {
	      int[] cell = (int[]) cells.nextElement();
	      cellGrid.setCell(xOffset + cell[0], yOffset + cell[1], true);
	    }

	  }
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	import java.awt.Button;
	import java.awt.Choice;
	import java.awt.Label;
	import java.awt.Panel;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.awt.event.ItemEvent;
	import java.awt.event.ItemListener;
	import java.util.Enumeration;
	import java.util.Vector;


	/**
	 * GUI-controls of the Game of Life.
	 * It contains controls like Shape, zoom and speed selector, next and start/stop-button.
	 * It is a seperate class, so it can be replaced by another implementation for e.g. mobile phones or PDA's.
	 * Communicates via the GameOfLifeControlsListener.
	 * @author Edwin Martin
	 *
	 */
	public class GameOfLifeControls extends Panel {
	  private Label genLabel;
	  private final String genLabelText = "Generations: ";
	  private final String nextLabelText = "Next";
	  private final String startLabelText = "Start";
	  private final String stopLabelText = "Stop";
	  public static final String SLOW = "Slow";
	  public static final String FAST = "Fast";
	  public static final String HYPER = "Hyper";
	  public static final String BIG = "Big";
	  public static final String MEDIUM = "Medium";
	  public static final String SMALL = "Small";
	  public static final int SIZE_BIG = 11;
	  public static final int SIZE_MEDIUM = 7;
	  public static final int SIZE_SMALL = 3;
	  private Button startstopButton;
	  private Button nextButton;
	  private Vector listeners;
	  private Choice shapesChoice;
	  private Choice zoomChoice;

	  /**
	   * Contructs the controls.
	   */
	  public GameOfLifeControls() {
	    listeners = new Vector();

	    // pulldown menu with shapes
	    shapesChoice = new Choice();
	  
	    // Put names of shapes in menu
	    Shape[] shapes = ShapeCollection.getShapes();
	    for ( int i = 0; i < shapes.length; i++ )
	      shapesChoice.addItem( shapes[i].getName() );

	    // when shape is selected
	    shapesChoice.addItemListener(
	      new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	          shapeSelected( (String) e.getItem() );
	        }
	      }
	    );
	  
	    // pulldown menu with speeds
	    Choice speedChoice = new Choice();
	  
	    // add speeds
	    speedChoice.addItem(SLOW);
	    speedChoice.addItem(FAST);
	    speedChoice.addItem(HYPER);
	  
	    // when item is selected
	    speedChoice.addItemListener(
	      new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	          String arg = (String) e.getItem();
	          if (SLOW.equals(arg)) // slow
	            speedChanged(1000);
	          else if (FAST.equals(arg)) // fast
	            speedChanged(100);
	          else if (HYPER.equals(arg)) // hyperspeed
	            speedChanged(10);
	        }
	      }
	    );
	  
	    // pulldown menu with speeds
	    zoomChoice = new Choice();
	  
	    // add speeds
	    zoomChoice.addItem(BIG);
	    zoomChoice.addItem(MEDIUM);
	    zoomChoice.addItem(SMALL);
	  
	    // when item is selected
	    zoomChoice.addItemListener(
	      new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	          String arg = (String) e.getItem();
	          if (BIG.equals(arg))
	            zoomChanged(SIZE_BIG);
	          else if (MEDIUM.equals(arg))
	            zoomChanged(SIZE_MEDIUM);
	          else if (SMALL.equals(arg))
	            zoomChanged(SIZE_SMALL);
	        }
	      }
	    );
	  
	    // number of generations
	    genLabel = new Label(genLabelText+"         ");
	  
	    // start and stop buttom
	    startstopButton = new Button(startLabelText);
	      
	    // when start/stop button is clicked
	    startstopButton.addActionListener(
	      new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          startStopButtonClicked();
	        }
	      }
	    );
	  
	    // next generation button
	    nextButton = new Button(nextLabelText);
	      
	    // when next button is clicked
	    nextButton.addActionListener(
	      new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          nextButtonClicked();
	        }
	      }
	    );
	  
	    // create panel with controls
	    this.add(shapesChoice);
	    this.add(nextButton);
	    this.add(startstopButton);
	    this.add(speedChoice);
	    this.add(zoomChoice);
	    this.add(genLabel);
	    this.validate();
	  }
	  

	  /**
	   * Add listener for this control
	   * @param listener Listener object
	   */
	  public void addGameOfLifeControlsListener( GameOfLifeControlsListener listener ) {
	    listeners.addElement( listener );
	  }

	  /**
	   * Remove listener from this control
	   * @param listener Listener object
	   */
	  public void removeGameOfLifeControlsListener( GameOfLifeControlsListener listener ) {
	    listeners.removeElement( listener );
	  }

	  /**
	   * Set the number of generations in the control bar.
	   * @param generations number of generations
	   */
	  public void setGeneration( int generations ) {
	    genLabel.setText(genLabelText + generations + "         ");
	  }
	  
	  /**
	   * Start-button is activated.
	   */
	  public void start() {
	    startstopButton.setLabel(stopLabelText);
	    nextButton.disable();
	    shapesChoice.disable();
	  }

	  /**
	   * Stop-button is activated.
	   */
	  public void stop() {
	    startstopButton.setLabel(startLabelText);
	    nextButton.enable();
	    shapesChoice.enable();
	  }

	  /**
	   * Called when the start/stop-button is clicked.
	   * Notify event-listeners.
	   */
	  public void startStopButtonClicked() {
	    GameOfLifeControlsEvent event = new GameOfLifeControlsEvent( this );
	    for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
	      ((GameOfLifeControlsListener) e.nextElement()).startStopButtonClicked( event );
	  }

	  /**
	   * Called when the next-button is clicked.
	   * Notify event-listeners.
	   */
	  public void nextButtonClicked() {
	    GameOfLifeControlsEvent event = new GameOfLifeControlsEvent( this );
	    for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
	      ((GameOfLifeControlsListener) e.nextElement()).nextButtonClicked( event );
	  }

	  /**
	   * Called when a new speed from the speed pull down is selected.
	   * Notify event-listeners.
	   */
	  public void speedChanged( int speed ) {
	    GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getSpeedChangedEvent( this, speed );
	    for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
	      ((GameOfLifeControlsListener) e.nextElement()).speedChanged( event );
	  }

	  /**
	   * Called when a new zoom from the zoom pull down is selected.
	   * Notify event-listeners.
	   */
	  public void zoomChanged( int zoom ) {
	    GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getZoomChangedEvent( this, zoom );
	    for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
	      ((GameOfLifeControlsListener) e.nextElement()).zoomChanged( event );
	  }

	  /**
	   * Called when a new shape from the shape pull down is selected.
	   * Notify event-listeners.
	   */
	  public void shapeSelected( String shapeName ) {
	    GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getShapeSelectedEvent( this, shapeName );
	    for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
	      ((GameOfLifeControlsListener) e.nextElement()).shapeSelected( event );
	  }
	  
	  /**
	   * Called when a new cell size from the zoom pull down is selected.
	   * Notify event-listeners.
	   */
	  public void setZoom( String n ) {
	    zoomChoice.select(n);
	  }


	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

package org.bitstorm.gameoflife;

import java.awt.Event;

/**
 * Event class for GameOfLifeControls.
 * Can pass speed, cellSize and shapeName.
 * Objects from this class are generated by GameOfLifeControls
 * @see GameOfLifeControls
 * @author Edwin Martin
 */
public class GameOfLifeControlsEvent extends Event {
  private int speed;
  private int zoom;
  private String shapeName;

  /**
   * Construct a GameOfLifeControls.ControlsEvent
   * @param source source of event
   */
  public GameOfLifeControlsEvent(Object source) {
    super(source, 0, null);
  }

  /**
   * Constructs a event due to the speed changed.
   * @param source source of the event
   * @param speed new speed
   * @return new event object
   */
  public static GameOfLifeControlsEvent getSpeedChangedEvent( Object source, int speed ) {
    GameOfLifeControlsEvent event = new GameOfLifeControlsEvent(source);
    event.setSpeed(speed);
    return event;
  }
  
  /**
   * Constructs a event due to the zoom changed.
   * @param source source of the event
   * @param zoom new zoom (cell size in pixels)
   * @return new event object
   */
  public static GameOfLifeControlsEvent getZoomChangedEvent( Object source, int zoom ) {
    GameOfLifeControlsEvent event = new GameOfLifeControlsEvent(source);
    event.setZoom(zoom);
    return event;
  }
  
  /**
   * Constructs a event due to the shape changed.
   * @param source source of the event
   * @param shapeName name of selected shape
   * @return new event object
   */
  public static GameOfLifeControlsEvent getShapeSelectedEvent( Object source, String shapeName ) {
    GameOfLifeControlsEvent event = new GameOfLifeControlsEvent(source);
    event.setShapeName(shapeName);
    return event;
  }
  
  /**
   * Gets speed of Game
   * @return speed (10 is fast, 1000 is slow)
   */
  public int getSpeed() {
    return speed;
  }

  /**
   * Sets speed of Game
   * @param speed (10 is fast, 1000 is slow)
   */
  public void setSpeed( int speed ) {
    this.speed = speed;
  }

  /**
   * Gets size of cell
   * @return speed (10 is big, 2 is small)
   */
  public int getZoom() {
    return zoom;
  }

  /**
   * Sets zoom of Game
   * @param zoom size of cell in pixels
   */
  public void setZoom( int zoom ) {
    this.zoom = zoom;
  }

  /**
   * Gets name of shape
   * @return name of selected shape
   */
  public String getShapeName() {
    return shapeName;
  }

  /**
   * Sets name of shape
   * @param shapeName name of shape
   */
  public void setShapeName( String shapeName ) {
    this.shapeName = shapeName;
  }

}
















import java.util.EventListener;

/**
 * Listener interface for GameOfLifeControls.
 * The idea behind this interface is that the controls can be replaced by something else for
 * e.g. smart phones and PDA's.
 * @see GameOfLifeControls
 * @author Edwin Martin
 */
public interface GameOfLifeControlsListener extends EventListener {
  /**
   * The Start/Stop button is clicked.
   * @param e event object
   */
  public void startStopButtonClicked( GameOfLifeControlsEvent e );

  /**
   * The Next button is clicked.
   * @param e event object
   */
  public void nextButtonClicked( GameOfLifeControlsEvent e );

  /**
   * A new speed is selected.
   * @param e event object
   */
  public void speedChanged( GameOfLifeControlsEvent e );

  /**
   * A new cell size is selected.
   * @param e event object
   */
  public void zoomChanged( GameOfLifeControlsEvent e );

  /**
   * A new shape is selected.
   * @param e event object
   */
  public void shapeSelected( GameOfLifeControlsEvent e );
}

















/**
 * Copyright 1996-2004 Edwin Martin <edwin@bitstorm.nl>
 * @author Edwin Martin
 */

package org.bitstorm.gameoflife;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Contains the cellgrid, the current shape and the Game Of Life algorithm that changes it.
 *
 * @author Edwin Martin
 */
public class GameOfLifeGrid implements CellGrid {
  private int cellRows;
  private int cellCols;
  private int generations;
  private static Shape[] shapes;
  /**
   * Contains the current, living shape.
   * It's implemented as a hashtable. Tests showed this is 70% faster than Vector.
   */
  private Hashtable currentShape;
  private Hashtable nextShape;
  /**
   * Every cell on the grid is a Cell object. This object can become quite large.
   */
  private Cell[][] grid;

  /**
   * Contructs a GameOfLifeGrid.
   * 
   * @param cellCols number of columns
   * @param cellRows number of rows
   */
  public GameOfLifeGrid(int cellCols, int cellRows) {
    this.cellCols = cellCols;
    this.cellRows = cellRows;
    currentShape = new Hashtable();
    nextShape = new Hashtable();

    grid = new Cell[cellCols][cellRows];
    for ( int c=0; c<cellCols; c++)
      for ( int r=0; r<cellRows; r++ )
        grid[c][r] = new Cell( c, r );
  }

  /**
   * Clears grid.
   */
  public synchronized void clear() {
    generations = 0;
    currentShape.clear();
    nextShape.clear();
  }

  /**
   * Create next generation of shape.
   */
  public synchronized void next() {
    Cell cell;
    int col, row;
    int neighbours;
    Enumeration enum;

    generations++;
    nextShape.clear();

    // Reset cells
    enum = currentShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      cell.neighbour = 0;
    }
    // Add neighbours
    // You can't walk through an hashtable and also add elements. Took me a couple of ours to figure out. Argh!
    // That's why we have a hashNew hashtable.
    enum = currentShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      col = cell.col;
      row = cell.row;
      addNeighbour( col-1, row-1 );
      addNeighbour( col, row-1 );
      addNeighbour( col+1, row-1 );
      addNeighbour( col-1, row );
      addNeighbour( col+1, row );
      addNeighbour( col-1, row+1 );
      addNeighbour( col, row+1 );
      addNeighbour( col+1, row+1 );
    }
    
    // Bury the dead
    // We are walking through an enum from we are also removing elements. Can be tricky.
    enum = currentShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      // Here is the Game Of Life rule (1):
      if ( cell.neighbour != 3 && cell.neighbour != 2 ) {
        currentShape.remove( cell );
      }
    }
    // Bring out the new borns
    enum = nextShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      // Here is the Game Of Life rule (2):
      if ( cell.neighbour == 3 ) {
        setCell( cell.col, cell.row, true );
      }
    }
  }
  
  /**
   * Adds a new neighbour to a cell.
   * 
   * @param col Cell-column
   * @param row Cell-row
   */
  public synchronized void addNeighbour(int col, int row) {
    try {
      Cell cell = (Cell)nextShape.get( grid[col][row] );
      if ( cell == null ) {
        // Cell is not in hashtable, then add it
        Cell c = grid[col][row];
        c.neighbour = 1;
        nextShape.put(c, c);
      } else {
        // Else, increments neighbour count
        cell.neighbour++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      // ignore
    }
  }
  
  /**
   * Get enumeration of Cell's
   * @see org.bitstorm.gameoflife.CellGrid#getEnum()
   */
  public Enumeration getEnum() {
    return currentShape.keys();
  }

  /**
   * Get value of cell.
   * @param col x-coordinate of cell
   * @param row y-coordinate of cell
   * @return value of cell
   */
  public synchronized boolean getCell( int col, int row ) {
    try {
      return currentShape.containsKey(grid[col][row]);
    } catch (ArrayIndexOutOfBoundsException e) {
      // ignore
    }
    return false;
  }

  /**
   * Set value of cell.
   * @param col x-coordinate of cell
   * @param row y-coordinate of cell
   * @param c value of cell
   */
  public synchronized void setCell( int col, int row, boolean c ) {
    try {
      Cell cell = grid[col][row];
      if ( c ) {
        currentShape.put(cell, cell);
      } else {
        currentShape.remove(cell);
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      // ignore
    }
  }
  
  /**
   * Get number of generations.
   * @return number of generations
   */
  public int getGenerations() {
    return generations;
  }
  
  /**
   * Get dimension of grid.
   * @return dimension of grid
   */
  public Dimension getDimension() {
    return new Dimension( cellCols, cellRows );
  }

  /**
   * Resize grid. Reuse existing cells.
   * @see org.bitstorm.gameoflife.CellGrid#resize(int, int)
   */
  public synchronized void resize(int cellColsNew, int cellRowsNew) {
    if ( cellCols==cellColsNew && cellRows==cellRowsNew )
      return; // Not really a resize

    // Create a new grid, reusing existing Cell's
    Cell[][] gridNew = new Cell[cellColsNew][cellRowsNew];
    for ( int c=0; c<cellColsNew; c++)
      for ( int r=0; r<cellRowsNew; r++ )
        if ( c < cellCols && r < cellRows )
          gridNew[c][r] = grid[c][r];
        else
          gridNew[c][r] = new Cell( c, r );

    // Copy existing shape to center of new shape
    int colOffset = (cellColsNew-cellCols)/2;
    int rowOffset = (cellRowsNew-cellRows)/2;
    Cell cell;
    Enumeration enum;
    nextShape.clear();
    enum = currentShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      int colNew = cell.col + colOffset;
      int rowNew = cell.row + rowOffset;
      try {
        nextShape.put( gridNew[colNew][rowNew], gridNew[colNew][rowNew] );
      } catch ( ArrayIndexOutOfBoundsException e ) {
        // ignore
      }
    }

    // Copy new grid and hashtable to working grid/hashtable
    grid = gridNew;
    currentShape.clear();
    enum = nextShape.keys();
    while ( enum.hasMoreElements() ) {
      cell = (Cell) enum.nextElement();
      currentShape.put( cell, cell );
    }
    
    cellCols = cellColsNew;
    cellRows = cellRowsNew;
  }
}



















import java.awt.Dimension;
import java.util.Enumeration;

/**
 * Shape contains data of one (predefined) shape.
 *
 * @author Edwin Martin
 */
public class Shape {
  private final String name;
  private final int[][] shape;
  
  /**
   * Constructa a Shape.
   * @param name name of shape
   * @param shape shape data
   */
  public Shape( String name, int[][] shape ) {
    this.name = name;
    this.shape = shape;
  }
  
  /**
   * Get dimension of shape.
   * @return dimension of the shape in cells
   */
  public Dimension getDimension() {
    int shapeWidth = 0;
    int shapeHeight = 0;
    for (int cell = 0; cell < shape.length; cell++) {
      if (shape[cell][0] > shapeWidth)
        shapeWidth = shape[cell][0];
      if (shape[cell][1] > shapeHeight)
        shapeHeight = shape[cell][1];
    }
    shapeWidth++;
    shapeHeight++;
    return new Dimension( shapeWidth, shapeHeight );
  }
  
  /**
   * Get name of shape.
   * @return name of shape
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get shape data.
   * Hide the shape implementation. Returns a anonymous Enumerator object.
   * @return enumerated shape data
   */
  public Enumeration getCells() {
    return new Enumeration() {
      private int index=0;
      public boolean hasMoreElements() {
        return index < shape.length;
      }
      public Object nextElement() {
        return shape[index++];
      }
    };
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return name+" ("+shape.length+" cell"+(shape.length==1?"":"s")+")";
  }
}



















public class ShapeCollection {
	  private static final Shape CLEAR;
	  private static final Shape GLIDER;
	  private static final Shape SMALLEXPL;
	  private static final Shape EXPLODER;
	  private static final Shape CELL10;
	  private static final Shape FISH;
	  private static final Shape PUMP;
	  private static final Shape SHOOTER;
	  private static final Shape[] COLLECTION;

	  static {
	    CLEAR = new Shape("Clear", new int[][] {} );
	    GLIDER = new Shape("Glider", new int[][] {{1,0}, {2,1}, {2,2}, {1,2}, {0,2}});
	    SMALLEXPL = new Shape("Small Exploder", new int[][] {{0,1}, {0,2}, {1,0}, {1,1}, {1,3}, {2,1}, {2,2}});
	    EXPLODER = new Shape("Exploder", new int[][] {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}, {2,0}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}});
	    CELL10 = new Shape("10 Cell Row", new int[][] {{0,0}, {1,0}, {2,0}, {3,0}, {4,0}, {5,0}, {6,0}, {7,0}, {8,0}, {9,0}});
	    FISH = new Shape("Lightweight spaceship", new int[][] {{0,1}, {0,3}, {1,0}, {2,0}, {3,0}, {3,3}, {4,0}, {4,1}, {4,2}});
	    PUMP = new Shape("Tumbler", new int[][] {{0,3}, {0,4}, {0,5}, {1,0}, {1,1}, {1,5}, {2,0}, {2,1}, {2,2}, {2,3}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}, {5,0}, {5,1}, {5,5}, {6,3}, {6,4}, {6,5}});
	    SHOOTER = new Shape("Gosper Glider Gun", new int[][] {{0,2}, {0,3}, {1,2}, {1,3}, {8,3}, {8,4}, {9,2}, {9,4}, {10,2}, {10,3}, {16,4}, {16,5}, {16,6}, {17,4}, {18,5}, {22,1}, {22,2}, {23,0}, {23,2}, {24,0}, {24,1}, {24,12}, {24,13}, {25,12}, {25,14}, {26,12}, {34,0}, {34,1}, {35,0}, {35,1}, {35,7}, {35,8}, {35,9}, {36,7}, {37,8}});
	    COLLECTION = new Shape[] {CLEAR, GLIDER, SMALLEXPL, EXPLODER, CELL10, FISH, PUMP, SHOOTER};
	  }

	  /**
	   * Get array of shapes.
	   * 
	   * It's not tamper-proof, but that's okay.
	   * @return collection of shapes
	   */
	  public static Shape[] getShapes() {
	    return COLLECTION;
	  }
	  
	  /**
	   * Get shape by its name.
	   * @param name name of shape
	   * @return shape object
	   * @throws ShapeException if no shape with this name exist
	   */
	  public static Shape getShapeByName( String name ) throws ShapeException {
	    Shape[] shapes = getShapes();
	    for ( int i = 0; i < shapes.length; i++ ) {
	      if ( shapes[i].getName().equals( name )  )
	        return shapes[i];
	    }
	    throw ( new ShapeException("Unknown shape: "+name) );
	  }
	}













public class ShapeException extends Exception {
	  /**
	   * Constructs a ShapeException.
	   */
	  public ShapeException() {
	    super();
	  }
	  /**
	   * Constructs a ShapeException with a description.
	   */
	  public ShapeException( String s ) {
	    super( s );
	  }
	}














import java.applet.Applet;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The Game Of Life Applet.
 * This is the heart of the program. It initializes everything en put it together.
 * @author Edwin Martin
 */
public class GameOfLife extends Applet implements Runnable, GameOfLifeControlsListener {
  protected CellGridCanvas gameOfLifeCanvas;
  protected GameOfLifeGrid gameOfLifeGrid;
  protected int cellSize;
  protected int cellCols;
  protected int cellRows;
  protected int genTime;
  protected GameOfLifeControls controls;
  protected static Thread gameThread = null;

  /**
   * Initialize UI.
   * @see java.applet.Applet#init()
   */
  public void init() {
    getParams();

    // set background colour
    setBackground(new Color(0x999999));

    // create gameOfLifeGrid
    gameOfLifeGrid = new GameOfLifeGrid(cellCols, cellRows);
    gameOfLifeGrid.clear();

    // create GameOfLifeCanvas
    gameOfLifeCanvas = new CellGridCanvas(gameOfLifeGrid, cellSize);

    // create GameOfLifeControls
    controls = new GameOfLifeControls();
    controls.addGameOfLifeControlsListener( this );

    // put it all together
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        GridBagConstraints canvasContraints = new GridBagConstraints();

        canvasContraints.fill = GridBagConstraints.BOTH;
        canvasContraints.gridx = GridBagConstraints.REMAINDER;
        canvasContraints.gridy = 0;
        canvasContraints.weightx = 1;
        canvasContraints.weighty = 1;
        canvasContraints.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(gameOfLifeCanvas, canvasContraints);
        add(gameOfLifeCanvas);

        GridBagConstraints controlsContraints = new GridBagConstraints();
        canvasContraints.gridy = 1;
        canvasContraints.gridx = 0;
        controlsContraints.gridx = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(controls, controlsContraints);
        add(controls);
    
        try {
      // Start with a shape (My girlfriend clicked "Start" on a blank screen and wondered why nothing happened).
      setShape( ShapeCollection.getShapeByName( "Glider" ) );
    } catch (ShapeException e) {
      // Ignore. Not going to happen.
    }
    setVisible(true);
    validate();
  }
  
  /**
   * Get params (cellSize, cellCols, cellRows, genTime) from applet-tag
   */
  protected void getParams() {
    cellSize = getParamInteger( "cellsize", 11 );
    cellCols = getParamInteger( "cellcols", 50 );
    cellRows = getParamInteger( "cellrows", 30 );
    genTime  = getParamInteger( "gentime", 1000 );
  }
  
  /**
   * Read applet parameter (int) or, when unavailable, get default value.
   * @param name name of parameter
   * @param defaultParam default when parameter is unavailable
   * @return value of parameter
   */
  protected int getParamInteger( String name, int defaultParam ) {
    String param;
    int paramInt;

    param = getParameter( name );
    if ( param == null )
      paramInt = defaultParam;
    else
      paramInt = Integer.valueOf(param).intValue();
    return paramInt;
  }

  /**
   * Starts creating new generations.
   * No start() to prevent starting immediately.
   */
  public synchronized void start2() {
    controls.start();
    if (gameThread == null) {
      gameThread = new Thread(this);
      gameThread.start();
    }
  }

  /**
   * @see java.applet.Applet#stop()
   */
  public void stop() {
    controls.stop();
    gameThread = null;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public synchronized void run() {
    while (gameThread != null) {
      nextGeneration();
      try {
        Thread.sleep(genTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Is the applet running?
   * @return true: applet is running
   */
  public boolean isRunning() {
    return gameThread != null;
  }
  
  /**
   * Go to the next generation.
   */
  public void nextGeneration() {
    gameOfLifeGrid.next();
    gameOfLifeCanvas.repaint();
    showGenerations();
  }
  
  /**
   * Set the new shape
   * @param shape name of shape
   */
  public void setShape( Shape shape ) {
    if ( shape == null )
      return;

    try {
      gameOfLifeCanvas.setShape( shape );
      reset();
    } catch (ShapeException e) {
      alert( e.getMessage() );
    }
  }
  
  /**
   * Resets applet (after loading new shape)
   */
  public void reset() {
    stop(); // might otherwise confuse user
    gameOfLifeCanvas.repaint();
    showGenerations();
    showStatus( "" );
  }

  /**
   * @see java.applet.Applet#getAppletInfo()
   */
  public String getAppletInfo() {
    return "Game Of Life v. 1.5\nCopyright 1996-2004 Edwin Martin";
  }

  /**
   * Show number of generations.
   */
  private void showGenerations() {
    controls.setGeneration( gameOfLifeGrid.getGenerations() );
  }
  
  /**
   * Set speed of new generations.
   * @param fps generations per second
   */
  public void setSpeed( int fps ) {
    genTime = fps;
  }
  
  /**
   * Sets cell size.
   * @param p size of cell in pixels
   */
  public void setCellSize( int p ) {
    cellSize = p;
    gameOfLifeCanvas.setCellSize( cellSize );
  }
  
  /**
   * Gets cell size.
   * @return size of cell
   */
  public int getCellSize() {
    return cellSize;
  }
  
  /**
   * Shows an alert
   * @param s text to show
   */
  public void alert( String s ) {
    showStatus( s );
  }

  /** Callback from GameOfLifeControlsListener
   * @see org.bitstorm.gameoflife.GameOfLifeControlsListener#startStopButtonClicked(org.bitstorm.gameoflife.GameOfLifeControlsEvent)
   */
  public void startStopButtonClicked( GameOfLifeControlsEvent e ) {
    if ( isRunning() ) {
      stop();
    } else {
      start2();
    }
  }

  /** Callback from GameOfLifeControlsListener
   * @see org.bitstorm.gameoflife.GameOfLifeControlsListener#nextButtonClicked(org.bitstorm.gameoflife.GameOfLifeControlsEvent)
   */
  public void nextButtonClicked(GameOfLifeControlsEvent e) {
    nextGeneration();
  }

  /** Callback from GameOfLifeControlsListener
   * @see org.bitstorm.gameoflife.GameOfLifeControlsListener#speedChanged(org.bitstorm.gameoflife.GameOfLifeControlsEvent)
   */
  public void speedChanged(GameOfLifeControlsEvent e) {
    setSpeed( e.getSpeed() );
  }

  /** Callback from GameOfLifeControlsListener
   * @see org.bitstorm.gameoflife.GameOfLifeControlsListener#speedChanged(org.bitstorm.gameoflife.GameOfLifeControlsEvent)
   */
  public void zoomChanged(GameOfLifeControlsEvent e) {
    setCellSize( e.getZoom() );
  }

  /** Callback from GameOfLifeControlsListener
   * @see org.bitstorm.gameoflife.GameOfLifeControlsListener#shapeSelected(org.bitstorm.gameoflife.GameOfLifeControlsEvent)
   */
  public void shapeSelected(GameOfLifeControlsEvent e) {
    String shapeName = (String) e.getShapeName();
    Shape shape;
    try {
      shape = ShapeCollection.getShapeByName( shapeName );
      setShape( shape );
    } catch (ShapeException e1) {
      // Ignore. Not going to happen.
    }
  }
}
















import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.bitstorm.util.AboutDialog;
import org.bitstorm.util.AlertBox;
import org.bitstorm.util.EasyFile;
import org.bitstorm.util.LineEnumerator;
import org.bitstorm.util.TextFileDialog;

/**
 * Turns GameOfLife applet into application.
 * It adds a menu, a window, drag-n-drop etc.
 * It can be run stand alone.
 * 
 * @author Edwin Martin
 */
public class StandaloneGameOfLife extends GameOfLife {
  private Frame appletFrame;
  private String[] args;
  private GameOfLifeGridIO gridIO;
    /**
     * main() for standalone version.
   * @param args Not used.
   */
  public static void main(String args[]) {
    StandaloneGameOfLife gameOfLife = new StandaloneGameOfLife();
    gameOfLife.args = args;
    new AppletFrame( "Game of Life", gameOfLife );
  }

  /**
   * Initialize UI.
   * @param parent Parent frame.
   * @see java.applet.Applet#init()
   */
  public void init( Frame parent ) {
    appletFrame = parent;
    getParams();

    // set background colour
    setBackground(new Color(0x999999));

    // TODO: casten naar interface
    // create StandAloneGameOfLifeGrid
    gameOfLifeGrid = new GameOfLifeGrid( cellCols, cellRows);
    gridIO = new GameOfLifeGridIO( gameOfLifeGrid );

    // create GameOfLifeCanvas
    gameOfLifeCanvas = new CellGridCanvas(gameOfLifeGrid, cellSize);

    try {
      // Make GameOfLifeCanvas a drop target
      DropTarget dt = new DropTarget( gameOfLifeCanvas, DnDConstants.ACTION_COPY_OR_MOVE, new MyDropListener() );
    } catch (NoClassDefFoundError e) {
      // Ignore. Older Java version don't support dnd
    }

    // create GameOfLifeControls
    controls = new GameOfLifeControls();
    controls.addGameOfLifeControlsListener( this );

    // put it all together
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints canvasContraints = new GridBagConstraints();
        setLayout(gridbag);
        canvasContraints.fill = GridBagConstraints.BOTH;
        canvasContraints.weightx = 1;
        canvasContraints.weighty = 1;
        canvasContraints.gridx = GridBagConstraints.REMAINDER;
        canvasContraints.gridy = 0;
        canvasContraints.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(gameOfLifeCanvas, canvasContraints);
        add(gameOfLifeCanvas);
        GridBagConstraints controlsContraints = new GridBagConstraints();
        canvasContraints.gridx = GridBagConstraints.REMAINDER;
        canvasContraints.gridy = 1;
        controlsContraints.gridx = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(controls, controlsContraints);
        add(controls);
    setVisible(true);
    validate();
  }

  /**
   * Set the shape.
   * 
   * This is not done in init(), because the window resize in GameOfLifeGridIO.setShape(Shape)
   * needs a fully opened window to do new size calculations.
   */
  public void readShape() {   
      if ( args.length > 0 ) {
        gridIO.openShape(args[0]);
      reset();
      } else {
      try {
        setShape( ShapeCollection.getShapeByName( "Glider" ) );
      } catch (ShapeException e) {
        // Ignore. It's not going to happen here.
      }
      }
  }

    /**
     * Override method, called by applet.
   * @see java.applet.Applet#getParameter(java.lang.String)
   */
  public String getParameter( String parm ) {
        return System.getProperty( parm );
    }

  /**
   * Shows an alert
   * @param s text to show
   */
  public void alert( String s ) {
    new AlertBox( appletFrame, "Alert", s );
  }
  
  /**
   * Do not use showStatus() of the applet.
   * @see java.applet.Applet#showStatus(java.lang.String)
   */
  public void showStatus( String s ) {
    // do nothing
  }
  
  /**
   * get GameOfLifeGridIO
   * @return GameOfLifeGridIO object
   */
  protected GameOfLifeGridIO getGameOfLifeGridIO() {
    return gridIO;
  }

  /**
   * Handles drag and drops to the canvas.
   * 
   * This class does handle the dropping of files and URL's to the canvas.
   * The code is based on the dnd-code from the book Professional Java Programming by Brett Spell.
   * 
   * @author Edwin Martin
   *
   */
  class MyDropListener implements DropTargetListener {
    private final DataFlavor urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL", "Game of Life URL");

    /**
     * The canvas only supports Files and URL's
     * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragEnter(DropTargetDragEvent event) {
      if ( event.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) || event.isDataFlavorSupported( urlFlavor ) ) {
        return;
      }
      event.rejectDrag();
      }

      /**
       * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
       */
      public void dragExit(DropTargetEvent event) {
      }

      /**
       * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
       */
      public void dragOver(DropTargetDragEvent event) {
      }

      /**
       * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
       */
      public void dropActionChanged(DropTargetDragEvent event) {
      }

      /**
       * The file or URL has been dropped.
       * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
       */
      public void drop(DropTargetDropEvent event) {
        // important to first try urlFlavor
      if ( event.isDataFlavorSupported( urlFlavor ) ) {
        try {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          Transferable trans = event.getTransferable();
          URL url = (URL)( trans.getTransferData( urlFlavor ) );
          String urlStr = url.toString();
          gridIO.openShape( url );
          reset();
          event.dropComplete(true);
        } catch (Exception e) {
          event.dropComplete(false);
        }
      } else if ( event.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
        try {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          Transferable trans = event.getTransferable();
          java.util.List list = (java.util.List)( trans.getTransferData( DataFlavor.javaFileListFlavor ) );
          File droppedFile = (File) list.get(0); // More than one file -> get only first file
          gridIO.openShape( droppedFile.getPath() );
          reset();
          event.dropComplete(true);
        } catch (Exception e) {
          event.dropComplete(false);
        }
      }
    } 
  }

  /**
   * File open and save operations for GameOfLifeGrid.
   */
  class GameOfLifeGridIO {
    public final String FILE_EXTENSION = ".cells";
    private GameOfLifeGrid grid;
    private String filename;

    /**
     * Contructor.
     * @param grid grid to read/write files from/to 
     */
    public GameOfLifeGridIO( GameOfLifeGrid grid ) {
      this.grid = grid;
    }

    /**
     * Load shape from disk
     */
    public void openShape() {
      openShape( (String)null );
    }
    
    /**
     * Load shape from disk
     * @param filename filename to load shape from, or null when no filename given.
     */
    public void openShape( String filename ) {
      int col = 0;
      int row = 0;
      boolean cell;
      // Cope with different line endings ("\r\n", "\r", "\n")
      boolean nextLine = false;
      EasyFile file;
      try {
        if ( filename != null ) {
          file = new EasyFile( filename );
        } else {
          file = new EasyFile( appletFrame, "Open Game of Life file" );
        }
        openShape( file );
      } catch (FileNotFoundException e) {
        new AlertBox( appletFrame, "File not found", "Couldn't open this file.\n"+e.getMessage());
      } catch (IOException e) {
        new AlertBox( appletFrame, "File read error", "Couldn't read this file.\n"+e.getMessage());
      }
    }

    /**
     * Open shape from URL.
     * @param url URL pointing to GameOfLife-file
     */
    public void openShape( URL url ) {
      int col = 0;
      int row = 0;
      boolean cell;
      // Cope with different line endings ("\r\n", "\r", "\n")
      boolean nextLine = false;
      EasyFile file;
      String text;
      try {
        if ( url != null ) {
          file = new EasyFile( url );
          openShape( file );
        }
      } catch (FileNotFoundException e) {
        new AlertBox( appletFrame, "URL not found", "Couldn't open this URL.\n"+e.getMessage());
      } catch (IOException e) {
        new AlertBox( appletFrame, "URL read error", "Couldn't read this URL.\n"+e.getMessage());
      }
    }

    /**
     * Use EasyFile object to read GameOfLife-file from.
     * @param file EasyFile-object
     * @throws IOException
     * @see org.bitstorm.util.EasyFile
     */
    public void openShape( EasyFile file ) throws IOException {
      Shape shape = makeShape( file.getFileName(), file.readText() );
      setShape( shape );
    }
    
    /**
     * Set a shape and optionally resizes window.
     * @param shape Shape to set
     */
    public void setShape( Shape shape ) {
      int width, height;
      Dimension shapeDim = shape.getDimension();
      Dimension gridDim = grid.getDimension();
      if ( shapeDim.width > gridDim.width || shapeDim.height > gridDim.height ) {
        // Window has to be made larger
        Toolkit toolkit = getToolkit();
        Dimension screenDim =  toolkit.getScreenSize();
        Dimension frameDim = appletFrame.getSize();
        int cellSize = getCellSize();
        // Calculate new window size
        width = frameDim.width + cellSize*(shapeDim.width - gridDim.width);
        height = frameDim.height + cellSize*(shapeDim.height - gridDim.height);
        // Does it fit on the screen?
        if ( width > screenDim.width || height > screenDim.height ) {
          // With current cellSize, it doesn't fit on the screen
          // GameOfLifeControls.SIZE_SMALL corresponds with GameOfLifeControls.SMALL
          int newCellSize = GameOfLifeControls.SIZE_SMALL;
          width = frameDim.width + newCellSize*shapeDim.width - cellSize*gridDim.width;
          height = frameDim.height + newCellSize*shapeDim.height - cellSize*gridDim.height;
          // a little kludge to prevent de window from resizing twice
          // setNewCellSize only has effect at the next resize
          gameOfLifeCanvas.setAfterWindowResize( shape, newCellSize );
          // The UI has to be adjusted, too
          controls.setZoom( GameOfLifeControls.SMALL );
        } else {
          // Now resize the window (and optionally set the new cellSize)
          gameOfLifeCanvas.setAfterWindowResize( shape, cellSize );
        }
        if ( width < 400 )
          width = 400;
        appletFrame.setSize( width, height );
        return;
      }
      try {
        gameOfLifeCanvas.setShape( shape );
      } catch (ShapeException e) {
        // ignore
      }
    }
    
    /**
     * "Draw" the shape on the grid. (Okay, it's not really drawing).
     * The lines of text represent the cells of the shape.
     * 
     * @param name name of shape
     * @param text lines of text
     */
    public Shape makeShape( String name, String text ) {
      int col = 0;
      int row = 0;
      boolean cell;
      // Cope with different line endings ("\r\n", "\r", "\n")
      int[][] cellArray;
      Vector cells = new Vector();
      
      if ( text.length() == 0 )
        return null;

      grid.clear();

      Enumeration enum = new LineEnumerator( text );
      while ( enum.hasMoreElements() ) {
        String line = (String) enum.nextElement();
        if ( line.startsWith("#") || line.startsWith("!") )
          continue;
        
        char[] ca = line.toCharArray();
        for ( col=0; col < ca.length; col++ ) {
          switch( ca[col] ) {
            case '*':
            case 'O':
            case 'o':
            case 'X':
            case 'x':
            case '1':
              cell = true;
              break;
            default:
              cell = false;
              break;
          }
          if ( cell )
            cells.addElement(new int[] {col, row});
        }
        row++;
      }

      cellArray = new int[cells.size()][];
      for ( int i=0; i<cells.size(); i++ )
        cellArray[i] = (int[]) cells.get(i);
      return new Shape( name, cellArray );
    }    
    
    /**
     * Write shape to disk.
     */
    public void saveShape() {
      int colEnd = 0;
      int rowEnd = 0;
      Dimension dim = grid.getDimension();
      int colStart = dim.width;
      int rowStart = dim.height;

      String lineSeperator = System.getProperty( "line.separator" );
      StringBuffer text = new StringBuffer("!Generator: Game of Life (http://www.bitstorm.org/gameoflife/)"+lineSeperator+"!Variation: 23/3"+lineSeperator+"!"+lineSeperator);

      for ( int row = 0; row < dim.height; row++ ) {
        for ( int col = 0; col < dim.width; col++ ) {
          if ( grid.getCell( col, row ) ) {
            if ( row < rowStart )
              rowStart = row;
            if ( col < colStart )
              colStart = col;
            if ( row > rowEnd )
              rowEnd = row;
            if ( col > colEnd )
              colEnd = col;
          }
        }
      }
      
      for ( int row = rowStart; row <= rowEnd; row++ ) {
        for ( int col = colStart; col <= colEnd; col++ ) {
          text.append( grid.getCell( col, row ) ? 'O' : '-' );
        }
        text.append( lineSeperator );
      }
      EasyFile file;
      try {
        file = new EasyFile( appletFrame, "Save Game of Life file" );
        file.setFileName( filename );
        file.setFileExtension( FILE_EXTENSION );
        file.writeText( text.toString() );
      } catch (FileNotFoundException e) {
        new AlertBox( appletFrame, "File error", "Couldn't open this file.\n"+e.getMessage());
      } catch (IOException e) {
        new AlertBox( appletFrame, "File error", "Couldn't write to this file.\n"+e.getMessage());
      }
    }
  }
}

/**
 * The window with the applet. Extra is the menu bar.
 *
 * @author Edwin Martin
 */
class AppletFrame extends Frame {
  private final GameOfLife applet;
    /**
     * Constructor.
   * @param title title of window
   * @param applet applet to show
   */
  public AppletFrame(String title, StandaloneGameOfLife applet) {
        super( title );
    this.applet = applet;

    URL iconURL = this.getClass().getResource("icon.gif");
    Image icon = Toolkit.getDefaultToolkit().getImage( iconURL );
    this.setIconImage( icon );

    enableEvents(Event.WINDOW_DESTROY);
    
        MenuBar menubar = new MenuBar();
        Menu fileMenu = new Menu("File", true);
    MenuItem readMenuItem = new MenuItem( "Open...");
    readMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          getStandaloneGameOfLife().getGameOfLifeGridIO().openShape();
          getStandaloneGameOfLife().reset();
        }
      }

    );
    MenuItem writeMenuItem = new MenuItem( "Save...");
    writeMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          getStandaloneGameOfLife().getGameOfLifeGridIO().saveShape();
        }
      }

    );
    MenuItem quitMenuItem = new MenuItem( "Exit");
    quitMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
      }

    );
    Menu helpMenu = new Menu("Help", true);
    MenuItem manualMenuItem = new MenuItem( "Manual");
    manualMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          showManualDialog();
        }
      }
    );
    MenuItem licenseMenuItem = new MenuItem( "License");
    licenseMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          showLicenseDialog();
        }
      }
    );
     MenuItem aboutMenuItem = new MenuItem( "About");
    aboutMenuItem.addActionListener(
      new ActionListener() {
        public synchronized void actionPerformed(ActionEvent e) {
          showAboutDialog();
        }
      }
    );
        fileMenu.add(readMenuItem);
    fileMenu.add(writeMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(quitMenuItem);
    helpMenu.add(manualMenuItem);
    helpMenu.add(licenseMenuItem);
    helpMenu.add(aboutMenuItem);
        menubar.add(fileMenu);
    menubar.add(helpMenu);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints appletContraints = new GridBagConstraints();
        setLayout(gridbag);
        appletContraints.fill = GridBagConstraints.BOTH;
        appletContraints.weightx = 1;
        appletContraints.weighty = 1;
        gridbag.setConstraints(applet, appletContraints);
        setMenuBar(menubar);
    setResizable(true);
        add(applet);
        Toolkit screen = getToolkit();
        Dimension screenSize = screen.getScreenSize();
        // Java in Windows opens windows in the upper left corner, which is ugly! Center instead.
        if ( screenSize.width >= 640 && screenSize.height >= 480 )
          setLocation((screenSize.width-550)/2, (screenSize.height-400)/2);
    applet.init( this );
    applet.start();
    pack();
    // Read shape after initialization
    applet.readShape();
    // Bring to front. Sometimes it stays behind other windows.
        show();
    toFront();
    }

  /**
   * Process close window button.
   * @see java.awt.Component#processEvent(java.awt.AWTEvent)
   */
  public void processEvent( AWTEvent e ) {
    if ( e.getID() == Event.WINDOW_DESTROY )
      System.exit(0);
  }

    /**
   * Show about dialog.
   */
  private void showAboutDialog() {
    Properties properties = System.getProperties();
    String jvmProperties = "Java VM "+properties.getProperty("java.version")+" from "+properties.getProperty("java.vendor");
    Point p = getLocation();
        new AboutDialog( this, "About the Game of Life", new String[] {"Version 1.5 - Copyright 1996-2004 Edwin Martin", "http://www.bitstorm.org/gameoflife/", jvmProperties}, "about.jpg", p.x+100, p.y+60 );
    }
    
  /**
   * Show manual.
   */
  private void showManualDialog() {
    Point p = getLocation();
    new TextFileDialog( this, "Game of Life Manual", "manual.txt",  p.x+60, p.y+60 );
  }

  /**
   * Show license.
   */
  private void showLicenseDialog() {
    Point p = getLocation();
    new TextFileDialog( this, "Game of Life License", "license.txt", p.x+60, p.y+60 );
  }

  /**
   * Get StandaloneGameOfLife object.
   *
   * @return StandaloneGameOfLife
   */
  private StandaloneGameOfLife getStandaloneGameOfLife() {
    return (StandaloneGameOfLife) applet;
  }
}

	
}
