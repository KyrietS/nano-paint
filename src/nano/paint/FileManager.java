package nano.paint;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import nano.paint.shapes.MyCircle;
import nano.paint.shapes.MyPolygon;
import nano.paint.shapes.MyRectangle;
import nano.paint.shapes.MyShape;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa obsługująca zapis i odczyt figur. Weryfikuje poprawność
 * odczytu / zapisu wyświetlajac odpowiednie komunikaty.
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyShape
 */
public class FileManager
{
    /**
     * Zbiór wszystkich elementów dodanych do planszy.
     */
    private ObservableList<Node> nodes;
    /**
     * Instancja konsoli służąca do wypisywania komunikatów.
     */
    private Console console;

    /**
     * Konstruktor FileManager
     * @param nodes Lista zawierająca figury implementującye {@link MyShape}.
     * @param console Obiekt {@link Console}, który zostanie użyta do wypisywania komunikatów.
     */
    public FileManager( ObservableList<Node> nodes, Console console )
    {
        this.nodes = nodes;
        this.console = console;
    }

    /**
     * Zapisuje wszystkie figury z planszy do pliku {@link File} podanego jako parametr.
     * @param file Plik, do którego zostaną zapisane figury.
     */
    public void saveShapes( File file )
    {
        if( file == null )
            return;
        List<MyShape> shapes = getMyShapes();
        List<String> lines = new ArrayList<>();

        for( MyShape shape : shapes )
            lines.add( shape.saveToString() );

        try
        {
            Files.write( file.toPath(), lines, Charset.forName( "UTF-8" ) );
            console.write( "Zapisano " + shapes.size() + " figur" );
        }
        catch( Exception e )
        {
            console.write( "Błąd przy próbie zapisu!" );
        }
    }

    /**
     * Wczytuje figury na planszę z pliku {@link File} podanego jako parametr.
     * <b>Uwaga:</b> przed wczytaniem funkcja wyczyści całą planszę z obecnych figur.
     * @param file Plik z którego zostaną odczytane figury.
     */
    public void loadShapes( File file )
    {
        if( file == null )
            return;
        try
        {
            List<String> lines = Files.readAllLines( file.toPath(), Charset.forName( "UTF-8" ) );
            newBoard();
            for( String line : lines )
            {
                switch( line.charAt( 0 ) )
                {
                case 'r':
                    MyRectangle myRect = new MyRectangle( nodes, line );
                    console.write( "Wczytano prostokąt (" + (int)myRect.getX() + ", " + (int)myRect.getY() + ") "
                            + (int)myRect.getWidth() + "x" + (int)myRect.getHeight() );
                    break;
                case 'c':
                    MyCircle myCircle = new MyCircle( nodes, line );
                    console.write( "Wczytano koło (" + (int) myCircle.getCenterX() + ", "
                            + (int) myCircle.getCenterY() + ")  r = " + (int) myCircle.getRadius() );
                    break;
                case 'p':
                    MyPolygon myPolygon = new MyPolygon( nodes, line );
                    console.write( "Wczytano " + myPolygon.getPoints().size() / 2 + "-kąt" );
                    break;
                default:
                        throw new RuntimeException();
                }
            }
        }
        catch( Exception e )
        {
            console.write( "Błąd przy próbie odczytu!" );
        }
    }

    /**
     * Usuwa wszystkie figury z planszy i czyści konsolę.
     */
    public void newBoard()
    {
        List<MyShape> shapes = getMyShapes();
        for( MyShape shape : shapes )
            shape.remove();
        console.clear();
    }

    /**
     * Funkcja pomocnicza wybierająca z planszy tylko obiekty {@link MyShape} i zwraca je w postaci listy.
     * @return Lista obiektów typu {@link MyShape}, które znajdują się na planszy.
     * @see MyShape
     */
    private List< MyShape > getMyShapes()
    {
        List<MyShape> shapes = new ArrayList<>();
        for( Node node : nodes )
            if( node instanceof MyShape )
                shapes.add( (MyShape)node );
        return shapes;
    }
}
