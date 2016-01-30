package min3d.sampleProject1;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Cylinder;
import min3d.objectPrimitives.SkyBox;
import min3d.objectPrimitives.Sphere;
import min3d.sampleProject1.ExampleWaterSimulate.MyGLSurfaceView;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.TextureVo;

public class ExampleHydra extends RendererActivity {

	Object3dContainer mball;
	TextureVo _cloudTexture;
	private SkyBox mSkyBox;

	int _count = 0;
	private float _dx;
	private float _dy;
	private float _d1x, _d2x, _d1y, _d2y;
	private float _rotx = 0;
	private float _roty = 0;
	TextView editBox;

	public void initScene() {

		mSkyBox = new SkyBox(30.0f, 1);
		mSkyBox.addTexture(SkyBox.Face.North, R.drawable.en_cube_back, "north");
		mSkyBox.addTexture(SkyBox.Face.East, R.drawable.en_cube_right, "east");
		mSkyBox.addTexture(SkyBox.Face.South, R.drawable.en_cube_front, "south");
		mSkyBox.addTexture(SkyBox.Face.West, R.drawable.en_cube_left, "west");
		mSkyBox.addTexture(SkyBox.Face.Up, R.drawable.en_cube_top, "up");
		mSkyBox.addTexture(SkyBox.Face.Down, R.drawable.en_cube_bottom, "down");
		// mSkyBox.scale().y = 1.0f;
		// mSkyBox.scale().z = 1.0f;
		scene.addChild(mSkyBox);

		Light light = new Light();
		light.ambient.setAll((short) 64, (short) 64, (short) 64, (short) 255);
		light.position.setAll(3, 3, 3);
		scene.lights().add(light);

		mball = new Cylinder(1f, 2f , 20, 4 , new Color4());
		scene.addChild(mball);

		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.backover);
		Shared.textureManager().addTextureId(b, "backover", false);
		b.recycle();

		b = Utils.makeBitmapFromResourceId(this, R.drawable.clouds_alpha2b);
		Shared.textureManager().addTextureId(b, "clouds", false);
		b.recycle();

		TextureVo t = new TextureVo("backover");
		mball.textures().add(t);

		_cloudTexture = new TextureVo("clouds");
		_cloudTexture.textureEnvs.get(0).param = GL10.GL_DECAL;
		_cloudTexture.repeatU = true; // .. this is the default, but just to be
										// explicit

		mball.textures().add(_cloudTexture);
		mball.colorMaterialEnabled(true);
		mball.lightingEnabled(true);
		_count = 0;
		scene.camera().frustum.shortSideLength(3f);
		Shared.renderer().logFps(true);
		detectOpenGLES20() ;
	}

	private boolean detectOpenGLES20() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		Log.d("hydra" , ""+info.reqGlEsVersion) ;
		return (info.reqGlEsVersion >= 0x20000);
	}

	@Override
	protected void onCreateSetContentView() {
		super.onCreateSetContentView();
		editBox = new TextView(this);
		editBox.setText("" + Shared.renderer().fps());
		editBox.setTextColor(Color.BLACK);
		addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	float cameraScale = 5f;

	float r2 = (float) Math.cos(_roty * Math.PI / 180.0);
	float h2 = (float) Math.sin(_roty * Math.PI / 180.0);

	float co = (float) Math.cos(_rotx * Math.PI / 180.0);
	float si = -(float) Math.sin(_rotx * Math.PI / 180.0);

	@Override
	public void updateScene() {
		scene.camera().setDirtyFlag();
		mball.rotation().y += 1.0f;

		// Animate texture offset
		_cloudTexture.offsetU += 0.001f;

		/**
		 * When trackball moves horizontally...
		 * 
		 * Rotate camera around center of the scene in a circle. Its position is
		 * 2 units above the boxes, but its target() position remains at the
		 * scene origin, so the camera always looks towards the center.
		 */
		if (_dx != 0 || _dy != 0) {
			_rotx += _dx;

			_dx = 0;

			_roty += _dy;

			_dy = 0;

			r2 = (float) Math.cos(_roty * Math.PI / 180.0);
			h2 = (float) Math.sin(_roty * Math.PI / 180.0);

			co = (float) Math.cos(_rotx * Math.PI / 180.0);
			si = -(float) Math.sin(_rotx * Math.PI / 180.0);

			scene.camera().position.setAll(r2 * co * cameraScale, h2 * cameraScale, r2 * si * cameraScale);
		}
		if (_d1x != 0 || _d1y != 0) {
			if (Math.abs(pre_d1x) + Math.abs(pre_d1y) > Math.abs(_d1x) + Math.abs(_d1y)) {
				cameraScale += 0.1f;
			} else if (Math.abs(pre_d1x) + Math.abs(pre_d1y) < Math.abs(_d1x) + Math.abs(_d1y)) {
				cameraScale -= 0.1f;
			}

			pre_d1x = _d1x;
			pre_d1y = _d1y;
			scene.camera().position.setAll(r2 * co * cameraScale, h2 * cameraScale, r2 * si * cameraScale);
		}
		// Log.d("name", "camera:" + scene.camera().position.x +
		// scene.camera().position.y + scene.camera().position.z);
		// Log.d("multi" , "" + gTouchX1 + gTouchY1 + gTouchX2 + gTouchY2 ) ;
	}
	
	





	@Override
	public void onUpdateScene() {
		editBox.setText("" + Shared.renderer().fps());
	}

	float mPreviousX;
	float mPreviousY;
	float adjustPoint = 2.0f;
	float pre_d1x, pre_d1y;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX() * adjustPoint;
		float y = e.getY() * adjustPoint;
		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (e.getPointerCount() == 1) {
				_dx = x - mPreviousX;
				_dy = y - mPreviousY;
			} else if (e.getPointerCount() == 2) {

				_d1x = e.getX(0) - e.getX(1);
				_d1y = e.getY(0) - e.getY(1);
			}
		}
		mPreviousX = x;
		mPreviousY = y;
		return true;
	}
	
}
