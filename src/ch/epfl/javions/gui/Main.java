package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
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

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

//VOIR TREAD 2170

/**
 * Contient le programme principal
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class Main extends Application {

    //===================================== Attributs privées statiques ================================================

    private static final String DISK_CACHE_REPERTORY = "tile-cache";
    private static final  String TILES_WEBSITE = "tile.openstreetmap.org";
    private static final int START_ZOOM = 8;
    private static final int START_MIN_X = 33_530;
    private static final int START_MIN_Y = 23_070;
    private static final String DATABASE_DIRECTORY_PATH = "/aircraft.zip";
    private static final String PRIMARY_STAGE_NAME = "Javions";
    private static final int WINDOW_STARTING_WIDTH = 800;
    private static final int WINDOW_STARTING_HEIGHT = 600;
    private static final int UNSTARTED_TIMESTAMPS = -1;

    //===================================== Méthodes publiques =========================================================

    /**
     * Lance le programme (grâce à la méthode launch())
     * @param args : arguments du programme
     */
    public static void main(String[] args) {
        launch(args);
    }   //DEMARAGE DU FIL D'EXECUTION JAVAFX

    /**
     * Méthode appelée par le fil d'exécution JavaFX au démarrage de l'application s'occupant de la gestion des données
     * du programme pour le bon fonctionnement de l'application Javion
     * @param primaryStage : fenêtre principale de l'application
     * @throws Exception : exception lancée si un problème survient lors de l'exécution de la méthode
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //***************************** Création du graphe de scène ****************************************************

        //Création de la map
        Path tileCache = Path.of(DISK_CACHE_REPERTORY);
        TileManager tileManager = new TileManager(tileCache, TILES_WEBSITE);
        MapParameters mapParameters = new MapParameters(START_ZOOM, START_MIN_X, START_MIN_Y);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);


        // Création de la base de données
        URL u = getClass().getResource(DATABASE_DIRECTORY_PATH);
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase database = new AircraftDatabase(p.toString());


        //Crée la map avec les avions par-dessus
        AircraftStateManager asm = new AircraftStateManager(database);
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
        //liaison doubles clics sur table et centrage sur carte
        Consumer<ObservableAircraftState> DoubleClicConsumer = (state)->baseMapController.centerOn(state.getPosition());
        aircraftTableController.setOnDoubleClick(DoubleClicConsumer);

        //Crée la partie du graphe de scène contenant la table et la ligne de statut
        BorderPane statusAndTable = new BorderPane();
        statusAndTable.setTop(statusLine);
        statusAndTable.setCenter(tableView);

        //Crée le panneau graphique final
        SplitPane finalPane = new SplitPane(mapWithAircrafts, statusAndTable);
        finalPane.setOrientation(Orientation.VERTICAL);

        //Met en place le graphe de scène avec la fenêtre principale
        primaryStage.setScene(new Scene(finalPane));
        primaryStage.setTitle(PRIMARY_STAGE_NAME);
        primaryStage.setMinWidth(WINDOW_STARTING_WIDTH);
        primaryStage.setMinHeight(WINDOW_STARTING_HEIGHT);
        primaryStage.show();


        //******************************* Création du fil d'exécution **************************************************

        //Préparation des variables pour le fil d'exécution
        List<String> args = getParameters().getRaw();
        ConcurrentLinkedDeque<RawMessage> allMessages = new ConcurrentLinkedDeque<>();

        //DEMARAGE DU FIL D'EXECUTION CHARGE D'OBTENIR LES MESSAGES
        Thread obtentionMessages = new Thread(() -> {

            if(args.isEmpty()){

                //LIRE DIRECT DEPUIS LA AIRSPY (voir etape 11 Test pour faire fonctionner cette partie)
                try {
                    AdsbDemodulator demodulateur = new AdsbDemodulator(System.in);
                    while(true){
                        RawMessage rawMessage = demodulateur.nextMessage();
                        if (rawMessage != null) allMessages.addFirst(rawMessage);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else{
                //LIRE DEPUIS UN FICHIER .bin (mis en argument du programme : choisi dans Run puis edit configuration)
                long timestampsLastMessage = UNSTARTED_TIMESTAMPS;
                String fileToRead = args.get(0);

                try (DataInputStream s = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(fileToRead)))) {

                    //Pour lire un fichier démodulé
                    byte[] bytes = new byte[RawMessage.LENGTH];
                    while (s.available() != 0) {      // true ?ou bien? : s.available() != 0
                        long timeStampNs = s.readLong();
                        int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                        assert bytesRead == RawMessage.LENGTH;
                        RawMessage rawMessage = RawMessage.of(timeStampNs, bytes);
                        if (rawMessage != null) {
                            //pour que les avions se déplacent à vitesse réelle
                            if(timestampsLastMessage>=0) {

                                //MICRO représente la conversion entre nano et mili
                                long tempsAttenteMillisecond =
                                        (long) ((rawMessage.timeStampNs() - timestampsLastMessage) * Units.MICRO);
                                try {   //commenter ce bloc try pour que les avions se déplacent en accéléré
                                    if (tempsAttenteMillisecond > 0) Thread.sleep(tempsAttenteMillisecond);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            allMessages.addFirst(rawMessage);
                            timestampsLastMessage = rawMessage.timeStampNs();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //Pour que le fil d'exécution s'arrête quand la fenêtre principale est fermée
        obtentionMessages.setDaemon(true);
        //lançage du fil d'exécution
        obtentionMessages.start();

        //******************************* Démarrage du "Minuteur" d'animations *****************************************
        new AnimationTimer() {
            long lastPurgeTimestamps = 0;
            @Override
            public void handle(long now) {
                try {
                    //permet de rendre le programme plus fluide
                    for (int i = 0; i < 10; i += 1) {

                        // Vérification supplémentaire pour éviter les erreurs
                        if (!allMessages.isEmpty()) {
                            Message m = MessageParser.parse(allMessages.getLast());
                            allMessages.removeLast();
                            if (m != null) {
                                messageCount.set(messageCount.longValue() + 1);
                                asm.updateWithMessage(m);
                                if(now - lastPurgeTimestamps > Units.Time.SECOND/Units.NANO) {  //représente 10^9
                                    asm.purge();
                                    lastPurgeTimestamps = now;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }
}
