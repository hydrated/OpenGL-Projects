package min3d.sampleProject1;

import java.util.Vector;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Rectangle;
import min3d.sampleProject1.ExampleWaterSimulate.WaterWave;
import min3d.vos.Color4;
import min3d.vos.Light;

public class ExampleWaterSimulate extends RendererActivity {

	Object3dContainer myRectangle;
	Object3dContainer oRectangle;
	private final static int Segw =  35;
	private final static int Segh =  50;
	TextView editBox;
	private long now;
	private MyGLSurfaceView myGLSurfaceView;
	private Handler mHandler = new Handler();

	@Override
	public void onUpdateScene() {
		editBox.setText("" + Shared.renderer().fps());
	}

	@Override
	protected void onCreateSetContentView() {
		super.onCreateSetContentView();
		editBox = new TextView(this);
		editBox.setText("" + Shared.renderer().fps());
		editBox.setTextColor(Color.WHITE);
		addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void initScene() {
		Light light = new Light();

		myRectangle = new Rectangle(11, 16, Segw, Segh, new Color4());
		oRectangle = new Rectangle(11, 16, Segw, Segh, new Color4());
		myRectangle.position().z = -10;
		// myRectangle.rotation().y = 180;
		myRectangle.doubleSidedEnabled(true);
		oRectangle.position().z = -10;
		// oRectangle.rotation().y = 180;
		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.backover);
		Shared.textureManager().addTextureId(b, "backover", false);
		myRectangle.textures().addById("backover");
		scene.addChild(myRectangle);
		scene.lightingEnabled(false);
		Shared.renderer().logFps(true);
		now = System.currentTimeMillis();

		/*
		 * WaterWave waterWave1 = new WaterWave(0, 0); WaterWave waterWave2 =
		 * new WaterWave(480, 800); WaterWaveManager.addWaterWave(waterWave1);
		 * WaterWaveManager.addWaterWave(waterWave2);
		 */
	}
	
	@Override
	public void updateScene() {
		WaterWaveManager.calculateWaterWave(myRectangle, oRectangle, Segw, Segh);
		/*for(int x=0; x<=Segh; x++) {
			for(int y=0; y<=Segw; y++) {
				int point = x * (Segw + 1) + y;
				float z = oRectangle.points().getPropertyZ(getRelativeIndex(point-1 , myRectangleSize)) 
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point+1 , myRectangleSize))
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point+Segw+1 , myRectangleSize)) 
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point-Segw-1 , myRectangleSize)) ;
				Log.d("hydra" ,"4 phase " + point + " " +oRectangle.points().getPropertyZ(getRelativeIndex(point-1 , myRectangleSize)) 
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point+1 , myRectangleSize))
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point+Segw , myRectangleSize)) 
						+ oRectangle.points().getPropertyZ(getRelativeIndex(point-Segw , myRectangleSize))  ) ;
				float tmp = myRectangle.points().getPropertyZ(point) ;
				myRectangle.points().setPropertyZ(point,
						(z/2 - tmp) * DRAG  );
				
				//Drag (otherwise water never stop moving)
			}
		}*/
	}
	

	public class WaterWave {
		public final float circleX;
		public final float circleY;
		private final float waveLengthRate;
		public float waveAmplitude;
		public final float waveSpeed;
		public float waveLength;
		public final long StartTime;

		public WaterWave(float x, float y) {
			this(x, y, 30, 1, 1, 2);
		}

		public WaterWave(float x, float y, float waveL, float waveLengthRate, float waveAmplitude, float waveSpeed) {
			this.circleX = x;
			this.circleY = y;
			this.waveLength = waveL;
			this.waveLengthRate = waveLengthRate;
			this.waveAmplitude = waveAmplitude;
			this.waveSpeed = waveSpeed;
			this.StartTime = System.currentTimeMillis();
		}

	}

	public static class WaterWaveManager {
		private static Vector<WaterWave> vWaterWave = new Vector<WaterWave>();
		private static final long timeout = 10000;
		private static final float pixelX = 1080/Segw;
		private static final float pixelY = 1776/Segh;
		private static int location = 0 ;
		
		public static void addWaterWave(WaterWave $WaterWave) {
			if(location>10){
				vWaterWave.setElementAt($WaterWave, location%10) ;
			}
			else {
				vWaterWave.add($WaterWave);
			}
			location++ ;
		}

		public static void calculateWaterWave(Object3dContainer myObject, Object3dContainer oMyObject, int Segw,
				int Segh) {
			synchronized (vWaterWave) {
				int k ;
				final int size = vWaterWave.size();
				for (int i = 0; i <= Segh; i++) {
					for (int j = 0; j <= Segw ; j++) {
						int point = i * (Segw + 1) + j;
						for (k = size - 1; k > -1; k--) {
							if(vWaterWave.get(k).waveAmplitude<0) continue ;
							// calculate if points in out wave , Math.abs( radius-squr(x^2 + y^2) ) < Offset
							float distance = (float) Math.sqrt((j * pixelX - vWaterWave.get(k).circleX)
									* (j * pixelX - vWaterWave.get(k).circleX)
									+ (i * pixelY - vWaterWave.get(k).circleY)
									* (i * pixelY - vWaterWave.get(k).circleY));
							float radius = (System.currentTimeMillis() - vWaterWave.get(k).StartTime)
									/ vWaterWave.get(k).waveSpeed;
							float interval = (distance - radius) / vWaterWave.get(k).waveLength;

							if (Math.abs(distance - radius) < vWaterWave.get(k).waveLength) {
								myObject.points().setPropertyZ(point,
										oMyObject.points().getPropertyZ(point) + (float) Math.sin(interval * Math.PI)*vWaterWave.get(k).waveAmplitude);
								break;
							}
						}
						if( k==-1 ) myObject.points().setPropertyZ(point, oMyObject.points().getPropertyZ(point));
					}
				}
				for (k = size - 1; k > -1; k--) {
					vWaterWave.get(k).waveAmplitude -= 0.01f ;
				}
			}
		}

		public static void removeWaterWave(int index) {
			synchronized (vWaterWave) {
				vWaterWave.remove(index);
			}
		}
	}

	@Override
	protected void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(this);
		glSurfaceView(myGLSurfaceView);
	}

	int myRectangleSize ;
	private final float DRAG = 0.5f ;
	
	private final static int getRelativeIndex(int now , int total) {
		return  now < 0 ? ( now % total + total ) % total : (now % total) ;
	}

	class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);
			// Set this as Renderer
			// Request focus, otherwise buttons won't react
			this.requestFocus();
			this.setFocusableInTouchMode(true);
			//
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			Log.d("hydra", e.toString());

			switch (e.getAction()) {

			case MotionEvent.ACTION_MOVE:
				// Log.d("hydra" , "moving" ) ;
				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_DOWN:
				WaterWaveManager.addWaterWave(new WaterWave(e.getX(), Math.abs(e.getY() - 1776)));
				//myRectangle.points().setPropertyZ(15 ,1) ;
				break;
			}
			return true;
		}

	}
}
