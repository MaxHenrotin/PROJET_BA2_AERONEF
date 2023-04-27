package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Classe représentant un gestionnaire de tuiles OSM
 * Son rôle est d'obtenir les tuiles depuis un serveur de tuiles et les stocker dans un cache mémoire et disque
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class TileManager {

    //===================================== Attributs privées statiques ================================================

    private static final int CACHE_MEMORY_CAPACITY = 100;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final boolean CACHE_MEMORY_LINKED_IN_ACCESS_ORDER = true;
    private static final String URL_CONNECTION_REQUEST_KEY = "User-Agent";
    private static final String URL_CONNECTION_REQUEST_VALUE = "Javions";
    private static final String FILE_EXTENSION = ".png";
    private static final String HTTPS_PROTOCOL_CONSTANT = "https://";


    //===================================== Attributs privées ==========================================================

    private final Path filePath;
    private final String serverName;    //p.ex : "tile.openstreetmap.org"
    private final Map<TileId, Image> cacheMemory = new LinkedHashMap<>(
            CACHE_MEMORY_CAPACITY, DEFAULT_LOAD_FACTOR, CACHE_MEMORY_LINKED_IN_ACCESS_ORDER);


    //===================================== Méthodes privées ===========================================================

    private void deleteCacheMemoryLRUIfFull(){
        if(cacheMemory.size() >= CACHE_MEMORY_CAPACITY){
            Iterator<TileId> cacheMemoryIterator = cacheMemory.keySet().iterator();
            TileId keyToRemove = cacheMemoryIterator.next();
            cacheMemory.remove(keyToRemove);
        }
    }

    private Path DiscPathOfTile(TileId tileId) {
        return filePath
                .resolve(String.valueOf(tileId.zoomLevel()))
                .resolve(String.valueOf(tileId.x()))
                .resolve(tileId.y() + FILE_EXTENSION);
    }

    private void createDirectoryForTile(TileId tileId) throws IOException{
        Path directoryPath = filePath
                .resolve(String.valueOf(tileId.zoomLevel))
                .resolve(String.valueOf(tileId.x));
        Files.createDirectories(directoryPath);
    }

    private URLConnection serverConnectionForTile(TileId tileId) throws IOException {
        StringJoiner urlAdress = new StringJoiner("/", HTTPS_PROTOCOL_CONSTANT, FILE_EXTENSION);
        urlAdress.add(serverName)
                .add(String.valueOf(tileId.zoomLevel()))
                .add(String.valueOf(tileId.x()))
                .add(String.valueOf(tileId.y()));
        URL url = new URL(urlAdress.toString());
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty(URL_CONNECTION_REQUEST_KEY, URL_CONNECTION_REQUEST_VALUE);
        return urlConnection;
    }


    //===================================== Méthodes publiques =========================================================

    /**
     * Enregistrement imbriqué représentant l'identité d'une tuile OSM
     * @param x : index X de la tuile
     * @param y : index Y de la tuile
     * @param zoomLevel : niveau de zoom de la tuile
     */
    public record TileId(int zoomLevel, int x, int y){

        public static boolean isValid(int zoomLevel, int x, int y){
            int indexMax = 1<<zoomLevel - 1 ; //= 2^zoomLevel - 1
            return zoomLevel >= 6 && zoomLevel <= 19 && x >=0 && x <= indexMax && y >= 0 && y <= indexMax;
        }

    }

    /**
     * Constructeur
     * @param filePath : chemin d'accès au dossier contenant le cache disque
     * @param serverName : nom du serveur de tuiles (p.ex : tile.openstreetmap.org)
     */
    public TileManager(Path filePath, String serverName){
        this.filePath = filePath;
        this.serverName = serverName;
    }

    /**
     * Méthode retournant l'image correspondant à la tuile OSM représenté par l'identité donnée
     * @param tileId : identité de la tuile OSM voulue
     * @return : image correspondant à la tuile OSM voulue
     */
    public Image imageOfTile(TileId tileId) throws IOException {

        //recherche dans le cache mémoire
        Image image = cacheMemory.get(tileId);
        //si existe, retourner image
        if(image != null){
            return image;
        }else {
            //sinon recherche dans le cache disque
            Path imagePath = DiscPathOfTile(tileId);

            if (Files.exists(imagePath)) {
                // supprimer 100eme image du cache memoire si il y a 100
                deleteCacheMemoryLRUIfFull();
                //recupere l'image depuis le cache disque
                try(InputStream stream = new FileInputStream(imagePath.toFile())){
                    image = new Image(stream);
                    //puis placer dans cache memoire
                    cacheMemory.put(tileId, image);
                    //puis retourner image
                    return image;
                }
            }else{
                //sinon télécharger depuis le serveur de tuiles
                URLConnection urlConnection = serverConnectionForTile(tileId);

                //créer un nouveau repertoire disque si il n'existe pas
                createDirectoryForTile(tileId);

                try(InputStream inputStream = urlConnection.getInputStream();
                    OutputStream outputStream = Files.newOutputStream(imagePath)){
                    //copie le stream d'entrée dans un tableau d'octet
                    byte[] imageInBytes = inputStream.readAllBytes();

                    //placer dans le cache disque
                    outputStream.write(imageInBytes);

                    //puis supprimer 100eme image du cache memoire si il y a 100
                    deleteCacheMemoryLRUIfFull();
                    //puis placer dans cache memoire
                    image = new Image(new ByteArrayInputStream(imageInBytes));
                    cacheMemory.put(tileId, image);
                    //puis retourner image
                    return image;
                }
            }
        }
    }
}
