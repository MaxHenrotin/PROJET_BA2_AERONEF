package ch.epfl.javions;

/**
 * A pour but de faciliter l'écriture de préconditions qui devront être satisfaites dans d'autres parties du projet
 *
 * @author Max Henrotin (341463)
 * @author Julien Erbland (346893)
 */
public final class Preconditions {
    private Preconditions(){}   //constructeurs private pour rendre la classe non instanciable

    /**
     * lève une exception si l'argument est faux
     * @param shouldBeTrue argument à vérifier (qu'il soit vrai)
     * @throws IllegalArgumentException si son argument (shouldBeTrue) est faux
     */
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}
