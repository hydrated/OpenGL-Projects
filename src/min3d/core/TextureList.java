package min3d.core;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;

import min3d.Shared;
import min3d.vos.TextureVo;

/** Manages a list of TextureVo's used by Object3d's. This allows an Object3d to
 * use multiple textures.
 * 
 * If more textures are added than what's supported by the hardware running the
 * application, the extra items are ignored by Renderer
 * 
 * Uses a subset of ArrayList's methods. */
public class TextureList {
	private ArrayList<TextureVo> _t;

	public TextureList() {
		_t = new ArrayList<TextureVo>();
	}

	/** Adds item to the list */
	public boolean add(TextureVo $texture) {
		if (!Shared.textureManager().contains($texture.textureId))
			return false;
		return _t.add($texture);
	}

	/** Adds item at the given position to the list */
	public void add(int $index, TextureVo $texture) {
		_t.add($index, $texture);
	}

	/** Adds a new TextureVo with the given textureId to the list, and returns
	 * that textureVo */
	public TextureVo addById(String $textureId) {
		if (!Shared.textureManager().contains($textureId)) {
			throw new Error("Could not create TextureVo using textureId \"" + $textureId
					+ "\". TextureManager does not contain that id.");
		}

		TextureVo t = new TextureVo($textureId);
		_t.add(t);
		return t;
	}

	public TextureVo addByIdDAPP(String $textureId) {
		if (!Shared.textureManager().contains($textureId)) {
			if ($textureId.startsWith("index")) {
				Bitmap b = indexToBitmap(String.valueOf(Integer.valueOf($textureId.substring(5)) + 1));
				Shared.textureManager().addTextureId(b, $textureId);
			}
		}

		if (!Shared.textureManager().contains($textureId)) {
			throw new Error("Could not create TextureVo using textureId \"" + $textureId
					+ "\". TextureManager does not contain that id.");
		}

		TextureVo t = new TextureVo($textureId);
		_t.add(t);
		return t;
	}

	public TextureVo addByIdDAPP(String $textureId, List<ResolveInfo> $mList) {
		if (!Shared.textureManager().contains($textureId)) {
			if ($textureId.startsWith("index")) {
				Bitmap b = indexToBitmap(String.valueOf(Integer.valueOf($textureId.substring(5)) + 1));
				Shared.textureManager().addTextureId(b, $textureId);
			} else if ($textureId.startsWith("icon")) {
				ResolveInfo info = $mList.get(Integer.valueOf($textureId.substring(4)));
				// info.activityInfo.loadIcon(getPackageManager());
				Bitmap b = drawableToBitmap(
						info.activityInfo.loadIcon(Shared.context().getPackageManager()),
						info.activityInfo.loadLabel(Shared.context().getPackageManager()).toString(), 255);
				Shared.textureManager().addTextureId(b, $textureId);
			} else {
				throw new Error("Could not create TextureVo using textureId \"" + $textureId
						+ "\". TextureManager does not contain that id.");
			}

		}

		TextureVo t = new TextureVo($textureId);
		_t.add(t);
		return t;
	}

	/** Adds texture as the sole item in the list, replacing any existing items */
	public boolean addReplace(TextureVo $texture) {
		_t.clear();
		return _t.add($texture);
	}

	public boolean addReplace(String $textureId) {
		if (!Shared.textureManager().contains($textureId)) {

			throw new Error("Could not create TextureVo using textureId \"" + $textureId
					+ "\". TextureManager does not contain that id.");
		}

		TextureVo t = new TextureVo($textureId);
		_t.clear();
		return _t.add(t);

	}

	public boolean addReplaceDAPP(String $textureId) {
		if (!Shared.textureManager().contains($textureId)) {
			if ($textureId.startsWith("index")) {
				Bitmap b = indexToBitmap(String.valueOf(Integer.valueOf($textureId.substring(5)) + 1));
				Shared.textureManager().addTextureId(b, $textureId);
			}
		}

		if (!Shared.textureManager().contains($textureId)) {

			throw new Error("Could not create TextureVo using textureId \"" + $textureId
					+ "\". TextureManager does not contain that id.");
		}

		TextureVo t = new TextureVo($textureId);
		_t.clear();
		return _t.add(t);

	}

	public boolean addReplaceDAPP(String $textureId, List<ResolveInfo> $mList) {
		if (!Shared.textureManager().contains($textureId)) {
			if ($textureId.startsWith("index")) {
				Bitmap b = indexToBitmap(String.valueOf(Integer.valueOf($textureId.substring(5)) + 1));
				Shared.textureManager().addTextureId(b, $textureId);
			} else if ($textureId.startsWith("icon")) {
				ResolveInfo info = $mList.get(Integer.valueOf($textureId.substring(4)));
				// info.activityInfo.loadIcon(getPackageManager());
				Bitmap b = drawableToBitmap(
						info.activityInfo.loadIcon(Shared.context().getPackageManager()),
						info.activityInfo.loadLabel(Shared.context().getPackageManager()).toString(), 255);
				Shared.textureManager().addTextureId(b, $textureId);
			} else {
				throw new Error("Could not create TextureVo using textureId \"" + $textureId
						+ "\". TextureManager does not contain that id.");
			}
		}

		TextureVo t = new TextureVo($textureId);
		_t.clear();
		return _t.add(t);

	}
	
	private static final int MAX_LINES = 2;
	private static final int textFirstRowOffset = 36;
	
	public static Bitmap drawableToBitmap(Drawable drawable, String text, int alpha) {

		// Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ?
		// Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap.Config c = Bitmap.Config.ARGB_8888;

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
		// textPaint.setTypeface(Typeface.DEFAULT_BOLD) ;
		textPaint.setTextSize(32);
		// textPaint.setTextScaleX(1.1f);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(4, 3, 6, 0XFF000000);
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		TextPaint mTextPaint = new TextPaint(textPaint);
		StaticLayout layout = new StaticLayout(text, mTextPaint, (int) bitmap.getWidth(), Alignment.ALIGN_CENTER, 1, 0,
				true);
		int lineCount = layout.getLineCount();
		if (lineCount > MAX_LINES) {
			lineCount = MAX_LINES;
		}

		for (int i = 0; i < lineCount; i++) {
			final String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));
			int x = (int) (1 + ((bitmap.getWidth() - mTextPaint.measureText(lineText)) * 0.5f));
			int y = bitmap.getWidth() + textFirstRowOffset + (i * textFirstRowOffset);
			canvas.drawText(lineText, x, y, mTextPaint);
		}

		return bitmap;
	}

	public static Bitmap indexToBitmap(String text) {

		// Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ?
		// Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap.Config c = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = Bitmap.createBitmap(128, 128, c);

		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		// canvas.drawARGB(128, 128, 128, 128);

		Paint mPaint = new Paint();
		mPaint.setTypeface(Typeface.SERIF);
		//mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
		mPaint.setTextSize(112);
		// textPaint.setTextScaleX(1.1f);
		mPaint.setAntiAlias(true);
		// textPaint.setColor(Resources.getSystem().getColor(R.color.ha1) );
		mPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		TextPaint mTextPaint = new TextPaint(mPaint);
		float canvasWidth = canvas.getWidth();
		float textWidth = mTextPaint.measureText(text);
		float x = (canvasWidth - textWidth) / 2;
		float y = -mTextPaint.ascent();
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(text, x, y, mTextPaint);
		// canvas.drawText(lineText, x+128, y, mTextPaint);

		return bitmap;
	}


	/** Removes item from the list */
	public boolean remove(TextureVo $texture) {
		return _t.remove($texture);
	}

	/** Removes item with the given textureId from the list */
	public boolean removeById(String $textureId) {
		TextureVo t = this.getById($textureId);
		if (t == null) {
			throw new Error("No match in TextureList for id \"" + $textureId + "\"");
		}
		return _t.remove(t);
	}

	public void removeAll() {
		for (int i = 0; i < _t.size(); i++)
			_t.remove(0);
	}

	/** Get item from the list which is at the given index position */
	public TextureVo get(int $index) {
		return _t.get($index);
	}

	/** Gets item from the list which has the given textureId */
	public TextureVo getById(String $textureId) {
		for (int i = 0; i < _t.size(); i++) {
			String s = _t.get(i).textureId;
			if ($textureId == s) {
				TextureVo t = _t.get(i);
				return t;
			}
		}
		return null;
	}

	public int size() {
		return _t.size();
	}

	public void clear() {
		_t.clear();
	}

	/** Return a TextureVo array of TextureList's items */
	public TextureVo[] toArray() {
		Object[] a = _t.toArray();
		TextureVo[] ret = new TextureVo[a.length];
		for (int i = 0; i < _t.size(); i++) {
			ret[i] = (TextureVo) _t.get(i);
		}
		return ret;
	}

	/** Returns a String Array of the textureIds of each of the items in the list */
	public String[] getIds() {
		// BTW this makes a casting error. Why?
		// (TextureVo[])_t.toArray();

		String[] a = new String[_t.size()];
		for (int i = 0; i < _t.size(); i++) {
			a[i] = _t.get(i).textureId;
		}
		return a;
	}
}
