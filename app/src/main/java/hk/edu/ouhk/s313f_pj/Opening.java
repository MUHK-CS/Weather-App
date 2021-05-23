package hk.edu.ouhk.s313f_pj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Opening extends AppCompatActivity {

    ConstraintLayout open_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        open_layout = findViewById(R.id.opening_layout);
        open_layout.setAnimation(animation);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Opening.this, MainActivity.class);
                startActivity(intent);

            }
        }, 3000);
    }


}
