package com.huawei.ott.gadget.tabbar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.ott.gadget.R;
import com.huawei.ott.gadget.extview.ImageViewExt;
import com.huawei.ott.gadget.extview.RelativeLayoutExt;
import com.huawei.ott.gadget.extview.TextViewExt;
import com.huawei.ott.gadget.extview.utils.Colors;
import com.huawei.ott.gadget.util.FontCustom;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.log.LogScenario;
import com.huawei.ott.sdk.ottutil.android.DensityUtil;

import java.util.List;

/**
 * Horizontal tab menu, with menu name, separate line and bottom red line(selected menu).
 */
public class HorizontalScrollTabBar extends LinearLayout implements View.OnClickListener
{
    private static final String TAG = HorizontalScrollTabBar.class.getSimpleName();

    /**
     * inflater;
     */
    private LayoutInflater inflater;

    /**
     * HorizontalScrollView
     */
    private HorizontalScrollView scrollView;

    /**
     * container for menus.
     */
    private LinearLayout container;
    /**
     * container for menus.
     */
    private RelativeLayoutExt horLayout;

    /**
     * position of current selected menu, start with 0, -1 for not selected.
     */
    private int currentPosition = -1;
    private int todayPosition = -1;

    /**
     * listener of menu click.
     */
    private OnItemClickListener onItemClickListener;

    private View[] items;

    private boolean isFirstLoad = true;

    private ImageViewExt leftImageView;

    private ImageViewExt rightImageView;

    private ImageViewExt leftShadowImageView;

    private ImageViewExt rightShadowImageView;

    private Context context;

    private int color23;
    private int color25;

    private int color41;
    private int color42;

    private int color21;
    private int screenWidth;

    private boolean isVOD = false;
    private boolean isNewDetail = false;

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

    public HorizontalScrollTabBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        color23 = Colors.getInstance().getColor(context.getResources(), R.color.C23);
        color25 = Colors.getInstance().getColor(context.getResources(), R.color.C25);
        color41 = Colors.getInstance().getColor(context.getResources(), R.color.C41);
        color42 = Colors.getInstance().getColor(context.getResources(), R.color.C42);
        color21 = Colors.getInstance().getColor(context.getResources(), R.color.C21);
        inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.horizontalscrolltabbar, this);

        //set default view color transparent
        findViewById(R.id.scroll_indicator).setBackgroundColor(Colors.getInstance().getColor
            (context.getResources(), R.color.C15));
        // get references of views
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollview);
        container = (LinearLayout) findViewById(R.id.items_layout);
        horLayout = (RelativeLayoutExt) findViewById(R.id.hor_relative_layout);
        leftImageView = (ImageViewExt) findViewById(R.id.imageview_left);
        rightImageView = (ImageViewExt) findViewById(R.id.imageview_right);


        leftShadowImageView = (ImageViewExt) findViewById(R.id.imageview_left_shadow);
        rightShadowImageView = (ImageViewExt) findViewById(R.id.imageview_right_shadow);
        setImageViewleftAndRightVisible(GONE);

        leftImageView.setOnClickListener(onLeftImageViewOnClickListener);
        rightImageView.setOnClickListener(onRightImageViewOnClickListener);

        screenWidth = DensityUtil.getScreenWidth(context);
    }

    OnClickListener onLeftImageViewOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            scrollToPosition(0);
        }
    };

    OnClickListener onRightImageViewOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            scrollToPosition(items.length - 1);
        }
    };

    public void setNewTimeData(List<String> weekList, List<String> dayList, boolean isNewDetail,
        int todayPosition)
    {
        this.horLayout.setBackgroundColor(Colors.getInstance().getColor(getResources(), R.color
            .C04));
        this.items = new RelativeLayoutExt[dayList.size()];
        this.isNewDetail = isNewDetail;
        this.todayPosition = todayPosition;
        container.removeAllViews();
        currentPosition = -1;
        isFirstLoad = true;
        for (int i = 0; i < dayList.size(); i++)
        {
            RelativeLayoutExt item = (RelativeLayoutExt) inflater.inflate(R.layout
                .horizontalscrolltabbar_item_new, null);

            // every menu item contains a textView, a bottom line image and a separate line images.
            TextViewExt day = (TextViewExt) item.findViewById(R.id.textView_day_new);
            TextViewExt week = (TextViewExt) item.findViewById(R.id.textView_week_new);

            // set menu name

            day.setText(dayList.get(i));
            week.setText(weekList.get(i));

            // when this method invoked for second time, and current position is not -1, then the
            // current position need to show bottom line.
            dealNewSelectionColor(item, currentPosition == i, i);
            // the first separate line to the left need to be invisible.

            // set tag for every menu.
            item.setTag(i);

            // click listener for inner use.
            item.setOnClickListener(this);

            items[i] = item;

            // add menu item to container view.
            container.addView(item);
            int itemLeftMargin;
            int itemRightMargin = 0;
            if (i != 0)
            {
                itemLeftMargin = getResources().getDimensionPixelOffset(R.dimen
                    .new_week_margin_left);
                if (i == dayList.size() - 1)
                {
                    itemRightMargin = getResources().getDimensionPixelOffset(R.dimen
                        .new_week_first_margin_left);
                }
            }
            else
            {
                itemLeftMargin = getResources().getDimensionPixelOffset(R.dimen
                    .new_week_first_margin_left);
            }
            LayoutParams layoutParams = (LayoutParams) item.getLayoutParams();
            layoutParams.setMargins(itemLeftMargin, 0, itemRightMargin, 0);
            item.setLayoutParams(layoutParams);
        }
    }

    private void dealNewSelectionColor(View item, boolean isSelected, int position)
    {
        View bottomLine = item.findViewById(R.id.bottom_line_new);
        TextViewExt day = (TextViewExt) item.findViewById(R.id.textView_day_new);

        if (isSelected)
        {
            bottomLine.setVisibility(VISIBLE);
            day.setTextColor(color21);
            if (position == todayPosition)
            {
                day.setTextAppearance(context, R.style.T132);
            }
            else
            {
                day.setTextAppearance(context, R.style.T273);
            }
        }
        else
        {
            bottomLine.setVisibility(INVISIBLE);
            day.setTextColor(color25);
            if (position == todayPosition)
            {
                day.setTextAppearance(context, R.style.T13);
            }
            else
            {
                day.setTextAppearance(context, R.style.T271);
            }
        }
    }

    public void setImageViewleftAndRightVisible(int visible)
    {
        leftImageView.setVisibility(visible);
        rightImageView.setVisibility(visible);
        leftShadowImageView.setVisibility(visible);
        rightShadowImageView.setVisibility(visible);
    }

    /**
     * select one menu
     *
     * @param position position, start with 0.
     */
    public void setSelection(final int position)
    {
        DebugLog.debug(TAG, "current position is " + currentPosition, " new position is " +
            position);

        // position not changed, do nothing
        if (currentPosition == position)
        {
            return;
        }

        // Hidden the bottom red line of last selection, skip this action when no selection
        if (-1 != currentPosition)
        {
            if (isVOD)
            {
                ((TextViewExt) items[currentPosition].findViewById(R.id.item_name)).setTextColor
                    (color42);
                ((TextViewExt) items[currentPosition].findViewById(R.id.item_name))
                    .setTextAppearance(context, R.style.T13);
            }
            else if (isNewDetail)
            {
                dealNewSelectionColor(items[currentPosition], false, currentPosition);
            }
            else
            {
                dealSelectionColor(items[currentPosition], false);
            }
        }
        // selected menu need show the bottom red line.
        this.currentPosition = position;
        if (isVOD)
        {
            ((TextViewExt) items[currentPosition].findViewById(R.id.item_name)).setTextColor
                (color41);
            ((TextViewExt) items[currentPosition].findViewById(R.id.item_name)).setTextAppearance
                (context, R.style.T132);
        }
        else if (isNewDetail)
        {
            dealNewSelectionColor(items[currentPosition], true, currentPosition);
        }
        else
        {
            dealSelectionColor(items[currentPosition], true);
        }

        // must use handler
        scrollToPosition(currentPosition);
    }

    private void dealSelectionColor(View item, boolean isSelected)
    {
        View bottomLine = item.findViewById(R.id.bottom_line);
        TextViewExt day = (TextViewExt) item.findViewById(R.id.textView_day);
        TextViewExt week = (TextViewExt) item.findViewById(R.id.textView_week);

        if (isSelected)
        {
            bottomLine.setVisibility(VISIBLE);
            day.setTextColor(color23);
            week.setTextColor(color23);
        }
        else
        {
            bottomLine.setVisibility(GONE);
            day.setTextColor(color25);
            week.setTextColor(color25);
        }
    }

    private void scrollToPosition(int pos)
    {
        Message message = Message.obtain();
        message.what = 1;
        message.arg1 = pos;
        scrollHandler.sendMessageDelayed(message, 50);
    }

    private Handler scrollHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            int pos = msg.arg1;
            DebugLog.info(TAG, "handleMessage(), position:" + pos, "scrollView.getLeft():" +
                scrollView.getLeft());

            int left = items[pos].getLeft();

            int screenHalf = screenWidth / 2 - scrollView.getLeft();

            int itemHalf = items[pos].getMeasuredWidth() / 2;

            final int move = left + itemHalf - screenHalf;

            if (isFirstLoad)
            {
                scrollView.scrollTo(move, 0);
                isFirstLoad = false;
            }
            else
            {
                scrollView.smoothScrollTo(move, 0);
            }
        }
    };

    @Override
    public void onClick(View v)
    {
        DebugLog.info(TAG, LogScenario.BEHAVIOR, "view.getId()=" + v.toString());
        int position = 0;
        try
        {
            position = Integer.parseInt(v.getTag().toString());
        }
        catch (NumberFormatException e)
        {
            DebugLog.error(TAG, e);
        }

        // trigger the listener
        if (null != onItemClickListener)
        {
            onItemClickListener.onItemClick(v, position, this.getId());
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

    public int getCurrentPosition()
    {
        return currentPosition;
    }
}