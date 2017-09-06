package com.github.andreilisun.circularlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.andreilisun.circular_layout.CircularLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final CircularLayout circularLayout = findViewById(R.id.circular_layout);
        int expectedViewsQuantity = 12;
        circularLayout.setCapacity(expectedViewsQuantity);
        for (int i = 0; i < expectedViewsQuantity; i++) {
            TextView textView = (TextView)
                    LayoutInflater.from(this).inflate(R.layout.number_text_view, null);
            textView.setText(String.valueOf(i));
            circularLayout.addView(textView);
        }
    }
}
