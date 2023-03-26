package ch.epfl.javions;

import java.util.Objects;

/**
 * permet d'extraire un sous-ensemble des 64 bits d'une valeur de type long
 *
 * @author Max Henrotin (341463)
 * @author Julien Erbland (346893)
 */
public class Bits {
    private Bits(){}    //constructeur privé pour rendre la classe non instanciable

    /**
     * qui extrait du vecteur de 64 bits value la plage de size bits commençant au bit d'index start, qu'elle interprète comme une valeur non signée
     * @param value vecteur de 64 bit du quel on extrait
     * @param start index du bit ou commence la plage à extraire
     * @param size  taille de la plage à extraire
     * @throws IllegalArgumentException si la taille n'est pas strictement supérieure à 0 et strictement inférieure à 32
     * @throws IndexOutOfBoundsException si la plage décrite par start et size n'est pas totalement comprise entre 0 (inclus) et 64 (exclu)
     * @return vecteur contenant la plage de bit extraite
     */
    public static int extractUInt(long value, int start, int size){
        Preconditions.checkArgument((size>0) && (size<Integer.SIZE));
        Objects.checkFromIndexSize(start,size,Long.SIZE);   //throw un IndexOutOfBundsException
        value = value << Long.SIZE - size- start; // supprime tous les bits supplémentaires à gauche
        value = value >>> Long.SIZE - size;       // supprime tous les bits supplémentaires à droite (mets des 0)
        return (int) value;
    }

    /**
     * qui retourne vrai ssi le bit de value d'index donné vaut 1
     * @param value vecteur de bit dans lequel on va vérifier le bit voulu
     * @param index index du bit à vérifier dans value
     * @throws IndexOutOfBoundsException s'il n'est pas compris entre 0 (inclus) et 64 (exclu)
     * @return  vrai ssi le bit de value d'index donné vaut 1
     */
    public static boolean testBit(long value, int index){
        Objects.checkIndex(index,Long.SIZE); //vérifie que index est bien entre 0 et 64 (throw IndexOutOfBoundsException)
        long maskIndex = 1L << index;  //voir notes de cours "Types entiers" 5.5.1
        return (value & maskIndex) == maskIndex;    //voir notes de cours "Types entiers" 5.5.2
    }
}
