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
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    /*static void readAllMessages(String fileName,LongProperty messageCount) throws IOException {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))){

            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rawMessage = RawMessage.of(timeStampNs,bytes);
                if(rawMessage != null){
                    allMessages.add(rawMessage);
                    messageCount.set(messageCount.longValue() + 1);
                }
            }
        }
        catch (EOFException e) { }
    }*/

    @Override
    public void start(Stage primaryStage) throws Exception {
        getParameters().getRaw();
        ConcurrentLinkedDeque<RawMessage> allMessages = new ConcurrentLinkedDeque<>();
        List<String> args = getParameters().getRaw();

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

        SplitPane finalPane = new SplitPane(mapWithAircrafts,statusAndTable);
        finalPane.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(finalPane));
        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        new Thread(() -> {
            try (DataInputStream s = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("resources\\messages_20230318_0915.bin")))){

                byte[] bytes = new byte[RawMessage.LENGTH];
                while (true) {
                    long timeStampNs = s.readLong();
                    int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                    assert bytesRead == RawMessage.LENGTH;
                    RawMessage rawMessage = RawMessage.of(timeStampNs,bytes);
                    if(rawMessage != null){
                        allMessages.add(rawMessage);
                        messageCount.set(messageCount.longValue() + 1);
                    }
                }
            }
            catch (EOFException e) { /* nothing to do */ } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var mi = allMessages.iterator();

            // Animation des aéronefs
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    try {
                        for (int i = 0; i < 10; i += 1) {
                            Message m = MessageParser.parse(mi.next());
                            if (m != null) asm.updateWithMessage(m);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    asm.purge();
                }
            }.start();
            });
    }
}
