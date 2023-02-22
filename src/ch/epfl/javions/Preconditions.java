package ch.epfl.javions;

public final class Preconditions {
    private Preconditions(){}   //constructeurs private => classe non instanciable
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}
