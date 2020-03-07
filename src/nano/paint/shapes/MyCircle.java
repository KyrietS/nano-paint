package nano.paint.shapes;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import nano.paint.editor.Editor;

/**
 * Klasa reprezentująca figurę koła.
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyShape
 */
public class MyCircle extends Circle implements MyShape
{
    /** Pozycja myszy X, gdzie rozpoczęto Drag na figurze */
    private int mouseX;
    /** Pozycja myszy Y, gdzie rozpoczęto Drag na figurze */
    private int mouseY;
    /** minimalna długość promienia */
    private final int MIN_SIZE = 20;
    /** Czy figura jest aktualnie zaznaczona */
    private boolean isFocused = false;
    /** Zbiór elementów przypisanych do planszy */
    private ObservableList<Node> shapes;
    /** Czy figura jest aktualnie ciągnięta */
    private boolean isDragged = false;

    /**
     * Podstawowy konstruktor wykorzystywany przez {@link Editor}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param x Pozycja początkowa X
     * @param y Pozycja początkowa Y
     */
    public MyCircle( ObservableList<Node> shapes, int x, int y )
    {
        setCenterX( x );
        setCenterY( y );
        setRadius( MIN_SIZE );
        setFill( Color.DODGERBLUE );
        initialize( shapes );
    }

    /**
     * Konstruktor odczytujący ustawienia z tekstu, które wygenerowała funkcja {@link MyCircle#saveToString()}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param data Tekst z danymi figury wygenerowany przez {@link MyCircle#saveToString()}
     */
    public MyCircle( ObservableList<Node> shapes, String data )
    {
        String[] values = data.split( " " );
        if( values.length != 8 || !values[ 0 ].equals( "c" ) )
            throw new RuntimeException();

        Color color = new Color( Double.parseDouble( values[ 1 ] ), Double.parseDouble( values[ 2 ] ),
                Double.parseDouble( values[ 3 ] ), Double.parseDouble( values[ 4 ] ) );

        setFill( color );
        setCenterX( Double.parseDouble( values[ 5 ] ) );
        setCenterY( Double.parseDouble( values[ 6 ] ) );
        setRadius( Double.parseDouble( values[ 7 ] ) );

        initialize( shapes );
    }

    /**
     * Ustawia wartości początkowe figury.
     * @param shapes Kontener, do którego figura się dopisze.
     */
    private void initialize( ObservableList<Node> shapes )
    {
        this.shapes = shapes;
        setStrokeWidth( 1 );
        setStroke( Color.BLACK );
        this.shapes.add( this );

        addEventHandler( MouseEvent.MOUSE_DRAGGED, this::move );
        addEventHandler( MouseEvent.MOUSE_PRESSED, this::mousePressed );
        addEventHandler( MouseEvent.MOUSE_RELEASED, e -> mouseReleased() );
    }

    /**
     * Ustawia nowy promień figury.
     * Funkcja dba o to, aby promień nie był mniejszy niż określono w {@link MyCircle#MIN_SIZE}.
     * @param radius Nowy promień
     */
    public void newRadius( double radius )
    {
        if( radius < MIN_SIZE )
            radius = MIN_SIZE;
        setRadius( (int)radius );
    }

    /** {@inheritDoc} */
    @Override
    public boolean getFocused()
    {
        return isFocused;
    }

    /** {@inheritDoc} */
    @Override
    public void makeFocused( boolean isCtrlDown )
    {
        if( !isCtrlDown )
        {
            Editor.clearFocus();
            // Jeśli figura nie jest na wierzchu, to ma być.
            if( shapes.indexOf( this ) != shapes.size()-1 )
            {
                shapes.remove( this );
                shapes.add( this );
            }
        }

        setStrokeWidth( 3 );

        isFocused = true;
    }

    /** {@inheritDoc} */
    @Override
    public void rescale( double scale )
    {
        if( scale > 1.0 )
            setRadius( getRadius()*scale );
        else if( getRadius() * scale > MIN_SIZE )
            setRadius( getRadius() * scale );
    }

    /** {@inheritDoc} */
    @Override
    public void remove()
    {
        Editor.console.write( "Usunięto koło" );
        shapes.remove( this );
    }

    /** {@inheritDoc} */
    @Override
    public void removeFocused()
    {
        isFocused = false;
        setStrokeWidth( 1 );
    }

    /** {@inheritDoc} */
    @Override
    public void changeColor( Color color )
    {
        setFill( color );
    }

    /** {@inheritDoc} */
    @Override
    public String saveToString()
    {
        double r = ( (Color) getFill() ).getRed();
        double g = ( (Color) getFill() ).getGreen();
        double b = ( (Color) getFill() ).getBlue();
        double opacity = ( (Color) getFill() ).getOpacity();
        String saveString = "c " + r + " " + g + " " + b + " " + opacity + " "
                + getCenterX() + " " + getCenterY() + " " + getRadius();
        return saveString;
    }

    /**
     * Przesuwa figurę w nową pozycję, na której znajduje się kursor.
     * @param e Zdarzenie przesunięcia myszy.
     */
    private void move( MouseEvent e )
    {
        if( !isDragged )
        {
            Editor.console.write( "Przemieszczanie koła..." );
            Editor.console.write( "Pozycja początkowa: " + (int) getCenterX() + ", " + (int) getCenterY() + ")" );
        }
        isDragged = true;

        if( e.getX() >= 0 && e.getX() <= 500 )
            setCenterX( e.getX() - mouseX );
        if( e.getY() >= 0 && e.getY() <= 500 )
            setCenterY( e.getY() - mouseY );
    }

    /**
     * Obsługuje kliknięcie myszą w figurę
     * @param e Zdarzenie kliknięcia myszą.
     */
    private void mousePressed( MouseEvent e )
    {
        mouseX = (int)(e.getX() - getCenterX());
        mouseY = (int)(e.getY() - getCenterY());

        makeFocused( e.isControlDown() );
    }

    /**
     * Obsługuje pyszczenie przycisku po wcześniejszym przeciąganiu.
     */
    private void mouseReleased()
    {
        if( isDragged )
        {
            Editor.console.write( "Pozycja końcowa: (" + (int)getCenterX() + ", " + (int)getCenterY() + ")" );
            isDragged = false;
        }
    }
}
