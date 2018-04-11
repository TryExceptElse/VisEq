package tryexceptelse.viseq;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
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
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CONFIRMATION_FRAGMENT_DIALOG_TAG = "ConfirmationDialog";
    private @Nullable CameraView cameraView;
    private @Nullable Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set camera
        cameraView = findViewById(R.id.camera);
        cameraView.addCallback(cameraCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            if (cameraView == null) {
                throw new IllegalStateException("cameraView == null");
            }
            cameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            1,
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
     * Callback class handling camera events.
     */
    private final CameraView.Callback cameraCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "camera opened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "camera closed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);

            getBackgroundHandler().post(() -> {
                // todo
            });
        }
    };

    /**
     * Gets HandlerThread that runs background tasks, such as image
     * processing, so that it does not interrupt the ui.
     * @return Handler
     */
    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
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
        public static ConfirmationDialogFragment newInstance(
                @StringRes int message,
                String[] permissions,
                int requestCode,
                @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
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
}
