package min3d.vos;

import android.os.Handler;
import android.util.Log;
import min3d.sampleProject1.DLauncherAPP;

public class PageFlip {

	private final static float Radian = (float) (Math.PI / 180.0f);
	private static float cylinderMetric = 0;

	private static final int INDEX_POSITION_X = 0;
	private static final int INDEX_POSITION_Y = 1;
	private static final int INDEX_POSITION_Z = 2;
	private static final int INDEX_ROTATION_X = 3;
	private static final int INDEX_ROTATION_Y = 4;
	private static final int INDEX_ROTATION_Z = 5;
	private static final int IS_ROTATION_INCREASE = 6;
	private static final int IS_DRAW_CHILDREN = 7;

	/* @formatter:off */
	/* { position.x , position.y , position.z , rotation.x , rotation.y , rotation.z }  , rotation angle isIncrease, isDrawChildren
	 * Warning : plane numbers must be greater than 2  
	 */
	
	public static enum FLIP_TYPE {
		TYPE_NORMAL, TYPE_FLIP_SIMPLE, TYPE_FLIP_ROLL, TYPE_CUBE_OUTSIDE, TYPE_WAVE, TYPE_WINDMILL, TYPE_CUBE_INSIDE, 
		TYPE_BOUNCE, TYPE_BINARY, TYPE_TURNSTILE, TYPE_SHUTTER, TYPE_CYLINDER, TYPE_SPHERE,
	}
	public final static float[][][] routes = { 
		{ { 0,0,0, 0,180,0 ,1,1}, { 3.3f,0,0, 0,180,0 ,1,1}, { -3.3f,0,0, 0,180,0 ,1,0} } , //normal
		{ { 0,0,0, 0,180,0 ,-1,1}, { 0,0,0, 0,0,0 ,1,1} , {0,0,0, 0,180,0 ,1,0} , {0,0,0, 0,0,0 ,1,0} } ,//simple flip
		{ { 0,0,0, 0,180,0 ,1,1}, { 4,0,0, 0,180,180 ,1,1},{ -4,0,0, 0,180,-180 ,1,0} } , //roll
		{ { 0,0,-1f, 0,180,0 ,1,1}, { 1f,0,-2f, 0,270,0 ,1,1}, { 0,0,-3f, 0,360,0 ,1,0}, { -1f,0,-2f, 0,90,0 ,1,0} } , //cube(outside)
		{ { 0,0,0, 0,180,0 ,1,1}, { 4,0,-3, 0,180,0 ,1,1}, { -4,0,-3, 0,180,0 ,1,0} } , //wave
		{ { 0,0,0, 0,180,0 ,1,1}, { 5f,1f,0, 0,180,-30 ,1,1}, { -5f,1f,0, 0,180,30 ,1,0} } , //windmill
		{ { 0,0,0, 0,180,0 ,1,1}, { 2.4f,0,2.4f, 0,90,0 ,1,1}, { 0,0,4f, 0,0,0 ,1,0}, { -2.4f,0,2.4f, 0,270,0 ,1,0} } , //cube(inside)
		{ { 0,0,0, 0,180,0 ,1,1}, { 3.3f,3,0, 0,180,0 ,1,1}, { -3.3f,3,0, 0,180,0 ,1,0} } , //bounce		
		{ { 0,0,0, 0,180,0 ,1,1}, { 3.3f,0,0, 0,180,0 ,1,1}, { -3.3f,0,0, 0,180,0 ,1,0} } , //binary
		{ { 0,0,0, 0,180,0 ,1,1}, { 1.8f,0,-1.1f, 0,60,0 ,1,1 }, {-1.1f,0,1.1f, 0,90,0 ,1,0} } ,//Turnstile
		{ { 0,0,0, 0,180,0 ,-1,1}, { 0,0,0, 0,0,0 ,1,1} , {0,0,0, 0,180,0 ,1,0} , {0,0,0, 0,0,0 ,1,0} } , // Shutter
		{ { 0,0,-1.5f, 0,180,0 ,-1,1}, { 0,0,-1.5f, 0,0,0 ,1,1} , {0,0,-1.5f, 0,180,0 ,1,0} , {0,0,-1.5f, 0,0,0 ,1,0} } , // Cylinder
		{ { 0,0,-1.5f, 0,180,0 ,-1,1}, { 0,0,-1.5f, 0,0,0 ,1,1} , {0,0,-1.5f, 0,180,0 ,1,0} , {0,0,-1.5f, 0,0,0 ,1,0} } , // Sphere
	};
	
	public final static float[][][] iconRoutes = {
		
	};
	
	/* @formatter:on */

	public static void flipPage(int row, int column) {

		switch (DLauncherAPP.Flip_Method) {
		case TYPE_NORMAL:
			setPlanesLocation(FLIP_TYPE.TYPE_NORMAL.ordinal());
			break;
		case TYPE_FLIP_SIMPLE:
			setPlanesLocation(FLIP_TYPE.TYPE_FLIP_SIMPLE.ordinal());
			break;
		case TYPE_FLIP_ROLL:
			setPlanesLocation(FLIP_TYPE.TYPE_FLIP_ROLL.ordinal());
			break;
		case TYPE_CUBE_OUTSIDE:
			setPlanesLocation(FLIP_TYPE.TYPE_CUBE_OUTSIDE.ordinal());
			setIconsLocation(FLIP_TYPE.TYPE_CUBE_OUTSIDE, row, column);
			break;
		case TYPE_WAVE:
			setPlanesLocation(FLIP_TYPE.TYPE_WAVE.ordinal());
			break;
		case TYPE_WINDMILL:
			setPlanesLocation(FLIP_TYPE.TYPE_WINDMILL.ordinal());
			break;
		case TYPE_CUBE_INSIDE:
			setPlanesLocation(FLIP_TYPE.TYPE_CUBE_INSIDE.ordinal());
			break;
		case TYPE_BOUNCE:
			setPlanesLocation(FLIP_TYPE.TYPE_BOUNCE.ordinal());
			break;
		case TYPE_BINARY:
			setPlanesLocation(FLIP_TYPE.TYPE_BINARY.ordinal());
			setIconsLocation(FLIP_TYPE.TYPE_BINARY, row, column);
			break;
		case TYPE_TURNSTILE:
			setPlanesLocation(FLIP_TYPE.TYPE_TURNSTILE.ordinal());
			break;
		case TYPE_SHUTTER:
			setPlanesLocation(FLIP_TYPE.TYPE_SHUTTER.ordinal());
			setIconsLocation(FLIP_TYPE.TYPE_SHUTTER, row, column);
			break;
		case TYPE_CYLINDER:
			setPlanesLocation(FLIP_TYPE.TYPE_CYLINDER.ordinal());
			setIconsLocation(FLIP_TYPE.TYPE_CYLINDER, row, column);
			break;
		case TYPE_SPHERE:
			setPlanesLocation(FLIP_TYPE.TYPE_SPHERE.ordinal());
			setIconsLocation(FLIP_TYPE.TYPE_SPHERE, row, column);
			break;
		default:
			setPlanesLocation(FLIP_TYPE.TYPE_NORMAL.ordinal());
			break;

		}
	}

	private final static void setIconsLocation(FLIP_TYPE type, int row, int col) {

		// row 4 col5
		int tmpIndex;
		switch (type) {
		case TYPE_CUBE_OUTSIDE:
			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				for (int j = 0; j < col; ++j) {
					for (int i = 0; i < row; ++i) {
						tmpIndex = j * row + i;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().z = -1f;
					}
				}
			}
			break;
		case TYPE_BINARY:
			float tmpAngle = getRelativeFloat(DLauncherAPP.sceneAngle % DLauncherAPP.pageDistanceMeasure,
					DLauncherAPP.pageDistanceMeasure);

			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				if (k < routes[type.ordinal()].length) {
					for (int j = 0; j < col; ++j) {
						for (int i = 0; i < row; ++i) {
							tmpIndex = j * row + i;
							DLauncherAPP.lmPlanes
									.get(k)
									.getChildAt(tmpIndex)
									.position_offset()
									.setAll(-DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position().x
											* (1 - Math.abs((tmpAngle - DLauncherAPP.pageDistanceMeasure / 2)
													/ DLauncherAPP.pageDistanceMeasure * 2)),
											-DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position().y
													* (1 - Math.abs((tmpAngle - DLauncherAPP.pageDistanceMeasure / 2)
															/ DLauncherAPP.pageDistanceMeasure * 2)), 0);
						}
					}
				}
			}
			break;

		case TYPE_SHUTTER:
			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				if (k < routes[type.ordinal()].length) {
					for (int j = 0; j < col; ++j) {
						for (int i = 0; i < row; ++i) {
							tmpIndex = j * row + i;
							DLauncherAPP.lmPlanes
									.get(k)
									.getChildAt(tmpIndex)
									.position_offset()
									.setAll(-(float) (1 - Math.cos(DLauncherAPP.sceneAngle * Radian * 2))
											* DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position().x,
											0,
											DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position().x
													* (float) Math.sin(DLauncherAPP.sceneAngle * Radian * 2));
							if (DLauncherAPP.sceneAngle > 0) {
								if ((int) ((DLauncherAPP.sceneAngle + 45) / DLauncherAPP.pageDistanceMeasure) % 2 == 0) {
									DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).setReverse(false);
								} else {
									DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).setReverse(true);
								}
							} else {
								if ((int) (-(DLauncherAPP.sceneAngle - 45) / DLauncherAPP.pageDistanceMeasure) % 2 == 0) {
									DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).setReverse(false);
								} else {
									DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).setReverse(true);
								}
							}
						}
					}
				}

			}
			break;
		case TYPE_CYLINDER:
			float gapRowRadian = 180 * Radian / (row * 2);
			float gapColRadian = 180 * Radian / (col * 2);
			float gapRow = 180 / (row * 2);
			float gapCol = 180 / (col * 2);
			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				for (int j = 0; j < col; ++j) {
					for (int i = 0; i < row; ++i) {
						tmpIndex = j * row + i;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().z = -1.5f + 1.5f
								* ((1 - (float) Math.sin(gapRowRadian + i * 2 * gapRowRadian)) * cylinderMetric);
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().x = (-DLauncherAPP.lmPlanes
								.get(k).getChildAt(tmpIndex).position().x + 1.5f * (float) Math.cos(gapRowRadian + i
								* 2 * gapRowRadian))
								* cylinderMetric;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().y = (gapRow + i * 2 * gapRow - 90)
								* cylinderMetric;
						// DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().x
						// = -(gapCol + j * 2 * gapCol - 90)
						// * cylinderMetric;
					}
				}
			}
			break;
		case TYPE_SPHERE:
			gapRowRadian = 180 * Radian / (row * 2);
			gapColRadian = 120 * Radian / (col * 2);
			gapRow = 180 / (row * 2);
			gapCol = 120 / (col * 2);
			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				for (int j = 0; j < col; ++j) {
					for (int i = 0; i < row; ++i) {
						tmpIndex = j * row + i;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().z = -1.5f
								+ 1.5f
								* (1 - (float) Math.sin(gapRowRadian + i * 2 * gapRowRadian)
										* (float) Math.sin(30 * Radian + gapColRadian + j * 2 * gapColRadian))
								* cylinderMetric;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().x = (-DLauncherAPP.lmPlanes
								.get(k).getChildAt(tmpIndex).position().x + 1.5f
								* (float) Math.cos(gapRowRadian + i * 2 * gapRowRadian)
								* (float) Math.sin(30 * Radian + gapColRadian + j * 2 * gapColRadian))
								* cylinderMetric;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().y = (-DLauncherAPP.lmPlanes
								.get(k).getChildAt(tmpIndex).position().y + 1.5f * (float) Math.cos(30 * Radian
								+ gapColRadian + j * 2 * gapColRadian))
								* cylinderMetric;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().x = -(30 + gapCol + j * 2 * gapCol - 90)
								* cylinderMetric ;
						DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().y = (gapRow + i * 2 * gapRow - 90)
								* cylinderMetric;
						
/*						Log.d("hydra" ,"col"+j+" row"+i 
								+" rotationx"+DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().x
								+" rotationy"+DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().y
								+" rotationz"+DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).rotation().z) ;*/
						
					}
				}
			}
			break;
		default:
			for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
				if (k < routes[type.ordinal()].length) {
					for (int j = 0; j < col; ++j) {
						for (int i = 0; i < row; ++i) {
							tmpIndex = j * row + i;
							DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position().z = 0;
							DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).position_offset().setAll(0, 0, 0);
							DLauncherAPP.lmPlanes.get(k).getChildAt(tmpIndex).setReverse(false);
						}
					}
				}
			}
			break;
		}
	}

	private final static void setPlanesLocation(int Method) {

		for (int k = 0, size = DLauncherAPP.lmPlanes.size(); k < size; ++k) {
			if (k < routes[Method].length) {
				float tmpAngle = getRelativeFloat(DLauncherAPP.sceneAngle + k * DLauncherAPP.pageDistanceMeasure,
						routes[Method].length * DLauncherAPP.pageDistanceMeasure);
				int tmpIndex = getRelativeIndex((int) (tmpAngle / DLauncherAPP.pageDistanceMeasure),
						routes[Method].length);
				int tmpIndexNext = getRelativeIndex(tmpIndex + 1, routes[Method].length);
				if (routes[Method][tmpIndexNext][IS_DRAW_CHILDREN] == 0) {
					DLauncherAPP.lmPlanes.get(k).isDrawChildren(false);
					DLauncherAPP.lmPlanes.get(k).isVisible(false);
					continue;
				} else {
					DLauncherAPP.lmPlanes.get(k).isDrawChildren(true);
					DLauncherAPP.lmPlanes.get(k).isVisible(true);
				}
				float tmpAngleResidue = getRelativeFloat(DLauncherAPP.sceneAngle % DLauncherAPP.pageDistanceMeasure,
						DLauncherAPP.pageDistanceMeasure);
				float percentage = routes[Method][tmpIndex][IS_ROTATION_INCREASE] * tmpAngleResidue
						/ DLauncherAPP.pageDistanceMeasure;
				float $x = (routes[Method][tmpIndexNext][INDEX_POSITION_X] - routes[Method][tmpIndex][INDEX_POSITION_X])
						* percentage + routes[Method][tmpIndex][INDEX_POSITION_X];
				float $y = (routes[Method][tmpIndexNext][INDEX_POSITION_Y] - routes[Method][tmpIndex][INDEX_POSITION_Y])
						* percentage + routes[Method][tmpIndex][INDEX_POSITION_Y];
				float $z = (routes[Method][tmpIndexNext][INDEX_POSITION_Z] - routes[Method][tmpIndex][INDEX_POSITION_Z])
						* percentage + routes[Method][tmpIndex][INDEX_POSITION_Z];
				DLauncherAPP.lmPlanes.get(k).position().setAll($x, $y, $z);
				float _x = (routes[Method][tmpIndexNext][INDEX_ROTATION_X] - routes[Method][tmpIndex][INDEX_ROTATION_X])
						* percentage + routes[Method][tmpIndex][INDEX_ROTATION_X];
				float _y = (routes[Method][tmpIndexNext][INDEX_ROTATION_Y] - routes[Method][tmpIndex][INDEX_ROTATION_Y])
						* percentage + routes[Method][tmpIndex][INDEX_ROTATION_Y];
				float _z = (routes[Method][tmpIndexNext][INDEX_ROTATION_Z] - routes[Method][tmpIndex][INDEX_ROTATION_Z])
						* percentage + routes[Method][tmpIndex][INDEX_ROTATION_Z];
				DLauncherAPP.lmPlanes.get(k).rotation().setAll(_x, _y, _z);
			} else {
				DLauncherAPP.lmPlanes.get(k).isDrawChildren(false);
			}
		}
	}

	public final static void removeTouch(FLIP_TYPE $type, Handler $handler) {
		mPageFlipHandler = $handler;
		$handler.removeCallbacks(cylinderTouch);
		$handler.post(cylinderRemove);
	}

	public final static void setTouch(FLIP_TYPE $type, Handler $handler) {
		mPageFlipHandler = $handler;
		$handler.removeCallbacks(cylinderRemove);
		$handler.post(cylinderTouch);
	}

	public static final Runnable cylinderRemove = new Runnable() {
		@Override
		public void run() {
			if (cylinderMetric > 0) {
				cylinderMetric -= 0.05f;
				mPageFlipHandler.postDelayed(cylinderRemove, 15);
			} else {
				cylinderMetric = 0;
			}
		}
	};

	private static Handler mPageFlipHandler;

	public static final Runnable cylinderTouch = new Runnable() {
		@Override
		public void run() {
			if (cylinderMetric < 1) {
				cylinderMetric += 0.05f;
				mPageFlipHandler.postDelayed(cylinderTouch, 15);
			} else {
				cylinderMetric = 1;
			}
		}
	};

	public static void initPageFlip() {

	}

	private final static float getRelativeFloat(float now, float total) {
		return now < 0 ? (now % total + total) % total : (now % total);
	}

	private final static int getRelativeIndex(int now, int total) {
		return now < 0 ? (now % total + total) % total : (now % total);
	}

}
