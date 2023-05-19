package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

//VOIR TREAD 2170

/**
 * Contient le programme principal
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class Main extends Application {

    /**
     * Lance le programme (grâce à la méthode launch())
     * @param args : arguments du programme
     */
    public static void main(String[] args) {
        launch(args);
    }   //DEMARAGE DU FIL D'EXECUTION JAVAFX

    @Override
    public void start(Stage primaryStage) throws Exception {

        //PREPARTION DES VARIABLES POUR LE FIL D'EXECUTION

        List<String> args = getParameters().getRaw();
        ConcurrentLinkedDeque<RawMessage> allMessages = new ConcurrentLinkedDeque<>();

        // … à compléter (voir TestBaseMapController)
        Path tileCache = Path.of("tile-cache");
        TileManager tileManager = new TileManager(tileCache, "tile.openstreetmap.org");  // "tile.openstreetmap.org"
        MapParameters mapParameters = new MapParameters(8, 33_530, 23_070);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);

        // Création de la base de données
        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        //CREATION DU GRAPHE DE SCENE GRAPHIQUE

        //Crée la map avec les avions par-dessus
        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> selectedAircraft = new SimpleObjectProperty<>();
        ObservableSet<ObservableAircraftState> states = asm.states();
        AircraftController ac = new AircraftController(mapParameters, states, selectedAircraft);
        var mapWithAircrafts = new StackPane(baseMapController.pane(), ac.pane());

        //Crée la ligne affichant le nombre de messages et d'avions visibles
        StatusLineController statusLineController = new StatusLineController();
        IntegerProperty aircraftCount = statusLineController.aircraftCountPropertyProperty();
        LongProperty messageCount = statusLineController.messageCountProperty();
        aircraftCount.bind(Bindings.size(states));
        Pane statusLine = statusLineController.pane();

        //Crée la table des aéronefs visibles
        AircraftTableController aircraftTableController = new AircraftTableController(states, selectedAircraft);
        TableView<ObservableAircraftState> tableView = aircraftTableController.pane();

        BorderPane statusAndTable = new BorderPane();
        statusAndTable.setTop(statusLine);
        statusAndTable.setCenter(tableView);

        SplitPane finalPane = new SplitPane(mapWithAircrafts, statusAndTable);
        finalPane.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(finalPane));
        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();


        //DEMARAGE DU FIL D'EXECUTION CHARGE D'OBTENIR LES MESSAGES

        //Thread obtentionMessages = new Thread(() -> {

        //if(args == null || args.isEmpty()){     //pas sur que ==null soit necessaire
        //lire depuis System.in
        //}else{

        //String fichierALire = args.get(0);  //ex : "resources\\messages_20230318_0915.bin"
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("resources\\messages_20230318_0915.bin")))) {

            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rawMessage = RawMessage.of(timeStampNs, bytes);
                if (rawMessage != null) {
                    allMessages.add(rawMessage);
                }
            }
        } catch (EOFException e) {
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // }
        //});
        //obtentionMessages.setDaemon(true);
        //obtentionMessages.start();


        var iteratorOfAllMessages = allMessages.iterator();
        //DEMARRAGE DU "MINUTEUR D'ANIMATION"
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
                        if (iteratorOfAllMessages.hasNext()) {   //pas sur que ce soit necessaire
                            Message m = MessageParser.parse(iteratorOfAllMessages.next());
                            if (m != null) {
                                asm.updateWithMessage(m);
                                messageCount.set(messageCount.longValue() + 1);
                                asm.purge();
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();

//utiliser thread sleep pour attendre la publication des messages depuis le ficher



        }


}
