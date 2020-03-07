package nano.paint.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nano.paint.Console;
import nano.paint.FileManager;
import nano.paint.editor.Editor;

import java.io.File;
import java.util.Optional;

/**
 * Podstawowy kontroler aplikacji obsługujący wszystkie kontrolki w oknie aplikacji.
 * @author Sebastian Fojcik
 * @version 1.0
 * @see Editor
 * @see Console
 * @see FileManager
 */
public class MainController
{
    /** Tutaj wyświetlane będą komunikaty konsoli. */
    @FXML
    private TextArea consoleTextArea;
    /** Plansza, na której rysowane będą figury. */
    @FXML
    private Pane mainBoard;
    /** Podstawowy kontener, w którym znajdują się
     *  wszystkie pozostałe komponenty programu. */
    @FXML
    private StackPane mainStackPane;
    /** Wybierałka kolorów pokazywana po kliknięciu
     *  w odpowiednią opcje w menu. */
    @FXML
    private ColorPicker colorPicker;

    /** Obiekt sceny głównego okna programu.
     *  Potrzebny do wyświetlania okien dialogowych. */
    private Stage stage;

    /** Menu kontekstowe dostępne pod PPM na planszy. */
    private ContextMenu contextMenu;
    /** Konsola wypisująca komunikaty. */
    private Console console;
    /** Edytor zarządzający figurami na planszy. */
    private Editor editor;
    /** Menedżer plików obsługujący zapis/odczyt figur. */
    private FileManager fileManager;

    /** Okno dialogowe z informacjami o programie. */
    private Alert aboutDialog;
    /** Okno dialogowe z ostrzeżeniem o możliwej utracie niezapisanych postępów. */
    private Alert warningDialog;
    /** Okno dialogowe umożliwiające wpisanie nowego rozmiaru w % */
    private TextInputDialog resizeDialog;

    /** Systemowa przeglądarka plików.
     *  Używana przy wyborze pliku, który chcemy odczytać lub
     *  do którego chcemy zapisać zmiany. */
    private FileChooser fileChooser;        // Systemowa przeglądarka plików. Używana przy wyborze pliku
                                            // który chcemy odczytać lub do którego chcemy zapisać zmiany.

    /**
     * Odziedziczona metoda ustawiająca wartości początkowe elementom GUI.
     */
    @FXML
    private void initialize()
    {

        console = new Console( consoleTextArea );
        editor = new Editor( mainBoard, console );
        fileManager = new FileManager( mainBoard.getChildren(), console );
        createContextMenu();
        createAboutDialog();
        createResizeDialog();
        createFileChooser();
        createWarningDialog();

        mainStackPane.addEventFilter( MouseEvent.MOUSE_PRESSED, e -> editor.removeFocused(e.getSceneX(), e.getSceneY()) );

        colorPicker.setValue(Color.RED);
    }

    /**
     * Ustawia scenę, do której zostaną dowiązane okna dialogowe.
     * @param primaryStage Główna scena aplikacji.
     */
    public void setScene( Stage primaryStage )
    {
        this.stage = primaryStage;
    }

    /**
     * Tworzy obiekt menu kontekstowego.
     * Wydzielone do osobnej funkcji z uwagi na czytelność.
     */
    private void createContextMenu()
    {
        contextMenu = new ContextMenu();
        MenuItem resize = new MenuItem( "Zmień rozmiar" );
        resize.setOnAction( e -> onResize() );
        MenuItem changeColor = new MenuItem( "Zmień kolor" );
        changeColor.setOnAction( e -> onShowColorPicker() );
        MenuItem remove = new MenuItem( "Usuń" );
        remove.setOnAction( e -> onRemove() );
        MenuItem selectAll = new MenuItem( "Zaznacz wszystko" );
        selectAll.setOnAction( e -> onFocusAll() );
        MenuItem deselectAll = new MenuItem( "Odznacz wszystko" );
        deselectAll.setOnAction( e -> onClearFocus() );

        Menu insert = new Menu( "Wstaw" );
        MenuItem rectangle = new MenuItem( "Prostokąt" );
        rectangle.setOnAction( e -> onRectangleMode() );
        MenuItem circle = new MenuItem( "Koło" );
        circle.setOnAction( e -> onCircleMode() );
        MenuItem polygon = new MenuItem( "Wielokąt" );
        polygon.setOnAction( e -> onPolygonMode() );

        insert.getItems().addAll( rectangle, circle, new SeparatorMenuItem(), polygon );

        contextMenu.getItems().addAll( insert, resize, changeColor, remove, selectAll, deselectAll );
        mainBoard.setOnContextMenuRequested( e -> contextMenu.show( mainBoard.getScene().getWindow(), e.getScreenX(), e.getScreenY() ) );
    }

    /**
     * Tworzy okno dialogowe z informacjami o programie.
     * Wydzielone do osobnej funkcji z uwagi na czytelność.
     */
    private void createAboutDialog()
    {
        aboutDialog = new Alert( Alert.AlertType.INFORMATION );
        aboutDialog.setTitle( "Informacje o programie" );
        aboutDialog.setHeaderText( "Program „NanoPaint” realizowany w ramach Kursu Programowania WPPT" );
        aboutDialog.setContentText( "Autorem programu jest Sebastian Fojcik\n" +
                "Projekt jest realizacją zadania z listy nr 5\n\n" +
                "Strona kursu: http://cs.pwr.edu.pl/macyna/pkursprog.html" );
        ButtonType buttonTypeCancel = new ButtonType("Zamknij", ButtonBar.ButtonData.CANCEL_CLOSE);
        aboutDialog.getButtonTypes().setAll( buttonTypeCancel );
    }

    /**
     * Tworzy okienko dialogowe z ostrzeżeniem o możliwej utracie niezapisanych postępów.
     * Wydzielone do osobnej funkcji z uwagi na czytelność.
     */
    private void createWarningDialog()
    {
        warningDialog = new Alert( Alert.AlertType.WARNING );
        warningDialog.setHeaderText( null );
        warningDialog.setTitle( "Ostrzeżenie" );
        warningDialog.setContentText( "Uwaga! Niezapisane zmiany zostana utracone.\n Kontynuować?" );
        ButtonType buttonTypeYes = new ButtonType( "Tak", ButtonBar.ButtonData.YES );
        ButtonType buttonTypeNo = new ButtonType( "Nie", ButtonBar.ButtonData.NO );

        warningDialog.getButtonTypes().setAll( buttonTypeYes, buttonTypeNo );
    }

    /**
     * Tworzy okienko dialogowe umożliwiające podanie nowego rozmiaru zaznaczonych figur.
     * Wydzielone do osobnej funkcji z uwagi na czytelność.
     */
    private void createResizeDialog()
    {
        resizeDialog = new TextInputDialog( "100%" );
        resizeDialog.setHeaderText( null );
        resizeDialog.setGraphic( null );
        resizeDialog.setTitle( "Zmień rozmiar figury" );
        resizeDialog.setContentText( "\nNowy rozmiar:\n100% = bez zmian" );
    }

    /**
     * Tworzy systemową przeglądarke plików do wybrania miejsca zapisu/odczytu.
     */
    private void createFileChooser()
    {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory( new File( System.getProperty("user.home") + "/Desktop" ) );
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Shapes", "*.shapes" ) );
    }

    /**
     * Uruchamia tryb dodawania prostokąta.
     */
    @FXML
    private void onRectangleMode()
    {
        editor.setMode( Editor.Mode.RECTANGLE );
    }

    /**
     * Uruchamia tryb dodawania koła.
     */
    @FXML
    private void onCircleMode()
    {
        editor.setMode( Editor.Mode.CIRCLE );
    }

    /**
     * Uruchamia tryb dodawania wielokąta.
     */
    @FXML
    private void onPolygonMode()
    {
        editor.setMode( Editor.Mode.POLYGON );
    }

    /**
     * Wyświetla wybierałkę kolorów.
     */
    @FXML
    private void onShowColorPicker() { colorPicker.show(); }

    /**
     * Zleca do edytora ustawienie nowego koloru zaznaczonym figurom.
     */
    @FXML
    private void onColorChanged() { editor.setColor( colorPicker.getValue() ); }

    /**
     * Zleca do edytora zaznaczenie wszystkich figur.
     */
    @FXML
    private void onFocusAll() { editor.makeAllFocused(); }

    /**
     * Zleca do edytora odznaczenie wszystkich figur.
     */
    @FXML
    private void onClearFocus() { Editor.clearFocus(); }

    /**
     * Zleca do edytora usunięcie zaznaczonych figur.
     */
    @FXML
    private void onRemove() { editor.removeShapes(); }

    /**
     * Zleca wyczyszczenie konsoli.
     */
    @FXML
    private void onClearConsole() { console.clear(); }

    /**
     * Wyświetla okno dialogowe z informacjami o programie.
     */
    @FXML
    private void onAbout() { aboutDialog.showAndWait(); }

    /**
     * Zamyka program.
     */
    @FXML
    public void onClose()
    {
        if( mainBoard.getChildren().size() > 0 )
        {
            Optional<ButtonType> result = warningDialog.showAndWait();
            if( result.get().getText().equals( "Nie" ) )
                return;
        }
        System.exit(0);
    }

    /**
     * Wyświetla okno dialogowe do pworwadzenia nowego rozmiaru figur w %.
     * Zleca do edytora zmianę rozmiaru zaznaczonych figur.
     */
    @FXML
    private void onResize()
    {
        console.write( "Zmiana rozmiaru..." );
        console.write("Podaj docelowy rozmiar w procentach");
        Optional<String> result = resizeDialog.showAndWait();
        if( result.isPresent() )
        {
            try
            {
                String input = result.get();
                if( input.charAt( input.length()-1 ) == '%' )
                    input = input.substring( 0, input.length() - 1 );

                double newScale = Double.parseDouble( input );
                newScale = newScale / 100.0;
                editor.resizeShapes( newScale );
            }
            catch( Exception e )
            {
                console.write("Wprowadzono niepoprawny rozmiar!");
            }
        }
    }

    /**
     * Czyści planszę.
     */
    @FXML
    private void onNew()
    {
        if( mainBoard.getChildren().size() > 0 )
        {
            Optional<ButtonType> result = warningDialog.showAndWait();
            if( result.get().getText().equals( "Nie" ) )
                return;
        }
        fileManager.newBoard();
    }

    /**
     * Uruchamia systemową wybierałke plików i zleca do {@link FileManager}
     * odczyt z wybranego przez użytkownika pliku.
     */
    @FXML
    private void onOpenFile()
    {
        if( mainBoard.getChildren().size() > 0 )
        {
            Optional<ButtonType> result = warningDialog.showAndWait();
            if( result.get().getText().equals( "Nie" ) )
                return;
        }

        fileChooser.setTitle( "Wczytaj plik" );
        File file = fileChooser.showOpenDialog( stage );
        fileManager.loadShapes( file );
    }

    /**
     * Uruchamia systemową wybierałke plików i zleca do {@link FileManager}
     * zapis do wybranego przez użytkownika pliku.
     */
    @FXML
    private void onSaveFile()
    {
        fileChooser.setTitle( "Zapisz plik" );
        File file = fileChooser.showSaveDialog( stage );
        fileManager.saveShapes( file );
    }

}
