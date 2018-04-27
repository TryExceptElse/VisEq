package tryexceptelse.viseq.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

/**
 * Handles conversion from image to String equation.
 */
public class ImageParser {
    private static final String TAG = "ImageParser";

    public static final double Y_PARSE_RATIO = 0.2;  // ratio of image height that is parsed.
    public static final double X_PARSE_RATIO = 1.0;  // ratio of image width that is parsed.

    private final TextRecognizer recognizer;

    public ImageParser(Context context) {
        recognizer = new TextRecognizer.Builder(context).build();
    }

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
        // Create sub-bitmap from bitmap of region to be parsed.
        final Bitmap croppedBitmap = Bitmap.createBitmap(
                original,
                0,
                0,
                (int)(original.getWidth() * Y_PARSE_RATIO),
                (int)(original.getHeight() * X_PARSE_RATIO));
        // todo: chunk sub-bitmap into chunk-bitmaps
        final Frame frame = new Frame.Builder().setBitmap(croppedBitmap).build();
        // Pass frame to OCR
        SparseArray<TextBlock> blocks = recognizer.detect(frame);
        // Handle blocks.
        return new ImageParseResult().setEquation(parseBlocks(blocks));
    }

    @NonNull
    private String parseBlocks(@NonNull SparseArray<TextBlock> blocks) {
        if (blocks.size() == 0) {
            return "";
        }
        if (blocks.size() > 1) {
            Log.i(TAG, "Multiple blocks found.");
        }
        @NonNull TextBlock block = blocks.get(blocks.keyAt(0));
        return block.getValue();
    }

    /**
     * Stores results of parsing an image.
     *
     * Construction of ImageParseResult takes the form of the builder
     * pattern, but all values are stored internally instead of being
     * used to construct a separate class.
     */
    public class ImageParseResult {
        @Nullable private String equation;
        @Nullable private Point[] corners;  // Corners of image

        ImageParseResult() {
            equation = "";
            corners = null;
        }

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

        /**
         * Sets corners of equation found in image.
         * @return Point[]
         */
        ImageParseResult setCorners(@Nullable final Point[] corners) {
            this.corners = corners;
            return this;
        }

        /**
         * Sets corners of equation found in image, clockwise from
         * upper left corner.
         * @return Point[]
         */
        @Nullable
        public Point[] getCorners() {
            return corners;
        }
    }
}
