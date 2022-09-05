package ru.oversee.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class CustomSeekBar extends SeekBar {

    protected int minimumValue = 0;
    protected int maximumValue = 0;
    protected OnSeekBarChangeListener listener;

    public CustomSeekBar(Context context){
        super(context);
        setUpInternalListener();
    }

    public CustomSeekBar(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpInternalListener();
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setUpInternalListener();
    }

    public void setMin(int min){
        this.minimumValue = min;
        super.setMax(maximumValue - minimumValue);
    }

    public void setMax(int max){
        this.maximumValue = max;
        super.setMax(maximumValue - minimumValue);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener){
        this.listener = listener;
    }

    private void setUpInternalListener(){
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(listener != null){
                    listener.onProgressChanged(seekBar, minimumValue + i, b);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(listener != null)
                    listener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(listener != null)
                    listener.onStopTrackingTouch(seekBar);
            }
        });
    }
}
