package min3d.vos;

import min3d.interfaces.IDirtyManaged;

/**
 * Encapsulates camera-related properties, including view frustrum.
 */
public class CameraVo implements IDirtyManaged
{
	public Number3d position = new Number3d(0,0, 5); // ... note, not 'managed'
	public Number3d target = new Number3d(0,0,0);
	public Number3d upAxis = new Number3d(0,1,0);
	private boolean positionIsDirty = true ;
	
	public FrustumManaged frustum = new FrustumManaged(null);

	public CameraVo()
	{
		positionIsDirty = true ;
	}
	
	@Override
	public boolean isDirty() {
		return positionIsDirty ;
	}

	@Override
	public void setDirtyFlag() {
		positionIsDirty = true ;
		
	}

	@Override
	public void clearDirtyFlag() {
		positionIsDirty = false ;
		
	}
}
