package nano.paint.editor;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import nano.paint.Console;
import nano.paint.shapes.MyCircle;
import nano.paint.shapes.MyPolygon;
import nano.paint.shapes.MyRectangle;
import nano.paint.shapes.MyShape;

import java.util.ArrayList;

/**
 * Klasa zarządzająca planszą z figurami.
 * Do jej zadań należy wykonywanie odpowiednich operacji na
 * elementach planszy typu {@link MyShape}.
 * Klasa ta również odpowiada za poprawne dodawanie figur na planszę,
 * usuwanie figur, znaznaczanie, odznaczanie, czy usuwanie wszystkich figur na raz.
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyShape
 * @see MyRectangle
 * @see MyCircle
 * @see MyPolygon
 * @see Console
 */
public class Editor
{
    /**
     * Dostępne tryby działania edytora.
     */
    public enum Mode
    {
        /** Domyślny tryb, przesuwanie, zaznaczanie itp. */
        DEFAULT,
        /** Dodawanie prostokąta. */
        RECTANGLE,
        /** Dodawanie koła. */
        CIRCLE,
        /** Dodawanie wielokąta. */
        POLYGON
    }

    /** Instancja konsoli, któa będzie używana do wypisywania komunikatów. */
    public static Console console;

    /** Plansza, do której będą dodawane figury. */
    private Pane board;

    /** Obecny tryb, w którym działa edytor.*/
    private Mode currentMode = Mode.DEFAULT;

    /** Licznik kliknięć używany, przy dodawaniu figur, m.in {@link MyRectangle}, {@link MyPolygon} */
    private int clickCount = 0;

    /** Tymczasowy obiekt figury, która jest właśnie w trakcie tworzenia. */
    private MyRectangle myRect;
    /** Tymczasowy obiekt figury, która jest właśnie w trakcie tworzenia. */
    private MyCircle myCircle;
    /** Tymczasowy obiekt figury, która jest właśnie w trakcie tworzenia. */
    private MyPolygon myPolygon;

    /** Pomocnicza tablica elementów na planszy
     *  <b>Uwaga:</b> niektóre z tych elementów mogą <u>nie</u> być figurami */
    private static ObservableList<Node> shapes;

    /**
     * Podstawowy konstruktor klasy.
     * @param boardPane Plansza, do której będą dodawane figury.
     * @param mainConsole Konsola, do której będą wypisywane komunikaty.
     */
    public Editor( Pane boardPane, Console mainConsole )
    {
        this.board = boardPane;
        console = mainConsole;

        this.board.addEventFilter( MouseEvent.MOUSE_CLICKED, this::onClick );
        this.board.addEventFilter( MouseEvent.MOUSE_MOVED, this::onMouseMoved );
        this.board.addEventFilter( ScrollEvent.SCROLL, this::onScroll );

        shapes = this.board.getChildren();
    }

    /**
     * Ustawia tryb, w którym działa edytor.
     * <b>Uwaga:</b> nie należy zmieniać pola {@code currentMode} poza tą funkcją.
     * @param mode Tryb działania edytora.
     * @see Editor.Mode
     */
    public void setMode( Mode mode )
    {
        currentMode = mode;
        clickCount = 0;
        switch( mode )
        {
        case DEFAULT:
            enableShapes();
            console.write( "Tryb edycji..." );
            break;
        case RECTANGLE:
            disableShapes();
            console.write( "Dodawanie prostokąta..." );
            console.write( "Wierzchołek 1: " );
            break;
        case CIRCLE:
            disableShapes();
            console.write( "Dodawanie koła..." );
            console.write("Środek koła: ");
            break;
        case POLYGON:
            disableShapes();
            console.write( "Dodawanie wielokąta..." );
            console.write( "Wierzchołek 1: ");
            break;
        }
    }

    /**
     * Zmienia kolor zaznaczonych figur.
     * @param color Nowy kolor.
     */
    public void setColor( Color color )
    {
        ArrayList<MyShape> focusedShapes = getFocusedShapes();
        if( focusedShapes.size() > 0 )
        {
            console.write( "Wybrano kolor: " + color );
            for( MyShape shape : focusedShapes )
                shape.changeColor( color );
        }
        else
            console.write( "Nie zaznaczono figury!" );
    }

    /**
     * Zaznacza wszystkie figury.
     */
    public void makeAllFocused()
    {
        for( Node shape : shapes )
            if( shape instanceof MyShape )
                ( (MyShape) shape ).makeFocused( true );
    }

    /**
     * Usuwa zaznaczenie ze wszystkich figur.
     */
    public static void clearFocus()
    {
        for( int i = 0; i < shapes.size(); i++ )
        {
            if( shapes.get(i) instanceof MyShape )
                if( ( (MyShape) shapes.get(i) ).getFocused() )
                {
                    ( (MyShape) shapes.get(i) ).removeFocused();
                    i = 0;
                }
        }
    }

    /**
     * Anuluje dodawanie figury i przełącza edytor w tryb domyślny, w przypadku,
     * gdy kliknięto poza planszę.
     * @param x pozycja kursora X
     * @param y pozycja kursora Y
     */
    public void removeFocused( double x, double y )
    {
        if( x < 300 || y < 26 )
        {
            switch( currentMode )
            {
            case RECTANGLE:
                if( myRect != null && clickCount > 0 )
                    myRect.remove();
                console.write( "Anulowano dodawanie prostokąta" );
                setMode( Mode.DEFAULT );
                break;
            case CIRCLE:
                if( myCircle != null && clickCount > 0 )
                    myCircle.remove();
                console.write( "Anulowano dodawanie koła" );
                setMode( Mode.DEFAULT );
            case POLYGON:
                if( myPolygon != null && clickCount > 0 )
                    myPolygon.remove();
                console.write( "Anulowano dodawanie wielokąta" );
                setMode( Mode.DEFAULT );
            }

        }
    }

    /**
     * Usuwa zaznaczone figury z planszy.
     */
    public void removeShapes()
    {
        ArrayList<MyShape> focusedShapes = getFocusedShapes();
        if( focusedShapes.size() > 0 )
        {
            if( focusedShapes.size() > 1 )
                console.write( "Usuwanie " + focusedShapes.size() + " figur..." );
            for( MyShape shape : focusedShapes )
                shape.remove();
        }
        else
            console.write( "Nie zaznaczono figury!" );
    }

    /**
     * Zmienia rozmiar zaznaczonych figur.
     * @param scale nowa skala dla figur (1.0 = bez zmian).
     */
    public void resizeShapes( double scale )
    {
        ArrayList<MyShape> focusedShapes = getFocusedShapes();
        if( focusedShapes.size() > 0 )
        {
            double newScale = (double)((int)((scale-1)*10000))/100;
            console.write( "Zmieniono rozmiar " + focusedShapes.size() + " figur" + (
                    focusedShapes.size() == 1 ? "y" : "") + " o " + (scale >= 1.0 ? "+" : "")
                    + newScale + "%" );

            for( MyShape shape : focusedShapes )
                shape.rescale( scale );
        }
        else
            console.write( "Nie zaznaczono figury!" );
    }

    /**
     * Obsługuje kliknięcia w planszę.
     * @param e zdarzenie kliknięcia myszy.
     */
    private void onClick( MouseEvent e )
    {
        int x = (int) e.getX();
        int y = (int) e.getY();
        if( e.getButton() == MouseButton.PRIMARY )
        {
            switch( currentMode )
            {
            case RECTANGLE:
                addRectangle( x, y );
                break;
            case CIRCLE:
                addCircle( x, y );
                break;
            case POLYGON:
                if( e.getClickCount() == 2 )
                    addPolygon( x, y, true );
                else
                    addPolygon( x, y, false );
                break;
            default:
                break;
            }
        }
    }

    /**
     * Obsługuje poruszanie myszy nad planszą.
     * @param e zdarzenie poruszenia myszy.
     */
    private void onMouseMoved( MouseEvent e )
    {
        switch( currentMode )
        {
        case RECTANGLE:
            if( clickCount == 0 )
                console.rewriteLastLine( "Wierzchołek 1: (" + (int)e.getX() + ", " + (int)e.getY() + ")" );
            if( clickCount == 1)
            {
                console.rewriteLastLine( "Wierzchołek 2: (" + myRect.getX2() + ", " + myRect.getY2() + ")" );
                if( e.isShiftDown() )
                    myRect.setEnd( e.getX(), e.getY(), true );
                else
                    myRect.setEnd( e.getX(), e.getY(), false );
            }
            break;
        case CIRCLE:
            if( clickCount == 0 )
                console.rewriteLastLine( "Środek koła: (" + (int)e.getX() + ", " + (int)e.getY() + ")" );
            if( clickCount == 1 )
            {
                double x1 = e.getX();
                double y1 = e.getY();
                double x2 = myCircle.getCenterX();
                double y2 = myCircle.getCenterY();
                console.rewriteLastLine( "Promień: " + (int)myCircle.getRadius() );
                myCircle.newRadius( (int)Math.sqrt( ( x1 - x2 ) * ( x1 - x2 ) + ( y1 - y2 ) * ( y1 - y2 ) ) );
            }
            break;
        case POLYGON:
            int vertexNumber;
            if( myPolygon != null )
                vertexNumber = myPolygon.getPoints().size() / 2;
            else
                vertexNumber = 1;
            console.rewriteLastLine( "Wierzchołek " + vertexNumber + ": (" +
                    (int) e.getX() + ", " + (int) e.getY() + ")" );

            if( clickCount > 0 )
                myPolygon.visualize( e.getX(), e.getY() );
            break;
        }
    }

    /**
     * Obsługuje uzycie scrolla nad planszą.
     * @param e zdarzenie scrolla.
     */
    private void onScroll( ScrollEvent e )
    {
        ArrayList<MyShape> focusedShapes = getFocusedShapes();
        for( MyShape shape : focusedShapes )
            if( e.getDeltaY() > 0 )
                shape.rescale( 1.05 );
            else
                shape.rescale( 0.95 );
    }

    /**
     * Dodaje nowy prostokat na planszę (jeśli jest to pierwsze kliknięcie)
     * Kończy dodawanie prostokąta (jeśli jest to drugie kliknięcie).
     * @param x pozycja X
     * @param y pozycja Y
     */
    private void addRectangle( int x, int y )
    {
        if( clickCount == 0 )
        {
            myRect = new MyRectangle( shapes, x, y );
            console.rewriteLastLine( "Wierzchołek 1: (" + (int) myRect.getX() + ", " + (int) myRect.getY() + ")" );
            console.write( "Wierzchołek 2: (" + myRect.getX2() + ", " + myRect.getY2() + ")" );
            clickCount++;
        }
        else if( clickCount == 1 )
        {
            console.rewriteLastLine( "Wierzchołek 2: (" + myRect.getX2() + ", " + myRect.getY2() + ")" );
            console.write( "Dodano prostokąt  " + (int) myRect.getWidth() + "x" + (int) myRect.getHeight() );
            setMode( Mode.DEFAULT );
            myRect.makeFocused( false );
        }
    }

    /**
     * Dodaje nowe koło na planszę (jeśli jest to pierwsze kliknięcie)
     * Kończy dodawanie koła (jeśli jest to drugie kliknięcie).
     * @param x pozycja X
     * @param y pozycja Y
     */
    private void addCircle( int x, int y )
    {
        if( clickCount == 0 )
        {
            myCircle = new MyCircle( shapes, x, y );
            console.rewriteLastLine( "Środek koła: (" + (int) myCircle.getCenterX() + ", " + (int) myCircle.getCenterY() + ")" );
            console.write( "Promień: " + myCircle.getRadius() );
            clickCount++;
        }
        else if( clickCount == 1 )
        {
            console.rewriteLastLine( "Promień: " + (int) myCircle.getRadius() );
            setMode( Mode.DEFAULT );
            myCircle.makeFocused( false );
        }
    }

    /**
     * Dodaje nowy wielokąt na planszę.
     * Zatwierdza wielokąt (jeśli nastąpił doubleClick)
     * @param x pozycja X
     * @param y pozycja Y
     * @param doubleClick Czy nastąpiło podwójne wciśnięcie myszy.
     */
    private void addPolygon( int x, int y, boolean doubleClick )
    {
        if( doubleClick )
        {
            try
            {
                myPolygon.endShape();
                myPolygon.makeFocused( false );
            }
            catch( RuntimeException e )
            {
                console.write( e.getMessage() );
            }
            finally
            {
                setMode( Mode.DEFAULT );
            }
        }
        else if( clickCount == 0 )
        {
            myPolygon = new MyPolygon( shapes, x, y );
            clickCount++;
            console.write( "Wierzchołek 2: (" + x + ", " + y + ")" );
        }
        else // clickCount > 0
        {
            myPolygon.addVertex( x, y );
            clickCount++;
            console.write( "Wierzchołek " + (clickCount+1) + ": (" + x + ", " + y + ")" );
        }

    }

    /**
     * Dezaktywuje figury, aby nie można ich było zaznaczyć
     * ani przypadkowo przesunąć.
     *
     * Używane w trakcie dodawania nowych figur, aby te już
     * istniejące pozostawały w miejscu, gdy użytkownik będzie klikał.
     */
    private void disableShapes()
    {
        for( Node n : shapes )
        {
            if( n instanceof Shape )
                n.setDisable( true );
        }
    }

    /**
     * Aktywuje figury, aby można je było zaznaczać i przesuwać.
     * @see Editor#disableShapes()
     */
    private void enableShapes()
    {
        for( Node n : shapes )
        {
            if( n instanceof Shape )
                n.setDisable( false );
        }
    }

    /**
     * Wybiera ze wszystkich figur te, które są zaznaczone.
     * @return Lista zaznaczonych figur.
     */
    private ArrayList<MyShape> getFocusedShapes()
    {
        ArrayList<MyShape> focusedShapes = new ArrayList<>();
        for( Node n : shapes )
            if( n instanceof MyShape )
                if( ( (MyShape) n ).getFocused() )
                    focusedShapes.add( (MyShape)n );
        return focusedShapes;
    }
}
