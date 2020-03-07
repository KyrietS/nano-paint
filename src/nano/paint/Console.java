package nano.paint;

import javafx.scene.control.TextArea;

/**
 * Klasa obsługująca wyświetlanie informacji w konsoli wewnątrz okna aplikacji.
 * @author Sebastian Fojcik
 * @version 1.0
 */
public class Console
{
    /**
     * Pole {@link TextArea}, w którym umieszczane będą wypisywane komunikaty.
     */
    private TextArea textArea;

    /**
     * @param textArea obiekt {@link TextArea} w którym umieszczane będą wypisywane komunikaty.
     */
    public Console( TextArea textArea )
    {
        this.textArea = textArea;
    }

    /**
     * Wypisuje linię tekstu w konsoli rozpoczynając ją od '&gt;' i kończąc znakiem nowej linii.
     * @param text Tekst do wyświetlenia.
     */
    public void write( String text )
    {
        textArea.appendText( "> " + text + "\n" );
    }

    /**
     * Usuwa ostatnio dodaną linię tekstu i zastępuje ją nową.
     * @param text Nowy tekst do wyświetlenia.
     */
    public void rewriteLastLine( String text )
    {
        int endTextIndex = textArea.getText().lastIndexOf( '\n', textArea.getText().length()-2 );
        String newText = textArea.getText( 0, endTextIndex+1 );
        textArea.setText( newText );
        write( text );
    }

    /**
     * Usuwa cały tekst z konsoli.
     */
    public void clear()
    {
        textArea.clear();
    }
}
