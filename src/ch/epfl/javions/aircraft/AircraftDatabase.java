package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Classe qui représente la base de données mictronics des aéronefs.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public final class AircraftDatabase {

    //===================================== Attributs privées ==========================================================
    private final String fileName;

    /**
     * Constructeur qui retourne un objet représentant la base de données mictronics,
     * stockée dans le fichier de nom filename
     *
     * @param fileName : nom du fichier dans lequel est stockée la base de donnée
     * @throws NullPointerException si filename est nul
     */

    //===================================== Méthodes publiques =========================================================

    //pour la base de donnée mictronics de notre projet : fileName = aircraft.zip
    public AircraftDatabase(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * Classe qui retourne les données de l'aéronef dont l'adresse OACI est celle donnée,
     * ou null si aucune entrée n'existe dans la base pour cette adresse
     *
     * @param address : adresse Icao de l'avion qui recherche dans  la base de donnée
     * @return les données de l'aéronef dont l'adresse OACI est celle donnée,
     *          ou null si aucune entrée n'existe dans la base pour cette adresse
     * @throws IOException en cas d'erreur d'entrée/sortie
     */

    public AircraftData get(IcaoAddress address) throws IOException {

        //extreaction des deux derniers bits de l'adresse Icao
        String fileAdress = address.string().substring(4) + ".csv";

        try (ZipFile dataBase = new ZipFile(fileName);
            InputStream stream = dataBase.getInputStream(dataBase.getEntry(fileAdress));
            Reader reader = new InputStreamReader(stream, UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader)){

            String line;
            //vérifier que la premiere ligne commence bien par l'adresse Icao
            do{
                line = bufferedReader.readLine();
            }while(line != null && line.compareTo(address.string())<0);
            if(line != null && line.startsWith(address.string())){

                String[] data = line.split(",",-1);

                return new AircraftData(new AircraftRegistration(data[1]),
                                        new AircraftTypeDesignator(data[2]), data[3], new AircraftDescription(data[4]),
                                        WakeTurbulenceCategory.of(data[5]));

            }else{
                return null;    //si l'avion n'est pas dans la dataBase
            }
        }
    }
}
