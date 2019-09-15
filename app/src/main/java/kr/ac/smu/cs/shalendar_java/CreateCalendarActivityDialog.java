package kr.ac.smu.cs.shalendar_java;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class CreateCalendarActivityDialog extends Dialog {


    private Button gotomain;
    CreateCalendarActivity Bactivity = (CreateCalendarActivity) CreateCalendarActivity.CreateCalendarclearActivity;


    public CreateCalendarActivityDialog(Context context, View.OnClickListener singleListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //뒷배경 어둡게 해주기
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);


        setContentView(R.layout.activity_register_plan_dialog);

        gotomain = findViewById(R.id.CreateCalendarComplete);

        gotomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(intent2);
                dismiss();
                Bactivity.finish();
            }
        });
    }
}