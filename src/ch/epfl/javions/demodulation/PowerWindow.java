package ch.epfl.javions.demodulation;
//  Author:    Max Henrotin

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Représente une fenêtre de taille fixe sur une séquence d'échantillons de puissance produits par un calculateur de puissance.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class PowerWindow {

    private InputStream stream;

    private int windowSize;
    private PowerComputer powerComputer;

    private int[] echantillonsIndPair; //1er tableau stockant les échantillons de puissance (ceux d'index pair)
    private int[] echantillonsIndImpair; //2e tableau stockant les échantillons de puissance (ceux d'index impair)

    /**
     * Construit une fenêtre de taille donnée sur la séquence d'échantillons de puissance (calculés à partir des octets fournis par le flot d'entrée donné)
     * @param stream : flot d'entrée
     * @param windowSize : taille de la fenêtre
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si la taille de la fenêtre donnée n'est pas comprise entre 0 (exclu) et 2^16 (inclu)
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= Math.scalb(1, 16));

        this.stream = stream;
        this.windowSize = windowSize;
        powerComputer = new PowerComputer(stream, windowSize); //à reverifier si il faut bien mettre windowSize

        echantillonsIndPair = new int[windowSize/2]; //à reverifier si il faut bien mettre windowSize
        echantillonsIndImpair = new int[windowSize/2]; //à reverifier si il faut bien mettre windowSize
    }

    /**
     * retourne la taille de la fenêtre
     * @return la taille de la fenêtre
     */
    public int size() { return windowSize; }

    /**
     * retourne la position actuelle de la fenêtre par rapport au début du flot de valeurs de puissance
     * (vaut initialement 0 et est incrémentée à chaque appel à advance())
     * @return la position actuelle de la fenêtre par rapport au début du flot de valeurs de puissance
     */
    public long position() {

    }

    /**
     * qui retourne vrai ssi la fenêtre est pleine, c.-à-d. qu'elle contient autant d'échantillons que sa taille
     * (cela est toujours vrai, sauf lorsque la fin du flot d'échantillons a été atteinte, et que la fenêtre la dépasse)
     * @return vrai ssi la fenêtre est pleine
     */
    public boolean isFull() {

    }

    /**
     * retourne l'échantillon de puissance à l'index donné de la fenêtre
     * @param i : index de l'échantillon de puissance dans la fenêtre
     * @return l'échantillon de puissance à l'index donné de la fenêtre
     * @throws IndexOutOfBoundsException si l'index n'est pas compris entre 0 (inclus) et la taille de la fenêtre (exclu)
     */
    public int get(int i){
        Objects.checkIndex(0, windowSize);
        return
    }

    /**
     * avance la fenêtre d'un échantillon
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public void advance() throws IOException {
        jekbcka
    }

    /**
     * avance la fenêtre du nombre d'échantillons donné
     * @param offset nombre d'échantillons à faire avancer la fenêtre
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si offset n'est pas positif ou nul
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for(int i = 0; i < offset; i++){    //appelle advance() offset fois
            advance();
        }
    }

}
