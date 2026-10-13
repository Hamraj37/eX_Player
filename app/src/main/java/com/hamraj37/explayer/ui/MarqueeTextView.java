package com.hamraj37.explayer.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class MarqueeTextView extends AppCompatTextView {

    private String originalText = "";
    private String displaySegment = "";
    private float segmentWidth = 0f;
    private float currentOffset = 0f;
    private ValueAnimator animator;
    private static final String SEPARATOR = "   •   ";

    public MarqueeTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSingleLine(true);
        setMaxLines(1);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String newText = text != null ? text.toString() : "";
        if (originalText != null && originalText.equals(newText)) {
            return;
        }

        originalText = newText;
        displaySegment = originalText + SEPARATOR;

        super.setText(originalText, type);

        post(this::updateMarqueeAnimation);
    }

    public String getOriginalText() {
        return originalText;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(this::updateMarqueeAnimation);
    }

    private void updateMarqueeAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        Paint paint = getPaint();
        if (paint == null || TextUtils.isEmpty(originalText)) {
            currentOffset = 0f;
            invalidate();
            return;
        }

        float textWidth = paint.measureText(originalText);
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        if (viewWidth <= 0 || textWidth <= viewWidth) {
            currentOffset = 0f;
            invalidate();
            return;
        }

        segmentWidth = paint.measureText(displaySegment);
        if (segmentWidth <= 0f) {
            currentOffset = 0f;
            invalidate();
            return;
        }

        float speedPxPerSec = 50f * getResources().getDisplayMetrics().density;
        long durationMs = (long) ((segmentWidth / speedPxPerSec) * 1000f);
        if (durationMs <= 0) durationMs = 3000;

        animator = ValueAnimator.ofFloat(0f, segmentWidth);
        animator.setDuration(durationMs);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(animation -> {
            if (isAttachedToWindow()) {
                currentOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();
        if (paint == null || TextUtils.isEmpty(displaySegment) || animator == null || !animator.isRunning() || segmentWidth <= 0f) {
            super.onDraw(canvas);
            return;
        }

        paint.setColor(getCurrentTextColor());
        float y = getBaseline();
        if (y <= 0) {
            y = (getHeight() + getTextSize()) / 2f;
        }

        float startX = getPaddingLeft() - currentOffset;
        int viewWidth = getWidth();

        int maxIterations = 20;
        int iteration = 0;

        while (startX < viewWidth && iteration < maxIterations) {
            canvas.drawText(displaySegment, startX, y, paint);
            startX += segmentWidth;
            iteration++;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::updateMarqueeAnimation);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }
}
