package nano.paint.shapes;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nano.paint.editor.Editor;

import java.util.ArrayList;

/**
 * Klasa reprezentująca figurę wielokąta.
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyShape
 */
public class MyPolygon extends Polygon implements MyShape
{
    /** Czy figura jest aktualnie zaznaczona */
    private boolean isFocused = false;

    /**
     * Minimalny rozmiar figury.
     * Liczony jako długość najdalej wysuniętych punktów w poziomie i pionie.
     */
    private final int MIN_SIZE = 20;
    /** Zbiór wierzchołków wielokąta */
    private ObservableList<Double> points;
    /** Zbiór elementów przypisanych do planszy */
    private ObservableList<Node> shapes;
    /** Pozycja myszy X, gdzie rozpoczęto Drag na figurze */
    private double mouseX;
    /** Pozycja myszy Y, gdzie rozpoczęto Drag na figurze */
    private double mouseY;
    /** Pomocnicza lista przechowująca współrzędne wierzchołków
     * przed rozpicząciem przeciągania figury. */
    private ArrayList<Double> primaryPoints;
    /** Czy figura jest aktualnie ciągnięta */
    private boolean isDragged = false;

    /**
     * Podstawowy konstruktor wykorzystywany przez {@link Editor}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param x Pozycja X pierwszego wierzchołka
     * @param y Pozycja Y pierwiszego wierzchołka
     */
    public MyPolygon( ObservableList<Node> shapes, double x, double y )
    {
        initialize( shapes );

        this.points.addAll( x, y, x, y );
        setFill( Color.DODGERBLUE );
    }

    /**
     * Konstruktor odczytujący ustawienia z tekstu, które wygenerowała funkcja {@link MyPolygon#saveToString()}
     * @param shapes Kontener, do którego figura się dopisze.
     * @param data Tekst z danymi figury wygenerowany przez {@link MyPolygon#saveToString()}
     */
    public MyPolygon( ObservableList<Node> shapes, String data )
    {
        initialize( shapes );

        String[] values = data.split( " " );
        if( values.length % 2 == 0 || !values[ 0 ].equals( "p" ) )
            throw new RuntimeException();

        Color color = new Color( Double.parseDouble( values[ 1 ] ), Double.parseDouble( values[ 2 ] ),
                Double.parseDouble( values[ 3 ] ), Double.parseDouble( values[ 4 ] ) );

        setFill( color );

        for( int i = 5; i < values.length - 1; i += 2 )
        {
            addVertex( Double.parseDouble( values[i] ), Double.parseDouble( values[i+1] ) );
        }

        if( points.size() < 6 )
            throw new RuntimeException();
    }

    /**
     * Ustawia wartości początkowe figury.
     * @param shapes Kontener, do którego figura się dopisze.
     */
    private void initialize( ObservableList<Node> shapes )
    {
        this.points = getPoints();
        this.shapes = shapes;

        setStroke( Color.BLACK );
        setStrokeWidth( 1 );

        shapes.add( this );

        addEventHandler( MouseEvent.MOUSE_DRAGGED, this::move );
        addEventHandler( MouseEvent.MOUSE_PRESSED, this::mousePressed );
        addEventHandler( MouseEvent.MOUSE_RELEASED, e -> mouseReleased() );
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
    public void changeColor( Color color )
    {
        setFill( color );
    }

    /** {@inheritDoc} */
    @Override
    public void remove()
    {
        Editor.console.write( "Usunięto " + (points.size() / 2) + "-kąt" );
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

        String saveString = "p " + r + " " + g + " " + b + " " + opacity;

        StringBuilder builder = new StringBuilder( saveString );
        for( Double point : points )
            builder.append(" ").append( point );
        return builder.toString();
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
        Point2D centerPoint = getCenterPoint();
        if( scale > 1.0 )
        {
            for( int i = 0; i < points.size(); i += 2 )
            {
                double newX = centerPoint.getX() + scale * ( points.get( i ) - centerPoint.getX() );
                double newY = centerPoint.getY() + scale * ( points.get( i + 1 ) - centerPoint.getY() );
                points.set( i, newX );
                points.set( i+1, newY );
            }
        }
        else if( getMaxX() - getMinX() > MIN_SIZE && getMaxY() - getMinY() > MIN_SIZE )
        {
            for( int i = 0; i < points.size(); i += 2 )
            {
                double newX = centerPoint.getX() + scale * ( points.get( i ) - centerPoint.getX() );
                double newY = centerPoint.getY() + scale * ( points.get( i + 1 ) - centerPoint.getY() );
                points.set( i, newX );
                points.set( i+1, newY );
            }
        }
    }

    /**
     * Aktualizuje ostatnio dodany wierzchołek, zastępując go parametrami x i y.
     * @param x nowa pozycja X ostatniego wierzchołka
     * @param y nowa pozycja Y ostatnie wierzchołka
     */
    public void visualize( double x, double y )
    {
        points.set( points.size() - 2, x );
        points.set( points.size() - 1, y );
    }

    /**
     * Dodaje nowy wierzchołek do wielokąta.
     * @param x współrzędna X dodawanego wierzchołka.
     * @param y współrzędna Y dodawanego wierzchołka.
     */
    public void addVertex( double x, double y )
    {
        points.add( x );
        points.add( y );
    }

    /**
     * Zakończa dodawanie wierzchołków i sprawdza, czy figura spełnia określone warunki.
     * Warunki konieczne do istnienia wielokąta:
     * <ul>
     *     <li>Musi się składać z co najmniej 3 punktów</li>
     *     <li>Musi być większa, niż rozmiar określony w {@link MyPolygon#MIN_SIZE}</li>
     * </ul>
     * @throws RuntimeException rzuca wyjątek, gdy figura nie spełnia podanych wymagań.
     */
    public void endShape() throws RuntimeException
    {
        points.remove( points.size() - 2 );
        points.remove( points.size() - 1 );
        if( getPoints().size() < 6 )
        {
            remove();
            throw new RuntimeException( "Za mało wierzchołków, aby utworzyć figurę!" );
        }
        if( getMaxX() - getMinX() < MIN_SIZE || getMaxY() - getMinY() < MIN_SIZE )
        {
            remove();
            throw new RuntimeException( "Figura jest zbyt mała!" );
        }
    }

    /**
     * Obsługuje kliknięcie myszą w figurę
     * @param e Zdarzenie kliknięcia myszą.
     */
    private void mousePressed( MouseEvent e )
    {
        mouseX = e.getX();
        mouseY = e.getY();
        primaryPoints = new ArrayList<>( points );
        makeFocused( e.isControlDown() );
    }

    /**
     * Obsługuje pyszczenie przycisku po wcześniejszym przeciąganiu.
     */
    private void mouseReleased()
    {
        if( isDragged )
        {
            Editor.console.write( "Pozycja końcowa: (" + (int) getCenterPoint().getX() + ", " + (int) getCenterPoint().getY() +")" );
            isDragged = false;
        }
    }

    /**
     * Przesuwa figurę w nową pozycję, na której znajduje się kursor.
     * @param e Zdarzenie przesunięcia myszy.
     */
    private void move( MouseEvent e )
    {
        if( !isDragged )
        {
            Editor.console.write( "Przemieszczanie " + points.size()/2 + "-kąta..." );
            Editor.console.write( "Pozycja początkowa: (" + (int) getCenterPoint().getX() + ", " + (int) getCenterPoint().getY() + ")" );
        }
        isDragged = true;

        if( e.getX() >= 0 && e.getX() <= 500 )
        {
            for( int i = 0; i < points.size(); i += 2)
            {
                double newX = e.getX() + primaryPoints.get( i ) - mouseX;
                points.set( i, newX );
            }
        }
        if( e.getY() >= 0 && e.getY() <= 500 )
        {
            for( int i = 1; i < points.size(); i += 2 )
            {
                double newY = e.getY() + primaryPoints.get( i ) - mouseY;
                points.set( i, newY );
            }
        }
    }

    /**
     * Zwraca punkt, będący środkiem prostokąta opisanego na
     * skrajnych punktach wielokąta.
     * @return Punkt środkowy figury.
     */
    private Point2D getCenterPoint()
    {
        double minX = getMinX();
        double maxX = getMaxX();
        double minY = getMinY();
        double maxY = getMaxY();

        return new Point2D( minX + ( maxX - minX ) / 2, minY + ( maxY - minY ) / 2 );
    }

    /**
     * Zwraca najmniejszą współrzędną X, która występuje wśród wierzchołków.
     * @return Najmniejsza współrzędna X w figurze
     */
    private double getMinX()
    {
        double minX = Double.POSITIVE_INFINITY;
        for( int i = 0; i < points.size(); i += 2 )
            if( points.get( i ) < minX )
                minX = points.get( i );
        return minX;
    }
    /**
     * Zwraca największą współrzędną X, która występuje wśród wierzchołków.
     * @return Największa współrzędna X w figurze
     */
    private double getMaxX()
    {
        double maxX = Double.NEGATIVE_INFINITY;
        for( int i = 0; i < points.size(); i += 2 )
            if( points.get( i ) > maxX )
                maxX = points.get( i );
        return maxX;
    }
    /**
     * Zwraca najmniejszą współrzędną Y, która występuje wśród wierzchołków.
     * @return Najmniejsza współrzędna Y w figurze
     */
    private double getMinY()
    {
        double minY = Double.POSITIVE_INFINITY;
        for( int i = 1; i < points.size(); i += 2 )
            if( points.get( i ) < minY )
                minY = points.get( i );
        return minY;
    }
    /**
     * Zwraca największą współrzędną Y która występuje wśród wierzchołków.
     * @return Największa współrzędna Y w figurze
     */
    private double getMaxY()
    {
        double maxY = Double.NEGATIVE_INFINITY;
        for( int i = 1; i < points.size(); i += 2 )
            if( points.get( i ) > maxY )
                maxY = points.get( i );
        return maxY;
    }
}
