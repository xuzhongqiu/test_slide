package com.huawei.ott.gadget.tabbar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.ott.gadget.R;
import com.huawei.ott.gadget.extview.LinearLayoutExt;
import com.huawei.ott.gadget.extview.TextViewExt;
import com.huawei.ott.gadget.extview.utils.Colors;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.log.LogScenario;
import com.huawei.ott.sdk.ottutil.android.DensityUtil;
import com.huawei.ott.sdk.ottutil.java.OTTFormat;

import java.util.List;

public class VODHorizontalScrollTabBar extends LinearLayoutExt implements View.OnClickListener
{
    private static final String TAG = VODHorizontalScrollTabBar.class.getSimpleName();

    private Context context;
    private HorizontalScrollView horizontalScrollView;
    private ImageView fIndicator;
    private LinearLayout container;
    private int screenWidth;
    private View[] itemContains;
    private int currentPosition = -1;
    private int widthOfIndicator = 0;
    private int lastPage = -1;
    private int fMarginLeft = 0;
    private int paddingLeft = 0;
    private OnItemClickListener onItemClickListener;

    private int color41;
    private int color47;


    /**
     * OnItemClickListener
     */
    public interface OnItemClickListener
    {
        /**
         * onItemClick
         *
         * @param view     item view was clicked
         * @param position position
         * @param id       view id
         */
        void onItemClick(View view, int position, long id);
    }

    public VODHorizontalScrollTabBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.horizontalscrolltabbar, this);
        container = (LinearLayout) findViewById(R.id.items_layout);
        widthOfIndicator = getResources().getDimensionPixelSize(R.dimen
                .vod_scroll_tab_bar_image_width);
        paddingLeft = getResources().getDimensionPixelSize(R.dimen.vod_scroll_tab_bar_padding);
        color41 = Colors.getInstance().getColor(context.getResources(), R.color.C41);
        color47 = Colors.getInstance().getColor(context.getResources(), R.color.C47);
        // get references of views
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.scrollview);

        container.setPadding(paddingLeft, 0, 0, 0);
        screenWidth = DensityUtil.getScreenWidth(context);
    }


    public void onPageScrolling(int position, float positionOffset, int positionOffsetPixels)
    {
        View categoryBlock = this.itemContains[position];

        int curTabWidth = categoryBlock.getWidth();

        int moveTabDistance = 0;

        if (position < this.itemContains.length - 1 && 0 != positionOffset)
        {
            View nextTab = this.itemContains[position + 1];

            int nextTabWidth = nextTab.getWidth();

            //calculate the moving distance horizontally within every step between two tabs.
            moveTabDistance = (curTabWidth + nextTabWidth) / 2;
        }

        int centerIndicator = (curTabWidth - widthOfIndicator) / 2;

        DebugLog.info(TAG, "position is " + position + " , positionOffset is " + positionOffset);

        float translateX;

        if (0 == curTabWidth)
        {
            translateX = fMarginLeft;
        }
        else
        {
            translateX = centerIndicator + categoryBlock.getLeft() + moveTabDistance *
                    positionOffset;
        }

        fIndicator.setTranslationX(translateX);

        float scaleFactor;

        //Moving to right
        if (positionOffset > 0.5)
        {
            positionOffset = 1 - positionOffset;
        }

        positionOffset = positionOffset * 3;
        scaleFactor = positionOffset > 0 ? 1 + positionOffset : 1;

        fIndicator.setScaleX(scaleFactor);
    }


    public void onPageStopped(int position)
    {
        lastPage = position;
    }

    public void setVODList(List<String> textList)
    {
        this.itemContains = new View[textList.size()];
        container.removeAllViews();

        fIndicator = (ImageView) findViewById(R.id.scroll_indicator);
        currentPosition = -1;

        int firstLeft = 0;
        for (int index = 0; index < textList.size(); index++)
        {
            final int width = getResources().getDimensionPixelSize(R.dimen
                    .vod_scroll_tab_bar_layout_width);
            int parentLayoutHeight = getResources().getDimensionPixelSize(R.dimen.s96_height);
            String text = textList.get(index);
            RelativeLayout parentLayout = new RelativeLayout(context);
            int paintWidth = setTextLayoutParams(text, index, parentLayout);
            if (0 == index)
            {
                firstLeft = paddingLeft + (width + paintWidth - widthOfIndicator) / 2;
            }
            RelativeLayout.LayoutParams parentParams = new RelativeLayout.LayoutParams(width +
                    paintWidth, parentLayoutHeight);
            parentParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            parentLayout.setLayoutParams(parentParams);
            parentLayout.setTag(index);
            parentLayout.setOnClickListener(VODHorizontalScrollTabBar.this);
            container.addView(parentLayout);
            itemContains[index] = parentLayout;
        }

        fMarginLeft = firstLeft;
    }


    private int setTextLayoutParams(String text, int index, RelativeLayout parentLayout)
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout
                .LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        TextViewExt textView = new TextViewExt(context);
        textView.setText(text);
        textView.setSingleLine(true);
        textView.setIncludeFontPadding(false);
        textView.setAllCaps(true);
        if (currentPosition == index)
        {
            textView.setTextColor(color47);
            textView.setTextAppearance(context, R.style.T122);
        }
        else
        {
            textView.setTextColor(color41);
            textView.setTextAppearance(context, R.style.T12);
        }
        parentLayout.addView(textView, 0, params);
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(w, h);
        return textView.getMeasuredWidth();
    }


    @Override
    public void onClick(View v)
    {
        DebugLog.info(TAG, LogScenario.BEHAVIOR, "view.getId()=" + v.toString());
        int position = OTTFormat.convertInt(v.getTag().toString());
        // trigger the listener
        if (null != onItemClickListener)
        {
            onItemClickListener.onItemClick(v, position, this.getId());
        }
    }

    private void scrollToPosition(int index)
    {
        Message message = Message.obtain();
        message.what = 1;
        message.arg1 = index;
        scrollHandler.sendMessageDelayed(message, 50);
    }

    private Handler scrollHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            int index = msg.arg1;
            int scrollViewLeft = horizontalScrollView.getLeft();
            DebugLog.info(TAG, "handleMessage(), position:" + index, "scrollViewLeft:" +
                    scrollViewLeft);

            int left = itemContains[index].getLeft();

            int screenHalf = screenWidth / 2 - scrollViewLeft;

            int itemHalf = itemContains[index].getMeasuredWidth() / 2;

            final int move = left + itemHalf - screenHalf;
            horizontalScrollView.smoothScrollTo(move, 0);
        }
    };


    /**
     * select one menu
     *
     * @param position position, start with 0.
     */
    public void setSelection(final int position)
    {
        DebugLog.debug(TAG, "current position: " + currentPosition + " ,new position: " + position);
        // position not changed, do nothing
        if (currentPosition == position)
        {
            return;
        }
        // Hidden the bottom red line of last selection, skip this action when no selection
        if (-1 != currentPosition)
        {
            refreshRelativeLayout(GONE, color41, R.style.T12, false);
        }
        // selected menu need show the bottom red line.
        this.currentPosition = position;
        refreshRelativeLayout(VISIBLE, color47, R.style.T122, true);
    }


    private void refreshRelativeLayout(int visibility, int color, int resid, boolean isScroll)
    {
        RelativeLayout relativeLayout = (RelativeLayout) itemContains[currentPosition];
        if (null != relativeLayout)
        {
            TextView textView = (TextView) relativeLayout.getChildAt(0);
            textView.setTextColor(color);
            textView.setTextAppearance(context, resid);
            if (isScroll)
            {
                // must use handler
                scrollToPosition(currentPosition);
            }
        }
    }

    /**
     * set itemClickListener
     *
     * @param onItemClickListener Monitor
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this.onItemClickListener = onItemClickListener;
    }
}
