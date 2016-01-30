package min3d.sampleProject1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import dalvik.system.VMRuntime;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.text.StaticLayout;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Cuboid;
import min3d.objectPrimitives.Rectangle;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.LightType;
import min3d.vos.TextureVo;

public class ExampleLauncher3 extends RendererActivity {

	private final static String TAG = "ExampleLauncher";

	private static Cuboid cuboid;

	private static Object3dContainer _plane;

	private static float fixedsize;
	private static List<ResolveInfo> mApps;
	private static TextView editBox;
	private static boolean bTouched = false;
	private static boolean bMoving = false;
	private static boolean bLockTouched = false;
	private static boolean bLockRotationScreen = false;
	private static boolean bInitReady = false;
	private static final float mBackGroundBorder = 1.2f;
	private static float aspectRatio = 0f;
	private static TouchBorder mTouchBorder;
	private static MyGLSurfaceView myGLSurfaceView;
	public static int rowIconNumber = 2;
	public static int colIconNumber = 2;
	private static final int totalIconNumber = rowIconNumber * colIconNumber;
	public static int Angle ;
	private Handler mHandler = new Handler();
	private int itemIndex = 0;

	private SensorManager mSensorManager = null;

	private Sensor sensor;

	// tunable
	private final float gyroLimitY = 15;

	private final float iconSize = 0.5f;

	private final SensorEventListener mySensorListenor = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (!bInitReady || bLockRotationScreen)
				return;
			if (event.values[1] > gyroLimitY) {
				bLockRotationScreen = true;
				RotationScreenCounter = 0;
				mHandler.post(RotationScreenRight);
			} else if (event.values[1] < -1 * gyroLimitY) {
				bLockRotationScreen = true;
				RotationScreenCounter = 0;
				mHandler.post(RotationScreenLeft);
			}
		}
	};
	private int RotationScreenCounter = 0;

	private final Runnable RotationScreenRight = new Runnable() {
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

	private final Runnable RotationScreenLeft = new Runnable() {
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

	private final float offsetCameratotal = 1.0f;

	private final Runnable zoomInRunnable = new Runnable() {
		public void run() {
			// Log.d("hydra" , "runnableing" + scene.camera().position.z) ;	
			float x = (1.0f - (float) (itemIndex % totalIconNumber % rowIconNumber) * 2
					/ (rowIconNumber - 1) ) * offsetCameratotal / zoomInSteps;
			float y = (2.0f - (float) ((int) itemIndex % totalIconNumber / rowIconNumber) * 4
					/ (colIconNumber - 1) ) * offsetCameratotal / zoomInSteps;
			float z =  (float) (5f - 1.05f) / zoomInSteps;
			
			scene.camera().position.z -= z;
			scene.camera().position.x -= x;
			scene.camera().position.y += y;
			//scene.lights().get(1).position.setAllOffset(x, -y, z) ;
			//scene.lights().get(1).position.setDirtyFlag();
			// scene.getChildAt((int)itemIndex/20 + 1).scale().x = 1.0f +
			// (float)1/zoomInSteps;
			// scene.getChildAt((int)itemIndex/20 + 1).scale().y = 1.0f +
			// (float)1/zoomInSteps;
			int index = getRelativeIndex((int) (-cuboid.rotation().y / fQuarterofRoundAngle), cuboidFaces) ;
			int childIndex = itemIndex % totalIconNumber ;
			
			switch(RotationDimension) {
			case 0 :
				cuboid.getFaces()[index].getChildAt(childIndex).rotation().x += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[index].getChildAt(childIndex).doubleSidedEnabled(true);
				cuboid.getFaces()[index].getChildAt(childIndex).lightingEnabled(false) ;
				break ;
			case 1 :
				cuboid.getFaces()[index].getChildAt(childIndex).rotation().y += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[index].getChildAt(childIndex).doubleSidedEnabled(true);
				cuboid.getFaces()[index].getChildAt(childIndex).lightingEnabled(false) ;
				break ;
			case 2 :
				cuboid.getFaces()[index].getChildAt(childIndex).rotation().z += fTotalAngleofIconSpin / zoomInSteps;
				cuboid.getFaces()[index].getChildAt(childIndex).doubleSidedEnabled(true);
				cuboid.getFaces()[index].getChildAt(childIndex).lightingEnabled(false) ;
				break ;
			case 3 :
				break ;
			}
			
			scene.camera().setDirtyFlag();
			
			if (scene.camera().position.z < 1.05f) {
				mHandler.postDelayed(startActivityLate, 300);
			} else {
				mHandler.postDelayed(zoomInRunnable, 5);
			}
		}
	};

	private final Runnable startActivityLate = new Runnable() {
		public void run() {
			Intent intent = new Intent();
			intent.setClassName(mApps.get(itemIndex).activityInfo.packageName, mApps.get(itemIndex).activityInfo.name);
			startActivitySafely(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			finish();
		}
	};

	private final Runnable stepLighting = new Runnable() {
		public void run() {
			if (lightAttenuation < 0.4f) {
				lightAttenuation += 0.02f;
				mHandler.postDelayed(stepLighting, 50);
				scene.lights().get(1).attenuationSetAll(lightAttenuation, 0.0f, 0.0f);
			}
		}
	};
	
	

	private int RotationDimension = 0;

	private final static float accumulation = 10f;

	private float lightBorderX;
	private float lightBorderY;
	private float lightAttenuation = 0.1f;
	private int faceStage = 0;

	/*
	 * @Override protected void glSurfaceViewConfig() { myGLSurfaceView = new
	 * MyGLSurfaceView(this); myGLSurfaceView.setZOrderOnTop(true);
	 * myGLSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
	 * myGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	 * glSurfaceView(myGLSurfaceView); }
	 */

	private final int cuboidFaces = 4;

	private int totalFaces;

	private final float fTotalAngleofIconSpin = 360f;

	private final float fQuarterofRoundAngle = 90f;

	private final float fHalfofQuart = 45f;
	private final static float flingInterval = 0.7f ;
	private static final float totalOffsetY = -0.15f ;

	private final static int textFirstRowOffset = 36 ;

	private final static int textSecondRowOffset = 72 ;
	
	private final static int MAX_LINES = 2 ;

	private void adjustView() {
		if (Math.abs(cuboid.rotation().y) % fQuarterofRoundAngle > fHalfofQuart) {
			cuboid.rotation().y += (cuboid.rotation().y % fQuarterofRoundAngle) / 2;
		} else {
			cuboid.rotation().y -= (cuboid.rotation().y % fQuarterofRoundAngle) / 2;
		}
	}

	void coloringBackground(int $itemIndex) {
		/*
		 * Log.d("hydra","coloringhahahhahahah") ; TextureVo tmpTextureVo = new
		 * TextureVo("icontouch"+$itemIndex);
		 * cuboid.getFaces()[(int)itemIndex/20
		 * ].getChildAt(itemIndex%20).textures().addReplace(tmpTextureVo) ;
		 */
	}


	private Bitmap drawableToBitmap(Drawable drawable,String text , int alpha) {

		//Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap.Config c = Bitmap.Config.ARGB_8888 ;

		
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), c);
		// Bitmap bitmap = Bitmap.createBitmap( 128, 128, c);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = (float) 128 / width;
		float scaleHeight = (float) 256 / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// matrix.postRotate(180) ;

		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getWidth());
		drawable.setAlpha(alpha);
		drawable.draw(canvas);
		
		Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		//textPaint.setTextScaleX(1.1f);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(4, 3, 6, 0XFF000000) ;
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		TextPaint mTextPaint = new TextPaint(textPaint) ;
        StaticLayout layout = new StaticLayout(text, mTextPaint, (int)bitmap.getWidth(),
                Alignment.ALIGN_CENTER, 1, 0, true);
        int lineCount = layout.getLineCount();
        if (lineCount > MAX_LINES) {
            lineCount = MAX_LINES;
        }
        
        for (int i=0; i<lineCount; i++) {
           final String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));
           int x = (int)(1
                   + ((bitmap.getWidth() - mTextPaint.measureText(lineText)) * 0.5f));
           int y = bitmap.getWidth() + textFirstRowOffset + (i * textFirstRowOffset);
           canvas.drawText(lineText, x, y, mTextPaint);
       }
/*		if(textPaint.measureText(text)> bitmap.getWidth() ) {
			String[] tmpText = text.split(" ", 2) ;
			canvas.drawText(tmpText[0], (bitmap.getWidth()-textPaint.measureText(tmpText[0]))/2, 
					bitmap.getWidth()+textFirstRowOffset, textPaint);
			if(tmpText.length>1)
				canvas.drawText(tmpText[1], (bitmap.getWidth()-textPaint.measureText(tmpText[1]))/2, 
						bitmap.getWidth()+textSecondRowOffset, textPaint);
		} else {
			canvas.drawText(text, (bitmap.getWidth()-textPaint.measureText(text))/2 , bitmap.getWidth()+textFirstRowOffset, textPaint);
		}*/
		// draw the text centered
		
		return bitmap;
	}
	private void dumpStates(){
		Log.d("Launcher dumpStates" , " bTouch =" + bTouched +" bMoving =" + bMoving + " bLockTouched =" + bLockTouched 
				+" bLockRotationScreen =" + bLockRotationScreen +" bInitReady =" + bInitReady) ;

	}

	private void dynamicChangeTexture() {
		int innerFaceStage;
		if (cuboid.rotation().y > -fHalfofQuart) {
			innerFaceStage = (int) -((cuboid.rotation().y + fHalfofQuart) / fQuarterofRoundAngle);
		} else {
			innerFaceStage = (int) -(((cuboid.rotation().y + fHalfofQuart) / fQuarterofRoundAngle) - 1);
		}

		if (innerFaceStage > faceStage) {
			faceStage = innerFaceStage;
			int tmpIconIndex = getRelativeIndex(innerFaceStage + 1, totalFaces);
			int tmpIconIndexNext = getRelativeIndex(innerFaceStage + 2, totalFaces);
			int tmpFaceIndex = getRelativeIndex(innerFaceStage + 1, cuboidFaces);
			int tmpFaceIndexNext = getRelativeIndex(innerFaceStage + 2, cuboidFaces);
			for (int i = 0; i < totalIconNumber; i++) {
				String id = "icon" + String.valueOf(tmpIconIndex * totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndex].getChildAt(i).textures().addReplace(id);
				;
			}
			for (int i = 0; i < totalIconNumber; i++) {
				String id = "icon" + String.valueOf(tmpIconIndexNext * totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndexNext].getChildAt(i).textures().addReplace(id);
				;
			}
			// Log.d("hydra" , " " + tmpIconIndex2 + " "+ tmpIconIndex3 +" " +
			// tmpFaceIndex2 + " "+ tmpFaceIndex3 ) ;
		} else if (innerFaceStage < faceStage) {
			faceStage = innerFaceStage;
			int tmpIconIndex = getRelativeIndex(innerFaceStage - 1, totalFaces);
			int tmpIconIndexNext = getRelativeIndex(innerFaceStage - 2, totalFaces);
			int tmpFaceIndex = getRelativeIndex(innerFaceStage - 1, cuboidFaces);
			int tmpFaceIndexNext = getRelativeIndex(innerFaceStage - 2, cuboidFaces);
			for (int i = 0; i < totalIconNumber; i++) {
				String id = "icon" + String.valueOf(tmpIconIndex * totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndex].getChildAt(i).textures().addReplace(id);
			}
			for (int i = 0; i < totalIconNumber; i++) {
				String id = "icon" + String.valueOf(tmpIconIndexNext * totalIconNumber + i);
				cuboid.getFaces()[tmpFaceIndexNext].getChildAt(i).textures().addReplace(id);
				;
			}
			// Log.d("hydra" , " " + tmpIconIndex2 + " "+ tmpIconIndex3 +" " +
			// tmpFaceIndex2 + " "+ tmpFaceIndex3 ) ;
		}

		// Log.d("hydra", ""+cuboid.rotation().y + " " + faceStage ) ;
	}

	@Override
	protected void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(this);
		glSurfaceView(myGLSurfaceView);
	}

	private void initLauncher() {

		loadApps();
		int i, size;
		Color4 color = new Color4(255, 255, 255, 255);

		for (int col = 0; col < colIconNumber; ++col) {
			for (int row = 0; row < rowIconNumber; ++row) {
				Rectangle mRectangle = new Rectangle(iconSize, iconSize*2, 2, 2, color);
				//mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f + totalOffsetY - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[0].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize*2, 2, 2, color);
				//mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f + totalOffsetY - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[1].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize*2, 2, 2, color);
				//mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f + totalOffsetY - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[2].addChild(mRectangle);
				mRectangle = new Rectangle(iconSize, iconSize*2, 2, 2, color);
				//mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f  - (float) row * 2 / (rowIconNumber - 1);
				mRectangle.position().y += 2.0f + totalOffsetY - (float) col * 4 / (colIconNumber - 1);
				// mRectangle.position().z -= 0.1f;
				cuboid.getFaces()[3].addChild(mRectangle);
			}
		}
		

		totalFaces = (int) ((mApps.size() - 1) / totalIconNumber) + 1;
		Log.d("hydra", "" + totalFaces + " " + mApps.size());
		for (i = 0, size = mApps.size(); i < size; ++i) {

			ResolveInfo info = mApps.get(i);
			// info.activityInfo.loadIcon(getPackageManager());
			Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()),
					info.activityInfo.loadLabel(getPackageManager()).toString() ,255);
			String id = "icon" + String.valueOf(i);
			Shared.textureManager().addTextureId(b, id, false);
			
			// cuboid.getFaces()[(int) (i /totalIconNumber)].getChildAt(i %
			// totalIconNumber).textures().addById(id);
			// cuboid.addTexture(Cuboid.Face.All, b, id);

			b.recycle();
		}
		for (; i % totalIconNumber != 0; ++i) {
			ResolveInfo info = mApps.get(1);
			// info.activityInfo.loadIcon(getPackageManager());
			Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()), 
					"", 0);

			String id = "icon" + String.valueOf(i);
			Shared.textureManager().addTextureId(b, id, false);
			// cuboid.getFaces()[(int)i/totalIconNumber].getChildAt(i%totalIconNumber).textures().addById(id);
			// cuboid.addTexture(Cuboid.Face.All, b, id);

			b.recycle();
		}
		for (int n = -1; n < 2; n++) {
			int innerFaceIndex = getRelativeIndex(n, 4);
			int innerRelativeIndex = getRelativeIndex(n, totalFaces);
			Log.d("hydra", " " + innerFaceIndex + " " + innerRelativeIndex);
			for (int m = 0; m < totalIconNumber; m++) {
				String id = "icon" + String.valueOf(innerRelativeIndex * totalIconNumber + m);
				String dsid = "description" + String.valueOf(innerRelativeIndex * totalIconNumber + m);
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
		light.position.setAll(0, 0, 0);
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
		light1.position.setAll(0, 0, 0);
		light1.type(LightType.POSITIONAL);
		light1.spotCutoffAngle(45f);
		light1.direction.setAll(0f, 0f, -1f);
		// light1._spotExponent.set(10f) ;
		light1.attenuationSetAll(lightAttenuation, 0.0f, 0.0f);
		scene.lights().add(light);
		scene.lights().add(light1);
		scene.lightingEnabled(true);
	}

	private void initObject() {

		IntBuffer viewport = IntBuffer.allocate(4);
		Shared.renderer().gl().glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Log.d("hydra", "" + viewport.get(0) + viewport.get(1) + viewport.get(2) + viewport.get(3));
		aspectRatio = (float) viewport.get(2) / viewport.get(3);
		mTouchBorder = new TouchBorder(viewport);
		lightBorderX = 0.3f;
		lightBorderY = -1 * lightBorderX * mTouchBorder.borderY / mTouchBorder.borderX;
		fixedsize = aspectRatio > 1 ? 5f : 3f;
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
		Shared.textureManager().addTextureId(b, "backover", false);
		_plane.textures().addById("backover");
		_plane.rotation().y = 180;
		_plane.position().z = -10f;
		_plane.lightingEnabled(true);
		_plane.normalsEnabled(true);
		scene.addChild(_plane);
		b.recycle();
		scene.addChild(cuboid);
		scene.camera().target.z = -100f;
	}

	@Override
	public void initScene() {
		scene.backgroundColor().setAll(0x00000000);
		initObject();
		initLight();
		initSensor();
		initLauncher();
		Shared.renderer().logFps(true);
		bInitReady = true ;
		bLockTouched = false ;
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

	private void lightEvents(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHandler.post(stepLighting);
			setLightDirection(e);
			lightAttenuation = 0.1f;
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(stepLighting);
			scene.lights().get(1).attenuationSetAll(0.1f, 0.0f, 0.0f);
			break;
		case MotionEvent.ACTION_MOVE:
			setLightDirection(e);
			break;
		}
	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
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
	protected void onPause() {
		mSensorManager.unregisterListener(mySensorListenor);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onUpdateScene() {
		editBox.setText("" + Shared.renderer().fps());
	}

	private void setLightDirection(MotionEvent e) {

		float LightDirectionX = (e.getX() - mTouchBorder.borderX / 2) * lightBorderX * 2 / mTouchBorder.borderX;
		float LightDirectionY = (e.getY() - mTouchBorder.borderY / 2) * lightBorderY * 2 / mTouchBorder.borderY;
		scene.lights().get(0).direction.setX(LightDirectionX);
		scene.lights().get(0).direction.setY(LightDirectionY);
		//scene.lights().get(0).direction.setDirtyFlag();
		// Log.d("hydra" , "light point x:" + LightDirectionX + " y:" +
		// LightDirectionY ) ;
	}
	private void startActivitySafely(Intent intent) {
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
	private Bitmap textToBitmap(String text, int alpha, Color4 Background) {
		Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);

		// get a background image from resources
		// note the image format must match the bitmap format
		//Drawable background = this.getResources().getDrawable(R.drawable.dsbackground);
		//background.setBounds(0, 0, 128, 128);
		//background.setAlpha(128) ;
		//background.draw(canvas); // draw the background to our bitmap

	// draw the background to our bitmap

		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(34);
		//textPaint.setTextScaleX(1.1f);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(4, 3, 6, 0XFF000000) ;
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		// draw the text centered
		canvas.drawText(text, 1, 56, textPaint);
		
		return bitmap ;

	}
	private void touchEvents(MotionEvent e) {
		float x = e.getX() * adjustValue;
		float y = e.getY() * adjustValue;
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchBorder.setTouch(e.getX(), e.getY());
			break;

		case MotionEvent.ACTION_UP:
			mTouchBorder.getTouch(e.getX(), e.getY());
			break;

		case MotionEvent.ACTION_MOVE:
			if (e.getPointerCount() == 1) {
				_dx = x - mPreviousX;
			}
			if (!bMoving) {
				if (Math.abs(_dx) > flingSensity) {
					bMoving = true;
				} else {
					_dx = 0;
				}
			}

			break;
		}
		mPreviousX = x;
		mPreviousY = y;
	}

	@Override
	public void updateScene() {

		if (_dx != 0) {
			cuboid.rotation().y += _dx * flingInterval;
			if (Math.abs(_plane.position().x + _dx / 100) < mBackGroundBorder)
				_plane.position().x += _dx / 100;
			_dx = 0;
		}

		if (!bTouched && !bMoving && !bLockRotationScreen && Math.abs(cuboid.rotation().y % fQuarterofRoundAngle) > 1){
			adjustView();
		}
		
		// Log.d("hydra" , ""+_plane.position().x) ;
		dynamicChangeTexture();

	}

	private void zoomStart(int $itemIndex) {
		this.itemIndex = $itemIndex;
		bLockTouched = true;
		bLockRotationScreen = true;
		RotationDimension = (int) (Math.random() * 4);
		scene.lights().get(1).attenuationSetAll(0.5f, 0.05f, 0.0f);
		scene.lights().get(0).direction.setAll(0f, 0f, -100f) ;
		scene.lights().get(1).spotCutoffAngle(60f);
		mHandler.post(zoomInRunnable);
	}
	
	private final static int getRelativeIndex(int now, int total) {
		return now < 0 ? (now % total + total) % total : (now % total);
	}

	class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);
			// Set this as Renderer
			// Request focus, otherwise buttons won't react
			//this.requestFocus();
			//this.setFocusableInTouchMode(true);
			//
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			// Log.d("hydra", e.toString());
			if(false) dumpStates() ;
			if (bLockTouched || !bInitReady)
				return false;

			switch (e.getAction()) {

			case MotionEvent.ACTION_MOVE:
				lightEvents(e);
				touchEvents(e);
				break;
			case MotionEvent.ACTION_UP:
				lightEvents(e);
				touchEvents(e);
				bTouched = false;
				bMoving = false;
				break;
			case MotionEvent.ACTION_DOWN:
				lightEvents(e);
				touchEvents(e);
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
			// Log.d(TAG, "getting touch");
			if (Math.abs(X - touchX) + Math.abs(Y - touchY) < accumulation
					&& System.currentTimeMillis() - systemtime < 400) {
				if (cItemIndex < mApps.size() && Math.abs(cuboid.rotation().y % fQuarterofRoundAngle) < 3)
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
			// Log.d(TAG, "setting touch");
			systemtime = System.currentTimeMillis();
			touchX = X;
			touchY = Y;
			int x = (int) (touchX / borderX * rowIconNumber);
			int y = (int) (touchY / borderY * colIconNumber);
			int z = (int) (-cuboid.rotation().y / fQuarterofRoundAngle);
			// if (z < 0)
			// z += 4;
			// z = Math.abs(z - 4) % 4;
			z = getRelativeIndex(z, totalFaces);
			// Log.d("hydra", "icon" + x + " " + y + " " + z);
			cItemIndex = x + rowIconNumber * y + totalIconNumber * z;
			// spinIcon(cItemIndex);
			coloringBackground(cItemIndex);
		}
	}
	
}