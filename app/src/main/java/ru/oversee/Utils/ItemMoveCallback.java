package ru.oversee.Utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ru.oversee.Home.CameraItemsAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;

    public ItemMoveCallback(ItemTouchHelperContract adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags (@NonNull RecyclerView recyclerView, @NonNull RecyclerView . ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper .UP | ItemTouchHelper .DOWN |
                ItemTouchHelper .LEFT | ItemTouchHelper .RIGHT ;
        int swipeFlags = 0;
        return makeMovementFlags (dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof CameraItemsAdapter.ViewHolder) {
                CameraItemsAdapter.ViewHolder myViewHolder= (CameraItemsAdapter.ViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof CameraItemsAdapter.ViewHolder) {
            CameraItemsAdapter.ViewHolder myViewHolder= (CameraItemsAdapter.ViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder);
        }
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(CameraItemsAdapter.ViewHolder myViewHolder);
        void onRowClear(CameraItemsAdapter.ViewHolder myViewHolder);
    }

}
