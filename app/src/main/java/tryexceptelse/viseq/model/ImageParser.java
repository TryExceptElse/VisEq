package tryexceptelse.viseq.model;

import android.support.annotation.Nullable;

/**
 * Handles conversion from image to String equation.
 */
public class ImageParser {

    public ImageParser() {}

    /**
     * Attempts to find an equation in the passed image, and converts
     * it into String format.
     * @param data: Image data as passed to callback from
     * @return ImageParser.ImageParseResult containing String form of
     *          equation and other associated data.
     */
    public ImageParseResult parse(final byte[] data) {
        return new ImageParseResult().setEquation("f(x) = 2x + 1");  // PLACEHOLDER
    }

    /**
     * Stores results of parsing an image.
     *
     * Construction of ImageParseResult takes the form of the builder
     * pattern, but all values are stored internally instead of being
     * used in order to construct a separate class.
     */
    public class ImageParseResult {
        @Nullable private String equation;

        ImageParseResult() {}

        /**
         * Sets String equation that was the result of parsing the image data.
         * @param equation: String form of equation.
         * @return ImageParseResult
         */
        ImageParseResult setEquation(@Nullable final String equation) {
            this.equation = equation;
            return this;
        }

        @Nullable
        public String getEquation() {
            return equation;
        }
    }
}
