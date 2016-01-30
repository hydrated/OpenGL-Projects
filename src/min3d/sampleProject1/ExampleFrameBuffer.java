package min3d.sampleProject1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL11;

import dalvik.system.VMRuntime;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.Renderer10;
import min3d.core.RendererActivity;
import min3d.core.RendererFrameBuffer;
import min3d.core.Scene;
import min3d.interfaces.ISceneController;
import min3d.objectPrimitives.Cuboid;
import min3d.objectPrimitives.Rectangle;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.LightType;
import min3d.vos.TextureVo;

public class ExampleFrameBuffer extends Activity implements ISceneController{
	
	public Scene scene;
	public Scene scene1;
	protected GLSurfaceView _glSurfaceView;
	
	protected Handler _initSceneHander;
	protected Handler _updateSceneHander;
	
    private boolean _renderContinuously;

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
			//Log.d("hydra", e.toString());
			if (bLockTouched || !bInitReady)
				return false;

			switch (e.getAction()) {

			case MotionEvent.ACTION_MOVE:
				touchEvents(e);
				lightEvents(e);
				iconSpinAnkle = 0f;
				break;
			case MotionEvent.ACTION_UP:
				touchEvents(e);
				lightEvents(e);
				bTouched = false;
				bMoving = false;
				iconSpinAnkle = 0f;
				break;
			case MotionEvent.ACTION_DOWN:
				WaterWaveManager.addWaterWave(new WaterWave(e.getX(), Math.abs(e.getY() - 800)));
				touchEvents(e);
				lightEvents(e);
				bTouched = true;
			}
			return true;
		}

	}

	class TouchBorder {
		private float borderX;
		private float borderY;
		private float startX;
		private float startY;
		private float touchX;
		private float touchY;
		private long systemtime = 0;
		private int cItemIndex;

		TouchBorder(IntBuffer viewport) {
			if (viewport.limit() < 4)
				return;
			startX = viewport.get(0);
			startY = viewport.get(1);
			borderX = viewport.get(2);
			borderY = viewport.get(3);
		}

		public void getTouch(float X, float Y) {
			//Log.d(TAG, "getting touch");
			if (Math.abs(X - touchX) + Math.abs(Y - touchY) < accumulation
					&& System.currentTimeMillis() - systemtime < 400) {
				if (cItemIndex < mApps.size() && Math.abs(cuboid.rotation().y % fQuarterofRoundAngle) < 2)
					zoomStart(cItemIndex);
			}
		}

		public void setBorder(IntBuffer viewport) {
			if (viewport.limit() < 4)
				return;
			startX = viewport.get(0);
			startY = viewport.get(1);
			borderX = viewport.get(2);
			borderY = viewport.get(3);
		}

		public void setTouch(float X, float Y) {
			//Log.d(TAG, "setting touch");
			systemtime = System.currentTimeMillis();
			touchX = X;
			touchY = Y;
			int x = (int) (touchX / borderX * rowIconNumber);
			int y = (int) (touchY / borderY * colIconNumber);
			int z = (int) (-cuboid.rotation().y / fQuarterofRoundAngle);
			//if (z < 0)
				//z += 4;
			//z = Math.abs(z - 4) % 4;
			z = getRelativeIndex(z , totalFaces) ;
			//Log.d("hydra", "icon" + x + " " + y + " " + z);
			cItemIndex = x + rowIconNumber * y + totalIconNumber * z;
			//spinIcon(cItemIndex);
			coloringBackground(cItemIndex);
		}
	}
	
	private final static int getRelativeIndex(int now , int total) {
		return  now < 0 ? ( now % total + total ) % total : (now % total) ;
	}
	private final String TAG = "ExampleLauncher";
	private Cuboid cuboid;
	private Object3dContainer _plane;
	private float fixedsize;
	private List<ResolveInfo> mApps;
	private TextView editBox;
	boolean bTouched = false;
	boolean bMoving = false;
	boolean bLightDirectionDirty = false;
	boolean bLightWorkaround = false;
	boolean bLockTouched = false;
	boolean bLockRotationScreen = false;
	boolean bInitReady = false ;
	final float mBackGroundBorder = 1.2f;
	private float aspectRatio = 0f;
	private TouchBorder mTouchBorder;
	private MyGLSurfaceView myGLSurfaceView;
	public int rowIconNumber = 4;
	public int colIconNumber = 5;
	final private int totalIconNumber = rowIconNumber * colIconNumber ;

	private Handler mHandler = new Handler();

	private int itemIndex = 0;
	private float iconSpinAnkle = 0f;

	private SensorManager mSensorManager = null;
	
	private Sensor sensor;

	//tunable
	private final float gyroLimitY = 15;
	private final float iconSize = 0.5f ;
	

	private final SensorEventListener mySensorListenor = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(!bInitReady || bLockRotationScreen ) return ;
			if (event.values[1] > gyroLimitY ) {
				bLockRotationScreen = true;
				RotationScreenCounter = 0;
				mHandler.post(RotationScreenRight);
			} else if (event.values[1] < -1 * gyroLimitY ) {
				bLockRotationScreen = true;
				RotationScreenCounter = 0;
				mHandler.post(RotationScreenLeft);
			}
		}
	};

	private int RotationScreenCounter = 0;

	Runnable RotationScreenRight = new Runnable() {
		public void run() {
			cuboid.rotation().y += 5;
			RotationScreenCounter++;
			if (RotationScreenCounter == 14) {
				RotationScreenCounter = 0;
				bLockRotationScreen = false;
				return;
			}
			mHandler.postDelayed(RotationScreenRight, 50);
			
		}
	};

	Runnable RotationScreenLeft = new Runnable() {
		public void run() {
			cuboid.rotation().y -= 5;
			RotationScreenCounter++;
			if (RotationScreenCounter == 14) {
				RotationScreenCounter = 0;
				bLockRotationScreen = false;
				return;
			}
			mHandler.postDelayed(RotationScreenLeft, 50);
		}
	};

	private final float flingSensity = 2f;

	private float _dx;

	float mPreviousX;


	float mPreviousY;

	float adjustValue = 1.0f;

	float pre_d1x, pre_d1y;
	
	final int zoomInSteps = 100;


	final float offsetCamera = 1.0f;

	private Runnable zoomInRunnable = new Runnable() {
		public void run() {
			// Log.d("hydra" , "runnableing" + scene.camera().position.z) ;
			scene.camera().position.z -= (float) (5f - 1.05f) / zoomInSteps;
			scene.camera().position.x -= (1.0f - (float) (itemIndex % totalIconNumber % rowIconNumber) * 2
					/ (rowIconNumber - 1))
					* offsetCamera / zoomInSteps;
			scene.camera().position.y += (2.0f - (float) ((int) itemIndex % totalIconNumber / rowIconNumber) * 4
					/ (colIconNumber - 1))
					* offsetCamera / zoomInSteps;
			// scene.getChildAt((int)itemIndex/20 + 1).scale().x = 1.0f +
			// (float)1/zoomInSteps;
			// scene.getChildAt((int)itemIndex/20 + 1).scale().y = 1.0f +
			// (float)1/zoomInSteps;
			if (RotationDimension == 0) {
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).rotation().x += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).doubleSidedEnabled(true);
			} else if (RotationDimension == 1) {
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).rotation().y += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).doubleSidedEnabled(true);
			} else {
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).rotation().z += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle) , cuboidFaces)].getChildAt(itemIndex % totalIconNumber).doubleSidedEnabled(true);
			}
			scene.camera().setDirtyFlag();

			if (scene.camera().position.z < 1.05f) {
				mHandler.postDelayed(startActivityLate, 100);
			} else {
				mHandler.postDelayed(zoomInRunnable, 5);
			}
		}
	};

	private Runnable startActivityLate = new Runnable() {
		public void run() {
			Intent intent = new Intent();
			intent.setClassName(mApps.get(itemIndex).activityInfo.packageName, mApps.get(itemIndex).activityInfo.name);
			startActivitySafely(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			finish();
		}
	};

	final float fvIconSpinAnkle = 5f;
	
	private Runnable spinIconR = new Runnable() {
		public void run() {
			if (!bTouched || bMoving)
				return;
			iconSpinAnkle = fvIconSpinAnkle;
		}
	};
	
	private Runnable stepLighting = new Runnable() {
		public void run() {
			if(lightAttenuation < 0.4f ) {
				lightAttenuation += 0.02f ;
				mHandler.postDelayed(stepLighting, 50) ;
				scene.lights().get(1).attenuationSetAll(lightAttenuation, 0.0f, 0.0f);
			}
		}
	};
	
	int RotationDimension = 0;
	float accumulation = 10f;
	float lightBorderX;
	float lightBorderY;

	
/*	@Override
	protected void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(this);
		myGLSurfaceView.setZOrderOnTop(true);
		myGLSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
		myGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		glSurfaceView(myGLSurfaceView);
	}*/
	
	
	float lightAttenuation = 0.1f ;

	
	private int faceStage = 0 ;

	private final int cuboidFaces = 4 ;
	
	private int totalFaces ;


	private final float fTotalAngleofIconSpin = 360f ;
	private final float fQuarterofRoundAngle = 90f ;
	private final float fHalfofQuart = 45f ;
	
	@Override
	public void initScene() {
		scene.backgroundColor().setAll(0x00000000);
		initObject() ;
		initLight() ;
		initSensor() ;
		initLauncher() ;
		Shared.rendererFrameBuffer().logFps(true);
		bInitReady = true ;
		
		initScene1() ;
	}
	
	public void onUpdateScene() {
		editBox.setText("" + Shared.rendererFrameBuffer().fps());
	}
	
	@Override
	public void updateScene() {
		if (bLightDirectionDirty) {

			scene.lights().get(0).direction.setDirtyFlag();
			// Log.d("hydra" , scene.lights().get(0).direction.toString() + " "
			// + scene.lights().get(0).position.toString()) ;
		}

		if (_dx != 0) {
			cuboid.rotation().y += _dx;
			if (Math.abs(_plane.position().x + _dx / 100) < mBackGroundBorder)
				_plane.position().x += _dx / 100;
			_dx = 0;
		}

		if (!bTouched && !bMoving && !bLockRotationScreen && Math.abs(cuboid.rotation().y % fQuarterofRoundAngle) > 1)
			adjustView();

		if (iconSpinAnkle != 0) {
			//Log.d("hydra", "spin icon at" + itemIndex);
			cuboid.getFaces()[(int) itemIndex / totalIconNumber].getChildAt(itemIndex % totalIconNumber).rotation().y += iconSpinAnkle;
			cuboid.getFaces()[(int) itemIndex / totalIconNumber].getChildAt(itemIndex % totalIconNumber).doubleSidedEnabled(true);
		}
		// Log.d("hydra" , ""+_plane.position().x) ;
		dynamicChangeTexture () ;
		
		
		updateScene1() ;
	}
	
	
	private void adjustView() {
		if (Math.abs(cuboid.rotation().y) % fQuarterofRoundAngle > fHalfofQuart) {
			cuboid.rotation().y += (cuboid.rotation().y % fQuarterofRoundAngle) / 2;
		} else {
			cuboid.rotation().y -= (cuboid.rotation().y % fQuarterofRoundAngle) / 2;
		}
	}

	private void dynamicChangeTexture() {
		int innerFaceStage ;
		if(cuboid.rotation().y > -fHalfofQuart ) {
			innerFaceStage = (int) -((cuboid.rotation().y + fHalfofQuart) / fQuarterofRoundAngle) ;
		} else {
			innerFaceStage = (int) -(((cuboid.rotation().y + fHalfofQuart) / fQuarterofRoundAngle) -1) ;
		}
		
		if(innerFaceStage > faceStage) {
			faceStage = innerFaceStage ;
			int tmpIconIndex = getRelativeIndex(innerFaceStage + 1 , totalFaces) ; 
			int tmpIconIndexNext = getRelativeIndex(innerFaceStage + 2 , totalFaces) ;
			int tmpFaceIndex = getRelativeIndex(innerFaceStage + 1 , cuboidFaces) ;
			int tmpFaceIndexNext = getRelativeIndex(innerFaceStage + 2 , cuboidFaces) ;
			for(int i = 0 ; i < totalIconNumber ; i++) {
				String id = "icon" + String.valueOf(tmpIconIndex*totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndex].getChildAt(i).textures().addReplace(id); ;
			}
			for(int i = 0 ; i < totalIconNumber ; i++) {
				String id = "icon" + String.valueOf(tmpIconIndexNext*totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndexNext].getChildAt(i).textures().addReplace(id); ;
			}
			//Log.d("hydra" , " " + tmpIconIndex2 + " "+ tmpIconIndex3 +" " + tmpFaceIndex2 + " "+ tmpFaceIndex3 ) ;
		} else if (innerFaceStage < faceStage) {
			faceStage = innerFaceStage ;
			int tmpIconIndex = getRelativeIndex(innerFaceStage - 1 , totalFaces) ; 
			int tmpIconIndexNext = getRelativeIndex(innerFaceStage - 2 , totalFaces) ;
			int tmpFaceIndex = getRelativeIndex(innerFaceStage - 1 , cuboidFaces) ;
			int tmpFaceIndexNext = getRelativeIndex(innerFaceStage - 2 , cuboidFaces) ;
			for(int i = 0 ; i < totalIconNumber ; i++) {
				String id = "icon" + String.valueOf(tmpIconIndex*totalIconNumber+i);
				cuboid.getFaces()[tmpFaceIndex].getChildAt(i).textures().addReplace(id); ;
			}
			for(int i = 0 ; i < totalIconNumber ; i++) {
				String id = "icon" + String.valueOf(tmpIconIndexNext*totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndexNext].getChildAt(i).textures().addReplace(id); ;
			}
			//Log.d("hydra" , " " + tmpIconIndex2 + " "+ tmpIconIndex3 +" " + tmpFaceIndex2 + " "+ tmpFaceIndex3 ) ;
		}
		
		//Log.d("hydra", ""+cuboid.rotation().y + " " + faceStage ) ;
	}
	private void initLauncher() {

		loadApps();
		int i , size;
		Color4 color = new Color4(0,0,0,0);

		for (int col = 0; col < colIconNumber; ++col) {
			for (int row = 0; row < rowIconNumber; ++row) {
				Rectangle mRectangle = new Rectangle(iconSize, iconSize, 5, 5, color);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[0].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize, 5, 5, color);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[1].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize, 5, 5, color);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[2].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize, 5, 5, color);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[3].addChild(mRectangle);
			}
		}

		totalFaces = (int)( (mApps.size()-1) / totalIconNumber ) +1;
		Log.d("hydra" , "" + totalFaces + " " + mApps.size() ) ;
		for (i = 0 , size = Math.min(mApps.size() ,80); i < size; ++i) {

			ResolveInfo info = mApps.get(i);
			// info.activityInfo.loadIcon(getPackageManager());
			Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()), 255);

			String id = "icon" + String.valueOf(i);
			Shared.textureManager().addTextureId(b, id, false);

			//cuboid.getFaces()[(int) (i /totalIconNumber)].getChildAt(i % totalIconNumber).textures().addById(id);
			// cuboid.addTexture(Cuboid.Face.All, b, id);

			b.recycle();
		}
		for (; i % totalIconNumber != 0; ++i) {
			ResolveInfo info = mApps.get(1);
			// info.activityInfo.loadIcon(getPackageManager());
			Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()), 0);

			String id = "icon" + String.valueOf(i);
			Shared.textureManager().addTextureId(b, id, false);

			//cuboid.getFaces()[(int)i/totalIconNumber].getChildAt(i%totalIconNumber).textures().addById(id);
			// cuboid.addTexture(Cuboid.Face.All, b, id);

			b.recycle();
		}
		for(int n=-1 ; n<2 ; n++) {
			int innerFaceIndex = getRelativeIndex(n,4) ;
			int innerRelativeIndex = getRelativeIndex(n,totalFaces) ;
			Log.d("hydra" , " " + innerFaceIndex + " " + innerRelativeIndex) ;
			for(int m=0 ; m<totalIconNumber ; m++) {
				String id = "icon" + String.valueOf(innerRelativeIndex*totalIconNumber+m);
				cuboid.getFaces()[innerFaceIndex].getChildAt(m).textures().addReplace(id);
			}
		}

	}
	
	private void initLight() {
		Light light = new Light();
		// light.ambient.setAll(0xfffffff);
		light.ambient.setAll(0xffffffff);
		light.specular.setAll(0xffffffff);
		light.diffuse.setAll(0xffffffff);
		light.emissive.setAll(0xffffffff);
		light.position.setAll(0, 0, 6);
		light.spotCutoffAngle(15f);
		light.direction.setAll(0, 0, -1f);
		light._spotExponent.set(96f);
		light.attenuationSetAll(0.005f, 0.005f, 0.0015f);
		light.type(LightType.POSITIONAL);
		Light light1 = new Light();
		// light.ambient.setAll(0xfffffff);
		light1.ambient.setAll(0xffffffff);
		light1.specular.setAll(0xffffffff);
		light1.diffuse.setAll(0xffffffff);
		light1.emissive.setAll(0xffffffff);
		light1.position.setAll(0, 0, 6);
		light1.type(LightType.POSITIONAL);
		light1.spotCutoffAngle(45f);
		light1.direction.setAll(0f, 0f, -1f);
		// light1._spotExponent.set(10f) ;
		light1.attenuationSetAll(lightAttenuation, 0.0f, 0.0f);
		scene.lights().add(light);
		scene.lights().add(light1);
		scene.lightingEnabled(true) ;
	}

	private void initObject() {
		
		IntBuffer viewport = IntBuffer.allocate(4);
		Shared.rendererFrameBuffer().gl().glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Log.d("hydra", "" + viewport.get(0) + viewport.get(1) + viewport.get(2) + viewport.get(3));
		aspectRatio = (float) viewport.get(2) / viewport.get(3);
		mTouchBorder = new TouchBorder(viewport);
		lightBorderX = 0.3f;
		lightBorderY = -1 * lightBorderX * mTouchBorder.borderY / mTouchBorder.borderX;
		fixedsize = aspectRatio > 1 ? 5 : 3;
		cuboid = new Cuboid(fixedsize, fixedsize / aspectRatio, 1, 1);
		// cuboid.uvs().
		// cuboid = new Cuboid(0.1f, 0.1f, 1, 1);
		_plane = new Rectangle(fixedsize * 4, fixedsize * 4 / aspectRatio, 20, 40, new Color4());
		// cuboid.addTexture(Cuboid.Face.All, R.drawable.blacksheep, "all");

		// Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.glass);

		// Upload the Bitmap via TextureManager and assign it a
		// textureId ("uglysquares").

		// Shared.textureManager().addTextureId(b, "glass", false);

		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.desert);
		Shared.textureManager().addTextureId(b, "desert", false);
		_plane.textures().addById("desert");
		_plane.rotation().y = 180;
		_plane.position().z = -10f;
		_plane.lightingEnabled(true);
		_plane.normalsEnabled(true);
		scene.addChild(_plane);
		b.recycle();
		scene.addChild(cuboid);
		scene.camera().target.z = -100f;
	}

	private void initSensor() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager == null) {
			return;
		} else {
			sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			if (sensor == null) {
				return;
			} else {
				mSensorManager.registerListener(mySensorListenor, sensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
		}
	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}
	
	private void setLightDirection(MotionEvent e) {

		float LightDirectionX = (e.getX() - mTouchBorder.borderX / 2) * lightBorderX * 2 / mTouchBorder.borderX;
		float LightDirectionY = (e.getY() - mTouchBorder.borderY / 2) * lightBorderY * 2 / mTouchBorder.borderY;
		scene.lights().get(0).direction.setX(LightDirectionX);
		scene.lights().get(0).direction.setY(LightDirectionY);
		bLightDirectionDirty = true;
		// Log.d("hydra" , "light point x:" + LightDirectionX + " y:" +
		// LightDirectionY ) ;
	}

	private void touchEvents(MotionEvent e) {
		float x = e.getX() * adjustValue;
		float y = e.getY() * adjustValue;
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setLightDirection(e);
			mTouchBorder.setTouch(e.getX(), e.getY());
			break;

		case MotionEvent.ACTION_UP:
			mTouchBorder.getTouch(e.getX(), e.getY());
			break;

		case MotionEvent.ACTION_MOVE:
			if (e.getPointerCount() == 1) {
				_dx = x - mPreviousX;
			} 
			if(!bMoving) {
				if(Math.abs(_dx) > flingSensity  ){
					bMoving = true ;
				} else {
					_dx = 0 ;
				}
			}
				
			break;
		}
		mPreviousX = x;
		mPreviousY = y;
	}
	
	private void lightEvents(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHandler.post(stepLighting) ;
			setLightDirection(e);
			lightAttenuation = 0.1f ;
			break ;
		case MotionEvent.ACTION_UP:	
			mHandler.removeCallbacks(stepLighting) ;
			scene.lights().get(1).attenuationSetAll(0.1f, 0.0f, 0.0f);
			break ;
		case MotionEvent.ACTION_MOVE:
			setLightDirection(e);
			break ;
		}
	}

	protected void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(this);
		_glSurfaceView = myGLSurfaceView ;
	}


	protected void onCreateSetContentView() {
		setContentView(_glSurfaceView);
		editBox = new TextView(this);
		editBox.setText("" + Shared.rendererFrameBuffer().fps());
		editBox.setTextColor(Color.WHITE);
		addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(mySensorListenor);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	void coloringBackground(int $itemIndex) {
		/*
		 * Log.d("hydra","coloringhahahhahahah") ; TextureVo tmpTextureVo = new
		 * TextureVo("icontouch"+$itemIndex);
		 * cuboid.getFaces()[(int)itemIndex/20
		 * ].getChildAt(itemIndex%20).textures().addReplace(tmpTextureVo) ;
		 */
	}
	Bitmap drawableToBitmap(Drawable drawable, int alpha) {

		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), c);
		// Bitmap bitmap = Bitmap.createBitmap( 128, 128, c);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = (float) 128/ width;
		float scaleHeight = (float) 128/ height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// matrix.postRotate(180) ;

		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		drawable.setAlpha(alpha);
		drawable.draw(canvas);
		return bitmap;
	}

	// for (int k = 0; k <
	// cuboid.getFaces()[(int)(0)].getChildAt(0).uvs().size()
	// ; ++k) {
	// Log.d("hahaha",
	// cuboid.getFaces()[(int)(0)].getChildAt(0).uvs().getAsUv(k).toString());
	// }
	// MediaStore.Images.Media.insertImage(getContentResolver(), b, "hahahaha" +
	// ".jpg Card Image", "hahahaha"+ ".jpg Card Image");
	Bitmap drawableToBitmap(Drawable drawable, int alpha, Color4 color) {

		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), c);
		// Bitmap bitmap = Bitmap.createBitmap( 128, 128, c);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = (float) 64 / width;
		float scaleHeight = (float) 64 / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// matrix.postRotate(180) ;

		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.argb(color.a, color.r, color.g, color.b));
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		drawable.setAlpha(alpha);
		drawable.draw(canvas);
		return bitmap;
	}

	void spinIcon(int $itemIndex) {
		this.itemIndex = $itemIndex;
		mHandler.postDelayed(spinIconR, 400);
	}

	void startActivitySafely(Intent intent) {
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "activity_not_found", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unable to launch. tag=" + " intent=" + intent, e);
		} catch (SecurityException e) {
			Toast.makeText(this, "activity_not_found", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Launcher does not have the permission to launch " + intent
					+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
					+ "or use the exported attribute for this activity. " + "tag=" + " intent=" + intent, e);
		}
	}
	void zoomStart(int $itemIndex) {
		this.itemIndex = $itemIndex ;
		bLockTouched = true ;
		bLockRotationScreen = true ;
		RotationDimension = (int) (Math.random() * 3);
		mHandler.post(zoomInRunnable);
	}
	
    public void renderContinuously(boolean $b)
    {
    	_renderContinuously = $b;
    	if (_renderContinuously)
    		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    	
    	else
    		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

	public Handler getInitSceneHandler()
	{
		return _initSceneHander;
	}
	
	public Handler getUpdateSceneHandler()
	{
		return _updateSceneHander;
	}

    public Runnable getInitSceneRunnable()
    {
    	return _initSceneRunnable;
    }
	
    public Runnable getUpdateSceneRunnable()
    {
    	return _updateSceneRunnable;
    }
	final Runnable _initSceneRunnable = new Runnable() 
	{
        public void run() {
            onInitScene();
        }
    };
    
	final Runnable _updateSceneRunnable = new Runnable() 
    {
        public void run() {
            onUpdateScene();
        }
    };
    
    public void onInitScene()
    {
    }
    
    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		_initSceneHander = new Handler();
		_updateSceneHander = new Handler();
		
		//
		// These 4 lines are important.
		//
		Shared.context(this);
		scene = new Scene(this);
		scene1 = new Scene(this);
		RendererFrameBuffer r = new RendererFrameBuffer(scene,scene1);
		Shared.renderer(r);
		
		_glSurfaceView = new GLSurfaceView(this);
        glSurfaceViewConfig();
		_glSurfaceView.setRenderer(r);
		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
        onCreateSetContentView();
	}
    
    /**
     * Called _after_ updateScene()
     * Unlike initScene(), gets called from the UI thread.
     */
    
	Object3dContainer myRectangle;
	Object3dContainer oRectangle;
	private final static int Segw =  21;
	private final static int Segh =  35;
	private long now;
    
	
	private void updateScene1() {
		WaterWaveManager.calculateWaterWave(myRectangle, oRectangle, Segw, Segh); 
	}
	
	//water class
	private void initScene1() {
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
		myRectangle.textures().addById("MyFrameBuffer");
		scene1.addChild(myRectangle);
		scene1.lightingEnabled(false);
		now = System.currentTimeMillis();
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
			this(x, y, 30, 1, 1, 3);
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
		private static final float pixelX = 480/Segw;
		private static final float pixelY = 800/Segh;
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
					vWaterWave.get(k).waveAmplitude -= 0.02f ;
				}
			}
		}

		public static void removeWaterWave(int index) {
			synchronized (vWaterWave) {
				vWaterWave.remove(index);
			}
		}
	}
    
} 