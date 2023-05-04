package ch.epfl.javions.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorRampTest {
    @Test
    void testScaleColor(){
        ColorRamp colorRamp = ColorRamp.PLASMA;
        double[] altitudes = new double[]{1000,300,100,2500,700,4000,3100,150,1600,0};

        for (double alt:
             altitudes) {
            System.out.println("Altitude étudié : "+alt);
            System.out.println(colorRamp.colorAt(Math.pow((alt/12000d) , 1d/3d)).toString());
        }
    }

}