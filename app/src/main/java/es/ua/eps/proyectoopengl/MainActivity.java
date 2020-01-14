package es.ua.eps.proyectoopengl;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    private OpenGLRenderer openGLRenderer;

    private TextView titleSeekBar;
    private SeekBar seekBar;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        seekBar = new SeekBar(this);
        seekBar.setProgress(90);
        seekBar.setMax(180);

        titleSeekBar = new TextView(this);
        titleSeekBar.setText("Girar cabeza");
        titleSeekBar.setTextColor(Color.WHITE);

        openGLRenderer = new OpenGLRenderer(this);

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            // Para que funcione en el emulador
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            // Asigna nuestro renderer.
            glSurfaceView.setRenderer(openGLRenderer);
            rendererSet = true;
            Toast.makeText(this, "OpenGL ES 2.0 soportado", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Este dispositivo no soporta OpenGL ES 2.0", Toast.LENGTH_LONG).show();
            return;
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mScaleGestureDetector.onTouchEvent(event);

                if (event != null) {
                    // Convert touch coordinates into normalized device
                    // coordinates, keeping in mind that Android's Y
                    // coordinates are inverted.
                    final float normalizedX =   (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                openGLRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                openGLRenderer.handleTouchDrag(normalizedX, normalizedY);
                            }
                        });
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        openGLRenderer.handleSeekBar(progress);
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setContentView(glSurfaceView);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 100, 30, 30);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(70, 30, 30, 30);

        addContentView(seekBar,layoutParams);
        addContentView(titleSeekBar, layoutParams2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.info:
                buildAlertInfo();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void buildAlertInfo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Información de uso");
        builder.setMessage("Con el gesto pinch se puede hacer zoom al modelo.\n\n"
                + "Mediante el uso del seekBar se puede girar la cabeza.\n\n"
                + "Arrastrando el dedo sobre el modelo, éste girará.");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        builder.show();
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    openGLRenderer.handleZoom(mScaleFactor);
                }
            });
            return true;
        }
    }
}
