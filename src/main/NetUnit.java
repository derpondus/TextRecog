package main;

import java.util.Arrays;
import java.util.Random;

/**
 * Klasse NetUnit
 * <p>
 * Erstellung einer Unit eines neuronalen Netzes
 *
 * @author LiamBlind
 */
public class NetUnit {

    Random rnd = new Random();
    public double actValue = 0;
    public double inValue;
    public double outValue;
    public double delta = 0;
    public int wLength;

    public double[] weights;
    public double bias = 0;

    //vorgegeben
    public double leadDim = 5;
    public double learnAmount;

    /**
     * Konstruktor NetUnit
     * <p>
     * Initialisiert die Unit:
     * setzt Klassenvariablen
     * erstellt und füllt das gewichtearray mit "1"en
     *
     * @param learnAmount Lernfaktor dieser Unit
     * @param wLength Anzahl der Eingangsgewichte dieser Unit
     */
    public NetUnit(double learnAmount, int wLength) {
        this.learnAmount = learnAmount;
        this.wLength = wLength;

        weights = new double[wLength];
        Arrays.fill(weights, 1);
    }

    /**
     * Methode: input
     * <p>
     * setzt "input" direkt als eingangswert
     *
     * @param input neuer Aktivierungswert
     */
    public void input(double input) {
        actValue = input;
    }

    /**
     * Methode: activate
     * <p>
     * setzt "inValue" als Klassenvariable
     * addiert den gewichteten "invalue"-Wert zu "actValue"
     *
     * @param inValue Eingabewert
     * @param x index der Verbindung
     */
    public void activate(double inValue, int x) {
        this.inValue = inValue;
        if (inValue != 0)
            actValue += weights[x] * inValue * learnAmount;
    }

    /**
     * Methode getActValue
     * <p>
     * gibt die "actValue" nach Anwendung der Akivierungsformel als "outValue" im return zurück
     *
     * @return neue actValue
     */
    public double getActValue() {
        //System.out.print(actValue);
        //outValue = (double) ((2/(1+Math.exp(-leadDim*actValue)))-1)+(bias*learnAmount);
        outValue = learnAmount * actValue + bias;
        actValue = 0;
        return outValue;
    }
    //(2/(1+exp(-5x)))-1

    /**
     * Methode: output
     * <p>
     * gibt "actValue" direkt mit return aus
     *
     * @return actValue
     */
    public double output() {
        return actValue;
    }

    /**
     * Methode: setWeight
     * <p>
     * setzt das Gewicht der Verbindung dieser Unit mit der Unit an der Position "x"/"y" der vorherigen Ebene auf "var"
     *
     * @param var neues Gewicht
     * @param x index der Verbindung
     */
    public void setWeight(double var, int x) {
        weights[x] = var;
    }

    /**
     * Methode: addWeight
     * <p>
     * addiert "var" zu dem Gewicht der Verbindung dieser Unit mit der Unit an der Position "x"/"y" der vorherigen Ebene
     *
     * @param var zusätzliches Gewicht
     * @param x index der Verbindung
     */
    public void addWeight(double var, int x) {
        weights[x] += var;
    }

    /**
     * Methode: addBias
     * <p>
     * addiert "var" zu dem Bias-Wert dieser Unit
     *
     * @param var zusätzlicher Bias
     */
    public void addBias(double var) {
        bias += var;
    }

}
