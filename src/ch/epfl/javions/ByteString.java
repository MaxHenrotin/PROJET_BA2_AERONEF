package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Classe qui représente un tableau d'octets, immuable et non signée
 *
 * @author Erbland Julien (SCIPER : 346893)
 */

public final class ByteString {

    private byte[] tab;

    /**
     * Constructeur de byte en utilisant la fonction clone() pour éviter les problèmes de références
     * @param bytes : tableau de bytes
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public ByteString(byte[] bytes){
        tab = bytes.clone();
    }

    /**
     * Retourne la chaîne d'octets dont la chaîne passée en argument est la représentation hexadécimale
     * @param hexString : chaîne de charactère représentant un nombre em  base 16
     * @throws NumberFormatException : si la chaîne de charactère n'est pas en hexadéciaml
     * @throws IllegalArgumentException : si hexString n'est pas de longueur pair
     * @return : un ByteString correspondant à la représentation hexadécimal passée en paramètre
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static ByteString ofHexadecimalString(String hexString){
        Preconditions.checkArgument((hexString.length()%2)==0);

        try{
            ByteString output = new ByteString(HexFormat.of().parseHex(hexString));
            return output;
        }catch(IllegalArgumentException e){ //la méthode parseHex throw IllegalArgumentException si la string n'est pas hexadécimal
            throw new NumberFormatException(); //on souhaite pourtant throw NumberFormatException dans ce cas la donc on catch et throw le bon type d'exception voulu
        }
    }

    /**
     * Retourne la taille du tableau de bytes
     * @return : la taille de tab
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public int size(){return tab.length;}


    /**
     * Retourne l'octet (interprété en non signé) à l'index donné
     * @param index : index voulu dans le tableau
     * @throws IndexOutOfBoundsException : si l'index n'existe pas dans le tableau
     * @return : l'octet correspondant à l'index dans le tableau
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public int byteAt(int index){
        Objects.checkIndex(index,tab.length);
        return tab[index]&0xFF; //évite les problèmes de signes
    }


    /**
     * Retourne les octets compris entre les index fromIndex (inclus) et toIndex (exclu)
     * sous la forme d'une valeur de type long,l'octet d'index toIndex - 1 constituant l'octet de poids faible du résultat
     * @param fromIndex : index de départ (inclus)
     * @param toIndex : index finale(exclu)
     * @throws IndexOutOfBoundsException : si la plage décrite par fromIndex et toIndex n'est pas totalement comprise entre 0 et la taille de la chaîne
     * @throws IllegalArgumentException : si la différence entre toIndex et fromIndex n'est pas strictement inférieure à au nombre d'octets contenus dans une valeur de type long
     * @return : valeur de type long construite par les bytes compris entre fromIndex et toIndex
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public long bytesInRange(int fromIndex, int toIndex){
        Objects.checkFromToIndex(fromIndex,toIndex,tab.length);
        Preconditions.checkArgument(toIndex-fromIndex<Long.SIZE);

        byte[] output = java.util.Arrays.copyOfRange(tab,fromIndex,toIndex);

        long number = output[0];

        for(int i = 1; i<output.length; ++i){
            number = number << 8;
            long unsigned = output[i] & 0xFF;  //évite les problèmes de signes
            number = number | unsigned;
        }
        return number;

    }


    /**
     * Retourne vrai si et seulement si la valeur qu'on lui passe est aussi une instance de ByteString
     *  et que ses octets sont identiques à ceux du récepteur
     * @param object : objet à comparer
     * @throws IllegalArgumentException : si object n'est pas de type ByteString
     * @return : vrai si les tableau de chaque ByteString sont égaux ou faux sinon
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    @Override
    public boolean equals(Object object) {
        Preconditions.checkArgument(object instanceof ByteString);
        return Arrays.equals(tab,((ByteString) object).tab);
    }


    /**
     * Retourne une représentation des octets de la chaîne en hexadécimal, chaque octet occupant exactement deux caractères
     * @return : la représentation de tab en un String en hexadécimal
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    @Override
    public String toString(){
        HexFormat hexFormat=HexFormat.of().withUpperCase();
        return hexFormat.formatHex(tab);
    }


    /**
     * Retourne la valeur retournée par la méthode hashCode de Arrays appliquée au tableau contenant les octets
     * @return : la valeur de hachage appliquée à tab
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    @Override
    public int hashCode(){
        return Arrays.hashCode(tab);
    }
}
