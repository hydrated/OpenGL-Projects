package min3d.sampleProject1;

import java.nio.IntBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
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
import min3d.objectPrimitives.Cube;
import min3d.objectPrimitives.Rectangle;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.TextureVo;

public class ExampleLauncher2 extends RendererActivity {

	final String TAG = "ExampleLauncher";

	private Cube cube;
	Object3dContainer _plane;
	float fixedsize;
	private List<ResolveInfo> mApps;
	TextView editBox;
	boolean bTouched = false;
	final float mBackGroundBorder = 1.2f;
	private float aspectRatio = 0f;
	private TouchBorder mTouchBorder;
	private MyGLSurfaceView myGLSurfaceView;
	public int rowNumber = 4;
	public int colNumber = 5;
	float ankle = 0f ;

	@Override
	public void initScene() {

		Light light = new Light();
		//light.ambient.setAll(0xfffffff);
		light.position.setAll(0, 0, 3);
		scene.lights().add(light);
		scene.lightingEnabled(true);

		IntBuffer viewport = IntBuffer.allocate(4);
		Shared.renderer().gl().glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Log.d("hydra", "" + viewport.get(0) + viewport.get(1) + viewport.get(2) + viewport.get(3));
		aspectRatio = (float) viewport.get(2) / viewport.get(3);
		mTouchBorder = new TouchBorder(viewport);
		fixedsize = aspectRatio > 1 ? 5 : 3;
		cube = new Cube(fixedsize, fixedsize / aspectRatio, 1, 1);
		// cube.uvs().
		
		_plane = new Rectangle(fixedsize * 4, fixedsize * 4 / aspectRatio, 5, 5, new Color4(0,0,0,0));
		//cube.addTexture(cube.Face.All, R.drawable.blacksheep, "all");

		// Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.glass);

		// Upload the Bitmap via TextureManager and assign it a
		// textureId ("uglysquares").

		// Shared.textureManager().addTextureId(b, "glass", false);
		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.desert);
		Shared.textureManager().addTextureId(b, "backover", false);
		_plane.textures().addById("backover");
		_plane.rotation().y = 180;
		_plane.position().z = -10f;
		//scene.addChild(_plane);
		b.recycle();
		cube.isVisible(false) ;
		scene.addChild(cube);
		//scene.lightingEnabled(false);
		//scene.useOrtho(true) ;

		initLauncher();
		Shared.renderer().logFps(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	}

	float flingSensity = 1f;

	@Override
	public void updateScene() {
		if (Math.abs(_dx) > flingSensity) {
			for (int i=0 ; i<cube.getFaces()[0].numChildren() ; i++)
				cube.getFaces()[0].getChildAt(i).rotation().y += _dx;
			_dx = 0;
		}
		ankle += 0.1f;
		for (int i=0 ; i<cube.getFaces()[0].numChildren() ; i++)
			cube.getFaces()[0].getChildAt(i).position().z = (float) Math.sin(ankle) / 2 ;
		if (!bTouched && Math.abs(cube.rotation().y % 90) > 1)
			adjustView();

		// Log.d("hydra" , ""+_plane.position().x) ;
	}

	public void adjustView() {
		// Log.d("hydra" , "adjusting" ) ;
		if (Math.abs(cube.rotation().y) % 90 > 45) {
			cube.rotation().y += (cube.rotation().y % 90) / 2;
			// if(Math.abs(_plane.position().x + (cube.rotation().y % 90) /
			// 200) < mBackGroundBorder)_plane.position().x +=
			// (cube.rotation().y % 90) / 200 ;
		} else {
			cube.rotation().y -= (cube.rotation().y % 90) / 2;
			// if(Math.abs(_plane.position().x - (cube.rotation().y % 90) /
			// 200) < mBackGroundBorder)_plane.position().x -=
			// (cube.rotation().y % 90) / 200 ;
		}
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
	public void onUpdateScene() {
		editBox.setText("" + Shared.renderer().fps());
	}

	private void initLauncher() {

		loadApps();
		int i;
		Color4 color = new Color4();

		for (int col = 0; col < colNumber; ++col) {
			for (int row = 0; row < rowNumber; ++row) {
				
				Cube mRectangle = new Cube(0.5f  ,0.5f, 1,1);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colNumber - 1);
				mRectangle.position().z += 0.49f;
				cube.getFaces()[0].addChild(mRectangle);
				cube.colorMaterialEnabled(true) ;
/*				ResolveInfo info = mApps.get(col+row*colNumber);
				// info.activityInfo.loadIcon(getPackageManager());
				Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()));

				String id = "icon" + String.valueOf(col+row*colNumber);
				Shared.textureManager().addTextureId(b, id, false);
				TextureVo t = new TextureVo(id);
				mRectangle.textures().add(t);
				b.recycle() ;*/
/*				mRectangle = new cube(0.5f, 0.5f, 1, 1);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colNumber - 1);
				mRectangle.position().z += 0.49f;
				cube.getFaces()[1].addChild(mRectangle);
				mRectangle = new cube(0.5f, 0.5f, 1, 1);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colNumber - 1);
				mRectangle.position().z += 0.49f;
				cube.getFaces()[2].addChild(mRectangle);
				mRectangle = new cube(0.5f, 0.5f, 1, 1);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (colNumber - 1);
				mRectangle.position().z += 0.49f;
				cube.getFaces()[3].addChild(mRectangle);*/
			}
		}

		for (i = 0; i < 20; ++i) {

			ResolveInfo info = mApps.get(i);
			// info.activityInfo.loadIcon(getPackageManager());
			Bitmap b = drawableToBitmap(info.activityInfo.loadIcon(getPackageManager()));

			String id = "icon" + String.valueOf(i);
			Shared.textureManager().addTextureId(b, id, false);

			((Cube) (cube.getFaces()[(int) (i / (rowNumber * colNumber))].getChildAt(i % (rowNumber * colNumber)))).addTexture(Cube.Face.All, b, id);
			// cube.addTexture(cube.Face.All, b, id);
			//((Shpere) (cube.getFaces()[(int) (i / (rowNumber * colNumber))].getChildAt(i % (rowNumber * colNumber))))
			
			b.recycle();
		}
/*		for (; i < 80; ++i) {
			String id = "icon" + String.valueOf(i);
			Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.blacksheep);
			Shared.textureManager().addTextureId(b, id, false);
			cube.getFaces()[(int) (i / (rowNumber * colNumber))].getChildAt(i % (rowNumber * colNumber)).textures()
					.addById(id);
			b.recycle();
		}*/

	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	// for (int k = 0; k <
	// cube.getFaces()[(int)(0)].getChildAt(0).uvs().size()
	// ; ++k) {
	// Log.d("hahaha",
	// cube.getFaces()[(int)(0)].getChildAt(0).uvs().getAsUv(k).toString());
	// }
	// MediaStore.Images.Media.insertImage(getContentResolver(), b, "hahahaha" +
	// ".jpg Card Image", "hahahaha"+ ".jpg Card Image");
	Bitmap drawableToBitmap(Drawable drawable) {

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
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		drawable.setAlpha(200);
		drawable.draw(canvas);
		return bitmap;
	}

	private float _dx;
	private float _dy;
	private float _d1x, _d2x, _d1y, _d2y;
	private float downX, downY, upX, upY;
	float mPreviousX;
	float mPreviousY;
	float adjustValue = 1.0f;
	float pre_d1x, pre_d1y;

	enum TouchActions {
		down, up,
	}

	private void touchEvents(MotionEvent e, TouchActions s) {
		switch (s) {
		case down:
			mTouchBorder.setTouch(e.getX(), e.getY());
			break;
		case up:
			mTouchBorder.getTouch(e.getX(), e.getY());
			break;
		}
	}

	@Override
	public void glSurfaceViewConfig() {
		myGLSurfaceView = new MyGLSurfaceView(this);
		glSurfaceView(myGLSurfaceView);
		
	}

	void startActivitySafely(Intent intent) {
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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

	float accumulation = 10f;

	class TouchBorder {
		private float borderX;
		private float borderY;
		private float startX;
		private float startY;
		private float touchX;
		private float touchY;
		private int rowNumber = 5;
		private int colNumber = 4;
		private long systemtime = 0;

		TouchBorder(IntBuffer viewport) {
			if (viewport.limit() < 4)
				return;
			startX = viewport.get(0);
			startY = viewport.get(1);
			borderX = viewport.get(2);
			borderY = viewport.get(3);
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
			Log.d(TAG, "setting touch");
			systemtime = System.currentTimeMillis();
			touchX = X;
			touchY = Y;
		}

		public void getTouch(float X, float Y) {
			Log.d(TAG, "getting touch");
			if (Math.abs(X - touchX) + Math.abs(Y - touchY) < accumulation
					&& System.currentTimeMillis() - systemtime < 400) {
				int x = (int) (touchX / borderX * colNumber);
				int y = (int) (touchY / borderY * rowNumber);
				int z = (int) (cube.rotation().y / 90 % 4);
				if (z < 0)
					z += 4;
				z = Math.abs(z - 4) % 4;
				Log.d("hydra", "icon" + x + " " + y + " " + z);
				int itemIndex = x + 4 * y + 20 * z;
				if (itemIndex < mApps.size()) {
					Intent intent = new Intent();
					intent.setClassName(mApps.get(itemIndex).activityInfo.packageName,
							mApps.get(itemIndex).activityInfo.name);
					startActivitySafely(intent);
				}
			}

		}
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
			float x = e.getX() * adjustValue;
			float y = e.getY() * adjustValue;
			switch (e.getAction()) {

			case MotionEvent.ACTION_MOVE:
				// Log.d("hydra" , "moving" ) ;
				if (e.getPointerCount() == 1) {
					_dx = x - mPreviousX;
					_dy = y - mPreviousY;
				} else if (e.getPointerCount() == 2) {

					_d1x = e.getX(0) - e.getX(1);
					_d1y = e.getY(0) - e.getY(1);
				}
				bTouched = true;
				break;
			case MotionEvent.ACTION_UP:
				bTouched = false;
				// upX = e.getX();
				// upY = e.getY();
				touchEvents(e, TouchActions.up);
				break;
			case MotionEvent.ACTION_DOWN:
				// downX = e.getX() ;
				// downY = e.getY() ;
				touchEvents(e, TouchActions.down);
			}
			mPreviousX = x;
			mPreviousY = y;
			return true;
		}

	}
}

/*
 * // Create a Bitmap. Here we're generating it from an embedded resource, //
 * but the Bitmap could be created in any manner (eg, dynamically).
 * 
 * //Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.glass);
 * 
 * // Upload the Bitmap via TextureManager and assign it a // textureId
 * ("uglysquares").
 * 
 * //Shared.textureManager().addTextureId(b, "glass", false);
 * 
 * // Unless you have a specific reason for doing so, recycle the Bitmap, // as
 * it is no longer necessary.
 * 
 * //b.recycle();
 * 
 * // Create a TextureVo using the textureId that was previously added // to the
 * TextureManager ("uglysquares").
 * 
 * //TextureVo texture = new TextureVo("glass"); //b =
 * Utils.makeBitmapFromResourceId(this, R.drawable.clouds_alpha2b);
 * //Shared.textureManager().addTextureId(b, "clouds", false); //b.recycle();
 * //TextureVo texture1 = new TextureVo("clouds");
 * //texture1.textureEnvs.get(0).param = GL10.GL_DECAL;
 * //cube.addTexture(cube.Face.All, R.drawable.clouds_alpha2b, "all1");
 * 
 * // Add it to the TexturesList held by the Object3d, // and it will be duly
 * rendered. Shared.renderer().gl().glEnable(GL10.GL_BLEND); // Turn Blending On
 * ( // NEW ) Shared.renderer().gl().glDisable(GL10.GL_DEPTH_TEST);
 * Shared.renderer().gl().glColor4f(0.0f, 0.0f, 0.0f, 0.5f); // Full //
 * Brightness. // 50% Alpha // ( NEW )
 * Shared.renderer().gl().glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_DST_ALPHA);
 */