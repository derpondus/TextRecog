package main;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Klasse: NetController
 * <p>
 * Erstellung eines neuronalen Netzes zur erkennung von Zahlen in Bildern
 *
 * @author LiamBlind
 */
public class NetController {

    Random rnd = new Random(); //Random-Klasse: Zuständig um die zufallszahlen der ChangeValRnd zu generieren
    BufferedWriter bw = null; //Dient zur Ausgabe von dingen in die Ausgabe-textdatei

    public File[][] trainImages; //Array welches die Bilder zum trainieren enthält
    public File[][] testImages; //Array welches die Bilder zum testen enthält
    public NetUnit[][] units = null;    //Array welches die Units enthält
    public int[] colors = null; //Array welches die Farben des aktuellen Bildes enthält
    public double[] output = null; //Ausgbe des Neuronalen Netzes
    public double[] oldOutput = null; // Ausgabe des vorherigen Durchlaufes des neuronalen Netzes
    public int[] changedThings = new int[7]; // 3x zur festlegung der Unit in "units[][][]" | weight(0) oder bias(1) | hoch(1) oder runter(0)
    public double[] imgNumPercent;
    public double[] imgNumRate;

    public int picsPerLayer;
    public int characters = 10; //größe der Lösungsmenge
    public int lsgNum; //nummer der gewollten Ausgabe und dessen position im Outputarray
    public int imgAmountDecRate; //Menge der Bilder die zum Lernen genutzt werden; 5400 - Menge der Bilder die zum Überprüfen genutzt werden; ist von revisions abhängig
    public int mistakesL = 0; //menge der Fehler die in einer Wiederholung von revisions gemacht werden
    public int mistakesRun = 0;
    public double percent; //Prozentzahl der richtigen Ergebnisse in einem Durchlauf
    public double startTime; //Zeitpunkt des Programmstarts in Millisekunden
    int allImagesAmount; //gesammte Anzahl der Bilder von allen revisions

    public int anzTrainImg;
    public int anzTestImg;
    public int layers = 0; // anzahl Hidden-Layers
    public int revisions = 1000; //anzahl der Wiederholungen des Programms
    public int revWithAllImages = 1;  //wiederholungen aller Bilder von vorne während eines Durchlaufes
    public int numOfChanges = 1; // anz der veränderten Variablen | bei rndVeränderung immer 1
    public double learnValue = 0.1; // reguliert den Einfluss von den Lernvariablen

    /**
     * Konstruktor: NetController
     * <p>
     * Ablaufmethode des Programms:
     * initialisierung der Textdateiausgabe; der Konsolenausgabe
     * einlesen der bilder
     * auswählen eines bildes
     * initialisierung des neuronalen Netzes
     * Durchführung eines Lernzyklus
     * Wiederholung mit dem Rest der Lernbilder abhängig von "turn"
     * Durchführung der Probe mit dem Rest der Bilder
     * Wiederholung des Programms abhängig von "revisions"
     */
    public NetController() {
        trainImages = new File[10][];
        anzTrainImg = -1;
        for (int i = 0; i < trainImages.length; i++) {
            trainImages[i] = new File("pictures/train/" + i).listFiles();
            if (anzTrainImg == -1 || anzTrainImg > trainImages[i].length) {
                anzTrainImg = trainImages[i].length;
            }
        }

        testImages = new File[10][];
        anzTestImg = -1;
        for (int i = 0; i < testImages.length; i++) {
            testImages[i] = new File("pictures/test/" + i).listFiles();
            if (anzTestImg == -1 || anzTestImg > testImages[i].length) {
                anzTestImg = testImages[i].length;
            }
        }

        startOutput();

        startTime = System.currentTimeMillis();
        learnValue = (double) 1 / (anzTrainImg * trainImages.length) - 0.02;
        imgNumPercent = new double[characters];
        imgNumRate = new double[characters];
        Arrays.fill(imgNumRate, 100);
        //int processedImages = 0;

        for (int rev = 0; rev < revisions; rev++) {
            System.out.println("Run: " + rev);
            System.out.println("Test: |----------|");
            System.out.print("      |");

            //turnImgAmount = (int) ((rev+1)*imgNumPercent[1]);

            buildColorsArr(trainImages[0][0]);
            picsPerLayer = colors.length;
            buildNet();

            for (int b = 0; b < revWithAllImages; b++) {
                for (int ziffer = 0; ziffer < trainImages.length; ziffer++) {
                    System.out.print("-");
                    int imgNumAktChar = (int) (anzTrainImg * ((double) imgNumRate[ziffer] / 100));

                    //for (int zifferbild = 0; zifferbild < trainImages[ziffer].length; zifferbild++) {
                    //for (int zifferbild = 0; zifferbild < turn; zifferbild++) {
                    for (int zifferbild = 0; zifferbild < imgNumAktChar; zifferbild++) {

                        lsgNum = ziffer;

                        buildColorsArr(trainImages[ziffer][zifferbild]);
                        putInVal();
                        activate();
                        putOutVal();
                        changeValuesDlt();

                        //processedImages++;

                        //progressOutput(rev, b, processedImages);
                    }

                }
                System.out.println("|");

            }
            test();

            try {
                for (int index = 0; index < imgNumPercent.length; index++) {
                    bw.write("[" + index + ": " + String.format("%.2f", imgNumPercent[index]) + "%, Rate: " + imgNumRate[index] + "]");
                }
                bw.newLine();
                bw.flush();

                int num = getMaxIndex(imgNumPercent);
                if (imgNumRate[num] >= 1)
                    imgNumRate[num] -= 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        endOutput();
    }

    /**
     * Methode: startOutput
     * <p>
     * Initialisierung und erstellung der Textdatei und ihr anfang
     */
    public void startOutput() {
        FileWriter fw = null;
        try {
            fw = new FileWriter("ausgabe.txt");
            bw = new BufferedWriter(fw);

            bw.append("Anzahl Trainigs-Bilder: " + anzTrainImg * trainImages.length);
            bw.newLine();
            bw.append("Zahlen: ");
            for (int i = 0; i < trainImages.length; i++) {
                bw.append(i + ": " + anzTrainImg + ", ");
            }
            bw.newLine();

            bw.append("Anzahl Test-Bilder: " + anzTestImg * testImages.length);
            bw.newLine();
            bw.append("Zahlen: ");
            for (int i = 0; i < testImages.length; i++) {
                bw.append(i + ": " + anzTestImg + ", ");
            }
            bw.newLine();
            bw.newLine();
            bw.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    /**
     * Methode: progress Output
     * <p>
     * Ausgabe von Durchlaufinfos und Zeit in Konsole
     *
     * @param rev Anzahl der Wiederholungen
     * @param processedImages anzahl der bereits verarbeiteten Bilder
     */
    public void progressOutput(int rev, int b, int processedImages) {
        double pastTime = System.currentTimeMillis() - startTime;
        //int allRun = ((revisions*2)-1) * allImagesAmount;
        int imgPerRev = ((anzTrainImg * trainImages.length * revWithAllImages) + (anzTestImg * testImages.length));
        int allRun = revisions * imgPerRev;
        //int thsRun = (rev * allImagesAmount) + processedImages;
        int thsRun = processedImages + (rev * anzTestImg * testImages.length);
        System.out.print(thsRun + "/" + allRun);
        //double futTime = pastTime * (allRun / (float) thsRun) - pastTime;
        double futTime = ((float) pastTime / (float) thsRun) * (allRun - thsRun) * 2;
        System.out.println(" Weitere Zeit: " + (int) (((futTime / 1000) / 60) / 60) + ":"
                + (int) (((futTime / 1000) / 60) % 60) + ":" + (int) ((futTime / 1000) % 60) + "."
                + (int) (futTime % 1000));
    }

    /**
     * Methode: test
     * <p>
     * Durchführung des Erfolgesteste des Lernens
     * + Ausgabe von Probe: in Konsole und Erfolg jedes einzelnen Durchlaufes in textdatei
     * <p>
     * Gesammtes lernmaterial durchgehen (2x for())
     * in neuronales Netz eingeben; Netz akivieren; Variablen ausgeben
     * mit gewollter Variable vergleichen
     * Ergebnis ausgeben
     */
    public void test() {
        double pStartTime = System.currentTimeMillis();
        System.out.println("Probe: |----------|");
        System.out.print("       |");

        for (int ziffer = 0; ziffer < testImages.length; ziffer++) {
            for (int zifferbild = 0; zifferbild < anzTestImg; zifferbild++) {
                //for (int zifferbild = 0; zifferbild < testImages[ziffer].length; zifferbild++) {
                lsgNum = ziffer;

                buildColorsArr(testImages[ziffer][zifferbild]);
                putInVal();
                activate();
                putOutVal();

                int hIndex = 0;
                double hValue = output[0];
                for (int j = 1; j < output.length; j++) {
                    if (hValue < output[j]) {
                        hValue = output[j];
                        hIndex = j;
                    }
                }
                if (hIndex != lsgNum) {
                    mistakesRun++;
                }

				/*try {

					String name = testImages[ziffer][zifferbild].getName();
					int bildId = Integer.parseInt(name.substring(0, name.length() - 4));
					bw.append("Bild: " + String.format("%02d", bildId) + " Soll: " + lsgNum + " Ist: " + hIndex
							+ " //  ");
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
            }
            imgNumPercent[ziffer] = 100 - (((double) mistakesRun / ((double) (anzTestImg))) * 100);
            //String messageRun = 100 - imgNumpPercent[ziffer] + "% sind Richtig! ";
            mistakesL += mistakesRun;
            mistakesRun = 0;
			/*try {
				bw.newLine();
				bw.write(messageRun);
				bw.newLine();
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}*/

            System.out.print("-");
        }
        System.out.println("|");
        double probeTime = System.currentTimeMillis() - pStartTime;
        startTime -= probeTime;
        //String messageL = 100 - (((double) mistakesL / ((double) (anzTestImg*testImages.length))) * 100) + "% sind Richtig! " + (anzTestImg*testImages.length) + ", " + mistakesL;
        double percent = 100 - (((double) mistakesL / ((double) (anzTestImg * testImages.length))) * 100);
        //100 - (((double) mistakesL / ((double) (anzTestImg*testImages.length))) * 100)
        String messageL = String.format("%.2f", percent) + "%; ";
        mistakesL = 0;
        try {
            //bw.newLine();
            bw.append(messageL);
            //bw.newLine();
            //bw.append("turn: " + turn);
            //bw.newLine();
            //bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxIndex(double[] arr) {
        if (arr == null || arr.length == 0)
            return -1;

        int max = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[max])
                max = i;
        }
        return max;
    }

    /**
     * Methode endOutput
     * <p>
     * Ausgabe der Zusammen fassung des Programmablaufes in textdatei
     */
    public void endOutput() {
        try {
            bw.newLine();
            bw.append("layers: " + layers);
            bw.newLine();
            bw.append("learnValue: " + learnValue);
            bw.newLine();
            bw.append("numOfChanges: " + numOfChanges);
            bw.newLine();
            bw.append("revWithAllImages: " + revWithAllImages);
            bw.newLine();
            bw.append("revisionsOfPrgrm: " + revisions);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methode: buildColorsArr
     * <p>
     * auslesen der Pixel von file
     * einspeisen der werte für schwarz und nicht schwarz in das "colors"-Array
     *
     * @param file Datei, die das Bild enthält
     */
    public void buildColorsArr(File file) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        colors = new int[img.getWidth() * img.getHeight()];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (img.getRGB(x, y) == -1) {
                    colors[x * img.getWidth() + y] = -1;
                } else {
                    colors[x * img.getWidth() + y] = 1;
                }
            }
        }
    }

    /**
     * Methode: buildNet
     * <p>
     * Erstellung des Neuronalen Netzes aufgrund von der Größe des "colors"-Arrays:
     * ein Inputlayer: selbe größe wie colors
     * Hiddenlayer: Menge abhängig von "layers": selbe Größe wie "colors"
     * ein Outputlayer: selbe Länge wie characters
     */
    public void buildNet() {
        units = new NetUnit[layers + 2][];
        for (int i = 0; i <= layers; i++) {
            units[i] = new NetUnit[picsPerLayer];
            for (int j = 0; j < units[i].length; j++) {
                if (i == 0) {
                    units[i][j] = new NetUnit(learnValue, 0);
                } else {
                    units[i][j] = new NetUnit(learnValue, picsPerLayer);
                }
            }
        }
        units[layers + 1] = new NetUnit[characters];
        for (int c = 0; c < characters; c++) {
            units[layers + 1][c] = new NetUnit(learnValue, picsPerLayer);
        }
    }

    /**
     * Methode: putInVal
     * <p>
     * Eingabe der "colors"-Werte in die Inputschicht des "units"-Array an selber x-,y-Position
     */
    public void putInVal() {
        for (int x = 0; x < colors.length; x++) {
            units[0][x].input(((double) colors[x]) / 10000);
        }
    }

    /**
     * Methode: activate
     * <p>
     * Ruft activateLayer für alle layer außer den Eingabelayer auf.
     */
    public void activate() {
        for (int i = 1; i <= layers + 1; i++) {
            activateLayer(i);
        }
    }

    /**
     * Methode: activate Layer
     * <p>
     * Ruft nacheinander die Units aus dem Layer: "layer"-1 auf
     * überprüft ob das die Eingabeschicht ist
     * ja: liest die Aktivierung mit "output()" aus
     * nein: liest die Aktivierung mit "getActValue()" aus
     * Ruft für jede Unit aus "layer"-1" alle Units aus "layer" auf
     * Gibt die Aktivierung mit "activate()" in die aktuelle Unit ein
     *
     * @param layer index des zu aktivierenden Layers
     */
    public void activateLayer(int layer) {
        for (int x1 = 0; x1 < units[layer - 1].length; x1++) {
            double actVal;
            if (layer - 1 == 0) {
                actVal = units[layer - 1][x1].output();
            } else {
                actVal = units[layer - 1][x1].getActValue();
            }

            // alle Units der startreihe durchgehen (StartUnit auswählen)
            for (int x2 = 0; x2 < units[layer].length; x2++) {
                // alle Units der Zielreihe durchtehen (ZielUnit auswählen)
                units[layer][x2].activate(actVal, x1);
            }
        }
    }

    /**
     * Methode: putOutVal
     * <p>
     * Speichert die aktuell in "output" gespeicherten werte in "oldOutput" ab
     * ruft die Aktivitäten der Units des Outputlayers mit "getActValue" ab und speichert sie in "output"
     */
    public void putOutVal() {
        oldOutput = output;
        output = new double[units[layers + 1].length];
        for (int i = 0; i < units[layers + 1].length; i++) {
            output[i] = units[layers + 1][i].getActValue();
        }
    }

    /**
     * Methode: changeValuesRnd
     * <p>
     * Lernmethode auf zufälliger Basis.
     * Füllung des "changedThings"-Arrays mit zufälligen Varablen
     * 0.: der Layer der zielunit der Verbindung
     * 1.: x-Position der zielunit der Verbindung
     * 2.: x-Position der startunit der Verbindung
     * 3.: Entscheidung ob der weight oder bias verändert werden soll
     * 4.: Richtung der Veränderung: 0.1 // -0.1
     * Ausführung der Veränderung
     */
    public void changeValuesRnd() {
        changedThings[0] = rnd.nextInt(units.length - 1) + 1;
        changedThings[1] = rnd.nextInt(units[changedThings[0]].length);

        changedThings[2] = rnd.nextInt(units[changedThings[0] - 1].length); // Start x

        changedThings[3] = rnd.nextInt(2);
        if (changedThings[0] == units.length)
            changedThings[3] = 0;
        changedThings[4] = rnd.nextInt(2);
        // changedThings[4] = 1;
        if (changedThings[3] == 0) {
            units[changedThings[0]][changedThings[1]].addWeight(((double) (changedThings[4] * 2 - 1)) / 10, changedThings[2]);
        } else if (changedThings[3] == 1) {
            units[changedThings[0]][changedThings[1]].addBias(((double) (changedThings[4] * 2 - 1)) / 10);
        } else {
            System.err.println("Falsche ausgabe bei der weight/bias wahl: " + changedThings[3]);
        }
    }

    /**
     * Methode: changeValuesDlt
     * <p>
     * Ruft abhängig von dem Vorhandensein von hidden-Layers die delta-Methoden auf
     * vorhanden: "backPropagation"; nicht vorhanden: nur "basicDelta"
     */
    public void changeValuesDlt() {
        basicDelta();
        if (layers != 0) {
            backPropagation();
        }
    }

    /**
     * Methode basicDelta
     * <p>
     * Einfache Delta-Lernmethode
     * funktioniert nur mit 0 Hidden-Layern
     * Geht jede OutputUnits  durch und berechnet den Unterschied zwischen dem Output
     * und wenn die Units die gewollt Unit ist 0.9, sonst -0,9
     * Geht für jede Outputunit alle Units des Inputlayers durch
     * Addiert die Formel: (delta*"learnValue"*der Output der StartUnit der Verbindung)
     * auf das Gewicht der Verbindung zwischen den beiden Units
     */
    public void basicDelta() {
        for (int k = 0; k < units[units.length - 1].length; k++) {
            //getUnit
            for (int m = 0; m < units[units.length - 2].length; m++) {
                //sendUnit
                double delta2 = 0;
                if (k == lsgNum) {
                    delta2 = 0.9 - output[k];
                } else {
                    delta2 = -0.9 - output[k];
                }
                units[units.length - 1][k].addWeight(delta2 * learnValue * units[units.length - 2][m].output(), m);
                units[units.length - 1][k].delta = delta2;
            }
        }
    }

    /**
     * Methode backPropagation
     * <p>
     * Funktioniert nach dem Schema:
     * 1. weight der letzten ebene verändern
     * 2. mit neuem weight delta nach sum(weight(nextLayerUnits)*delta(nextLayerUnits) berechnen
     * 3. von vorne: mit neuem delta das weight der vorherigen Ebene berechnen
     * 4. wenn bei erster Hidden-Ebene angekommen: Stop
     * <p>
     * O(c)
     * O(a)						O(f)
     * O(d)
     * O(b)						O(g)
     * O(e)
     * <p>
     * bsp: dlt(f) = 0,9 - Out(f)
     * w(c,f) = dlt(f)*Out(c)
     * dlt(c) = w(c,f)*dlt(f) + w(c,g)*dlt(g)
     * w(a,c) = dlt(c)*Out(a)
     * und so weiter!
     */
    public void backPropagation() {
        for (int i = units.length - 2; i > 0; i--) {
            for (int j = 0; j < units[i].length; j++) {
                for (int l = 0; l < units[i + 1].length; l++) {
                    units[i][j].delta += units[i + 1][l].weights[j] * units[i + 1][l].delta;
                }

                for (int n = 0; n < units[i][j].weights.length; n++) {
                    units[i][j].addWeight(units[i][j].delta * units[i - 1][n].output() * learnValue, n);
                }
            }
        }
    }

    /**
     * Methode badRun // analyzeOutput
     * <p>
     * Ruft Analysemethoden auf um die Veränderung der letzten Runde zu überprüfen und bei bedarf zurückzusetzen
     * Gibt außerdem das Ergebnis der Überprüfung als return zurück
     *
     * @return True, wenn die letzte Veränderung der Gewichte den Output verschlechtert hat
     */
    // public void analyzeOutput() {
    public boolean badRun() {
        boolean bool = valGotWorse() || (!valIsBest(output) && valIsBest(oldOutput));
        if (bool) {
            rechange();
        }
        return bool;
    }

    /**
     * Methode: rechange
     * <p>
     * setzt die Veränderungen die durch die zufällige Veränderung zuletzt gemacht wurden zurück
     */
    public void rechange() {
        if (changedThings[3] == 0) {
            units[changedThings[0]][changedThings[1]].addWeight(((double) (changedThings[4] * 2 - 1)) / -10, changedThings[2]);
        } else if (changedThings[3] == 1) {
            units[changedThings[0]][changedThings[1]].addBias(((double) (changedThings[4] * 2 - 1)) / -10);
        } else {
            System.err.println("Falsche ausgabe bei der weight/bias wahl: " + changedThings[3]);
        }
    }

    /**
     * Methode: valisBest
     * <p>
     * Überprüft ob der zweite Wert des Arrays "outputArr" genauso groß ist wie der erste
     * also ob es mehrere größte Werte gibt und gibt sie als return zurück
     *
     * @param outputArr Array das überprüft werden soll
     * @return ob der größte Wert des Array als einziges "lsgNum" entspricht
     */
    public boolean valIsBest(double[] outputArr) {
        return sortArrayDESC(outputArr)[0] == lsgNum && sortArrayDESC(outputArr)[1] != lsgNum;
    }

    /**
     * Methode sortArrayASC
     * <p>
     * sortiert "arr" aufsteigend und gibt das Ergebnis als return zurück
     *
     *
     */
    public static double[] sortArrayASC(double[] arr) {
        Arrays.sort(arr);
        return arr;
    }

    /**
     * Methode sortArrayDESC
     * <p>
     * sortiert "arr" absteigend und gibt das Ergebnis als return zurück
     *
     * @param arr Array das sortiert werden soll
     * @return das sortierte array
     */
    public static double[] sortArrayDESC(double[] arr) {
        sortArrayASC(arr);
        double[] out = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = arr[arr.length - (i + 1)];
        }
        return out;
    }

    /**
     * Methode: valGotWorse
     * <p>
     * Vergleicht den gewollten Output dieses Durchganges mit dem Output des letzen Durchganges, also vor der Veränderung der Lernvariablen
     * Gibt "true" zurück wenn die variable kleiner geworden ist
     *
     * @return ob Ausgabe schlechter geworden ist
     */
    public boolean valGotWorse() {
        return output[lsgNum] < oldOutput[lsgNum];
    }
}
