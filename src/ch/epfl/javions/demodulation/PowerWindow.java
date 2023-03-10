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

    /**
     * Taille d'un lot d'échantillons de puissance
     */
    private final int BATCH_SIZE = (int) Math.scalb(1,16);  //2^16 de base (mais on peut changer pour les tests de PowerWindow (à 2^3 = 8))

    private InputStream stream;

    private int windowSize;
    private PowerComputer powerComputer;

    /**
     * Tableaux contenant les lots d'échantillons de puissance d'index pair
     * (Est le premier tableau à la construction de PowerWindow)
     */
    private int[] echantillonsIndPair;

    /**
     * Tableaux contenant les lots d'échantillons de puissance d'index impair
     */
    private int[] echantillonsIndImpair;

    /**
     * Position actuelle de la fenêtre par rapport au début du flot
     */
    private long position = 0;

    /**
     * Premier index du lot où il n'y a plus de nouvel echantillon de puissance
     */
    private long BatchEnd =-1;

    /**
     * Permet de garder une information sur quel tableau est le "premier" tableau
     * si nbrLotsTraites est pair, alors echantillonsIndPair est le premier tableau
     */
    private boolean PremierTableauAIndexPair;

    private boolean flotFini = false; //indique si le flot a été entièrement lu // à changer on pourait simplement verifier si batchend est negatif

    /**
     * Construit une fenêtre de taille donnée sur la séquence d'échantillons de puissance (calculés à partir des octets fournis par le flot d'entrée donné)
     * @param stream : flot d'entrée
     * @param windowSize : taille de la fenêtre
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si la taille de la fenêtre donnée n'est pas comprise entre 0 (exclu) et 2^16 (inclu)
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= BATCH_SIZE);

        this.stream = stream;
        this.windowSize = windowSize;
        powerComputer = new PowerComputer(stream, BATCH_SIZE);

        echantillonsIndPair = new int[BATCH_SIZE];
        echantillonsIndImpair = new int[BATCH_SIZE];

        int nbrElemMisDansLeBatch = powerComputer.readBatch(echantillonsIndPair);   //remplit le 1er tableau
        if(nbrElemMisDansLeBatch < BATCH_SIZE){
            BatchEnd = nbrElemMisDansLeBatch;
            flotFini=true;
        }
        PremierTableauAIndexPair = true;
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
        return position;
    }

    /**
     * qui retourne vrai ssi la fenêtre est pleine, c.-à-d. qu'elle contient autant d'échantillons que sa taille
     * (cela est toujours vrai, sauf lorsque la fin du flot d'échantillons a été atteinte, et que la fenêtre la dépasse)
     * @return vrai ssi la fenêtre est pleine
     */
    public boolean isFull() {
       if(flotFini){
           return (position + windowSize) % BATCH_SIZE <= BatchEnd; //si le flot est fini, on vérifie si la window depasse le dernier élément disponible
       }else{
           return true;
       }
    }

    /**
     * retourne l'échantillon de puissance à l'index donné de la fenêtre
     * @param i : index de l'échantillon de puissance dans la fenêtre
     * @return l'échantillon de puissance à l'index donné de la fenêtre
     * @throws IndexOutOfBoundsException si l'index n'est pas compris entre 0 (inclus) et la taille de la fenêtre (exclu)
     */
    public int get(int i){
        Objects.checkIndex(i, windowSize);

        int positionDansLot = (int) (position % BATCH_SIZE) + i;  //on peut caster en int car BATCH_SIZE est un int

        if(positionDansLot >= BATCH_SIZE){  //si la fenêtre chevauche les 2 tableaux et que l'échantillon est dans le 2e tableau
            positionDansLot -= BATCH_SIZE;
            //System.out.print(" position dans lot " + positionDansLot + " valeur : ");
            if(PremierTableauAIndexPair) {   //si le 1er tableau est le tableau des lots pair
                return echantillonsIndImpair[positionDansLot];
            }else { //si le 1er tableau est le tableau des lots impair
                return echantillonsIndPair[positionDansLot];
            }
        }else { //si la fenêtre ne chevauche pas les 2 tableaux
            //System.out.print("position dans lot " + positionDansLot + " valeur : ");
            if (PremierTableauAIndexPair) {   //si le 1er tableau est le tableau des lots pair
                return echantillonsIndPair[positionDansLot];
            } else { //si le 1er tableau est le tableau des lots impair
                return echantillonsIndImpair[positionDansLot];
            }
        }
    }

    /**
     * avance la fenêtre d'un échantillon
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public void advance() throws IOException {
        ++position;
        int nbrElemMisDansBatch;
        if(position % BATCH_SIZE == 0){ //si on est au début d'un lot avec la fenêtre (permet de savoir si le premier tableau est celui des lots pair ou impair)
            PremierTableauAIndexPair = !PremierTableauAIndexPair;
        }
        if(position % BATCH_SIZE + windowSize == BATCH_SIZE ){   //moment où la fenêtre chevauche le prochain lot
            if (PremierTableauAIndexPair) {   //si le 1er tableau est celui des lots d'indices pair
                if((nbrElemMisDansBatch = powerComputer.readBatch(echantillonsIndImpair)) < BATCH_SIZE){    //remplit le tableau des lots d'indices pair et verifie si c'est le dernier lot
                    BatchEnd = nbrElemMisDansBatch;
                    flotFini = true;
                }
            } else {  //si le 1er tableau est celui des lots d'indices impair
                if((nbrElemMisDansBatch = powerComputer.readBatch(echantillonsIndPair)) < BATCH_SIZE){ //remplit le tableau des lots d'indices impair
                    BatchEnd = nbrElemMisDansBatch;
                    flotFini = true;
                }
            }
        }
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