package tryexceptelse.viseq;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

import java.lang.ref.WeakReference;

import tryexceptelse.viseq.model.ImageParser;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    // TAG used to identify activity or dialog for debugging and other
    // internal uses. Not displayed to user.
    private static final String TAG = "MainActivity";
    private static final String CONFIRMATION_FRAGMENT_DIALOG_TAG = "ConfirmationDialog";

    // Code integer used to identify the type of a message or
    // message-like object.
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int IMAGE_PARSE_MESSAGE_CODE = 1;

    private static final ImageParser imageParser = new ImageParser();
    @Nullable private CameraView cameraView;
    @Nullable private TextView equationReadout;
    @Nullable private Handler backgroundHandler;
    @Nullable private ParseResultMessageHandler parseResultMessageHandler;

    /**
     * On creation, initializes fields, and finalizes ui construction.
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.camera);
        cameraView.addCallback(cameraCallback);
        cameraView.setFacing(CameraView.FACING_BACK);  // Back == Away from screen.
        cameraView.setFlash(CameraView.FLASH_OFF);
        cameraView.setOnClickListener(onClickListener);

        equationReadout = findViewById(R.id.equation_readout);

        final EquationPlot equationPlot = findViewById(R.id.plot);
        equationPlot.makeTransparent();

        parseResultMessageHandler = new ParseResultMessageHandler(this);
    }

    /**
     * On resumption of activity, restarts camera if camera permission
     * has been granted. Otherwise, shows
     * {@link ConfirmationDialogFragment} requesting permission.
     */
    @Override
    protected void onResume() {
        Log.d(TAG, "Resumed");
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            if (cameraView == null) {
                throw new IllegalStateException("cameraView == null");
            }
            Log.d(TAG, "Restarting Camera View");
            cameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), CONFIRMATION_FRAGMENT_DIALOG_TAG);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    1
            );
        }
    }

    /**
     * When activity is paused, suspends any resource-significant
     * operations, such as {@link CameraView}.
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "Paused");
        assert cameraView != null;
        Log.d(TAG, "Stopping Camera View");
        cameraView.stop();
        super.onPause();
    }

    /**
     * Cleans up activity before its destruction.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroyed");
        super.onDestroy();
        if (backgroundHandler != null) {
            backgroundHandler.getLooper().quitSafely();
            backgroundHandler = null;
        }
    }

    /**
     * Called when results of a permission request have been received.
     * @param requestCode: int identifying which permission request
     *                   led to this call.
     * @param permissions: String array of permissions requested.
     * @param grantResults: int array of results.
     */
    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Expected only a single permission & result.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            this,
                            R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT
                    ).show();
                }
                // camera start handled by onResume
                break;
        }
    }

    /**
     * Callback class handling camera events passed by {@link CameraView}.
     */
    private final CameraView.Callback cameraCallback = new CameraView.Callback() {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated {@link CameraView}.
         *                   This has been reported to be null on rare occasions,
         *                   if an err occurs.
         */
        @Override
        public void onCameraOpened(@Nullable CameraView cameraView) {
            Log.d(TAG, "Camera opened.");
        }

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated {@link CameraView}.
         *                   This has been reported to be null on rare occasions,
         *                   if an err occurs.
         */
        @Override
        public void onCameraClosed(@Nullable CameraView cameraView) {
            Log.d(TAG, "Camera closed.");
        }

        /**
         * Called when a picture is taken.
         * In a background thread, sends photo data to {@link ImageParser} to convert data into
         * String equation and then sends results as {@link ImageParser.ImageParseResult}
         * to {@link ParseResultMessageHandler} to handle results in main thread.
         *
         * @param cameraView The associated {@link CameraView}.
         *                   This has been reported to be null on rare occasions,
         *                   if an error occurs.
         * @param data       JPEG data. Presumed to be nullable if an error occurs.
         */
        @Override
        public void onPictureTaken(
                @Nullable final CameraView cameraView,
                @Nullable final byte[] data) {
            // validate arguments.
            if (cameraView == null) {
                Log.w(TAG, "cameraCallback.onPictureTaken was passed null cameraView.");
                return;
            }
            if (data == null) {
                Log.w(TAG, "cameraCallback.onPictureTaken was passed null data.");
                return;
            }
            Log.d(TAG, "Picture Taken. Size: " + data.length);

            getBackgroundHandler().post(() -> {
                final ImageParser.ImageParseResult result = imageParser.parse(data);
                final Message msg = new Message();
                msg.what = IMAGE_PARSE_MESSAGE_CODE;
                msg.obj = result;
                if (parseResultMessageHandler != null) {
                    parseResultMessageHandler.sendMessage(msg);
                } else {
                    Log.d(TAG, "Attempted to send msg but parseResultMessageHandler was null");
                }
            });
        }
    };

    /**
     * Handles clicks made on widgets within this activity.
     */
    private final View.OnClickListener onClickListener = (final View view) -> {
        switch (view.getId()) {
            case R.id.camera:
                if (cameraView != null) {
                    cameraView.takePicture();
                } else {
                    Log.d(TAG, "Attempted to take photo, but cameraView was null.");
                }
                break;
        }
    };

    /**
     * Gets HandlerThread that runs background tasks, such as image
     * processing, so that it does not interrupt the ui.
     * @return Handler
     */
    @NonNull
    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            final HandlerThread thread = new HandlerThread("background");
            thread.start();
            backgroundHandler = new Handler(thread.getLooper());
        }
        return backgroundHandler;
    }

    /**
     * Dialog fragment that handles getting permission from the user for camera use
     */
    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        /**
         * Creates a new instance of ConfirmationDialogFragment.
         * @param message: Message to display to user.
         * @param permissions: Permissions to request
         * @param requestCode: Whether to enable or disable (usually enable; 1)
         * @param notGrantedMessage: Message displayed if permission not granted.
         * @return ConfirmationDialogFragment created using passed arguments.
         */
        @NonNull
        public static ConfirmationDialogFragment newInstance(
                @StringRes final int message,
                @NonNull final String[] permissions,
                final int requestCode,
                @StringRes final int notGrantedMessage) {
            final ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            final Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            if (args == null) {
                throw new IllegalStateException("Arguments == null");
            }
            FragmentActivity activity = getActivity();
            if (activity == null) {
                throw new IllegalStateException("Activity == null");
            }
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> {
                                String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                if (permissions == null) {
                                    throw new IllegalArgumentException("Permissions == null");
                                }
                                ActivityCompat.requestPermissions(getActivity(),
                                        permissions, args.getInt(ARG_REQUEST_CODE));
                            })
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> Toast.makeText(getActivity(),
                                    args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                    Toast.LENGTH_SHORT).show())
                    .create();
        }
    }

    /**
     * Class handling messages received from background thread
     * containing results of image parsing, or other data.
     */
    private static class ParseResultMessageHandler extends Handler {
        // hold a weak reference to prevent leaks
        private WeakReference<MainActivity> activityReference;

        ParseResultMessageHandler(MainActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IMAGE_PARSE_MESSAGE_CODE:
                    final ImageParser.ImageParseResult result =
                            (ImageParser.ImageParseResult) msg.obj;
                    final TextView equationReadout = activityReference.get().equationReadout;
                    if (equationReadout != null) {
                        equationReadout.setText(result.getEquation());
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
