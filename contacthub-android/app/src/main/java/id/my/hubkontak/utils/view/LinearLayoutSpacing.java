package id.my.hubkontak.utils.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LinearLayoutSpacing extends RecyclerView.ItemDecoration {
    private int itemOffset;

    public LinearLayoutSpacing(int itemOffset) {
        itemOffset = itemOffset;
    }

    public LinearLayoutSpacing(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
        this.itemOffset = context.getResources().getDimensionPixelSize(itemOffsetId);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = itemOffset;
        outRect.right = itemOffset;
    }
}