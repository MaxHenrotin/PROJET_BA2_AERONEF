package ch.epfl.javions.aircraft;
//  Author:    Max Henrotin

import ch.epfl.javions.ByteString;

import java.io.*;
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
        ByteString adressInBit = ByteString.ofHexadecimalString(address.toString());
        int fileIndex = adressInBit.byteAt(0); //on récupère les 8 derniers bits de l'adresse Icao (=2 hexadécimaux)
        String fileAdress = fileIndex + ".csv";

        String dataBaseAdress = getClass().getResource(fileName).getFile();

        try (ZipFile dataBase = new ZipFile(dataBaseAdress);

            InputStream stream = dataBase.getInputStream(dataBase.getEntry(fileAdress));
            Reader reader = new InputStreamReader(stream, UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader)){

            String line;
            //vérifier que la premiere ligne commence bien par l'adresse Icao
            do{
                line = bufferedReader.readLine();
            }while(line.substring(0,6).compareTo(address.toString())<0);
            if(line.startsWith(address.toString())){

                String[] data = line.split(",");
                return new AircraftData(new AircraftRegistration(data[1]), new AircraftTypeDesignator(data[2]), data[3], new AircraftDescription(data[4]), WakeTurbulenceCategory.of(data[5]));

            }else{
                return null;    //si l'avion n'est pas dans la dataBase
            }
        }catch (Exception e){
            throw new IOException();
        }
    }
}
