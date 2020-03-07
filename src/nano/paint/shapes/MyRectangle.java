package nano.paint.shapes;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import nano.paint.editor.Editor;

/**
 * Klasa reprezentująca figurę prostokąta (w szczególnym przypadku kwadratu).
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyShape
 */
public class MyRectangle extends Rectangle implements MyShape
{
    /** Współrzędna x początku kwadratu */
    private int x1;
    /** Współrzędna y początku kwadratu */
    private int y1;
    /** Współrzędna x końca kwadratu */
    private int x2;
    /** Współrzędna y końca kwadratu */
    private int y2;

    /** Czy figura jest aktualnie zaznaczona */
    private boolean isFocused = false;

    /** Pozycja myszy X, gdzie rozpoczęto Drag na figurze */
    private int mouseX;
    /** Pozycja myszy Y, gdzie rozpoczęto Drag na figurze */
    private int mouseY;

    /** Minimalny rozmiar boku prostokąta */
    private final int MIN_SIZE = 20;
    /** Zbiór elementów przypisanych do planszy */
    private ObservableList<Node> shapes;
    /** Czy figura jest aktualnie ciągnięta */
    private boolean isDragged = false;

    /**
     * Podstawowy konstruktor wykorzystywany przez {@link Editor}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param x1 Pozycja początkowa X
     * @param y1 Pozycja początkowa Y
     */
    public MyRectangle( ObservableList<Node> shapes, int x1, int y1 )
    {
        setBegin( x1, y1 );
        setEnd( x1, y1, false );
        setFill( Color.DODGERBLUE );

        initialize( shapes );
    }

    /**
     * Konstruktor odczytujący ustawienia z tekstu, które wygenerowała funkcja {@link MyRectangle#saveToString()}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param data Tekst z danymi figury wygenerowany przez {@link MyRectangle#saveToString()}
     */
    public MyRectangle( ObservableList<Node> shapes, String data )
    {
        String[] values = data.split( " " );
        if( values.length != 9 || !values[ 0 ].equals( "r" ) )
            throw new RuntimeException();

        Color color = new Color( Double.parseDouble( values[ 1 ] ), Double.parseDouble( values[ 2 ] ),
                Double.parseDouble( values[ 3 ] ), Double.parseDouble( values[ 4 ] ) );

        setFill( color );
        setX( Double.parseDouble( values[ 5 ] ) );
        setY( Double.parseDouble( values[ 6 ] ) );
        setWidth( Double.parseDouble( values[ 7 ] ) );
        setHeight( Double.parseDouble( values[ 8 ] ) );

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
        shapes.add( this );

        addEventHandler( MouseEvent.MOUSE_DRAGGED, this::move );
        addEventHandler( MouseEvent.MOUSE_PRESSED, this::mousePressed );
        addEventHandler( MouseEvent.MOUSE_RELEASED, e -> mouseReleased() );
    }

    /** {@inheritDoc} */
    @Override
    public void changeColor( Color color )
    {
        setFill( color );
    }

    /** {@inheritDoc} */
    @Override
    public boolean getFocused()
    {
        return isFocused;
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
        {
            setWidth( getWidth()*scale );
            setHeight( getHeight()*scale );
        }
        else if( getWidth() * scale > MIN_SIZE && getHeight() * scale > MIN_SIZE )
        {
            setWidth( getWidth()*scale );
            setHeight( getHeight()*scale );
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove()
    {
        Editor.console.write( "Usunięto prostokąt" );
        shapes.remove( this );
    }

    /** {@inheritDoc} */
    @Override
    public String saveToString()
    {
        double r = ( (Color) getFill() ).getRed();
        double g = ( (Color) getFill() ).getGreen();
        double b = ( (Color) getFill() ).getBlue();
        double opacity = ( (Color) getFill() ).getOpacity();
        String saveString = "r " + r + " " + g + " " + b + " " + opacity + " "
                + getX() + " " + getY() + " " + getWidth() + " " + getHeight();
        return saveString;
    }

    /**
     * Zwraca współrzędną X punktu po skosie od miejsca dodania figury.
     * @return współrzędna X punktu x2
     */
    public int getX2()
    {
        if( x2 < x1 )
            return (int)(x1 - getWidth());
        else
            return (int)(x1 + getWidth());
    }
    /**
     * Zwraca współrzędną Y punktu po skosie od miejsca dodania figury.
     * @return współrzędna Y punktu y2
     */
    public int getY2()
    {
        if( y2 < y1 )
            return (int)(y1 - getHeight());
        else
            return (int)(y1 + getHeight());
    }

    /**
     * Ustawia pozycję górnego, lewego rogu figury.
     * Używane w przypadku, gdy szerokość albo wysokość figury stanie
     * się ujemna. Wtedy należy przesunąć lewy górny róg.
     * @param x nowa pozycja X
     * @param y nowa pozycja Y
     */
    private void setBegin( double x, double y )
    {
        x1 = (int) x;
        y1 = (int) y;
        setBounds( false );
    }

    /**
     * Ustawia pozycję końcową prostokąta.
     * W przeciwieństiwe do {@link MyRectangle#setBegin(double, double) setBegin}
     * punkt końcowy znajduje się zawsze tam, gdzie wskazuje kursor myszy, za wyjątkiem
     * sytuacji, kiedy prostokąt ma być kwadratem. Wtedy jego boki skalują się odpowiednio,
     * aby być wyrównane.
     * @param x pozycja X punktu końcowego
     * @param y pozycja Y punktu końcowego
     * @param isSquare określa czy prostokąt ma być kwadratem
     */
    public void setEnd( double x, double y, boolean isSquare )
    {
        x2 = (int) x;
        y2 = (int) y;
        setBounds( isSquare );
    }

    /**
     * Ustawia poprawne współrzędne dla figury oraz określa długość i szerokość.
     * Funkcja ta zapewnia poprawne wyświetlanie prostokąta, upewnia się, że nie będzie
     * on mniejszy niż określono w {@link MyRectangle#MIN_SIZE MIN_SIZE}.
     * @param isSquare Określa, czy figura ma być traktowana jak kwadrat
     */
    private void setBounds( boolean isSquare )
    {
        int x = x1;
        int y = y1;
        int width = Math.abs(x2 - x1);
        if( width < MIN_SIZE )
            width = MIN_SIZE;
        int height = Math.abs(y2 - y1);
        if( height < MIN_SIZE )
            height = MIN_SIZE;

        if( isSquare )
        {
            if( width < height )
                height = width;
            else
                width = height;
        }

        if( x2 < x1 )
        {
            if( isSquare )
                x = x1 - width;
            else if( x1 - x2 < MIN_SIZE )
                x = x1 - MIN_SIZE;
            else
                x = x2;
        }
        if( y2 < y1 )
        {
            if( isSquare )
            {
                y = y1 - height;
            }
            else if( y1 - y2 < MIN_SIZE )
                y = y1 - MIN_SIZE;
            else
                y = y2;
        }

        super.setX( x );
        super.setY( y );
        super.setWidth( width );
        super.setHeight( height );
    }

    /**
     * Przesuwa figurę w nową pozycję, na której znajduje się kursor.
     * @param e Zdarzenie przesunięcia myszy.
     */
    private void move( MouseEvent e )
    {
        if( !isDragged )
        {
            Editor.console.write( "Przemieszczanie prostokąta..." );
            Editor.console.write( "Pozycja początkowa: " + (int) getX() + ", " + (int) getY() + ")" );
        }
        isDragged = true;
        if( e.getX() >= 0 && e.getX() <= 500 )
            setX( e.getX() - mouseX );
        if( e.getY() >= 0 && e.getY() <= 500 )
            setY( e.getY() - mouseY );
    }

    /**
     * Obsługuje kliknięcie myszą w figurę
     * @param e Zdarzenie kliknięcia myszą.
     */
    private void mousePressed( MouseEvent e )
    {
        mouseX = (int)(e.getX() - getX());
        mouseY = (int)(e.getY() - getY());

        makeFocused( e.isControlDown() );
    }

    /**
     * Obsługuje pyszczenie przycisku po wcześniejszym przeciąganiu.
     */
    private void mouseReleased()
    {
        if( isDragged )
        {
            Editor.console.write( "Pozycja końcowa: (" + (int)getX() + ", " + (int)getY() + ")" );
            isDragged = false;
        }
    }
}
