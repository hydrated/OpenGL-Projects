/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package min3d.sampleProject1;

import min3d.vos.PageFlip;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class SpinnerEffects extends Activity {

	private int chosen = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spinner_1);

		Spinner s1 = (Spinner) findViewById(R.id.spinner1);

		String[] tmpStringArray = enumNameToStringArray(PageFlip.FLIP_TYPE.values());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				tmpStringArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s1.setAdapter(adapter);
		s1.setSelection(DLauncherAPP.Flip_Method.ordinal());
		s1.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				chosen = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("Effect", chosen);
				Intent intent = new Intent();
				intent.putExtras(bundle); 
				setResult(Activity.RESULT_OK, intent); 
				finish();
			}
		});

	}

	public static <T extends Enum<T>> String[] enumNameToStringArray(T[] values) {
		int i = 0;
		String[] result = new String[values.length];
		for (T value : values) {
			result[i++] = value.name();
		}
		return result;
	}

}