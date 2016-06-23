package function.genotype.statistics;

import utils.MathManager;

/**
 *
 * @author minhe
 */
public class FisherExact {
    
    private static double[] func;
    private static int maxN = 0;
    private static void logFactorial(int n) {
        if (n > maxN) {
            maxN = n;
            func = new double[n + 1];
            func[0] = 0.0;
            for (int i = 1; i <= n; i++) {
                func[i] = func[i - 1] + Math.log(i);
            }
        }
    }

    /**
     * calculates the P-value for this specific state
     *
     * @param a a, b, c, d are the four cells in a 2x2 matrix
     * @param b
     * @param c
     * @param d
     * @return the P-value
     */
    private static double getP(int a, int b, int c, int d) {
        int n = a + b + c + d;
        double p;

        logFactorial(n);
        p = (func[a + b] + func[c + d] + func[a + c] + func[b + d])
                - (func[a] + func[b] + func[c] + func[d] + func[n]);
//        p = (logGamma(a + b +1) + logGamma(c + d +1) + logGamma(a + c +1) + logGamma(b + d +1)) -
//            (logGamma(a +1) + logGamma(b +1) + logGamma(c +1) + logGamma(d +1) + logGamma(n +1));
//        return Math.exp(p);
        return Math.exp(MathManager.roundToDecimals(p));
    }

    private static double getP(int a, int b, int c, int d, int e, int f) {
        int n = a + b + c + d + e + f;
        double p;

        logFactorial(n);
        p = (func[a + d] + func[c + f] + func[b + e] + func[a + b + c] + func[d + e + f])
                - (func[a] + func[b] + func[c] + func[d] + func[e] + func[f] + func[n]);
//        p = (logGamma(a + d +1) + logGamma(c + f +1) + logGamma(b + e +1) + logGamma(a + b + c +1) + logGamma(d + e + f +1)) -
//            (logGamma(a +1) + logGamma(b +1) + logGamma(c +1) + logGamma(d +1) + logGamma(e +1) + logGamma(f +1) + logGamma(n +1));
//        return Math.exp(p);
        return Math.exp(MathManager.roundToDecimals(p));
    }

    public static double getTwoTailedP(int a, int b, int c, int d) {
        int min, i;
        double p = 0;

        double baseP = getP(a, b, c, d);
//         in order for a table under consideration to have its p-value included
//         in the final result, it must have a p-value less than the baseP, i.e.
//         Fisher's exact test computes the probability, given the observed marginal
//         frequencies, of obtaining exactly the frequencies observed and any configuration more extreme.
//         By "more extreme," we mean any configuration (given observed marginals) with a smaller probability of
//         occurrence in the same direction (one-tailed) or in both directions (two-tailed).

        int initialA = a, initialB = b, initialC = c, initialD = d;
        p += baseP;

        min = (c < b) ? c : b;
        for (i = 0; i < min; i++) {
            double tempP = getP(++a, --b, --c, ++d);
//            double diff = (tempP - baseP) / baseP;
            if (tempP <= baseP) {
                p += tempP;
            }
        }

        // reset the values to their original so we can repeat this process for the other side
        a = initialA;
        b = initialB;
        c = initialC;
        d = initialD;

        min = (a < d) ? a : d;

        for (i = 0; i < min; i++) {
            double tempP = getP(--a, ++b, ++c, --d);
//            double diff = (tempP - baseP) / baseP;
            if (tempP <= baseP) {
                p += tempP;
            }
        }

        if (p > 1) {
            p = 1;
        }

        return p;
    }

    public static double getTwoTailedP(int a, int b, int c, int d, int e, int f) {
//        int n = a + b + c + d + e + f;

        double p = 0;
        double baseP = getP(a, b, c, d, e, f);
//        System.out.print("p("+a+","+b+","+c+","+d+","+e+","+f+"): ");
        int n1 = a + b + c;
        int n2 = d + e + f;
        int n3 = a + d;
        int n4 = b + e;
//        int n5 = c + f;

        double tempP = 0.0;
        int min1, min2;
        min1 = Math.min(n1, n3);
        min2 = Math.min(n1, n4);
        for (a = 0; a <= min1; a++) {
            for (b = min2; b >= 0; b--) {
                c = n1 - a - b;
                d = n3 - a;
                e = n4 - b;
                f = n2 - d - e;
                if ((c >= 0) && (d >= 0) && (e >= 0) && (f >= 0)) {
                    tempP = getP(a, b, c, d, e, f);
//                    double diff = (tempP - baseP) / baseP;
                    if (tempP <= baseP) {
//                    if ((tempP <= baseP) || (diff < 1E-6)) {
                        p += tempP;
                    }
                }
            }
        }
//        LogManager.writeAndPrint(p);

        if (p > 1.0) {
            p = 1.0;
        }

        return p;
    }
}
