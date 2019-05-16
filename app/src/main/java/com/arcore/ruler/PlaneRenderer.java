package com.arcore.ruler;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Plane;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class PlaneRenderer {
    private static final String TAG = PlaneRenderer.class.getSimpleName();

    private static final int INITIAL_COUNT = 128;

    private final String vertexShaderString =
            "uniform mat4 uMvpMatrix;\n" +
            "uniform vec4 uColor;\n" +
            "attribute vec3 aPosition;\n" +
            "varying vec4 vColor;\n" +
            "void main() {\n" +
            "   vColor = uColor;\n" +
            "   gl_Position = uMvpMatrix * vec4(aPosition.xyz, 1.0);\n" +
            "}";

    private final String fragmentShaderString =
            "precision mediump float;\n" +
            "varying vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_FragColor = vColor;\n" +
            "}";

    private int mProgram;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];

    private FloatBuffer mVertices;
    private ShortBuffer mIndices;
    private float[] mColor;

    public PlaneRenderer(int color, float alpha) {
        float r = Color.red(color) / 255.f;
        float g = Color.green(color) / 255.f;
        float b = Color.blue(color) / 255.f;
        float a = Color.alpha(color) / 255.f;

        mColor = new float[] { r, g, b, alpha };

        mVertices = ByteBuffer.allocateDirect(INITIAL_COUNT * 3 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.position(0);

        mIndices = ByteBuffer.allocateDirect(INITIAL_COUNT * 3 * 3 * Short.BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.position(0);
    }

    public void init() {

        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderString);
        GLES20.glCompileShader(vShader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vertex shader.");
            GLES20.glDeleteShader(vShader);
        }

        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderString);
        GLES20.glCompileShader(fShader);
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fragment shader.");
            GLES20.glDeleteShader(fShader);
        }

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);
        int[] linked = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program.");
        }
    }

    public  void update(Plane plane) {
        float[] planeMatrix = new float[16];
        plane.getCenterPose().toMatrix(planeMatrix, 0);

        setModelMatrix(planeMatrix);

        FloatBuffer polygon = plane.getPolygon();
        if (polygon == null) {
            mVertices.limit(0);
            mIndices.limit(0);
            return;
        }

        polygon.rewind();
        int boundaryVertices = polygon.limit() / 2;
        int numVertices = boundaryVertices * 2;
        int numIndices = boundaryVertices * 3;

        if (mVertices.capacity() < numVertices * 3) {
            int size = mVertices.capacity();
            while (size < numVertices * 3) {
                size *= 2;
            }
            mVertices = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        if (mIndices.capacity() < numIndices) {
            int size = mIndices.capacity();
            while (size < numIndices) {
                size *= 2;
            }
            mIndices = ByteBuffer.allocateDirect(size * Short.BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        }

        mVertices.rewind();
        mVertices.limit(numVertices * 3);

        while (polygon.hasRemaining()) {
            float x = polygon.get();
            float z = polygon.get();
            mVertices.put(x);
            mVertices.put(0.0f);
            mVertices.put(z);
            mVertices.put(x);
            mVertices.put(0.0f);
            mVertices.put(z);
        }

        mIndices.rewind();
        mIndices.limit(numIndices);

        mIndices.put((short) ((boundaryVertices - 1) * 2));
        for (int i = 0; i < boundaryVertices; ++i) {
            mIndices.put((short) (i * 2));
            mIndices.put((short) (i * 2 + 1));
        }
        mIndices.put((short) 1);

        for (int i = 1; i < boundaryVertices / 2; ++i) {
            mIndices.put((short) ((boundaryVertices - 1 - i) * 2 + 1));
            mIndices.put((short) (i * 2 + 1));
        }
        if (boundaryVertices % 2 != 0) {
            mIndices.put((short) ((boundaryVertices / 2) * 2 + 1));
        }

        mVertices.rewind();
        mIndices.rewind();
    }

    public void draw() {
        float[] mvMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0);

        GLES20.glUseProgram(mProgram);

        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetUniformLocation(mProgram, "uColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix");

        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * Float.BYTES, mVertices);

        GLES20.glUniform4f(color, mColor[0], mColor[1], mColor[2], mColor[3]);
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndices.limit(), GLES20.GL_UNSIGNED_SHORT, mIndices);

        GLES20.glDisableVertexAttribArray(position);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void setModelMatrix(float[] modelMatrix) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16);
    }

    public void setProjectionMatrix(float[] projMatrix) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16);
    }

    public void setViewMatrix(float[] viewMatrix) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16);
    }
}
