package main;

import java.util.Random;

/**
 * Klasse NetUnit
 * 
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
	 * 
	 * Initialisiert die Unit: 
	 * setzt Klassenvariablen
	 * erstellt und füllt das gewichtearray mit "1"en
	 * 
	 * @param learnAmount
	 * @param wLength
	 */
	public NetUnit(double learnAmount, int wLength) {
		this.learnAmount = learnAmount;
		this.wLength = wLength;
	    
		weights = new double[wLength];
		for(int i=0; i<weights.length; i++) {
			weights[i] = 1;
		}
	}
	
	/**
	 * Methode: input
	 * 
	 * setzt "input" direkt als eingangswert
	 * 
	 * @param input
	 */
	public void input(double input) {
		actValue = input;
	}
	
	/**
	 * Methode: activate
	 * 
	 * setzt "inValue" als Klassenvariable
	 * addiert den gewichteten "invalue"-Wert zu "actValue"
	 * 
	 * @param inValue
	 * @param x
	 * @param y
	 */
	public void activate(double inValue, int x) {
	    this.inValue = inValue;
	    if(inValue != 0)
	    actValue += weights[x]*inValue*learnAmount;
	}
	
	/**
	 * Methode getActValue
	 * 
	 * gibt die "actValue" nach Anwendung der Akivierungsformel als "outValue" im return zurück 
	 * 
	 * @return
	 */
	public double getActValue() {
		//System.out.print(actValue);
		//outValue = (double) ((2/(1+Math.exp(-leadDim*actValue)))-1)+(bias*learnAmount);
		outValue = (double) learnAmount*actValue+bias;
		actValue = 0;
		return outValue;
	}
	//(2/(1+exp(-5x)))-1
	
	/**
	 * Methode: output
	 * 
	 * gibt "actValue" direkt mit return aus
	 * 
	 * @return
	 */
	public double output() {
	    return actValue;
	  }
	
	/**
	 * Methode: setWeight
	 * 
	 * setzt das Gewicht der Verbindung dieser Unit mit der Unit an der Position "x"/"y" der vorherigen Ebene auf "var"
	 * 
	 * @param var
	 * @param x
	 * @param y
	 */
	public void setWeight(double var, int x) {
	    weights[x] = var;
	  }
	
	/**
	 * Methode: addWeight
	 * 
	 * addiert "var" zu dem Gewicht der Verbindung dieser Unit mit der Unit an der Position "x"/"y" der vorherigen Ebene
	 * 
	 * @param var
	 * @param x
	 * @param y
	 */
	public void addWeight(double var, int x) {
		weights[x] += var;
	}
	
	/**
	 * Methode: addBias
	 * 
	 * addiert "var" zu dem Bias-Wert dieser Unit
	 * 
	 * @param var
	 */
	public void addBias(double var) {
		bias += var;
	}
	
}
