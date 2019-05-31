package com.arcore.ruler;
//주석
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocateActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mUserRequestedInstall = true;

    private TextView mTextView;
    private GLSurfaceView mSurfaceView;

    private MainRenderer mRenderer;

    private Session mSession;
    private Config mConfig;

    private float mCurrentX;
    private float mCurrentY;
    private float mScaleFactor = 0.02f;
    private float mRotateFactor = 0.0f;

    private final int TABLE = 0;
    private final int CHAIR = 1;
    private final int BED = 2;

    private int mSelectedModel = -1;
    private float[] mModelMatrix = new float[16];

    private float[] mTableModelMatrix = new float[16];
    private float[] mChairModelMatrix = new float[16];
    private float[] mBedModelMatrix = new float[16];

    private boolean[] mModelInit = { false, false, false };
    private boolean[] mModelPut = { false, false, false };

    private boolean mIsPut = false;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;

    private Button btn_capture_locate;

    //save Check
    private Boolean isSaveClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_locate);

        mTextView = (TextView) findViewById(R.id.txt_locate);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        final DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);

            mGestureDetector = new GestureDetector(this,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent event) {
                            mCurrentX = event.getX();
                            mCurrentY = event.getY();
                            return true;
                        }
                        @Override
                        public boolean onDoubleTap(MotionEvent event) {
                            mCurrentX = event.getX();
                            mCurrentY = event.getY();

                            mIsPut = true;

                            return true;
                        }
                        @Override
                        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                float distanceX, float distanceY) {
                            if (mSelectedModel != -1) {
                                mRotateFactor -= (distanceX / 10);
                                Matrix.rotateM(mModelMatrix, 0, -distanceX / 10, 0.0f, 1.0f, 0.0f);
                            }
                            return true;
                        }
                    });
            mScaleDetector = new ScaleGestureDetector(this,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector detector) {
                            mScaleFactor *= detector.getScaleFactor();
                            if (mSelectedModel != -1) {
                                Matrix.scaleM(mModelMatrix, 0,
                                        detector.getScaleFactor(),
                                        detector.getScaleFactor(),
                                        detector.getScaleFactor());
                            }
                            return true;
                        }
                    });
        }

        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback(){
            @Override
            public void preRender() throws CameraNotAvailableException {
                if (mRenderer.isViewportChanged()) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = mSession.update();
                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.updatePointCloud(pointCloud);
                pointCloud.release();

                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);
                for(Plane plane : planes){
                    if(plane.getTrackingState() == TrackingState.TRACKING
                            && plane.getSubsumedBy() == null){
                        mRenderer.updatePlane(plane);
                    }
                }

                final Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                switch (mSelectedModel) {
                    case TABLE:
                        if (!mModelInit[TABLE]) {
                            float position[] = calculateInitialPosition(mRenderer.getWidth(),
                                    mRenderer.getHeight(), projMatrix, viewMatrix);

                            Matrix.setIdentityM(mModelMatrix, 0);
                            Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
                            Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f);

                            mModelInit[TABLE] = true;
                            mModelPut[TABLE] = false;
                        }
                        if (!mModelPut[TABLE]) {
                            mRenderer.setTableModelMatrix(mModelMatrix);
                        }
                        mRenderer.setModelDraw(true, mModelPut[CHAIR], mModelPut[BED]);

                        if (mModelInit[CHAIR] && !mModelPut[CHAIR]) {
                            mModelInit[CHAIR] = false;
                        }
                        if (mModelInit[BED] && !mModelPut[BED]) {
                            mModelInit[BED] = false;
                        }
                        break;
                    case CHAIR:
                        if (!mModelInit[CHAIR]) {
                            float position[] = calculateInitialPosition(mRenderer.getWidth(),
                                    mRenderer.getHeight(), projMatrix, viewMatrix);

                            Matrix.setIdentityM(mModelMatrix, 0);
                            Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
                            Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f);

                            mModelInit[CHAIR] = true;
                            mModelPut[CHAIR] = false;
                        }
                        if (!mModelPut[CHAIR]) {
                            mRenderer.setChairModelMatrix(mModelMatrix);
                        }
                        mRenderer.setModelDraw(mModelPut[TABLE], true, mModelPut[BED]);

                        if (mModelInit[TABLE] && !mModelPut[TABLE]) {
                            mModelInit[TABLE] = false;
                        }
                        if (mModelInit[BED] && !mModelPut[BED]) {
                            mModelInit[BED] = false;
                        }
                        break;
                    case BED:
                        if (!mModelInit[BED]) {
                            float position[] = calculateInitialPosition(mRenderer.getWidth(),
                                    mRenderer.getHeight(), projMatrix, viewMatrix);

                            Matrix.setIdentityM(mModelMatrix, 0);
                            Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
                            Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f);

                            mModelInit[BED] = true;
                            mModelPut[BED] = false;
                        }
                        if (!mModelPut[BED]) {
                            mRenderer.setBedModelMatrix(mModelMatrix);
                        }
                        mRenderer.setModelDraw(mModelPut[TABLE], mModelPut[CHAIR], true);

                        if (mModelInit[TABLE] && !mModelPut[TABLE]) {
                            mModelInit[TABLE] = false;
                        }
                        if (mModelInit[CHAIR] && !mModelPut[CHAIR]) {
                            mModelInit[CHAIR] = false;
                        }
                        break;
                    default:
                        break;
                }

                if (mIsPut) {
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    for (HitResult result : results) {
                        Trackable trackable = result.getTrackable();
                        Pose pose = result.getHitPose();
                        float[] modelMatrix = new float[16];
                        pose.toMatrix(modelMatrix, 0);
                        Matrix.scaleM(modelMatrix, 0, mScaleFactor, mScaleFactor, mScaleFactor);
                        Matrix.rotateM(modelMatrix, 0, mRotateFactor, 0.0f, 1.0f, 0.0f);
                        mScaleFactor = 0.02f;
                        if (trackable instanceof Plane
                                && ((Plane) trackable).isPoseInPolygon(result.getHitPose())) {
                            switch (mSelectedModel) {
                                case TABLE:
                                    if (!mModelPut[TABLE]) {
                                        mModelPut[TABLE] = true;
                                        mSelectedModel = -1;
                                        System.arraycopy(modelMatrix, 0, mTableModelMatrix, 0, 16);
                                        Matrix.setIdentityM(mModelMatrix, 0);
                                        mIsPut = false;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText(getString(R.string.table_put));
                                            }
                                        });
                                        TimerTask textTask = new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTextView.setText(getString(R.string.not_selected));
                                                    }
                                                });
                                            }
                                        };
                                        Timer textTimer = new Timer();
                                        textTimer.schedule(textTask, 2000);
                                    }
                                    break;
                                case CHAIR:
                                    if (!mModelPut[CHAIR]) {
                                        mModelPut[CHAIR] = true;
                                        mSelectedModel = -1;
                                        System.arraycopy(modelMatrix, 0, mChairModelMatrix, 0, 16);
                                        Matrix.setIdentityM(mModelMatrix, 0);
                                        mIsPut = false;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText(getString(R.string.chair_put));
                                            }
                                        });
                                        TimerTask textTask = new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTextView.setText(getString(R.string.not_selected));
                                                    }
                                                });
                                            }
                                        };
                                        Timer textTimer = new Timer();
                                        textTimer.schedule(textTask, 2000);
                                    }
                                    break;
                                case BED:
                                    if (!mModelPut[BED]) {
                                        mModelPut[BED] = true;
                                        mSelectedModel = -1;
                                        System.arraycopy(modelMatrix, 0, mBedModelMatrix, 0, 16);
                                        Matrix.setIdentityM(mModelMatrix, 0);
                                        mIsPut = false;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText(getString(R.string.bed_put));
                                            }
                                        });
                                        TimerTask textTask = new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTextView.setText(getString(R.string.not_selected));
                                                    }
                                                });
                                            }
                                        };
                                        Timer textTimer = new Timer();
                                        textTimer.schedule(textTask, 2000);
                                    }
                                    break;
                            }
                        }
                        if (!mIsPut) {
                            break;
                        }
                    }
                    if (mIsPut) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(getString(R.string.not_valid_position));
                            }
                        });
                        TimerTask textTask = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (mSelectedModel) {
                                            case TABLE:
                                                mTextView.setText(getString(R.string.table_selected));
                                                break;
                                            case CHAIR:
                                                mTextView.setText(getString(R.string.chair_selected));
                                                break;
                                            case BED:
                                                mTextView.setText(getString(R.string.bed_selected));
                                                break;
                                            default:
                                                mTextView.setText(getString(R.string.not_selected));
                                                break;
                                        }
                                    }
                                });
                            }
                        };
                        Timer textTimer = new Timer();
                        textTimer.schedule(textTask, 2000);
                    }
                    mIsPut = false;
                }

                if (mModelPut[TABLE]) {
                    mRenderer.setTableModelMatrix(mTableModelMatrix);
                    mRenderer.updateTableViewMatrix(viewMatrix);
                    mModelInit[TABLE] = false;
                }
                if (mModelPut[CHAIR]) {
                    mRenderer.setChairModelMatrix(mChairModelMatrix);
                    mRenderer.updateChairViewMatrix(viewMatrix);
                    mModelInit[CHAIR] = false;
                }
                if (mModelPut[BED]) {
                    mRenderer.setBedModelMatrix(mBedModelMatrix);
                    mRenderer.updateBedViewMatrix(viewMatrix);
                    mModelInit[BED] = false;
                }

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }
        });
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(mRenderer);


        //save picutre
        btn_capture_locate = (Button)findViewById(R.id.btn_capture_locate);
        btn_capture_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("hari", "pan button clicked");
                isSaveClick = true;
                mRenderer.printOptionEnable = isSaveClick;
                Toast.makeText(getApplicationContext(), "저장 완료!", Toast.LENGTH_SHORT).show();
                isSaveClick = false;
            }
        });



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestCameraPermission();
        requestMemoryPermission();

        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d(TAG, "ARCore Session created.");
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = false;
                        Log.d(TAG, "ARCore should be installed.");
                        break;
                }
            }
        }
        catch (UnsupportedOperationException e) {
            Log.e(TAG, e.getMessage());
        } catch (UnavailableApkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        } catch (UnavailableUserDeclinedInstallationException e) {
            e.printStackTrace();
        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);
        if (!mSession.isSupported(mConfig)) {
            Log.d(TAG, "This device is not support ARCore.");
        }
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 0);
        }
    }
    private void requestMemoryPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public float[] calculateInitialPosition(int width, int height,
                                            float[] projMat, float[] viewMat) {
        return getScreenPoint(width / 2, height - 300, width, height, projMat, viewMat);
    }

    public float[] getScreenPoint(float x, float y, float w, float h,
                                  float[] projMat, float[] viewMat) {
        float[] position = new float[3];
        float[] direction = new float[3];

        x = x * 2 / w - 1.0f;
        y = (h - y) * 2 / h - 1.0f;

        float[] viewProjMat = new float[16];
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0);

        float[] invertedMat = new float[16];
        Matrix.setIdentityM(invertedMat, 0);
        Matrix.invertM(invertedMat, 0, viewProjMat, 0);

        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];

        Matrix.multiplyMV(nearPlanePoint, 0, invertedMat, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedMat, 0, farScreenPoint, 0);

        position[0] = nearPlanePoint[0] / nearPlanePoint[3];
        position[1] = nearPlanePoint[1] / nearPlanePoint[3];
        position[2] = nearPlanePoint[2] / nearPlanePoint[3];

        direction[0] = farPlanePoint[0] / farPlanePoint[3] - position[0];
        direction[1] = farPlanePoint[1] / farPlanePoint[3] - position[1];
        direction[2] = farPlanePoint[2] / farPlanePoint[3] - position[2];

        normalize(direction);

        position[0] += (direction[0] * 0.1f);
        position[1] += (direction[1] * 0.1f);
        position[2] += (direction[2] * 0.1f);

        return position;
    }

    private void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    private void hideStatusBarAndTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onTableButtonClicked(View view) {
        mSelectedModel = TABLE;
        mTextView.setText(getString(R.string.table_selected));
    }

    public void onChairButtonClicked(View view) {
        mSelectedModel = CHAIR;
        mTextView.setText(getString(R.string.chair_selected));
    }

    public void onBedButtonClicked(View view) {
        mSelectedModel = BED;
        mTextView.setText(getString(R.string.bed_selected));
    }
}