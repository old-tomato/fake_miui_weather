package com.example.fackmiuiweather.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.fackmiuiweather.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 52426 on 2017/5/10.
 */

public class WeatherView extends FrameLayout {

    private TimeView timeView;
    private ViewDragHelper helper;
    private int screenWidth;
    private int allowWidth;
    private List<IconInfo> imageList;

    public WeatherView(@NonNull Context context) {
        this(context, null);
    }

    public WeatherView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获得当前的屏幕的宽度
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();

        // 获得拖动帮助类
        helper = ViewDragHelper.create(this , new MyCallback());

        setClickable(true);
        setFocusable(true);

        setWillNotDraw(false);

        initChildView();
    }

    private void initChildView() {
        // 首先放置当前的背景内容
        timeView = new TimeView(getContext());

        // 设定现在屏幕的宽度是整个时间轴的三分之一
        LayoutParams lp = new LayoutParams(screenWidth * 3 , ViewGroup.LayoutParams.MATCH_PARENT);
        addView(timeView , lp);

        imageList = new ArrayList();

        for (int x = 0 ; x < 4 ; x ++){
            ImageView ivIcon = new ImageView(getContext());
            ivIcon.setBackgroundResource(R.mipmap.ic_launcher);
            ivIcon.setClickable(true);
            ivIcon.setFocusable(true);
            addView(ivIcon);

            if(x == 0){
                IconInfo iconInfo = new IconInfo(ivIcon , 0 , 3);
                imageList.add(iconInfo);
            }else if(x == 1){
                IconInfo iconInfo = new IconInfo(ivIcon , 3 , 5);
                imageList.add(iconInfo);
            }else if(x == 2){
                IconInfo iconInfo = new IconInfo(ivIcon , 5 , 9);
                imageList.add(iconInfo);
            }else if(x == 3){
                IconInfo iconInfo = new IconInfo(ivIcon , 9 , 11);
                imageList.add(iconInfo);
            }
        }

        // 暂定该图标的移动位置是前三个区块
    }

    private class MyCallback extends ViewDragHelper.Callback{

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int edgeRight = -1 * (timeView.getMeasuredWidth()) + screenWidth;
            if(left > 0){
                return 0;
            }else if(left < edgeRight){
                return edgeRight;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            // 当前模拟的内容是移动前三个位置
            ivMove(left , top , dx , dy);
            invalidate();
        }

        private void ivMove(int left, int top, int dx, int dy) {
            // 获得当前的位置
            for (IconInfo iconInfo : imageList){
                computeIconEdge(iconInfo);
                computeIconPosition(iconInfo);
            }
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // TODO 预计还需要一些惯性移动
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int ivIconHeight = heightSize / 2;
        // 设定图片控件的高度
        int ivSpec = MeasureSpec.makeMeasureSpec(ivIconHeight, MeasureSpec.EXACTLY);
        for (IconInfo iconInfo : imageList){
            iconInfo.getIvIcon().measure(ivSpec , ivSpec);
        }

        // 测量背景图片的宽度
        int timeWidth = MeasureSpec.makeMeasureSpec(timeView.getLayoutParams().width , MeasureSpec.EXACTLY);
        int timeHeight = MeasureSpec.makeMeasureSpec(timeView.getLayoutParams().height , MeasureSpec.EXACTLY);
        timeView.measure(timeWidth , timeHeight);

        // 自动测量
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec) , heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // 获得当前的允许滑动的位置
        // 当前的能够滑动的是前三个区域
        allowWidth = (timeView.getMeasuredWidth() / 12 * 3);

        // 放置背景内容
        timeView.layout(0 , 0 , timeView.getMeasuredWidth() , getMeasuredHeight());

        // 图片的高度是当前的控件高度的二分之一
        // 第一个图标所在位置必须是在他所在区间中的中间
        for (IconInfo iconInfo : imageList){
            computeIconEdge(iconInfo);
            computeIconPosition(iconInfo);
        }
    }

    /**
     * 通过当前的时间轴的偏移量计算图标显示的范围
     * @param iconInfo
     */
    private void computeIconEdge(IconInfo iconInfo){
        int timeX = (int) timeView.getX();
        int startTime = iconInfo.getStartTime();
        int blockCount = iconInfo.getBlockCount();
        int timeWidth = timeView.getMeasuredWidth();
        int pieceWidth = timeWidth / 12;

        iconInfo.setAllowEdgeLeft(pieceWidth * startTime + timeX);
        iconInfo.setAllowEdgeRight(iconInfo.getAllowEdgeLeft() + pieceWidth * blockCount);
    }

    // 计算同时布局当前的图标位置
    private void computeIconPosition(IconInfo iconInfo){
        // 规则：
        // 如果块起点的位置和块终点的位置都在屏幕中，ICON就在中间显示
        // 如果块起点的位置不在，终点的位置在，ICON就向右进行偏移
        // 反之，向左偏移
        // 如果均不在屏幕显示范围中（不在的方法相同），不做任何动作，普通滚动
        // 如果是由于太长导致的边界都不在屏幕显示范围中，就在屏幕中间显示

        // 图标所在高度是统一的，不需要更多的计算
        int ivTop = (getMeasuredHeight() - iconInfo.getIvIcon().getMeasuredHeight())/2;
        // 区块的宽度
        int blockWidth = iconInfo.getAllowEdgeRight() - iconInfo.getAllowEdgeLeft();

        if(iconInfo.getAllowEdgeLeft() >= 0 && iconInfo.getAllowEdgeRight() <= screenWidth){
            // 布局在所在区块的中间
            int startPositionX = iconInfo.getAllowEdgeLeft() + (blockWidth - iconInfo.getIvIcon().getMeasuredWidth()) / 2;

            iconInfo.getIvIcon().layout(startPositionX ,
                    ivTop ,
                    startPositionX + iconInfo.getIvIcon().getMeasuredWidth() ,
                    ivTop + iconInfo.getIvIcon().getMeasuredHeight());
        }else if(iconInfo.getAllowEdgeLeft() < 0 && iconInfo.getAllowEdgeRight() <= screenWidth && iconInfo.getAllowEdgeRight() >= 0){
            // 左边消失，右边依旧存在
            // 获得左边被吞噬了多少，在通过和总的长度进行对比，当前的吞噬的比例是多少，这就是当前图片控件的在整个区块中的偏移比例
            double offsetScale = (double)Math.abs(iconInfo.getAllowEdgeLeft()) / (double)(iconInfo.getAllowEdgeRight() - iconInfo.getAllowEdgeLeft());

            int originStartPositionX = iconInfo.getAllowEdgeLeft() + (blockWidth - iconInfo.getIvIcon().getMeasuredWidth()) / 2;
            int currentStartPositionX = (int)(originStartPositionX + (blockWidth * offsetScale) / 2);

            // 当前的图标位置如果X的起始位置小于0，则紧贴在右侧边线上
            if(currentStartPositionX >= 0){
                iconInfo.getIvIcon().layout(currentStartPositionX ,
                        ivTop ,
                        currentStartPositionX + iconInfo.getIvIcon().getMeasuredWidth() ,
                        ivTop + iconInfo.getIvIcon().getMeasuredHeight());
            }else{
                iconInfo.getIvIcon().layout(iconInfo.getAllowEdgeRight() - iconInfo.getIvIcon().getMeasuredWidth() ,
                        ivTop,
                        iconInfo.getAllowEdgeRight(),
                        ivTop + iconInfo.getIvIcon().getMeasuredHeight());
            }
        }else if(iconInfo.getAllowEdgeLeft() >= 0 && iconInfo.getAllowEdgeRight() > screenWidth && iconInfo.getAllowEdgeLeft() < screenWidth){
            // 右侧消失，左边存在
            // 基本原理和上个IF中内容相同
            double dismissWidth = iconInfo.getAllowEdgeRight() - screenWidth;
            double offsetScale = dismissWidth / (double)(iconInfo.getAllowEdgeRight() - iconInfo.getAllowEdgeLeft());

            // 开始向右进行偏移
            int originStartPositionX = iconInfo.getAllowEdgeLeft() + (blockWidth - iconInfo.getIvIcon().getMeasuredWidth()) / 2;
            int currentStartPositionX = (int)(originStartPositionX - (blockWidth * offsetScale) / 2);
            int currentEndPositionX = currentStartPositionX + iconInfo.getIvIcon().getMeasuredWidth();
            if(currentEndPositionX < screenWidth ){
                iconInfo.getIvIcon().layout(currentStartPositionX ,
                        ivTop ,
                        currentStartPositionX + iconInfo.getIvIcon().getMeasuredWidth() ,
                        ivTop + iconInfo.getIvIcon().getMeasuredHeight());
            }else{
                // 紧贴着左边进行显示
                iconInfo.getIvIcon().layout(iconInfo.getAllowEdgeLeft() ,
                        ivTop,
                        iconInfo.getAllowEdgeLeft() + iconInfo.getIvIcon().getMeasuredWidth(),
                        ivTop + iconInfo.getIvIcon().getMeasuredHeight());
            }

        }

    }

    // 拦截当前的触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return helper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        helper.processTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class IconInfo{
        private ImageView ivIcon;
        // 开始的时间 0~11 之间
        private int startTime;
        // 结束的时间 1~12 之间
        private int endTime;
        // 规定移动的左侧边界
        private int allowEdgeLeft;
        // 规定移动的右侧边界
        private int allowEdgeRight;
        // 占用的块数
        private int blockCount;

        public IconInfo(ImageView ivIcon , int startTime , int endTime) {
            this.ivIcon = ivIcon;
            this.startTime = startTime;
            this.endTime = endTime;
            blockCount = endTime - startTime;
        }

        public ImageView getIvIcon() {
            return ivIcon;
        }

        public void setIvIcon(ImageView ivIcon) {
            this.ivIcon = ivIcon;
        }

        public int getStartTime() {
            return startTime;
        }

        public void setStartTime(int startTime) {
            this.startTime = startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }

        public int getAllowEdgeLeft() {
            return allowEdgeLeft;
        }

        public void setAllowEdgeLeft(int allowEdgeLeft) {
            this.allowEdgeLeft = allowEdgeLeft;
        }

        public int getAllowEdgeRight() {
            return allowEdgeRight;
        }

        public void setAllowEdgeRight(int allowEdgeRight) {
            this.allowEdgeRight = allowEdgeRight;
        }

        public int getBlockCount() {
            return blockCount;
        }

        public void setBlockCount(int blockCount) {
            this.blockCount = blockCount;
        }
    }

}
