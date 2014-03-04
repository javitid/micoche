package com.latarce.location.dondeestamicoche;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class Position extends Activity{
	
	private TextView txtLocation;
	private ClipboardManager clipboard;
	private String position;
	private String car_position;
	private String distance;
	private String textToShow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.position);
		
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		
		txtLocation = (TextView) this.findViewById(R.id.position_content);
		
		position = getIntent().getStringExtra("position");
		car_position = getIntent().getStringExtra("car_position");
		distance = getIntent().getStringExtra("distance");
		
		//textToShow = "Distancia al coche:\n" + distance + "\nEl coche está en:\n" + car_position + "\nTú estás en:\n" + position;
		textToShow = getResources().getString(R.string.position_text);
		txtLocation.setText(String.format(textToShow, distance, car_position, position));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_UP){
			if ((event.getEventTime() - event.getDownTime()) > 1500){
				clipboard.setText(textToShow);
				Toast.makeText(getBaseContext(), getResources().getString(R.string.position_copy), Toast.LENGTH_LONG).show();
			}
		}
		return super.onTouchEvent(event);
	}
}
