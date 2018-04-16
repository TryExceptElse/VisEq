package tryexceptelse.viseq;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

/**
 * Class Handling plotting of equations.
 */
public class EquationPlot extends XYPlot {
    public EquationPlot(Context context, String title) {
        super(context, title);
    }

    public EquationPlot(Context context, String title, RenderMode mode) {
        super(context, title, mode);
    }

    public EquationPlot(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public EquationPlot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets graph background to be transparent.
     */
    public void makeTransparent() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        int color = Color.TRANSPARENT;
        getBorderPaint().setColor(color);
        getBackgroundPaint().setColor(color);
        XYGraphWidget graphWidget = getGraph();
        graphWidget.getBackgroundPaint().setColor(color);
        graphWidget.getGridBackgroundPaint().setColor(color);
    }

    /**
     * Sets displayed equation.
     * @param equation: String form of equation, ex: "f(x) = 2x + 3".
     * @return boolean indicating whether passed String was valid or not.
     */
    boolean setEquation(@NonNull final String equation) {
        return false;  // PLACEHOLDER
    }
}
