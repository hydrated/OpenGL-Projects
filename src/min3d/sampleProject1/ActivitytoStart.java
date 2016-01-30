package min3d.sampleProject1;

import min3d.Shared;
import min3d.core.Renderer10;
import min3d.core.Scene;
import min3d.engine.DLauncherAPP;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import dalvik.system.VMRuntime;

public class ActivitytoStart extends Activity {

	private TextView editBox;
	private DLauncherAPP mExampleLauncher ;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mExampleLauncher = new DLauncherAPP(this) ;
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		
		onCreateSetContentView() ;
	}
    
	protected void onCreateSetContentView() {
		//setContentView(_glSurfaceView);
		setContentView(R.layout.custom_layout_example);
		editBox = new TextView(this);
		editBox.setText("" + Shared.renderer().fps());
		editBox.setTextColor(Color.WHITE);
		addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout ll = (LinearLayout) findViewById(R.id.scene1Holder);
        ll.addView(mExampleLauncher.getGLSurfaceView());
        
        Button b;
        b = (Button) this.findViewById(R.id.layoutOkay);
        //b.setOnClickListener(this);
        b = (Button) this.findViewById(R.id.layoutCancel);
        //b.setOnClickListener(this);
	}
}
