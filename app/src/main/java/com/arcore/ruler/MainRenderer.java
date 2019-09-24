package com.arcore.ruler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = MainRenderer.class.getSimpleName();

    private boolean mViewportChanged;
    private int mViewportWidth;
    private int mViewportHeight;

    private CameraRenderer mCamera;
    private PointCloudRenderer mPointCloud;
    private PlaneRenderer mPlane;

    /*
    private ObjRenderer mTable;
    private ObjRenderer mChair;
    private ObjRenderer mBed;
    */
    private final int assetsLength = 3;

    //Obj 렌더 배열
    private ObjRenderer[] mObj = new ObjRenderer[assetsLength];
    /*
    private boolean mIsDrawTable = false;
    private boolean mIsDrawChair = false;
    private boolean mIsDrawBed = false;
    //Obj 불린 배열*/
    private boolean mIsDrawObj[] = new boolean[assetsLength];

    private List<Sphere> mSpheres = new ArrayList<Sphere>();
    private List<float[]> mPoints = new ArrayList<float[]>();
    private List<Line> mLines = new ArrayList<Line>();

    private float[] mProjMatrix = new float[16];

    private RenderCallback mRenderCallback;

    protected boolean printOptionEnable = false;


    public interface RenderCallback {
            void preRender() throws CameraNotAvailableException;
    }

    public MainRenderer(RenderCallback callback) {
        mCamera = new CameraRenderer();
        mPointCloud = new PointCloudRenderer();

        mRenderCallback = callback;
    }

    public MainRenderer(Context context, RenderCallback callback) {
        mCamera = new CameraRenderer();
        mPointCloud = new PointCloudRenderer();

        mPlane = new PlaneRenderer(Color.GRAY, 0.5f);


        //Obj 추가하기
        mObj[0] = new ObjRenderer(context, "table.obj", "table.jpg");
        mObj[1] = new ObjRenderer(context, "chair.obj", "chair.jpg");
        mObj[2] = new ObjRenderer(context, "bed.obj", "bed.jpg");

        //mIs 초기화
        for(int i=0;i<assetsLength;i++){
            mIsDrawObj[i]=false;
            Log.d("a","mIsDraw초기화" + mIsDrawObj[i]);
        }

        mRenderCallback = callback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();

        mPlane.init();

        //obj 반복문화
        for(int i=0; i<assetsLength;i++){
            mObj[i].init();
        }
        /*
        mTable.init();
        mChair.init();
        mBed.init();
        */
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        try {
            mRenderCallback.preRender();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }


        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        mPointCloud.draw();

        mPlane.draw();

        for (int i = 0; i < mSpheres.size(); i++) {
            Sphere sphere = mSpheres.get(i);
            if (!sphere.isInitialized()) {
                sphere.init();
            }
            sphere.draw();
        }
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            if (!line.isInitialized()) {
                line.init();
            }
            line.draw();
        }

        //obj 반복문화
        for(int i=0 ; i<assetsLength;i++){
            if(mIsDrawObj[i]){
                mObj[i].draw();
            }
        }

        /*

        if (mIsDrawTable) {
            mTable.draw();
        }
        if (mIsDrawChair) {
            mChair.draw();
        }
        if (mIsDrawBed) {
            mBed.draw();
        }
        */


        try {
            if (printOptionEnable) {
                printOptionEnable = false ;

                int w = mViewportWidth ;
                int h = mViewportHeight  ;

                int b[]=new int[(int) (w*h)];
                int bt[]=new int[(int) (w*h)];
                IntBuffer buffer=IntBuffer.wrap(b);
                buffer.position(0);
                GLES20.glReadPixels(0, 0, w, h,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE, buffer);
                for(int i=0; i<h; i++)
                {
                    //remember, that OpenGL bitmap is incompatible with Android bitmap
                    //and so, some correction need.
                    for(int j=0; j<w; j++)
                    {
                        int pix=b[i*w+j];
                        int pb=(pix>>16)&0xff;
                        int pr=(pix<<16)&0x00ff0000;
                        int pix1=(pix&0xff00ff00) | pr | pb;
                        bt[(h-i-1)*w+j]=pix1;
                    }
                }
                Bitmap inBitmap = null ;
                if (inBitmap == null || !inBitmap.isMutable()
                        || inBitmap.getWidth() != w || inBitmap.getHeight() != h) {
                    inBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                }
                //Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                inBitmap.copyPixelsFromBuffer(buffer);
                //return inBitmap ;
                // return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
                inBitmap = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                inBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);


                //현재 날짜로 이미지 저장
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date currentTime_1 = new Date();
                String dateString = formatter.format(currentTime_1);
                String myfile="Ruler_Capture_"+dateString+".jpeg";


                File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ruler");
                dir_image.mkdirs();
                try {
                    File tmpFile = new File(dir_image,myfile);
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fis.close();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.v("hari", "저장경로:"+dir_image.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }



    }

    public int getTextureId() {
        return mCamera == null ? -1 : mCamera.getTextureId();
    }

    public void onDisplayChanged() {
        mViewportChanged = true;
    }

    public boolean isViewportChanged() {
        return mViewportChanged;
    }

    public int getWidth() {
        return mViewportWidth;
    }

    public int getHeight() {
        return mViewportHeight;
    }

    public void updateSession(Session session, int displayRotation) {
        if (mViewportChanged) {
                session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
                mViewportChanged = false;
        }
    }

    public void transformDisplayGeometry(Frame frame) {
        mCamera.transformDisplayGeometry(frame);
    }

    public void updatePointCloud(PointCloud pointCloud) {
        mPointCloud.update(pointCloud);
    }

    public void updatePlane(Plane plane) {
        mPlane.update(plane);
    }

    //새로 선언
    public void setObjModelMatrix(float[] matrix, int index){
        mObj[index].setModelMatrix(matrix);
    }


    /*
    public void setTableModelMatrix(float[] matrix) {
        mObj[0].setModelMatrix(matrix);
    }

    public void setChairModelMatrix(float[] matrix) {
        mObj[1].setModelMatrix(matrix);
    }

    public void setBedModelMatrix(float[] matrix) {
        mObj[2].setModelMatrix(matrix);
    }
    */



    public void setProjectionMatrix(float[] matrix) {
        System.arraycopy(matrix, 0, mProjMatrix, 0, 16);

        mPointCloud.setProjectionMatrix(matrix);

        mPlane.setProjectionMatrix(matrix);

        //Obj 반복문화
        for(int i=0; i<assetsLength ; i++){
            mObj[i].setProjectionMatrix(matrix);
        }

        /*
        mTable.setProjectionMatrix(matrix);
        mChair.setProjectionMatrix(matrix);
        mBed.setProjectionMatrix(matrix);
        */
    }

    public void updateViewMatrix(float[] matrix) {
        mPointCloud.setViewMatrix(matrix);

        for (int i = 0; i < mSpheres.size(); i++) {
            mSpheres.get(i).setViewMatrix(matrix);
        }

        for (int i = 0; i < mLines.size(); i++) {
            mLines.get(i).setViewMatrix(matrix);
        }

        mPlane.setViewMatrix(matrix);

        //Obj 반복문화
        for(int i=0; i<assetsLength ; i++){
            mObj[i].setViewMatrix(matrix);
        }

        /*
        mTable.setViewMatrix(matrix);
        mChair.setViewMatrix(matrix);
        mBed.setViewMatrix(matrix);
        */
    }

    public void setModelMatrix(float[] matrix) {
        Log.d("a", "setModelMatrix 여부 확인" );
    }

    public int addPoint(float[] point) {
        mPoints.add(point);

        Sphere currentPoint = new Sphere(0.01f, Color.GREEN);
        currentPoint.setProjectionMatrix(mProjMatrix);

        float[] translation = new float[16];
        Matrix.setIdentityM(translation, 0);
        Matrix.translateM(translation, 0, point[0], point[1], point[2]);
        currentPoint.setModelMatrix(translation);

        mSpheres.add(currentPoint);

        if (mPoints.size() >= 2) {
            float[] start = mPoints.get(mPoints.size() - 2);
            float[] end = mPoints.get(mPoints.size() - 1);

            Line currentLine = new Line(start[0], start[1], start[2], end[0], end[1], end[2], 10, Color.YELLOW);
            currentLine.setProjectionMatrix(mProjMatrix);

            float[] identity = new float[16];
            Matrix.setIdentityM(identity, 0);
            currentLine.setModelMatrix(identity);

            mLines.add(currentLine);
        }

        return mSpheres.size();
    }

    public int removePoint() {
        if (mPoints.size() >= 1) {
            mPoints.remove(mPoints.size() - 1);
        }
        if (mSpheres.size() >= 1) {
            mSpheres.remove(mSpheres.size() - 1);
        }
        if (mLines.size() >= 1) {
            mLines.remove(mLines.size() - 1);
        }

        return mSpheres.size();
    }

    public void updateObjViewMatrix(float[] matrix, int index){
        mObj[index].setViewMatrix(matrix);
        Log.d("a", "updateObjViewMatrix여부 확인 : " + index);
    }

    public void updateTableViewMatrix(float[] matrix) { mObj[0].setViewMatrix(matrix);
    }

    public void updateChairViewMatrix(float[] matrix) {
        mObj[1].setViewMatrix(matrix);
    }

    public void updateBedViewMatrix(float[] matrix) {
        mObj[2].setViewMatrix(matrix);
    }



    public void setModelDraw(boolean table, boolean chair, boolean bed) {
        mIsDrawObj[0] = table;
        mIsDrawObj[1] = chair;
        mIsDrawObj[2] = bed;
    }

    public void setObjDraw(boolean[] objExist) {
        int j=0;
        for(boolean i:objExist){
            mIsDrawObj[j]=i;
            Log.d("a", "setObjDraw여부 확인" + mIsDrawObj[j]);
            j++;

        }
    }
}