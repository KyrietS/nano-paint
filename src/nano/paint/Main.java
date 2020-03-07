
package nano.paint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nano.paint.controllers.MainController;

/**
 * Podstawowa klasa programu ładująca zasoby i inicjująca start głównego okna.
 * @author Sebastian Fojcik
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Ładuje zasoby, towrzy kontroler i dodaje scenę do okna.
     * Funkcja jest wywoływana automatycznie. <b>Nie należy jej
     * wywoływać ręcznie.</b>
     * @param primaryStage Podstawowa scena uruchamiana przy starcie programu.
     * @throws Exception Niepowodzenie przy uruchamiania aplikacji.
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation( this.getClass().getResource( "MainWindow.fxml" ) );
        Parent root = loader.load();

        Scene scene = new Scene( root );
        MainController mainController = loader.getController();
        mainController.setScene( primaryStage );

        primaryStage.setResizable( false );
        primaryStage.setTitle("NanoPaint");
        primaryStage.setScene( scene );
        primaryStage.show();


        primaryStage.setOnCloseRequest( we -> {we.consume(); mainController.onClose();} );
    }

    public static void main(String[] args)
    {
        launch( args );
    }
}