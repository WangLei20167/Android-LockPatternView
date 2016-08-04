package com.anbillon.widget.lock.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.anbillon.widget.lock.LockPatternView;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener, LockPatternView.OnPatternCompleteListener {
  private LockPatternView lockPatternView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.btn_clear).setOnClickListener(this);
    lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
    lockPatternView.setOnPatternCompleteListener(this);
  }

  @Override public void onClick(View view) {
    lockPatternView.clearPattern();
  }

  @Override public void onPatternComplete(String password) {
    Log.d("TAG", "password: " + password);
  }
}
