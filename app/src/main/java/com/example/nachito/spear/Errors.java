package com.example.nachito.spear;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;


public class Errors extends AppCompatActivity {

    TextView errors;
    Button ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.errors);
        errors = findViewById(R.id.errorsList);
        ref = findViewById(R.id.refresh);
        fillError();

        ref.setOnClickListener(v -> {
            if (errors != null) {
                errors.setText("");
            }
            fillError();

        });

    }

    public void fillError() {

        System.out.println("22222222222222");
        for (Map.Entry<String, List<String>> entry : MainActivity.allErrorsList.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String aux = errors.getText() + entry.toString().replace("=", "\n") + '\n';
                errors.setText(aux);
                System.out.println(MainActivity.allErrorsList.entrySet());


            }
        }
        if (errors.getText().toString().isEmpty()) {
            errors.setText(R.string.no_error);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
