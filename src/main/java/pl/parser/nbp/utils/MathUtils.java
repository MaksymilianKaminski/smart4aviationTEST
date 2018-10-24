package pl.parser.nbp.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    private static final int ROUND_PLACES = 4;

    public static double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
