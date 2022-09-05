package ru.oversee.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.RealmResults;
import ru.oversee.R;
import ru.oversee.MainActivity;
import ru.oversee.MainFragments.HomeFragment;
import ru.oversee.Utils.CameraControlFunctions;
import ru.oversee.Utils.ItemMoveCallback;

public class CameraItemsAdapter extends RecyclerView.Adapter<CameraItemsAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private final MainActivity parentActivity;
    private final HomeFragment subParentActivity;
    private final LayoutInflater mInflater;
    private final RealmResults<CameraItemRealm> cameraItemsList;
    public ArrayList<CameraItemRealm> sortedCameraList = new ArrayList<>();
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;

    public CameraItemsAdapter(MainActivity parentActivity, HomeFragment subParentActivity, RealmResults<CameraItemRealm> cameraItemsList) {
        this.parentActivity = parentActivity;
        this.subParentActivity = subParentActivity;
        this.mInflater = LayoutInflater.from(parentActivity);
        this.cameraItemsList = cameraItemsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.camera_item, parent, false);
        return new ViewHolder(view);
    }

    public void setSortedArrayList() {
        sortedCameraList.clear();
        sortedCameraList.addAll(cameraItemsList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        CameraItemRealm camera = cameraItemsList.get(position);

        if (camera != null) {
            viewHolder.cameraName.setText(camera.getName());

            viewHolder.cameraLoaderLayout.setVisibility(View.VISIBLE);
            viewHolder.ip = camera.getAddress();

            Animation anim = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.rotate);
            viewHolder.itemView.setHasTransientState(true);

            if (!subParentActivity.isChange) {
                viewHolder.cameraLibVlc = new LibVLC(parentActivity, CameraControlFunctions.getLibVLCOptions());
                viewHolder.cameraMediaPlayer = new MediaPlayer(viewHolder.cameraLibVlc);

                viewHolder.cameraRun = () -> {
                    CameraControlFunctions.createAdapterStream(
                            camera,
                            viewHolder.cameraVideoLayout,
                            viewHolder.cameraLibVlc,
                            viewHolder.cameraMediaPlayer,
                            viewHolder.cameraLoaderLayout
                    );
                };
                viewHolder.cameraLoaderLayout.post(viewHolder.cameraRun);

                viewHolder.itemView.setHasTransientState(false);
            } else {
                viewHolder.itemView.setAnimation(anim);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(cameraItemsList != null){
            return cameraItemsList.size();
        }
        return 0;
    }

    // convenience method for getting data at click position
    public CameraItemRealm getItem(int id) {
        if (cameraItemsList.size() > id)
            return cameraItemsList.get(id);
        return null;
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(sortedCameraList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(sortedCameraList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ViewHolder myViewHolder) {

    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {

    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public VLCVideoLayout cameraVideoLayout;
        public LibVLC cameraLibVlc;
        public MediaPlayer cameraMediaPlayer;

        public ConstraintLayout cameraLoaderLayout;
        public TextView cameraName;
        public String id;
        public String ip;
        public Runnable cameraRun;

        ViewHolder(View itemView) {
            super(itemView);
            cameraVideoLayout = itemView.findViewById(R.id.cameraVideoLayout);
            cameraLoaderLayout = itemView.findViewById(R.id.camera_loader_layout);
            cameraName = itemView.findViewById(R.id.camera_name);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (!subParentActivity.isChange) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, getBindingAdapterPosition());
                }
            } else {
                subParentActivity.changePositionCamera(false);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null)
                mLongClickListener.onItemLongClick(view, getBindingAdapterPosition());
            return true;
        }
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
