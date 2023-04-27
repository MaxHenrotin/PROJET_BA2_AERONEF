package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe représentant un gestionnaire de tuiles OSM
 * Son rôle est d'obtenir les tuiles depuis un serveur de tuiles et les stocker dans un cache mémoire et disque
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class TileManager {

    private static final int CACHE_MEMORY_CAPACITY = 100;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final boolean CACHE_MEMORY_LINKED_IN_ACCESS_ORDER = true;

    //constante pour .png ??

    private final Path filePath;    //p.ex : "
    private final String serverName;    //p.ex : "https://tile.openstreetmap.org"
    private Map<TileId, Image> cacheMemory = new LinkedHashMap<>(CACHE_MEMORY_CAPACITY, DEFAULT_LOAD_FACTOR, CACHE_MEMORY_LINKED_IN_ACCESS_ORDER);

    /**
     * Enregistrement imbriqué représentant l'identité d'une tuile OSM
     * @param x : index X de la tuile
     * @param y : index Y de la tuile
     * @param zoomLevel : niveau de zoom de la tuile
     */
    public record TileId(int x, int y, int zoomLevel){

        public static boolean isValid(int x, int y, int zoomLevel){
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
            Path imagePath = filePath
                    .resolve(tileId.zoomLevel() + "")   //équivalent à : Integer.toString(tileId.zoomLevel())
                    .resolve(tileId.x() + "")           //équivalent à : String.valueOf(tileId.x())
                    .resolve(tileId.y() + ".png");

            if (Files.exists(imagePath)) {
                try(InputStream stream = new FileInputStream(imagePath.toFile())){
                    image = new Image(stream);
                    //si existe, placer dans cache memoire
                    cacheMemory.put(tileId, image); //supprime automatiquement le 101e élément accédé (grâce au constructeur) ???? --> NON IL FAUT ENCORE GERER LA SUPPRESSION
                    //puis supprimer 101eme image du cache memoire si il y a 101
                    //puis retourner image
                    return image;
                }
            }else{
                //sinon télécharger depuis le serveur de tuiles
                URL url = new URL(serverName
                                    + "/" + tileId.zoomLevel() + "/" + tileId.x() + "/" + tileId.y() + ".png");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Javions");  //créer des constantes !!!

                try(InputStream stream = urlConnection.getInputStream()){
                    byte[] imageInBytes = stream.readAllBytes();

                    //si existe, placer dans le cache disque

                    image = new Image(new ByteArrayInputStream(imageInBytes));
                    //puis placer dans cache memoire
                    cacheMemory.put(tileId, image); //gerer suppression 101
                    //puis supprimer 101eme image du cache memoire si il y a 101
                    //puis retourner image
                    return image;
                }
                //sinon throw Exception (automatique grace à try-with-resources)
            }
        }
    }


}
