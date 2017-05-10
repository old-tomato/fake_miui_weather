package com.example.fackmiuiweather.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 52426 on 2017/5/10.
 */

public class TimeView extends View {

    // 总共12种颜色，对应12个小时
    private int[] colors = new int[]{Color.RED , Color.BLACK , Color.BLUE , Color.GRAY , Color.GREEN , Color.YELLOW,
                Color.RED , Color.BLACK , Color.BLUE , Color.GRAY , Color.GREEN , Color.YELLOW};

    /**
     * 总共的块数
     */
    private int pieceCount = 12;

    public TimeView(Context context) {
        this(context , null);
    }

    public TimeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 自动测量
        // TODO 需要对当前的时间进行判断，只显示之后的内容
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 分成12段标识颜色
        int measuredWidth = getMeasuredWidth();
        int piece = measuredWidth / pieceCount;
        int startX = 0;
        for (int x = 0 ; x < pieceCount ; x ++){
            RectF rectF = new RectF(startX , 0 , startX + piece , getMeasuredHeight());
            Paint paint = new Paint();
            paint.setColor(colors[x]);
            paint.setAntiAlias(true);
            canvas.drawRect(rectF , paint);
            startX = startX + piece;
        }
    }
}
