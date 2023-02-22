package ch.epfl.javions;
//  Author:    Max Henrotin

public final class Math2 {

    private Math2(){}

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

    public static double asinh(double x){
        return Math.log(x+Math.sqrt(1+x*x));
    }
}
