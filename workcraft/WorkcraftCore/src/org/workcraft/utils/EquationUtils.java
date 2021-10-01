package org.workcraft.utils;

import java.util.HashSet;
import java.util.Set;

public class EquationUtils {

    /*
     * Find roots of equation: a * x + b = 0
     */
    public static Set<Double> solveLinearEquation(double a, double b) {
        Set<Double> result = new HashSet<>();
        if (a != 0.0) {
            result.add(-b / a);
        }
        return result;
    }

    /*
     * Find roots of equation: a * x^2 + b * x + c = 0
     */
    public static Set<Double> solveQuadraticEquation(double a, double b, double c) {
        if (a == 0) {
            return solveLinearEquation(b, c);
        }
        Set<Double> result = new HashSet<>();
        double discriminant = b * b - 4.0 * a * c;
        if (discriminant >= 0.0) {
            double d = Math.sqrt(discriminant);
            result.add(0.5 * (-b + d) / a);
            result.add(0.5 * (-b - d) / a);
        }
        return result;
    }

    /*
     * Find roots of equation: a * x^3 + b * x^2 + c * x + d = 0
     */
    public static Set<Double> solveCubicEquation(double a, double b, double c, double d) {
        if (a == 0) {
            return solveQuadraticEquation(b, c, d);
        }
        Set<Double> result = new HashSet<>();
        b = b / a;
        c = c / a;
        d = d / a;

        double p = c / 3 - b * b / 9;
        double q = b * b * b / 27 - b * c / 6 + d / 2;
        double discriminant = p * p * p + q * q;

        if (discriminant < 0) {
            // 3 roots
            double angle = Math.acos(-q / Math.sqrt(-p * p * p));
            double distance = 2 * Math.sqrt(-p);
            for (int i = -1; i <= 1; i++) {
                double theta = (angle - 2 * Math.PI * i) / 3;
                result.add(distance * Math.cos(theta) - b / 3);
            }
        } else {
            if (discriminant < 0.000000001) {
                // 2 roots
                double r = Math.cbrt(-q);
                result.add(2 * r - b / 3);
                result.add(-r - b / 3);
            } else {
                // 1 root
                double r = Math.cbrt(-q + Math.sqrt(discriminant));
                double s = Math.cbrt(-q - Math.sqrt(discriminant));
                result.add(r + s - b / 3);
            }
        }
        return result;
    }

}
