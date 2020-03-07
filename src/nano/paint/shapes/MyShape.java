package nano.paint.shapes;

import javafx.scene.paint.Color;
import nano.paint.editor.Editor;

/**
 * Podstawowy interfejs, z którego dziedziczą wszystkie figury
 * obsługiwane przez {@link Editor}
 * @author Sebastian Fojcik
 * @version 1.0
 * @see MyRectangle
 * @see MyCircle
 * @see MyPolygon
 */
public interface MyShape
{
    /**
     * Sprawdza, czy figura jest aktualnie zaznaczona
     * @return Jeśli figura jest zaznaczona - {@code true}, w przeciwnym przypadku {@code false}.
     */
    boolean getFocused();

    /**
     * Zaznacza figurę.
     * Jeśli Ctrl jest wciśnięty, to figura doda się do listy aktualnie zaznaczonych.
     * W przeciwnym wypadku, usunie wszystkie zaznaczone figury i doda siebie.
     * @param isCtrlDown Określa czy klawisz Ctrl jest wciśnięty.
     */
    void makeFocused( boolean isCtrlDown );

    /**
     * Zmienia rozmiar figury.
     * @param scale nowa skala (1.0 = bez zmian)
     */
    void rescale( double scale );

    /**
     * Usuwa figurę z planszy.
     */
    void remove();

    /**
     * Odznacza figurę.
     */
    void removeFocused();

    /**
     * Zmienia kolor wypełnienia figury.
     * @param color Nowy kolor.
     */
    void changeColor( Color color );

    /**
     * Zapisuje wszystkie dane na temat figury w linii tekstu.
     * @return Dane figury w formie tekstu, które mogą być zinterpretowane przez konstruktor.
     */
    String saveToString();
}
