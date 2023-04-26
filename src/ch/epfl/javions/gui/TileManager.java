package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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

    private final Path filePath;

    private final String serverName;

    //constantes utilisées à la ligne suivante (HASHMAPCAPACITY = 100, DEFAULT_LOADFACTOR = 0.75f,ACCESSORDER = true)
    private Map<TileId, Image> cacheMemory = new LinkedHashMap<>(100, 0.75f, true);

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
    public Image imageOfTile(TileId tileId){
        Image image = cacheMemory.get(tileId);
        if(image != null){
            return image;
        }else {
            image = new Image(filePath + "/" + tileId.zoomLevel() + "/" + tileId.x() + "/" + tileId.y() + ".png");
            if (image != null) {
                cacheMemory.put(tileId, image);
                //supprimer automatiquement
                return image;
            }
        }
        //recherche dans le cache mémoire
            //si existe, retourner image
            //sinon recherche dans le cache disque
                //si existe, placer dans cache memoire
                            //puis supprimer 101eme image du cache memoire si il y a 101
                            //puis retourner image
                //sinon télécharger depuis le serveur de tuiles
                        //si existe, placer dans le cache disque
                                //puis placer dans cache memoire
                                //puis supprimer 101eme image du cache memoire si il y a 101
                                //puis retourner image
                        //sinon throw Exeption
        return null;
    }


}
