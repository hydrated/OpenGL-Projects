package min3d;

import min3d.core.Renderer;
import min3d.core.Renderer10;
import min3d.core.RendererFrameBuffer;
import min3d.core.TextureManager;
import android.content.Context;

/**
 * Holds static references to TextureManager, Renderer, and the application Context. 
 */
public class Shared 
{
	private static Context _context;
	private static Renderer10 _renderer;
	private static RendererFrameBuffer _rendererframe ;
	private static TextureManager _textureManager;

	
	public static Context context()
	{
		return _context;
	}
	public static void context(Context $c)
	{
		_context = $c;
	}

	public static Renderer10 renderer()
	{
		return _renderer;
	}
	
	public static RendererFrameBuffer rendererFrameBuffer()
	{
		return _rendererframe ;
	}
	
	public static void renderer(Renderer10 r)
	{
		_renderer = r;
	}
	public static void renderer(RendererFrameBuffer r)
	{
		_rendererframe = r;
	}
	/**
	 * You must access the TextureManager instance through this accessor
	 */
	public static TextureManager textureManager()
	{
		return _textureManager;
	}
	public static void textureManager(TextureManager $bm)
	{
		_textureManager = $bm;
	}
}
