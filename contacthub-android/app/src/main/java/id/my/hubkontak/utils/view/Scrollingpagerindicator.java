package id.my.hubkontak.utils.view;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import id.my.hubkontak.R;

public class Scrollingpagerindicator extends View {

    @IntDef({RecyclerView.HORIZONTAL, RecyclerView.VERTICAL})
    public @interface Orientation{}

    private int infiniteDotCount;

    private final int dotMinimumSize;
    private final int dotNormalSize;
    private final int dotSelectedSize;
    private final int spaceBetweenDotCenters;
    private int visibleDotCount;
    private int visibleDotThreshold;
    private int orientation;

    private float visibleFramePosition;
    private float visibleFrameWidth;

    private float firstDotOffset;
    private SparseArray<Float> dotScale;

    private int itemCount;

    private final Paint paint;
    private final ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    @ColorInt
    private int dotColor;

    @ColorInt
    private int selectedDotColor;

    private boolean looped;

    private Runnable attachRunnable;
    private PagerAttacher<?> currentAttacher;

    private boolean dotCountInitialized;

    public Scrollingpagerindicator(Context context) {
        this(context, null);
    }

    public Scrollingpagerindicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.scrollingPagerIndicatorStyle);
    }

    public Scrollingpagerindicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.Scrollingpagerindicator, defStyleAttr, R.style.ScrollingPagerIndicator);
        dotColor = attributes.getColor(R.styleable.Scrollingpagerindicator_spi_dotColor, 0);
        selectedDotColor = attributes.getColor(R.styleable.Scrollingpagerindicator_spi_dotSelectedColor, dotColor);
        dotNormalSize = attributes.getDimensionPixelSize(R.styleable.Scrollingpagerindicator_spi_dotSize, 0);
        dotSelectedSize = attributes.getDimensionPixelSize(R.styleable.Scrollingpagerindicator_spi_dotSelectedSize, 0);
        int dotMinimumSize = attributes.getDimensionPixelSize(R.styleable.Scrollingpagerindicator_spi_dotMinimumSize, -1);
        this.dotMinimumSize = dotMinimumSize <= dotNormalSize ? dotMinimumSize : -1;

        spaceBetweenDotCenters = attributes.getDimensionPixelSize(R.styleable.Scrollingpagerindicator_spi_dotSpacing, 0) + dotNormalSize;
        looped = attributes.getBoolean(R.styleable.Scrollingpagerindicator_spi_looped, false);
        int visibleDotCount = attributes.getInt(R.styleable.Scrollingpagerindicator_spi_visibleDotCount, 0);
        setVisibleDotCount(visibleDotCount);
        visibleDotThreshold = attributes.getInt(R.styleable.Scrollingpagerindicator_spi_visibleDotThreshold, 2);
        orientation = attributes.getInt(R.styleable.Scrollingpagerindicator_spi_orientation, RecyclerView.HORIZONTAL);
        attributes.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);

        if (isInEditMode()) {
            setDotCount(visibleDotCount);
            onPageScrolled(visibleDotCount / 2, 0);
        }
    }

    public void setLooped(boolean looped) {
        this.looped = looped;
        reattach();
        invalidate();
    }

    @ColorInt
    public int getDotColor() {
        return dotColor;
    }

    public void setDotColor(@ColorInt int color) {
        this.dotColor = color;
        invalidate();
    }

    @ColorInt
    public int getSelectedDotColor() {
        return selectedDotColor;
    }

    public void setSelectedDotColor(@ColorInt int color) {
        this.selectedDotColor = color;
        invalidate();
    }

    public int getVisibleDotCount() {
        return visibleDotCount;
    }

    public void setVisibleDotCount(int visibleDotCount) {
        if (visibleDotCount % 2 == 0) {
            throw new IllegalArgumentException("visibleDotCount must be odd");
        }
        this.visibleDotCount = visibleDotCount;
        this.infiniteDotCount = visibleDotCount + 2;

        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    public int getVisibleDotThreshold() {
        return visibleDotThreshold;
    }

    public void setVisibleDotThreshold(int visibleDotThreshold) {
        this.visibleDotThreshold = visibleDotThreshold;
        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    @Orientation
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@Orientation int orientation) {
        this.orientation = orientation;
        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    public void attachToPager(@NonNull ViewPager pager) {
        attachToPager(pager, new ViewPagerAttacher());
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        attachToPager(recyclerView, new RecyclerViewAttacher());
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView, int currentPageOffset) {
        attachToPager(recyclerView, new RecyclerViewAttacher(currentPageOffset));
    }

    public <T> void attachToPager(@NonNull final T pager, @NonNull final PagerAttacher<T> attacher) {
        detachFromPager();
        attacher.attachToPager(this, pager);
        currentAttacher = attacher;

        attachRunnable = new Runnable() {
            @Override
            public void run() {
                itemCount = -1;
                attachToPager(pager, attacher);
            }
        };
    }

    public void detachFromPager() {
        if (currentAttacher != null) {
            currentAttacher.detachFromPager();
            currentAttacher = null;
            attachRunnable = null;
        }
        dotCountInitialized = false;
    }

    public void reattach() {
        if (attachRunnable != null) {
            attachRunnable.run();
            invalidate();
        }
    }

    public void onPageScrolled(int page, float offset) {
        if (offset < 0 || offset > 1) {
            throw new IllegalArgumentException("Offset must be [0, 1]");
        } else if (page < 0 || page != 0 && page >= itemCount) {
            throw new IndexOutOfBoundsException("page must be [0, adapter.getItemCount())");
        }

        if (!looped || itemCount <= visibleDotCount && itemCount > 1) {
            dotScale.clear();

            if (orientation == LinearLayout.HORIZONTAL) {
                scaleDotByOffset(page, offset);

                if (page < itemCount - 1) {
                    scaleDotByOffset(page + 1, 1 - offset);
                } else if (itemCount > 1) {
                    scaleDotByOffset(0, 1 - offset);
                }
            }
            else { // Vertical orientation
                scaleDotByOffset(page - 1, offset);
                scaleDotByOffset(page, 1 - offset);
            }

            invalidate();
        }
        if (orientation == LinearLayout.HORIZONTAL) {
            adjustFramePosition(offset, page);
        } else {
            adjustFramePosition(offset, page - 1);
        }
        invalidate();
    }

    public void setDotCount(int count) {
        initDots(count);
    }

    public void setCurrentPosition(int position) {
        if (position != 0 && (position < 0 || position >= itemCount)) {
            throw new IndexOutOfBoundsException("Position must be [0, adapter.getItemCount()]");
        }
        if (itemCount == 0) {
            return;
        }
        adjustFramePosition(0, position);
        updateScaleInIdleState(position);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Width
        int measuredWidth;
        // Height
        int measuredHeight;

        if (orientation == LinearLayoutManager.HORIZONTAL) {
            // We ignore widthMeasureSpec because width is based on visibleDotCount
            if (isInEditMode()) {
                // Maximum width with all dots visible
                measuredWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            } else {
                measuredWidth = itemCount >= visibleDotCount
                        ? (int) visibleFrameWidth
                        : (itemCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            }
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            // Height
            int desiredHeight = dotSelectedSize;

            switch (heightMode) {
                case MeasureSpec.EXACTLY:
                    measuredHeight = heightSize;
                    break;
                case MeasureSpec.AT_MOST:
                    measuredHeight = Math.min(desiredHeight, heightSize);
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    measuredHeight = desiredHeight;
            }
        } else {
            if (isInEditMode()) {
                measuredHeight = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            } else {
                measuredHeight = itemCount >= visibleDotCount
                        ? (int) visibleFrameWidth
                        : (itemCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            }

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);

            // Width
            int desiredWidth = dotSelectedSize;

            switch (widthMode) {
                case MeasureSpec.EXACTLY:
                    measuredWidth = widthSize;
                    break;
                case MeasureSpec.AT_MOST:
                    measuredWidth = Math.min(desiredWidth, widthSize);
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    measuredWidth = desiredWidth;
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int dotCount = getDotCount();
        if (dotCount < visibleDotThreshold) {
            return;
        }

        // Some empirical coefficients
        float scaleDistance = (spaceBetweenDotCenters + (dotSelectedSize - dotNormalSize) / 2) * 0.7f;
        float smallScaleDistance = dotSelectedSize / 2;
        float centerScaleDistance = 6f / 7f * spaceBetweenDotCenters;

        int firstVisibleDotPos = (int) (visibleFramePosition - firstDotOffset) / spaceBetweenDotCenters;
        int lastVisibleDotPos = firstVisibleDotPos
                + (int) (visibleFramePosition + visibleFrameWidth - getDotOffsetAt(firstVisibleDotPos))
                / spaceBetweenDotCenters;

        // If real dots count is less than we can draw inside visible frame, we move lastVisibleDotPos
        // to the last item
        if (firstVisibleDotPos == 0 && lastVisibleDotPos + 1 > dotCount) {
            lastVisibleDotPos = dotCount - 1;
        }

        for (int i = firstVisibleDotPos; i <= lastVisibleDotPos; i++) {
            float dot = getDotOffsetAt(i);
            if (dot >= visibleFramePosition && dot < visibleFramePosition + visibleFrameWidth) {
                float diameter;
                float scale;

                // Calculate scale according to current page position
                if (looped && itemCount > visibleDotCount) {
                    float frameCenter = visibleFramePosition + visibleFrameWidth / 2;
                    if (dot >= frameCenter - centerScaleDistance
                            && dot <= frameCenter) {
                        scale = (dot - frameCenter + centerScaleDistance) / centerScaleDistance;
                    } else if (dot > frameCenter
                            && dot < frameCenter + centerScaleDistance) {
                        scale = 1 - (dot - frameCenter) / centerScaleDistance;
                    } else {
                        scale = 0;
                    }
                } else {
                    scale = getDotScaleAt(i);
                }
                diameter = dotNormalSize + (dotSelectedSize - dotNormalSize) * scale;

                // Additional scale for dots at corners
                if (itemCount > visibleDotCount) {
                    float currentScaleDistance;
                    if (!looped && (i == 0 || i == dotCount - 1)) {
                        currentScaleDistance = smallScaleDistance;
                    } else {
                        currentScaleDistance = scaleDistance;
                    }

                    int size = getWidth();
                    if (orientation == LinearLayoutManager.VERTICAL) {
                        size = getHeight();
                    }
                    if (dot - visibleFramePosition < currentScaleDistance) {
                        float calculatedDiameter = diameter * (dot - visibleFramePosition) / currentScaleDistance;
                        if (calculatedDiameter <= dotMinimumSize) {
                            diameter = dotMinimumSize;
                        } else if (calculatedDiameter < diameter) {
                            diameter = calculatedDiameter;
                        }
                    } else if (dot - visibleFramePosition > size - currentScaleDistance) {
                        float calculatedDiameter = diameter * (-dot + visibleFramePosition + size) / currentScaleDistance;
                        if (calculatedDiameter <= dotMinimumSize) {
                            diameter = dotMinimumSize;
                        } else if (calculatedDiameter < diameter) {
                            diameter = calculatedDiameter;
                        }
                    }
                }

                paint.setColor(calculateDotColor(scale));
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    canvas.drawCircle(dot - visibleFramePosition,
                            getMeasuredHeight() / 2,
                            diameter / 2,
                            paint);
                } else {
                    canvas.drawCircle(getMeasuredWidth() / 2,
                            dot - visibleFramePosition,
                            diameter / 2,
                            paint);
                }
            }
        }
    }

    @ColorInt
    private int calculateDotColor(float dotScale) {
        return (Integer) colorEvaluator.evaluate(dotScale, dotColor, selectedDotColor);
    }

    private void updateScaleInIdleState(int currentPos) {
        if (!looped || itemCount < visibleDotCount) {
            dotScale.clear();
            dotScale.put(currentPos, 1f);
            invalidate();
        }
    }

    private void initDots(int itemCount) {
        if (this.itemCount == itemCount && dotCountInitialized) {
            return;
        }
        this.itemCount = itemCount;
        dotCountInitialized = true;
        dotScale = new SparseArray<>();

        if (itemCount < visibleDotThreshold) {
            requestLayout();
            invalidate();
            return;
        }

        firstDotOffset = looped && this.itemCount > visibleDotCount ? 0 : dotSelectedSize / 2;
        visibleFrameWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;

        requestLayout();
        invalidate();
    }

    private int getDotCount() {
        if (looped && itemCount > visibleDotCount) {
            return infiniteDotCount;
        } else {
            return itemCount;
        }
    }

    private void adjustFramePosition(float offset, int pos) {
        if (itemCount <= visibleDotCount) {
            // Without scroll
            visibleFramePosition = 0;
        } else if (!looped && itemCount > visibleDotCount) {
            // Not looped with scroll
            float center = getDotOffsetAt(pos) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2;

            // Block frame offset near start and end
            int firstCenteredDotIndex = visibleDotCount / 2;
            float lastCenteredDot = getDotOffsetAt(getDotCount() - 1 - firstCenteredDotIndex);
            if (visibleFramePosition + visibleFrameWidth / 2 < getDotOffsetAt(firstCenteredDotIndex)) {
                visibleFramePosition = getDotOffsetAt(firstCenteredDotIndex) - visibleFrameWidth / 2;
            } else if (visibleFramePosition + visibleFrameWidth / 2 > lastCenteredDot) {
                visibleFramePosition = lastCenteredDot - visibleFrameWidth / 2;
            }
        } else {
            // Looped with scroll
            float center = getDotOffsetAt(infiniteDotCount / 2) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2;
        }
    }

    private void scaleDotByOffset(int position, float offset) {
        if (dotScale == null || getDotCount() == 0) {
            return;
        }
        setDotScaleAt(position, 1 - Math.abs(offset));
    }

    private float getDotOffsetAt(int index) {
        return firstDotOffset + index * spaceBetweenDotCenters;
    }

    private float getDotScaleAt(int index) {
        Float scale = dotScale.get(index);
        if (scale != null) {
            return scale;
        }
        return 0;
    }

    private void setDotScaleAt(int index, float scale) {
        if (scale == 0) {
            dotScale.remove(index);
        } else {
            dotScale.put(index, scale);
        }
    }

    public interface PagerAttacher<T> {

        void attachToPager(@NonNull Scrollingpagerindicator indicator, @NonNull T pager);

        void detachFromPager();
    }
}
