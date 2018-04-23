package tryexceptelse.viseq.model;

import android.support.annotation.NonNull;

/**
 * Class modelling a mathematical function; a relationship between a
 * set of inputs and a set of outputs.
 */
public class MathFunction {
    @NonNull private String rawString;
    @NonNull private String xVar;
    @NonNull private String yVar;

    public MathFunction(@NonNull final String s) {
        rawString = s;
    }

    public double calculate(final double x) {
        return x;  // PLACEHOLDER
    }

    // getters

    public String getRawString() {
        return rawString;
    }

    public String getXVariable() {
        return xVar;
    }

    public String getYVariable() {
        return yVar;
    }
}
