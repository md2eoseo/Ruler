package com.arcore.ruler;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.google.ar.core.Session;

public class MeasureActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GLSurfaceView mSurfaceView;
    //private MainRednerer mRenderer;
    Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);

        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
    }
}
