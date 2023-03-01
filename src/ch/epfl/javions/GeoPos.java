package ch.epfl.javions;

/**
 * Enregistrement (=Classe compacte) représentant des coordonnées géographiques (en T32) stockées sous la forme d'entiers de 32 bits (int)
 *
 * @author Max Henrotin (341463)
 * @author Julien Erbland (346893)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {
    /**
     * constructeur compact de GeoPos
     * @param longitudeT32 longitude exprimée en T32
     * @param latitudeT32 latitude exprimée en T32
     * @throws IllegalArgumentException si la longitude ou la latitude n'est pas dans l'intervalle [-2^30,2^30]
     */
    public GeoPos{
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * vérifie si la longitude est dans l'intervalle [-2^30,2^30]
     * @param latitudeT32 longitude exprimée en T32 à vérifier
     * @return vrai si la longitude est dans l'intervalle [-2^30,2^30]
     */
    public static boolean isValidLatitudeT32(int latitudeT32){
        return latitudeT32>= (int)Math.scalb(-1,30) && latitudeT32<= (int)Math.scalb(1,30);     //casté en int pour pas crée des problèmes talle de bit aux limites
    }

    /**
     * retourne la longitude (en radians)
     * @return la longitude (en radians)
     */
    public double longitude(){
        return Units.convertFrom(longitudeT32,Units.Angle.T32);
    }

    /**
     * retourne la latitude (en radians)
     * @return la latitude (en radians)
     */
    public double latitude(){
        return Units.convertFrom(latitudeT32,Units.Angle.T32);
    }

    /**
     * redéfinition de la méthode toString() pour afficher les coordonnées géographiques de manière textuelle en degrés
     * @return les coordonnées géographiques de manière textuelle en degrés
     */
    @Override
    public String toString() {
        return "(" + Units.convert(longitudeT32,Units.Angle.T32,Units.Angle.DEGREE)+ "°, " + Units.convert(latitudeT32,Units.Angle.T32,Units.Angle.DEGREE) + "°)";
    }
}
