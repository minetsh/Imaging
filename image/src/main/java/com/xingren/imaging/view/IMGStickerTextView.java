package com.xingren.imaging.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xingren.imaging.ImageTextDialog;
import com.xingren.imaging.R;
import com.xingren.imaging.core.IMGText;
import com.xingren.imaging.core.sticker.IMGSticker;
import com.xingren.imaging.core.sticker.IMGStickerAdjustHelper;
import com.xingren.imaging.core.sticker.IMGStickerHelper;
import com.xingren.imaging.core.sticker.IMGStickerMoveHelper;

/**
 * Created by felix on 2017/11/14 下午7:27.
 */

public class IMGStickerTextView extends FrameLayout implements IMGSticker,
        View.OnClickListener, ImageTextDialog.Callback {

    private static final String TAG = "IMGStickerTextView";

    private TextView mTextView;

    private ImageView mDeleteView, mAdjustView;

    private Rect mBorderRect;

    private IMGText mText;

    private ImageTextDialog mDialog;

    private IMGStickerHelper<IMGStickerTextView> mStickerHelper;

    private static final Paint PAINT;

    private static final int PADDING = 26;

    private static final int SIZE_OP = 32;

    private static final int SIZE_OP_HALF = SIZE_OP >> 1;

    static {
        PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
        PAINT.setColor(Color.WHITE);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(2);
    }

    {
        mBorderRect = new Rect(SIZE_OP_HALF, SIZE_OP_HALF, 0, 0);
    }

    public IMGStickerTextView(Context context) {
        this(context, null, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        assemble(context);
        mStickerHelper = new IMGStickerHelper<>(this);
        new IMGStickerAdjustHelper(this, mAdjustView);
        new IMGStickerMoveHelper(this);
    }

    private void assemble(Context context) {
        setBackgroundColor(Color.TRANSPARENT);

        mTextView = new TextView(context);
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        layoutParams.gravity = Gravity.CENTER;
        mTextView.setLayoutParams(layoutParams);
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setOnClickListener(this);
        addView(mTextView);

        mAdjustView = new ImageView(context);
        LayoutParams adjustParams = new LayoutParams(
                SIZE_OP, SIZE_OP
        );
        adjustParams.gravity = Gravity.END | Gravity.BOTTOM;
        mAdjustView.setLayoutParams(adjustParams);
        mAdjustView.setScaleType(ImageView.ScaleType.FIT_XY);
        mAdjustView.setImageResource(R.drawable.ic_adjust);
        addView(mAdjustView);

        mDeleteView = new ImageView(context);
        LayoutParams deleteParams = new LayoutParams(
                SIZE_OP, SIZE_OP
        );
        deleteParams.gravity = Gravity.START | Gravity.TOP;
        mDeleteView.setLayoutParams(deleteParams);
        mDeleteView.setScaleType(ImageView.ScaleType.FIT_XY);
        mDeleteView.setImageResource(R.drawable.ic_delete);
        addView(mDeleteView);

        mDeleteView.setOnClickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShowing()) {
            canvas.save();
            mBorderRect.right = getWidth() - SIZE_OP_HALF;
            mBorderRect.bottom = getHeight() - SIZE_OP_HALF;
            canvas.drawRect(mBorderRect, PAINT);
            canvas.restore();
        }
    }

    public void setText(IMGText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }

    public IMGText getText() {
        return mText;
    }

    @Override
    public boolean show() {
        return mStickerHelper.show();
    }

    @Override
    public boolean remove() {
        return mStickerHelper.remove();
    }

    @Override
    public boolean dismiss() {
        return mStickerHelper.dismiss();
    }

    @Override
    public boolean isShowing() {
        return mStickerHelper.isShowing();
    }

    @Override
    public RectF getFrame() {
        return mStickerHelper.getFrame();
    }

    @Override
    public void onSticker(Canvas canvas) {
        mTextView.draw(canvas);
    }

    @Override
    public void registerCallback(Callback callback) {
        mStickerHelper.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mStickerHelper.unregisterCallback(callback);
    }

    @Override
    public void onClick(View v) {
        if (v == mDeleteView) {
            mStickerHelper.remove();
        } else if (v == mTextView) {
            onTextClick();
        }
    }

    private void onTextClick() {
        ImageTextDialog dialog = getDialog();
        dialog.setText(mText);
        dialog.show();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return isShowing() && super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isShowing() && ev.getAction() == MotionEvent.ACTION_DOWN) {
            show();
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private ImageTextDialog getDialog() {
        if (mDialog == null) {
            mDialog = new ImageTextDialog(getContext(), this);
        }
        return mDialog;
    }

    @Override
    public void onText(IMGText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }
}
