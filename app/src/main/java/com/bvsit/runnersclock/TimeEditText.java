package com.bvsit.runnersclock;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

public class TimeEditText extends AppCompatEditText {

    public final String TAG="TimeEditText";
    private int mMaxValue=59;  //default minutes or seconds

    public TimeEditText(Context context) {
        super(context);
        init();
    }

    public TimeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setMaxValue(int val){
        mMaxValue=val;
    }

    public void init(){
        this.setCursorVisible(false);
        this.setGravity(Gravity.RIGHT);
        this.setTextDirection(View.TEXT_DIRECTION_RTL);
        this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        this.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.i(TAG,"beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.i(TAG,"onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.i(TAG,"afterTextChanged");
                if (s.length()==3) s.delete(0,2);
                if (Integer.parseInt(s.toString())>mMaxValue) s.replace(0,s.length(),Integer.toString(mMaxValue));
            }
        });
    }
}
