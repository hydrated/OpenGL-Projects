package min3d.objectPrimitives;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.util.Log;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;

public class Cube extends Object3dContainer {
	private float width;
	private float height;
	private int quality;
	private Color4 color = new Color4(0,0,0,0);
	private Rectangle[] faces;
	private float halfSize;

	public enum Face {
		North, East, South, West, Up , Down ,All
	}

	public Cube(float width, float height, int hquality, int wquality) {
		super(0, 0);
		this.width = width;
		this.height = height;
		this.halfSize = width * 0.5f;
		this.quality = hquality;
		build();
	}
	
	public Cube(float width, float height, int hquality, int wquality , Color4 icolor) {
		super(0, 0);
		this.width = width;
		this.height = height;
		this.halfSize = width * 0.5f;
		this.quality = hquality;
		this.color = icolor ;
		build();
	}
	

	int rowNumber = 4;
	int columnNumber = 5;

	private void build() {
		faces = new Rectangle[6];
		Rectangle north = new Rectangle(width, height, quality, quality, color);
		Rectangle east = new Rectangle(width, height, quality, quality, color);
		Rectangle south = new Rectangle(width, height, quality, quality, color);
		Rectangle west = new Rectangle(width, height, quality, quality, color);
		Rectangle up = new Rectangle(width, height, quality, quality, color);
		Rectangle down = new Rectangle(width, height, quality, quality, color);
		
/*		for (int col = 0; col < columnNumber; ++col) {
			for (int row = 0; row < rowNumber; ++row) {
				Rectangle mRectangle = new Rectangle(0.5f, 0.5f, 1, 1, color);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (columnNumber - 1);
				mRectangle.position().z -= 0.1f ;
				north.addChild(mRectangle);
				mRectangle = new Rectangle(0.5f, 0.5f, 1, 1, color);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (columnNumber - 1);
				mRectangle.position().z -= 0.1f ;
				east.addChild(mRectangle);
				mRectangle = new Rectangle(0.5f, 0.5f, 1, 1, color);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (columnNumber - 1);
				mRectangle.position().z -= 0.1f ;
				south.addChild(mRectangle);
				mRectangle = new Rectangle(0.5f, 0.5f, 1, 1, color);
				mRectangle.hasBlend(true);
				mRectangle.position().x += 1.0f - (float) row * 2 / (rowNumber - 1);
				mRectangle.position().y += 2.0f - (float) col * 4 / (columnNumber - 1);
				mRectangle.position().z -= 0.1f ;
				west.addChild(mRectangle);
			}
		}*/
		
		north.position().z = halfSize;
		north.rotation().y = 180;
		//north.hasBlend(true);
		// north.doubleSidedEnabled(true);

		east.rotation().y = -90;
		east.position().x = halfSize;
		//east.hasBlend(true);
		// east.doubleSidedEnabled(true);

		// south.rotation().y = 180;
		south.position().z = -halfSize;
		//south.hasBlend(true);
		// south.doubleSidedEnabled(true);

		west.rotation().y = 90;
		west.position().x = -halfSize;
		//west.hasBlend(true) ;
		// west.doubleSidedEnabled(true);
		
		up.rotation().x = 90;
		up.position().y = halfSize;
		
		down.rotation().x = -90;
		down.position().y = -halfSize;

		faces[Face.North.ordinal()] = north;
		faces[Face.East.ordinal()] = east;
		faces[Face.South.ordinal()] = south;
		faces[Face.West.ordinal()] = west;
		faces[Face.Up.ordinal()] = up ;
		faces[Face.Down.ordinal()] = down ;
		/*
		 * for (int i = 0; i < faces[Face.North.ordinal()].uvs().size(); ++i) {
		 * switch (i % 4) { case 0: faces[Face.North.ordinal()].uvs().set(i,
		 * 0.0f, 0.0f); break; case 1: faces[Face.North.ordinal()].uvs().set(i,
		 * 0.0f, 1.0f); break; case 2: faces[Face.North.ordinal()].uvs().set(i,
		 * 0.0f, 1.0f); break; case 3: faces[Face.North.ordinal()].uvs().set(i,
		 * 1.0f, 1.0f); break; } }
		 */
/*		for (int i = 0; i < faces[Face.North.ordinal()].uvs().size(); ++i) {
			Log.d("test", faces[Face.North.ordinal()].uvs().getAsUv(i).toString());
		}*/

		addChild(north);
		addChild(east);
		addChild(south);
		addChild(west);
		addChild(up);
		addChild(down);
		this.position().z -= halfSize;
	}

	public Rectangle[] getFaces() {
		return faces ;
	}
	
	public void addTexture(Face face, int resourceId, String id) {
		Bitmap bitmap = Utils.makeBitmapFromResourceId(resourceId);
		Shared.textureManager().addTextureId(bitmap, id, true);
		bitmap.recycle();
		addTexture(face, bitmap, id);
	}

	public void addTexture(Face face, Bitmap bitmap, String id) {
		if (face == Face.All) {
			for (int i = 0; i < 6; i++) {
				faces[i].textures().addById(id);
				faces[i].textures().getById(id).textureEnvs.get(0).param = GL10.GL_DECAL;
				faces[i].textures().getById(id).repeatU = false;
				faces[i].textures().getById(id).repeatV = false;
			}
		} else {
			faces[face.ordinal()].textures().addById(id);
			faces[face.ordinal()].textures().getById(id).textureEnvs.get(0).param = GL10.GL_DECAL;
			faces[face.ordinal()].textures().getById(id).repeatU = false;
			faces[face.ordinal()].textures().getById(id).repeatV = false;
		}
	}
}
