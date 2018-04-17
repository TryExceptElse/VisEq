package tryexceptelse.viseq.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.vision.Frame;

import static tryexceptelse.viseq.model.BitmapUtility.cropBitmapToRatio;

/**
 * Handles conversion from image to String equation.
 */
public class ImageParser {
    public static final double Y_PARSE_RATIO = 0.2;  // ratio of image height that is parsed.
    public static final double X_PARSE_RATIO = 1.0;  // ratio of image width that is parsed.

    public ImageParser() {}

    /**
     * Attempts to find an equation in the passed image, and converts
     * it into String format.
     * @param data: Image data as passed to callback from
     * @return ImageParser.ImageParseResult containing String form of
     *          equation and other associated data.
     */
    public ImageParseResult parse(final byte[] data) {
        // First get sub-section of image that is checked for equation
        final Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
        // create sub-bitmap from bitmap of region to be parsed.
        final Bitmap croppedBitmap = cropBitmapToRatio(original, X_PARSE_RATIO, Y_PARSE_RATIO);
        // chunk sub-bitmap into chunk-bitmaps
        final Frame frame = new Frame.Builder().setBitmap(croppedBitmap).build();
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
