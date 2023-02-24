package ch.epfl.javions;

/**
 * Offre des méthodes statiques permettant d'effectuer certains calculs mathématiques (Similaire à la classe Math de la bibliothèque standard Java)
 *
 * @author Max Henrotin (341463)
 * @author Julien Erbland (346893)
 */
public final class Math2 {
    private Math2(){}   //constructeur privé pour rendre la classe non instanciable

    /**
     * Limite la valeur v à l'intervalle allant de min à max, en retournant min si v est inférieure à min, max si v est supérieure à max, et v sinon
     * @param min borne minimale de l'intervalle à contrôler
     * @param v valeur dont il faut vérifier son appartenance à l'intervalle min-max
     * @param max borne maximale de l'intervalle à contrôler
     * @return min si v est inférieure à min, max si v est supérieure à max
     */
    public static int clamp(int min, int v, int max) {
        if(min>max){
            throw new IllegalArgumentException();
        }else if(v<min){
            return min;
        }else if(v>max){
            return max;
        }
        return v;
    }

    /**
     * Retourne le sinus hyperbolique réciproque de son argument
     * @param x argument pour lequel on doit calculer son sinus hyperbolique réciproque
     * @return le sinus hyperbolique réciproque de son argument (x)
     */
    public static double asinh(double x){
        return Math.log(x+Math.sqrt(1+x*x));
    }
}
