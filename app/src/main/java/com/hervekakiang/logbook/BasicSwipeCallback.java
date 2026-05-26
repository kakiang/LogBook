package com.hervekakiang.logbook;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class BasicSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface SwipeListener {
        void onSwipeLeft(RecyclerView.ViewHolder viewHolder, int position);

        void onSwipeRight(RecyclerView.ViewHolder viewHolder, int position);
    }

    private final SwipeListener listener;

    public BasicSwipeCallback(SwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAbsoluteAdapterPosition();
        if (listener == null) return;

        if (direction == ItemTouchHelper.LEFT) {
            listener.onSwipeLeft(viewHolder, position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            listener.onSwipeRight(viewHolder, position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        View itemView = viewHolder.itemView;
        Paint bgPaint = new Paint();
        RectF background;
        Drawable icon;
        int iconMargin, iconTop, iconBottom, iconLeft, iconRight;

        if (dX > 0) {// right, edit
            bgPaint.setColor(Color.parseColor("#2E7D32"));
            background = new RectF(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + dX,
                    itemView.getBottom()
            );
            c.drawRoundRect(background, 16f, 16f, bgPaint);

            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_edit_24);

            if (icon == null) return;
            iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            iconTop = itemView.getTop() + iconMargin;
            iconBottom = itemView.getBottom() - iconMargin;
            iconLeft = itemView.getLeft() + iconMargin;
            iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();

            if (dX > iconMargin) {
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.setTint(Color.WHITE);
                icon.draw(c);
            }

        } else if (dX < 0) {
            bgPaint.setColor(Color.parseColor("#B00020"));
            background = new RectF(
                    itemView.getRight() + dX,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
            c.drawRoundRect(background, 16f, 16f, bgPaint);
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete_outline);

            if (icon == null) return;
            iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            iconTop = itemView.getTop() + iconMargin;
            iconBottom = itemView.getBottom() - iconMargin;
            iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            iconRight = itemView.getRight() - iconMargin;

            if (itemView.getRight() + dX < iconLeft) {
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.setTint(Color.WHITE);
                icon.draw(c);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setTranslationX(0f);
    }
}
