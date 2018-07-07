package com.example.control3.view;
import java.io.File;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.control3.R;

public class LEDView extends LinearLayout {

    private static final String FONT_DIGITAL_7 = "fonts" + File.separator
            + "Digital-7Mono.TTF";
    private TextView ledNumber;
    private TextView ledBg;

    public LEDView(Context context) {
        this(context, null);
    }

    public LEDView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LEDView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        //ledBg.setTextColor(0x00000000);
    }

    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.led_view, this);
        ledNumber = (TextView) view.findViewById(R.id.ledview_number);
        ledBg = (TextView) view.findViewById(R.id.ledview_bg);

        AssetManager assets = context.getAssets();
        final Typeface font = Typeface.createFromAsset(assets, FONT_DIGITAL_7);
        ledNumber.setTypeface(font);// 设置字体样式
        ledBg.setTypeface(font);// 设置字体样式
    }

    /**
     * 显示电子数字
     * @param size 字体大小
     * @param bg 背景数字显示样式，即背景数字
     * @param number 需要显示的数字样式
     */
    public void setLedView(int size, String bg, String number) {
        ledBg.setTextSize(size);
        ledNumber.setTextSize(size);
        ledBg.setText(bg);
        ledNumber.setText(number);
    }

    public void setNumberColor(int color)
    {
        ledNumber.setTextColor(color);
    }

}