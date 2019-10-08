package com.arcore.ruler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.Arrays.sort;

public class LocateActivity extends Activity {
    private static final String TAG = LocateActivity.class.getSimpleName();
    private boolean mUserRequestedInstall = true;

    private TextView locate_rotate;
    private TextView locate_scale;


    //ㅁㅇㄴㄻㄴㅇㄹ
    private final int assetsLength = 0+(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler/obj/").listFiles().length)/2;


    private TextView mTextView;
    private GLSurfaceView mSurfaceView;

    private MainRenderer mRenderer;

    private Session mSession;
    private Config mConfig;

    // 선택한 좌표를 표현하는 변수
    private float mCurrentX;
    private float mCurrentY;

    private float mScaleFactor = 0.02f;
    private float mRotateFactor = 0.0f;


    /*
    private final int TABLE = 0;
    private final int CHAIR = 1;
    private final int BED = 2;
    */
    private int[] assetsNum = new int[assetsLength];



    //물체가 회전 하는지의 여부를 결정 + 어떤 물체가 선택되었는지를 결정하는 변수
    private int mSelectedModel = -1;

    private float[] mModelMatrix = new float[16];



    //Obj매트릭스 배열화
    private float[][] mObjModelMatrix = new float[assetsLength][16];

    /*
    private float[] mTableModelMatrix = new float[16];
    private float[] mChairModelMatrix = new float[16];
    private float[] mBedModelMatrix = new float[16];
    */

    /*
    private boolean[] mModelInit = { false, false, false };
    private boolean[] mModelPut = { false, false, false }; //모델이 놓여져 있는지 여부
    */

    private boolean[] mModelInit = new boolean[assetsLength];
    private boolean[] mModelPut = new boolean[assetsLength]; //모델이 놓여져 있는지 여부


    //더블탭시 발동하는 스위치 변수. false일 경우에는 위치 지정x, true일 경우에는 위치 지정
    private boolean mIsPut = false;

    private GestureDetector mGestureDetector; //가구회전 변수
    private ScaleGestureDetector mScaleDetector; // 가구 크기조절 변수

    private Button btn_capture_locate;

        //save Check
    private Boolean isSaveClick = false;

    private TextView statText;



    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_locate);

        mTextView = (TextView) findViewById(R.id.txt_locate);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        locate_rotate = (TextView) findViewById(R.id.locate_rotate);
        locate_scale = (TextView) findViewById(R.id.locate_scale);

        //인잇풋 초기화

        for (int i = 0 ; i<assetsLength ; i++) {
            mModelInit[i] = false;
            mModelPut[i] = false;
            assetsNum[i] = i;
        }

        ///////
        File[] ObjFiles;
        ObjFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler/obj/").listFiles();
        final File[] RealObjFiles = new File[(ObjFiles.length/2)];
        final String[] RealObjFilesName = new String[(ObjFiles.length)/2];

        String[] ObjFilesName = new String[ObjFiles.length];

        for(int i=0;i<ObjFiles.length; i++){
            ObjFilesName[i]=ObjFiles[i].getName();
        }

        sort(ObjFilesName);



        int j=0;
        for(int i=0;i<ObjFiles.length;i++){
            if (ObjFilesName[i].contains(".obj")) {
                RealObjFiles[j]=ObjFiles[i];
                RealObjFilesName[j]=ObjFilesName[i];
                j++;
            }
        }

        for(int i=0; i<RealObjFilesName.length;i++){
            RealObjFilesName[i]=RealObjFilesName[i].substring(0,RealObjFilesName[i].length()-4);
        }

        if(assetsLength==0){
            TextView statText = (TextView) findViewById(R.id.statText);
            statText.setText("Obj파일 및 텍스쳐 파일이 없습니다\n'Ruler/obj'에 obj파일 및 jpg파일을 넣어주세요");
        }


        Spinner spinner = (Spinner) findViewById(R.id.ObjSpinner);
        ArrayAdapter<String>adapter;
        String[] adaptString = new String[RealObjFilesName.length+1];
        adaptString[0]="선택하지 않음";
        for(int i=0;i<RealObjFilesName.length;i++){
            adaptString[i+1]=RealObjFilesName[i];
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, adaptString );
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    mTextView.setText("사물이 선택되지 않았습니다.");
                    mSelectedModel=-1;
                }else{
                    mSelectedModel = position-1;
                    mTextView.setText(RealObjFilesName[position-1]+"이 선택되었습니다.");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTextView.setText("사물이 선택되지 않았습니다.");
            }
        });


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
                            //mIsPut은 false로 들어옴
                            mCurrentX = event.getX();
                            mCurrentY = event.getY();
                            mIsPut = true;

                            return true;
                        }
                        @Override
                        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                float distanceX, float distanceY) {

                            //물체의 방향이 실시간으로 조절되는 기능
                            if (mSelectedModel != -1) {
                                mRotateFactor -= (distanceX / 10);
                                mRotateFactor%=360;
                                if(mRotateFactor<0){
                                    mRotateFactor += 360;
                                }
                                Matrix.rotateM(mModelMatrix, 0, -distanceX / 10, 0.0f, 1.0f, 0.0f);
                                String rotateFactor = String.format(Locale.getDefault(), "방향 : %d도", (int)mRotateFactor);
                                locate_rotate.setText(rotateFactor);
                            }
                            return true;
                        }
                    });
            mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector detector) {
                            //크기가 실시간으로 조절되는 기능
                            if (mSelectedModel != -1) {
                                mScaleFactor *= detector.getScaleFactor();
                                String scaleFactor = String.format(Locale.getDefault(), "크기 : %.2f배율", mScaleFactor*100 );
                                locate_scale.setText(scaleFactor);

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


                for(int k=0;k<assetsLength;k++){
                    if(mSelectedModel==k){
                        Log.d("a","사물 선택 시 선택된 물체 번호 : " + k);
                        if (!mModelInit[k]) {
                            float position[] = calculateInitialPosition(mRenderer.getWidth(), mRenderer.getHeight(), projMatrix, viewMatrix);

                            Matrix.setIdentityM(mModelMatrix, 0); //단위행렬 생성. 매트릭스를 만들어냄 -> 오브젝트가 생성되도록 하는 기능
                            Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]); // 평행이동 행렬 : 포지션값만큼 평행이동
                            Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f); // 자기 위치에 생기는 사물의 초기 크기를 인위적으로 잡아줌

                            mModelInit[k] = true;
                            mModelPut[k] = false;
                            Log.d("a","사물 선택 여부 알림 : " + mModelInit[k]);
                        }
                        if (!mModelPut[k]) {
                            mRenderer.setObjModelMatrix(mModelMatrix, k);
                        }
                        mModelPut[k]=true;
                        mRenderer.setObjDraw(mModelPut);
                        mModelPut[k]=false;
                        for(int j=0;j<assetsLength;j++) {
                            if (j != k) {
                                if (mModelInit[j] && !mModelPut[j]) { //init:true, put:false 일 시
                                mModelInit[j] = false;
                                }
                            }
                        }
                    }
                }


                //<<더블탭 이벤트 발생 시>> mIsPut이 true가 되며 아래 이벤트가 발생
                if (mIsPut) {
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    //HitTest를 하여 result를 가져옴. 만약 평면이 존재하면 그 위치에 물체를 놓음
                    for (HitResult result : results) {
                        Trackable trackable = result.getTrackable();
                        Pose pose = result.getHitPose(); // hit된 result를 pose객체에 대입
                        float[] modelMatrix = new float[16];
                        pose.toMatrix(modelMatrix, 0);

                        //지정한 방향과 크기를 적용하는 함수를 사용.
                        Matrix.scaleM(modelMatrix, 0, mScaleFactor, mScaleFactor, mScaleFactor); // 확대/축소 행렬 : 지정된 배수만큼 확대/축소
                        Matrix.rotateM(modelMatrix, 0, mRotateFactor, 0.0f, 1.0f, 0.0f); // 비율만큼 회전 ( 매트릭스, 배열시작점 보통0, 회전각, 회전벡터)

                        mScaleFactor = 0.02f;


                        for(int k=0;k<assetsLength;k++) {
                            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(result.getHitPose())) {
                                Log.d("a", "배치 시 선택된 물체 번호 : " + k);
                                if (mSelectedModel == k) {
                                    if (!mModelPut[k]) {
                                        mModelPut[k] = true;

                                        //회전과 크기 변환을 막는 기능. 단, 주석처리 할 경우 원하는 위치에 놓였다가 바로 사라짐.
                                        mSelectedModel = -1;

                                        System.arraycopy(modelMatrix, 0, mObjModelMatrix[k], 0, 16);
                                        Matrix.setIdentityM(mModelMatrix, 0);

                                        mIsPut = false;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //수정필요
                                                for(int i=0;i<assetsLength;i++){
                                                    if(mModelPut[i]==true){
                                                        mTextView.setText(RealObjFilesName[i]+"을 놓았습니다.");
                                                    }

                                                }

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
                                }
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
                                        for(int k=0;k<assetsLength;k++){
                                            if(mSelectedModel==k){


                                                //스트링 수정필요
                                                mTextView.setText(getString(R.string.table_selected));
                                                break;
                                            }
                                        }


                                        switch (mSelectedModel) {
                                            case 0:
                                                mTextView.setText(getString(R.string.table_selected));
                                            case1:
                                                mTextView.setText(getString(R.string.chair_selected));
                                                break;
                                            case 2:
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


                //원하는 사물을 해당 위치에 배치

                for(int i=0;i<assetsLength;i++){
                    if (mModelPut[i]) {
                        Log.d("a","사물배치시 번호 : " + i);
                        mRenderer.setObjModelMatrix(mObjModelMatrix[i], i);
                        mRenderer.updateObjViewMatrix(viewMatrix, i);
                        mModelInit[i] = false;
                    }
                }

                /*
                if (mModelPut[TABLE]) {
                    mRenderer.setTableModelMatrix(mObjModelMatrix[0]);
                    mRenderer.updateTableViewMatrix(viewMatrix);
                    mModelInit[TABLE] = false;
                }
                if (mModelPut[CHAIR]) {
                    mRenderer.setChairModelMatrix(mObjModelMatrix[1]);
                    mRenderer.updateChairViewMatrix(viewMatrix);
                    mModelInit[CHAIR] = false;
                }
                if (mModelPut[BED]) {
                    mRenderer.setBedModelMatrix(mObjModelMatrix[2]);
                    mRenderer.updateBedViewMatrix(viewMatrix);
                    mModelInit[BED] = false;
                }
                */


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
                isSaveClick = true;
                mRenderer.printOptionEnable = isSaveClick;
                Toast.makeText(getApplicationContext(), "저장 완료!", Toast.LENGTH_SHORT).show();
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


    //물체가 생성될 위치를 잡아주는 함수
    public float[] calculateInitialPosition(int width, int height, float[] projMat, float[] viewMat) {
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onTableButtonClicked(View view) {
        mSelectedModel = 0;
        mTextView.setText(getString(R.string.table_selected));
    }

    public void onChairButtonClicked(View view) {
        mSelectedModel = 1;
        mTextView.setText(getString(R.string.chair_selected));
    }

    public void onBedButtonClicked(View view) {
        mSelectedModel = 2;
        mTextView.setText(getString(R.string.bed_selected));
    }


}