package com.anbillon.widget.lock.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.anbillon.widget.lock.LockPatternView;

public class MainActivity extends AppCompatActivity
	implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
	LockPatternView.OnPatternCompleteListener {
	private LockPatternView lockPatternView;
	private Button btnClear;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnClear = (Button) findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(this);
		SwitchCompat autoClearSwitch = (SwitchCompat) findViewById(R.id.auto_clear_switch);
		autoClearSwitch.setOnCheckedChangeListener(this);
		lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
		lockPatternView.setOnPatternCompleteListener(this);
	}

	@Override public void onClick(View view) {
		lockPatternView.clearPattern();
	}

	@Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		if (b) {
			Toast.makeText(this, getString(R.string.auto_clear), Toast.LENGTH_SHORT).show();
		}
		lockPatternView.setAutoClear(b);
		btnClear.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
	}

	@Override public void onPatternComplete(String password) {
		Log.d("TAG", "password: " + password);
	}
}
