package com.example.swipelayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanrunqi on 2016/6/26.
 */
public class SwipeLayout extends LinearLayout {
    /**
     * 滑动的工具类
     */
    private Scroller scroller;
    /**
     * 认为是滑动的最短距离
     */
    private int mTouchSlop;
    /**
     * 菜单的宽度
     */
    private int rightViewWidth;
    /**
     * 侧边栏收缩状态
     */
    public static final int EXPAND = 0;
    public static final int SHRINK = 1;
    /**
     * 是否发生横向移动
     */
    Boolean isHorizontalMove;
    /**
     * 用于记录位置
     */
    float startX;
    float startY;
    float curX;
    float curY;
    float lastX;


    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(HORIZONTAL);
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 获取菜单view的宽度，用于滑动位置判断
         */
        View rightView = this.getChildAt(1);
        rightViewWidth = rightView.getMeasuredWidth();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                /**
                 * 不允许父view对触摸事件的拦截
                 */
                disallowParentsInterceptTouchEvent(getParent());
                startX = ev.getX();
                startY = ev.getY();
                isHorizontalMove =false;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isHorizontalMove){
                curX = ev.getX();
                curY = ev.getY();
                float dx = curX - startX;
                float dy = curY - startY;
                    /**
                     * 认为发生了滑动
                     */
                    if(dx*dx+dy*dy > mTouchSlop*mTouchSlop){
                        /**
                         * 垂直滑动
                         */
                        if (Math.abs(dy) > Math.abs(dx)){
                            /**
                             * 允许父view对触摸事件拦截,让其他view去处理事件
                             */
                            allowParentsInterceptTouchEvent(getParent());
                            /**
                             * 垂直滚动复原所有item
                             */
                            shrinkAllView();
                        }else{
                            /**
                             * 水平滑动，拦截来自己处理
                             */
                            isHorizontalMove = true;
                            /**
                             * 为了在onTouchEvent的Move事件中第一次模拟滑动距离不要太大，
                             * 记录上一次发生move的位置
                             */
                            lastX = curX;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isHorizontalMove){
            /**
             * 发生水平滑动，把事件中断到本层onTouchEvent中处理
             */
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if(isHorizontalMove){
                    curX = ev.getX();
                    float dX = curX-lastX;
                    /**
                     * 不断更新lastX的位置，用于模拟滑动
                     */
                    lastX = curX;
                    /**
                     * 滑动的距离与实际相反，因为滚动的时候移动的是内容，不是view
                     */
                    int disX = getScrollX() + (int)(-dX);
                    /**
                     * 手指向右移动
                     */
                    if(disX<0){
                        /**
                         * 如果菜单收缩，防止越界（越界后ACTION_UP又会滚动回来，但还是不越界的好）
                         * 如果菜单展开，，我们希望迅速关闭菜单，不需要模拟滚动
                         */
                        scrollTo(0, 0);
                    }
                    /**
                     * 手指向左移动，如果累加的移动距离已经大于menu的宽度，就让menu显示出来。
                     * 如果移动距离还不到，就模拟滚动
                     */
                    else if(disX>rightViewWidth){
                        scrollTo(rightViewWidth,0);
                    }
                    else{
                        scrollTo(disX, 0);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                float endX = ev.getX();
                float dis =endX -startX;
                /**
                 * 手指向左滑动，模拟展开
                 */
                if(dis<0){
                    SimulateScroll(EXPAND);
                }
                /**
                 * 手指向右滑动，模拟关闭
                 */
                else{
                    SimulateScroll(SHRINK);
                }
            default:
                break;
        }

        return true;
    }

    /**
     * move事件里模拟滑动完成后，判断展开状态
     * 再模拟滚动到目标位置
     */
    public void SimulateScroll(int type){
        int dx =0;
        switch (type){
            case EXPAND:
                dx = rightViewWidth-getScrollX();
                break;
            case SHRINK:
                dx = 0-getScrollX();
                break;
            default:
                break;
        }
        /**
         * Start scrolling by providing a starting point, the distance to travel,
         * and the duration of the scroll.
         *
         * @param startX Starting horizontal scroll offset in pixels. Positive
         *        numbers will scroll the content to the left.
         * @param startY Starting vertical scroll offset in pixels. Positive numbers
         *        will scroll the content up.
         * @param dx Horizontal distance to travel. Positive numbers will scroll the
         *        content to the left.
         * @param dy Vertical distance to travel. Positive numbers will scroll the
         *        content up.
         * @param duration Duration of the scroll in milliseconds.
         */
        scroller.startScroll(getScrollX(),0,dx,0,Math.abs(dx)/2);
        invalidate();
    }

    @Override
    public void computeScroll() {
        /**
         * Call this when you want to know the new location.  If it returns true,
         * the animation is not yet finished.
         *
         * 返回true代表正在模拟数据，false 已经停止模拟数据
         */
        if (scroller.computeScrollOffset()) {
            /**
             * 更新X轴的偏移量
             */
            scrollTo(scroller.getCurrX(), 0);
            /**
             * 递归调用computeScroll()方法，直到模拟滚动完成
             */
            invalidate();
        }
    }

    /**
     * 用于上下滑动和删除item时的，状态改变
     */
    static List<SwipeLayout> swipelayouts = new ArrayList<SwipeLayout>();
        public  static void addSwipeView(SwipeLayout v){
            if(null==v){
                return;
            }
            swipelayouts.add(v);
        }
        public static void removeSwipeView(SwipeLayout v){
            if(null==v){
                return;
            }
            v.SimulateScroll(SwipeLayout.SHRINK);
        }
        private void shrinkAllView(){
            for(SwipeLayout s :swipelayouts){
                if(null==s){
                    swipelayouts.remove(s);
                    continue;
                }else {
                    s.SimulateScroll(SwipeLayout.SHRINK);
                }

            }
        }

    /**
     * 因为不知道是父view那一层会拦截触摸事件，所以递归向上设置标志位
     * 直到顶层view，就直接返回
     */
    private void disallowParentsInterceptTouchEvent(ViewParent parent) {
        if (null == parent) {
            return;
        }
        parent.requestDisallowInterceptTouchEvent(true);
        disallowParentsInterceptTouchEvent(parent.getParent());
    }
    private void allowParentsInterceptTouchEvent(ViewParent parent) {
        if (null == parent) {
            return;
        }
        parent.requestDisallowInterceptTouchEvent(false);
        allowParentsInterceptTouchEvent(parent.getParent());
    }
}
