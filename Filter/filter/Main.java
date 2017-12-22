import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



/**
 * Driver class for filter, a class that filters out noise from
 * turbulence data
 *
 * @author Joshua Yurtsever
 */

public class Main {

    /**
     * Takes the name of the file as the input, ARGS[0]. Outputs
     * a new file with the filtered array. ARGS[1] gives the
     * starting position and Args[2] gives the ending position
     */
    public static void main(String... args) throws IOException {
        try {
            if (args.length == 0) {
                throw FilterException.error("No Name of File Given");
            }
            BufferedReader br = Utils.makeReader(args[0]);
            int count = 0;
            int[] cols1 = new int[3];
            int[] cols2 = new int[3];

            Scanner inp = new Scanner(System.in);
            System.out.print("Enter moving average interval: ");
            int avgInterval = Integer.parseInt(inp.next());
            System.out.print("Enter starting index: ");
            int startIndex = Integer.parseInt(inp.next());
            System.out.print("Enter window length: ");
            int windowLength = Integer.parseInt(inp.next());


            if (args.length > 3) {
                int[] temp = new int[args.length - 1];
                for (int i = 1; i < args.length; i++) {
                    temp[i - 1] = Integer.parseInt(args[i]);
                }
                System.arraycopy(temp,0, cols1,0, 3);
                System.arraycopy(temp,3, cols2,0, 3);
            } else {
                System.out.println("Using Defult Columns : 3,4,5 and 15,16,17");
                cols1 = new int[]{3,4,5};
                cols2 = new int[]{15,16,17};
            }

            //Writes the labels
            br.readLine(); br.readLine();
//            String[] lbs = br.readLine().split("\t");
//            String labels = "";
//            for (int i = 0; i < lbs.length && !lbs[i].equals(""); i++) {
//                labels += lbs[i] + ",";
//            }
//            byte[] filtered = labels.getBytes();
//            System.out.println(labels);
            ArrayList<Double> loc1 = new ArrayList<>();
            ArrayList<Double> loc2 = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                boolean stop = false;
                Double n1 = 0.0;
                for (int i : cols1) {
                    if (i > split.length) {
                        stop = true;
                        break;
                    }
                    Double c = Double.parseDouble(split[i]);
                    n1 += c*c;
                }

                Double n2 = 0.0;
                for (int j : cols2) {
                    if (j > split.length || stop) {
                        stop = true;
                        break;
                    }
                    Double c = Double.parseDouble(split[j]);
                    n2 += c*c;
                }

                if (stop) {
                    break;
                }

                loc1.add(Math.sqrt(n1) - 1.0);
                loc2.add(Math.sqrt(n2) - 1.0);

                count += 1;

            }
            List<Double> movAvg1 = new ArrayList<>();
            List<Double> movAvg2 = new ArrayList<>();
            for (int i = 0; i < loc1.size() - avgInterval; i++) {
                Double a1 = 0.0;
                Double a2 = 0.0;
                for (int j = 0; j < avgInterval; j++) {
                    a1 += loc1.get(i + j);
                    a2 += loc2.get(i + j);
                }
                movAvg1.add(a1/avgInterval);
                movAvg2.add(a2/avgInterval);
            }

            int offset = clockOffSet(movAvg1,movAvg2,windowLength,startIndex);
            System.out.println(offset);
            movAvg1 = movAvg1.subList(offset, offset + windowLength);
            movAvg2 = movAvg2.subList(startIndex, startIndex + windowLength);
//            for (int i = 0; i < windowLength; i++) {
//                System.out.print(movAvg1.get(i));
//                System.out.print("      ");
//                System.out.print(movAvg2.get(i));
//                System.out.println();
//            }

            System.out.println("Offset and moving average calculation successful, offset was "
            + String.valueOf(offset - startIndex));
            System.out.print("Enter alpha, the strength of filtering: ");

            double alpha = Double.parseDouble(inp.next());

            double[] signals = new double[windowLength];
            double[] noise = new double[windowLength];
            double[] complexSignals = new double[windowLength];
            double[] complexNoise = new double[windowLength];

            for (int i = 0; i < signals.length; i++) {
                signals[i] = .5 * (movAvg1.get(i) + movAvg2.get(i));
                noise[i] = .5 * (movAvg1.get(i) - movAvg2.get(i));
            }
//            for (int i = 0; i < windowLength; i++) {
//                System.out.print(signals[i]);
//                System.out.print("      ");
//                System.out.print(noise[i]);
//                System.out.println();
//            }
//            System.out.println("\n\n\n");
            double[] original = signals.clone();
            fft(signals,complexSignals);
            fft(noise,complexNoise);
            weiner(signals,complexSignals,noise,complexNoise, alpha);
            inversefft(signals,complexSignals);
//            for (int i = 0; i < windowLength; i++) {
//                System.out.print(signals[i]);
//                System.out.print("      ");
//                System.out.print(original[i]);
//                System.out.println();
//            }
            plot(signals);
        } catch (FilterException exp) {
            System.out.println(exp.getMessage());
        }

    }
    /** Returns the index from the first list, A1, for which the dot product with
     * the interval in the second list is the highest */
    public static int clockOffSet(List<Double> a1, List<Double> a2, int win, int start) {
        if (win >= a1.size()) {
            throw FilterException.error("Error: window same size as file length;" +
                    " please restart program;\n and enter a different value for window length");
        }
        int min = start - win/2;
        int max = start + win/2;
        if (min < 0) {
            throw FilterException.error("Error: starting point must be greater than 1/2" +
                    "the windo size");
        }
        if (max > a1.size()) {
            throw FilterException.error("Error: starting point must be less than 1/2" +
                    "the window size from the end of the data set");
        }
        int k = min;
        Double best = Double.NEGATIVE_INFINITY;

        for (int i = min; i < max; i++) {
            Double temp = innerProduct(a1,a2,i,start,win);

            if (temp > best) {
                k = i; best = temp;
            }
        }
        System.out.println(best);
        return k;
    }

    /** Returns the inner product of the lists starting from indices s1 and s2 in the
     * interval of length len*/
    public static Double innerProduct(List<Double> a1, List<Double> a2, int s1, int s2, int len) {
        Double res = 0.0;
        List<Double> sub1 = normalize(a1.subList(s1, s1 + len));
        List<Double> sub2 = normalize(a2.subList(s2, s2 + len));
        for (int i = 0; i < sub1.size(); i++) {
            res += sub1.get(i)*sub2.get(i);
        }
        return res;
    }

    /** Returns the normalized version of the list*/
    public static List<Double> normalize(List<Double> lst) {
        Double c = 0.0;
        for (Double d : lst) {
            c += d*d;
        }
        c = Math.sqrt(c);
        List<Double> res = new ArrayList<>();
        for (Double d : lst) {
            res.add(d/c);
        }
        return res;
    }
    /** Does FFT with the CORRECT convention
     */
    public static void fft(double[] real, double[] imag) {
        int n = real.length;
        double sqrtn = Math.sqrt((double) n);
        FFT.transform(real,imag);
        for (int i = 0; i < n; i++) {
            real[i] = real[i]/sqrtn;
            imag[i] = imag[i]/sqrtn;
        }
    }

    public static void inversefft(double[] real, double[] imag) {
        int n = real.length;
        double sqrtn = Math.sqrt((double) n);
        FFT.inverseTransform(real,imag);
        for (int i = 0; i < n; i++) {
            real[i] = real[i]/sqrtn;
            imag[i] = imag[i]/sqrtn;
        }
    }


    /** This is given and already FFT array and arrays are same length. Does the Weiner transformation*/
    public static void weiner(double[] realS, double[] imagS,
                              double[] realN, double[] imagN, double alpha) {
        for (int i = 0; i < realS.length; i++) {
            double reS = realS[i];
            double imS = imagS[i];
            double Sf = Math.sqrt(reS*reS + imS*imS);
            double Nf = Math.sqrt(imagN[i]*imagN[i] + realN[i]*realN[i]);
            double w = Sf/(Sf + alpha * Nf);
            realS[i] = w * realS[i];
            imagS[i] = w * imagS[i];
        }
    }

    /** Graphs an array */
    public static void plot(double[] arr) {
        Plot2DPanel plt = new Plot2DPanel();
        double[] x = new double[arr.length];
        plt.addLinePlot("Signals vs Index", arr);
        JFrame  frame= new JFrame("Histogram");
        frame.setContentPane(plt);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
