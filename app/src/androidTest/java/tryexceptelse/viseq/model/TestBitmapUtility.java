package tryexceptelse.viseq.model;

import android.graphics.Bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static tryexceptelse.viseq.model.BitmapUtility.cropBitmapToRatio;

/**
 * Tests functionality of BitmapUtility class
 */
public class TestBitmapUtility {
    @Test
    public void testCropBitmapToRatioProducesCorrectSize() {
        final Bitmap.Config config = Bitmap.Config.ARGB_8888;
        final Bitmap originalBitmap = Bitmap.createBitmap(1024, 1024, config);
        final double widthRatio = 0.5;
        final double heightRatio = 0.25;
        final Bitmap croppedBitmap = cropBitmapToRatio(originalBitmap, widthRatio, heightRatio);

        assertEquals(256, croppedBitmap.getHeight());
        assertEquals(512, croppedBitmap.getWidth());
    }
}

