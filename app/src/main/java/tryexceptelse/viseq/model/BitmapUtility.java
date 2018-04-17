package tryexceptelse.viseq.model;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

// Methods are located here, outside ImageParser in order to allow them
// to be tested directly and re-used if needed.

/**
 * Provides convenience methods for ImageParser and any other classes
 * that require them.
 */
final class BitmapUtility {

    /**
     * Produces cropped bitmap from original.
     * Resulting bitmap has width and height determined by passed
     * ratio values.
     * @param original: Bitmap of full image that is to be cropped.
     * @return Cropped Bitmap.
     */
    @NonNull
    static Bitmap cropBitmapToRatio(
            @NonNull final Bitmap original,
            final double x_ratio,
            final double y_ratio) {
        // PLACEHOLDER
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight());
    }
}
