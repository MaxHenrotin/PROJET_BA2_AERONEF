package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ColorRamp {

    private final Color[] tabOfColors;

    private final double ecart;
    public ColorRamp(Color... tabOfColors){
        Preconditions.checkArgument(tabOfColors.length >= 2);

        this.tabOfColors = tabOfColors.clone();
        ecart = 1d / (tabOfColors.length-1);
        //System.out.println("l'écart est de : " +ecart);
    }

    public Color colorAt(double colorRange){
        int firstColorIndex = (int) Math.floor(colorRange/ecart);
        int secondColorIndex = Math2.clamp(0,firstColorIndex+1,tabOfColors.length-1);



        double upperBound = ecart*secondColorIndex;
        double gap = upperBound - colorRange;

        return tabOfColors[firstColorIndex].interpolate(tabOfColors[secondColorIndex],gap/ecart);
    }


    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
}
