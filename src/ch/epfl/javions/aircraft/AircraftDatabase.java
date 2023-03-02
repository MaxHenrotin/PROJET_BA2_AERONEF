package ch.epfl.javions.aircraft;
//  Author:    Max Henrotin

import ch.epfl.javions.ByteString;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.URLDecoder;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class AircraftDatabase {

    private String fileName;

    public AircraftDatabase(String fileName) {  //pour la base de donnée mictronics de notre projet : fileName = aircraft.zip
        if(fileName.isEmpty()){
            throw new NullPointerException();
        }
        this.fileName = fileName;
    }

    public AircraftData get(IcaoAddress address) throws IOException {

        //extreaction des deux derniers bits de l'adresse Icao
        String fileAdress = address.OACIAddress().substring(4,6) + ".csv";

        String dataBaseAdress = getClass().getResource(fileName).getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        try (ZipFile dataBase = new ZipFile(dataBaseAdress);
            InputStream stream = dataBase.getInputStream(dataBase.getEntry(fileAdress));    //fonctionne si je remplace par 14.csv
            Reader reader = new InputStreamReader(stream, UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            String line;
            //vérifier que la premiere ligne commence bien par l'adresse Icao
            do{
                line = bufferedReader.readLine();
            }while(line != null && line.substring(0,6).compareTo(address.OACIAddress())<0);
            if(line != null && line.startsWith(address.OACIAddress())){

                String[] data = line.split(",",-1);
                return new AircraftData(new AircraftRegistration(data[1]), new AircraftTypeDesignator(data[2]), data[3], new AircraftDescription(data[4]), WakeTurbulenceCategory.of(data[5]));

            }else{
                return null;    //si l'avion n'est pas dans la dataBase
            }
        }catch (Exception IOException){
            throw new IOException(); //attention on perd l'information de l'exception si elle n'est pas de type IOException
        }
    }
}
