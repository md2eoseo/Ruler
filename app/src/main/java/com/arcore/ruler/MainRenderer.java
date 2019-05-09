package com.arcore.ruler;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = MainRenderer.class.getSimpleName();

    private boolean mViewportChanged;
    private int mViewportWidth;
    private int mViewportHeight;

    private CameraPreview mCamera;
    private PointCloudRenderer mPointCloud;

    private List<Sphere> mSpheres = new ArrayList<Sphere>();
    private List<float[]> mPoints = new ArrayList<float[]>();
    private List<Line> mLines = new ArrayList<Line>();

    private float[] mProjMatrix = new float[16];

    private RenderCallback mRenderCallback;

    public interface RenderCallback {
            void preRender();
    }

    public MainRenderer(RenderCallback callback) {
        mCamera = new CameraPreview();
        mPointCloud = new PointCloudRenderer();

        mRenderCallback = callback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();
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

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        mPointCloud.draw();

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

    public void setProjectionMatrix(float[] matrix) {
        System.arraycopy(matrix, 0, mProjMatrix, 0, 16);

        mPointCloud.setProjectionMatrix(matrix);
    }

    public void updateViewMatrix(float[] matrix) {
        mPointCloud.setViewMatrix(matrix);

        for (int i = 0; i < mSpheres.size(); i++) {
            mSpheres.get(i).setViewMatrix(matrix);
        }

        for (int i = 0; i < mLines.size(); i++) {
            mLines.get(i).setViewMatrix(matrix);
        }
    }

    public void setModelMatrix(float[] matrix) {
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
}