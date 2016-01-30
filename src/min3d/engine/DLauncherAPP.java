package min3d.engine;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL11;

import dalvik.system.VMRuntime;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Rect;

import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.Renderer10;
import min3d.core.RendererActivity;
import min3d.core.Scene;
import min3d.interfaces.ISceneController;
import min3d.objectPrimitives.Rectangle;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.LightType;

import static min3d.engine.PageFlip.*;

public class DLauncherAPP implements ISceneController {

	public static final float pageDistanceMeasure = 90f;
	public static final float fixedIconLocationX = 1.1f;
	public static final float fixedIconLocationY = 1.9f;
	private static final String TAG = "DLauncherAPP";
	private static final float Cameraoffsettotal = 1.0f;
	private static final int MAX_LINES = 2;
	private static final int mPlanesNumber = 4;
	private static final int textFirstRowOffset = 36;
	private static final int zoomInSteps = 120;
	private static final float fTotalAngleofIconSpin = 360f;
	private static final float totalOffsetY = -0.15f;
	private static final float iconSize = 0.5f;
	private static final float touchAdjustValue = 0.5f;
	private static final float adjustPageSpeed = 3.0f;
	private static final float flingSensity = 2f;
	private static final float MAXLIGHTATTENUATION = 0.4f;
	private static final float MINLIGHTATTENUATION = 0.1f;
	private static final int MAXINDEXALPHA = 48;
	private static final int MININDEXALPHA = 0;

	public static Handler mHandler = new Handler();
	private static Handler sceneHandler = new Handler();

	public static FLIP_TYPE Flip_Method = FLIP_TYPE.TYPE_SPHERE;
	public static List<Object3dContainer> lmPlanes;
	public static float sceneAngle = 0;
	private static List<ResolveInfo> lmApps;
	private static MyGLSurfaceView myGLSurfaceView;
	private static TouchBorder mTouchBorder;
	private static TextView textBox;
	private static int rowIconNumber = 4;
	private static int colIconNumber = 5;
	private static int totalIconNumber = rowIconNumber * colIconNumber;
	private static int faceChangeIndexNow;
	private static int totalFaces;
	private static float touchEventsMetric;
	private static float touchPreviousX;
	private static float touchPreviousY;
	private static float lightAttenuation;
	private static float lightBorderX;
	private static float lightBorderY;
	private static boolean bLockTouched = false;
	private static boolean bInitReady = false;
	private static boolean bTouched = false;
	public static boolean bMoving = false;
	private static Object3dContainer _background;

	// for testing
	private static final boolean debug = false;

	private static final Runnable touchDownEvents = new Runnable() {
		public void run() {
			if (touchEventsMetric < 1) {
				touchEventsMetric += 0.02;
				sceneHandler.postDelayed(touchDownEvents, 10);
			} else {
				touchEventsMetric = 1;
			}
			stepAllEvents();
		}
	};

	private static final Runnable touchUpEvents = new Runnable() {
		public void run() {
			if (touchEventsMetric > 0) {
				touchEventsMetric -= 0.02;
				sceneHandler.postDelayed(touchUpEvents, 10);
			} else {
				touchEventsMetric = 0;
			}
			stepAllEvents();
		}
	};

	private static void stepAllEvents() {
		scene.lights()
				.get(1)
				.attenuationSetAll(
						MINLIGHTATTENUATION + (MAXLIGHTATTENUATION - MINLIGHTATTENUATION) * touchEventsMetric, 0.0f,
						0.0f);

		short tmpIndexAlpha = (short) ((MAXINDEXALPHA - MININDEXALPHA) * touchEventsMetric);
		for (int i = 0, size = lmPlanes.size(); i < size; ++i) {
			for (int j = 0, sizej = lmPlanes.get(i).vertices().size(); j < sizej; ++j) {
				lmPlanes.get(i).colors().setPropertyA(j, tmpIndexAlpha);
			}
		}
		callRequestRender() ;
	}

	private final Runnable zoomInRunnable = new Runnable() {
		public void run() {
			// Log.d("hydra" , "runnableing" + scene.camera().position.z) ;
			float x = (fixedIconLocationX - (float) (mTouchBorder.cIconIndexX) * 2 * fixedIconLocationX
					/ (rowIconNumber - 1))
					* Cameraoffsettotal / zoomInSteps;
			float y = (fixedIconLocationY - (float) (mTouchBorder.cIconIndexY) * 2 * fixedIconLocationY
					/ (colIconNumber - 1))
					* Cameraoffsettotal / zoomInSteps;
			float z = (float) (5f - 1.05f) / zoomInSteps;

			scene.camera().position.z -= z;
			scene.camera().position.x -= x;
			scene.camera().position.y += y;
			// int faceindex = sceneAngle > 0 ? getRelativeIndex((int)
			// ((-sceneAngle - 5) / pageDistanceMeasure),
			// routes[Flip_Method.ordinal()].length) : getRelativeIndex(
			// (int) ((-sceneAngle + 5) / pageDistanceMeasure),
			// routes[Flip_Method.ordinal()].length);
			int faceindex = getRelativeIndex(getPageIndex(), routes[Flip_Method.ordinal()].length);
			int childIndex = mTouchBorder.cItemIndex % totalIconNumber;

			switch (mTouchBorder.RotationDimension) {
			case 0:
				lmPlanes.get(faceindex).getChildAt(childIndex).rotation().x += fTotalAngleofIconSpin / zoomInSteps;
				lmPlanes.get(faceindex).getChildAt(childIndex).doubleSidedEnabled(true);
				lmPlanes.get(faceindex).getChildAt(childIndex).lightingEnabled(false);
				break;
			case 1:
				lmPlanes.get(faceindex).getChildAt(childIndex).rotation().y += fTotalAngleofIconSpin / zoomInSteps;
				lmPlanes.get(faceindex).getChildAt(childIndex).doubleSidedEnabled(true);
				lmPlanes.get(faceindex).getChildAt(childIndex).lightingEnabled(false);
				break;
			case 2:
				lmPlanes.get(faceindex).getChildAt(childIndex).rotation().z += fTotalAngleofIconSpin / zoomInSteps;
				lmPlanes.get(faceindex).getChildAt(childIndex).doubleSidedEnabled(true);
				lmPlanes.get(faceindex).getChildAt(childIndex).lightingEnabled(false);
				break;
			case 3:
				break;
			}
			lightAttenuation += (1.2f - 0.1f) / zoomInSteps;
			scene.lights().get(1).attenuationSetAll(lightAttenuation, 0.0f, 0.0f);
			// scene.lights().get(1).direction.setAll(0, 0, 1) ;

			scene.camera().setDirtyFlag();
			callRequestRender() ;
			if (scene.camera().position.z < 1.05f) {
				mHandler.postDelayed(startActivityLate, 200);
			} else {
				mHandler.postDelayed(zoomInRunnable, 4);
			}
		}
	};

	private Runnable startActivityLate = new Runnable() {
		public void run() {
			Intent intent = new Intent();
			intent.setClassName(lmApps.get(mTouchBorder.cItemIndex).activityInfo.packageName,
					lmApps.get(mTouchBorder.cItemIndex).activityInfo.name);
			startActivitySafely(intent);
		}
	};

	private void startActivitySafely(Intent intent) {
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		try {
			((Activity) mContext).startActivity(intent);
			((Activity) mContext).finish();
		} catch (ActivityNotFoundException e) {
			// Toast.makeText(this, "activity_not_found",
			// Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unable to launch. tag=" + " intent=" + intent, e);
		} catch (SecurityException e) {
			// Toast.makeText(this, "activity_not_found",
			// Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Launcher does not have the permission to launch " + intent
					+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
					+ "or use the exported attribute for this activity. " + "tag=" + " intent=" + intent, e);
		}
	}

	private static float getRelativeFloat(float now, float total) {
		return now < 0 ? (now % total + total) % total : (now % total);
	}

	private static int getRelativeIndex(int now, int total) {
		return now < 0 ? (now % total + total) % total : (now % total);
	}

	private static boolean getCloseEnuf(float myNumber, float judgeNumber, float openBall) {
		if (openBall <= 0)
			return false;
		return (Math.abs(myNumber) + openBall) % judgeNumber < openBall * 2;
	}

	private static int getPageIndex() {
		return sceneAngle > 0 ? -(int) ((sceneAngle + pageDistanceMeasure / 2) / pageDistanceMeasure)
				: -(int) ((sceneAngle - pageDistanceMeasure / 2) / pageDistanceMeasure);
	}

	@Override
	public void updateScene() {
		// Log.d("hydra" ,
		// ""+bTouched+getRelativeFloat(sceneAngle,pageDistanceMeasure)) ;
		if (!bTouched && !getCloseEnuf(sceneAngle, pageDistanceMeasure, 0.2f)) {
			adjustView();
		} else if (!bTouched && getCloseEnuf(sceneAngle, pageDistanceMeasure, 0.2f)) {
			// Log.d("hydra" , "set dirty!!") ;
		}

		flipPage(rowIconNumber, colIconNumber);
		dynamicChangeTexture();

		// for temporary
	}

	private void adjustView() {
		if (Math.abs(sceneAngle) % pageDistanceMeasure > pageDistanceMeasure / 2) {
			if (sceneAngle > 0) {
				sceneAngle += (pageDistanceMeasure - sceneAngle % pageDistanceMeasure) / adjustPageSpeed;
			} else {
				sceneAngle += (-pageDistanceMeasure - sceneAngle % pageDistanceMeasure) / adjustPageSpeed;
			}

		} else {
			sceneAngle -= (sceneAngle % pageDistanceMeasure) / adjustPageSpeed;
		}

		if (Math.abs(sceneAngle % pageDistanceMeasure) < 0.2f || Math.abs(sceneAngle % pageDistanceMeasure) > 89.8f)
			removeTouch(Flip_Method, mHandler);
		callRequestRender() ;
		// Log.d("hydra" ,"adjusting"+sceneAngle) ;
	}

	@Override
	public void initScene() {
		loadApps();
		initVariables();
		initObjects();
		initLauncherTextures();
		initLauncher();
		initLight();
		scene.camera().target.z = -100f;
		flipPage(rowIconNumber, colIconNumber);

		bInitReady = true;
		bLockTouched = false;
		Shared.renderer().logFps(true);
		if (debug)
			scene.camera().frustum.shortSideLength(3f);
		callRequestRender();
	}

	// TODO : make it merge
/*	@Override
	protected void onPause() {
		super.onPause();
		bInitReady = false;
		mHandler.removeCallbacks(touchDownEvents);
		mHandler.removeCallbacks(touchUpEvents);
	}*/

	private void initVariables() {
		sceneAngle = 0;
		totalFaces = (int) ((lmApps.size() - 1) / totalIconNumber) + 1;
		faceChangeIndexNow = 9999;
		touchEventsMetric = 0;

		IntBuffer viewport = IntBuffer.allocate(4);
		Shared.renderer().gl().glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		mTouchBorder = new TouchBorder(viewport);

		// Log.d("hydra"
		// ," "+viewport.get(0)+" "+viewport.get(1)+" "+viewport.get(2)+" "+viewport.get(3))
		// ;
	}

	private void initLauncherTextures() {

		for (int i = 0, size = Math.min(totalIconNumber, lmApps.size()); i < size; ++i) {
			String id = "icon" + String.valueOf(i);
			lmPlanes.get(0).getChildAt(i).textures().addReplaceDAPP(id, lmApps);
			lmPlanes.get(0).getChildAt(i).lightingEnabled(true);
			lmPlanes.get(0).getChildAt(i).isVisible(true);
		}
		String id = "index" + String.valueOf(0);
		lmPlanes.get(0).textures().addByIdDAPP(id);
		dynamicChangeTexture();

	}

	private void initLauncher() {
		for (int i = 0; i < mPlanesNumber; ++i) {
			scene.addChild(lmPlanes.get(i));
		}
	}

	private void initLight() {
		lightAttenuation = 0.1f;
		Light light = new Light();
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
		lightBorderX = 0.3f;
		lightBorderY = -1 * lightBorderX / mTouchBorder.aspectRatio;
	}

	private void initObjects() {

		lmPlanes = new ArrayList<Object3dContainer>();

		Color4 color = new Color4(255, 255, 255, 255);
		for (int i = 0; i < mPlanesNumber; ++i) {
			Rectangle mPlane = new Rectangle(2.4f, 3.6f, 1, 1, new Color4(255, 255, 255, 0));
			mPlane.isVisible(true);
			mPlane.lightingEnabled(false);
			for (int col = 0; col < colIconNumber; ++col) {
				for (int row = 0; row < rowIconNumber; ++row) {
					Rectangle mRectangle = new Rectangle(iconSize, iconSize * 2, 2, 2, color);
					mRectangle.lightingEnabled(false);
					mRectangle.isVisible(false);
					mRectangle.position().x += fixedIconLocationX - (float) row * 2 * fixedIconLocationX
							/ (rowIconNumber - 1);
					mRectangle.position().y += fixedIconLocationY + totalOffsetY - (float) col * 2 * fixedIconLocationY
							/ (colIconNumber - 1);
					// mRectangle.position().z += -0.1f ;
					mPlane.addChild(mRectangle);
				}
			}
			// String id = "index" + String.valueOf(i);
			// mPlane.textures().addById(id) ;
			lmPlanes.add(mPlane);
		}

	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		lmApps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	private static void dynamicChangeTexture() {
		int innerFaceStage = getPageIndex();
		/*
		 * if (sceneAngle > 0) { innerFaceStage = -(int) ((sceneAngle + 5) /
		 * pageDistanceMeasure) ; } else { innerFaceStage = -(int) ((sceneAngle
		 * - 4) / pageDistanceMeasure); }
		 */
		if (innerFaceStage != faceChangeIndexNow) {
			faceChangeIndexNow = innerFaceStage;
			int tmpIconIndexRight = getRelativeIndex(innerFaceStage + 1, totalFaces);
			int tmpIconIndexLeft = getRelativeIndex(innerFaceStage - 1, totalFaces);
			int tmpFaceIndexRight = getRelativeIndex(innerFaceStage + 1, routes[Flip_Method.ordinal()].length);
			int tmpFaceIndexLeft = getRelativeIndex(innerFaceStage - 1, routes[Flip_Method.ordinal()].length);
			for (int i = 0; i < totalIconNumber; i++) {
				String id = "icon" + String.valueOf(tmpIconIndexLeft * totalIconNumber + i);
				if (tmpIconIndexLeft * totalIconNumber + i < lmApps.size()) {
					lmPlanes.get(tmpFaceIndexLeft).getChildAt(i).textures().addReplaceDAPP(id, lmApps);
					lmPlanes.get(tmpFaceIndexLeft).getChildAt(i).isVisible(true);
					lmPlanes.get(tmpFaceIndexLeft).getChildAt(i).lightingEnabled(true);
				} else {
					lmPlanes.get(tmpFaceIndexLeft).getChildAt(i).isVisible(false);
				}
				id = "icon" + String.valueOf(tmpIconIndexRight * totalIconNumber + i);
				if (tmpIconIndexRight * totalIconNumber + i < lmApps.size()) {
					lmPlanes.get(tmpFaceIndexRight).getChildAt(i).textures().addReplaceDAPP(id, lmApps);
					lmPlanes.get(tmpFaceIndexRight).getChildAt(i).isVisible(true);
					lmPlanes.get(tmpFaceIndexRight).getChildAt(i).lightingEnabled(true);
				} else {
					lmPlanes.get(tmpFaceIndexRight).getChildAt(i).isVisible(false);
				}
			}
			String id = "index" + String.valueOf(tmpIconIndexRight);
			lmPlanes.get(tmpFaceIndexRight).textures().addReplaceDAPP(id);
			id = "index" + String.valueOf(tmpIconIndexLeft);
			lmPlanes.get(tmpFaceIndexLeft).textures().addReplaceDAPP(id);
		}

		// Log.d("hydra", " " + sceneAngle + " " + innerFaceStage + "page:" +
		// getPageIndex());
	}

	// TODO : make it merge
/*	@Override
	protected void onCreate(Bundle savedInstanceState) {
		VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	}*/

	private static void touchEvents(MotionEvent e) {
		float x = e.getX() * touchAdjustValue;
		float y = e.getY() * touchAdjustValue;
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchBorder.setTouch(e.getX(), e.getY());
			bTouched = true;
			break;

		case MotionEvent.ACTION_UP:
			mTouchBorder.getTouch(e.getX(), e.getY());
			bTouched = false;
			bMoving = false;
			break;

		case MotionEvent.ACTION_MOVE:
			if (e.getPointerCount() == 1) {
				if (!bMoving) {
					if (Math.abs(x - touchPreviousX) > flingSensity) {
						setTouch(Flip_Method, mHandler);
						bMoving = true;
						sceneAngle += x - touchPreviousX;
					}
				} else {
					sceneAngle += x - touchPreviousX;
				}

			}

			bTouched = true;
			break;
		}
		touchPreviousX = x;
		touchPreviousY = y;
	}

	private static void lightEvents(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			sceneHandler.removeCallbacks(touchUpEvents);
			sceneHandler.post(touchDownEvents);
			setLightDirection(e);
			break;

		case MotionEvent.ACTION_UP:
			sceneHandler.removeCallbacks(touchDownEvents);
			sceneHandler.post(touchUpEvents);
			break;

		case MotionEvent.ACTION_MOVE:
			setLightDirection(e);
			break;
		}
	}

	private static void setLightDirection(MotionEvent e) {

		float LightDirectionX = (e.getX() - mTouchBorder.viewRect.width() / 2) * lightBorderX * 2
				/ mTouchBorder.viewRect.width();
		float LightDirectionY = (e.getY() - mTouchBorder.viewRect.height() / 2) * lightBorderY * 2
				/ mTouchBorder.viewRect.height();
		scene.lights().get(0).direction.setX(LightDirectionX);
		scene.lights().get(0).direction.setY(LightDirectionY);
	}

	public static void callRequestRender() {
		_glSurfaceView.requestRender();
	}

	/*
	 * MyGLSurfaceView to clickable
	 */
	private static class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);
			this.requestFocus();
			this.setFocusableInTouchMode(true);
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			Log.d("hydra", e.toString());
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
				break;
			case MotionEvent.ACTION_DOWN:
				lightEvents(e);
				touchEvents(e);
				break;
			}
			callRequestRender();
			return true;
		}
	}

	private class TouchBorder {

		private Rect viewRect;
		private float touchedX, touchedY;
		private float aspectRatio;
		private long touchedsystemTime;
		private int cItemIndex;
		private int cIconIndexX;
		private int cIconIndexY;
		private int cPageIndex;
		private int RotationDimension;

		private static final int touchedTimer = 400;
		private static final float accumulation = 10f;

		TouchBorder(IntBuffer viewport) {
			if (viewport.limit() < 4)
				return;
			viewRect = new Rect(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
			aspectRatio = (float) viewport.get(2) / viewport.get(3);
			touchedsystemTime = 0;
		}

		public void getTouch(float X, float Y) {
			// Log.d("hydra", "getting touch" +"acc:" +Math.abs(X - touchX) +
			// Math.abs(Y - touchY) + "ItemIndex"+cItemIndex +"sceneAngle" +
			// sceneAngle);

			if (Math.abs(X - touchedX) + Math.abs(Y - touchedY) < accumulation
					&& System.currentTimeMillis() - touchedsystemTime < touchedTimer) {
				if (cItemIndex < lmApps.size() && getCloseEnuf(sceneAngle, pageDistanceMeasure, 1f))
					zoomStart();
			}
		}

		public void setTouch(float $X, float $Y) {

			// Log.d(TAG, "setting touch");
			touchedsystemTime = System.currentTimeMillis();
			touchedX = $X;
			touchedY = $Y;
			int x = (int) (touchedX / viewRect.width() * rowIconNumber);
			int y = (int) (touchedY / viewRect.height() * colIconNumber);
			int z = getPageIndex();
			z = getRelativeIndex(z, totalFaces);
			cIconIndexX = x;
			cIconIndexY = y;
			cPageIndex = z;
			// Log.d("hydra", "icon" + x + " " + y + " " + z);
			cItemIndex = x + rowIconNumber * y + totalIconNumber * z;
			// coloringBackground(cItemIndex);
		}

		private void zoomStart() {
			Log.d(TAG, "zoomStarting " + lmApps.get(cItemIndex).toString());
			bLockTouched = true;
			RotationDimension = (int) (Math.random() * 3);

			scene.lights().get(0).direction.setAll(0f, 0f, 1f); // get off
			scene.lights().get(1).spotCutoffAngle(90f); // expansion
			mHandler.post(zoomInRunnable);
		}

	}

	public static Scene scene;
	protected static GLSurfaceView _glSurfaceView;

	protected Handler _initSceneHander;
	protected Handler _updateSceneHander;

	private boolean _renderContinuously;
	private Context mContext;
	

	protected void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(mContext);
		_glSurfaceView = myGLSurfaceView;
    	_glSurfaceView.setZOrderOnTop(true);
        _glSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
        _glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}

	protected void onCreateSetContentView() {
		textBox = new TextView(mContext);
		textBox.setText("" + Shared.renderer().fps());
		textBox.setTextColor(Color.WHITE);
	}

	public void onUpdateScene() {
		textBox.setText("" + Shared.renderer().fps());
	}

	public void renderContinuously(boolean $b) {
		_renderContinuously = $b;
		if (_renderContinuously)
			_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		else
			_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public Handler getInitSceneHandler() {
		return _initSceneHander;
	}

	public Handler getUpdateSceneHandler() {
		return _updateSceneHander;
	}

	public Runnable getInitSceneRunnable() {
		return _initSceneRunnable;
	}

	public Runnable getUpdateSceneRunnable() {
		return _updateSceneRunnable;
	}

	final Runnable _initSceneRunnable = new Runnable() {
		public void run() {
			onInitScene();
		}
	};

	final Runnable _updateSceneRunnable = new Runnable() {
		public void run() {
			onUpdateScene();
		}
	};

	public void onInitScene() {
	}

	public DLauncherAPP(Context $Context) {
		mContext = $Context;
		_initSceneHander = new Handler();
		_updateSceneHander = new Handler();

		//
		// These 4 lines are important.
		//
		Shared.context(mContext);
		scene = new Scene(this);
		Renderer10 r = new Renderer10(scene);
		Shared.renderer(r);

		// _glSurfaceView = new GLSurfaceView(mContext);
		glSurfaceViewConfig();
		_glSurfaceView.setRenderer(r);
		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		onCreateSetContentView();
	}

	public GLSurfaceView getGLSurfaceView() {
		return _glSurfaceView;
	}

}
