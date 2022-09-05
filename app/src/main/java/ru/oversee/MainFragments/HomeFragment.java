package ru.oversee.MainFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import io.realm.OrderedCollectionChangeSet;
import io.realm.RealmResults;
import io.realm.Sort;
import ru.oversee.R;
import ru.oversee.Utils.BottomSheetCameraDetail;
import ru.oversee.Home.CameraItemRealm;
import ru.oversee.Home.CameraItemsAdapter;
import ru.oversee.MainActivity;
import ru.oversee.Utils.CameraControlFunctions;
import ru.oversee.Utils.ItemMoveCallback;

public class HomeFragment extends Fragment implements CameraItemsAdapter.ItemClickListener, CameraItemsAdapter.ItemLongClickListener {

    private View rootView;
    public MainActivity parentActivity;

    public CameraItemsAdapter cameraItemsAdapter;
    public RecyclerView cameraItemsRecyclerView;
    private ItemTouchHelper camerasTouchHelper;
    private RealmResults<CameraItemRealm> cameraItems;

    public boolean isChange;

    public HomeFragment(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        return rootView;
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (parentActivity == null) {
            parentActivity = (MainActivity) getActivity();
            assert parentActivity != null;
            parentActivity.restartApp();
        }

        cameraItems = CameraItemRealm.getAll(parentActivity.uiThreadRealm).sort("sort", Sort.ASCENDING);

        cameraItemsRecyclerView = rootView.findViewById(R.id.cameras_recycler_view);
        cameraItemsRecyclerView.setNestedScrollingEnabled(false);

        cameraItemsAdapter = new CameraItemsAdapter(parentActivity, this, cameraItems);

        ItemTouchHelper.Callback camerasCallback = new ItemMoveCallback(cameraItemsAdapter);
        camerasTouchHelper = new ItemTouchHelper(camerasCallback);
        camerasTouchHelper.attachToRecyclerView(null);

        cameraItemsAdapter.setLongClickListener(this);
        cameraItemsAdapter.setClickListener(this);

        RecyclerView.LayoutManager layoutCamerasManager = new GridLayoutManager(parentActivity, parentActivity.appSettings.getGridCount());
        cameraItemsRecyclerView.setAdapter(cameraItemsAdapter);
        cameraItemsRecyclerView.setLayoutManager(layoutCamerasManager);

        initNotification();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changePositionCamera(boolean active) {
        if (active) {
            if (!isChange) {
                isChange = true;
                camerasTouchHelper.attachToRecyclerView(cameraItemsRecyclerView);

                int cameraItemsSize = cameraItems.size();
                parentActivity.backUiThreadHandler.post(() -> {
                    CameraControlFunctions.destroyAdapterStreams(cameraItemsAdapter, cameraItemsSize, cameraItemsRecyclerView);
                });
                cameraItemsAdapter.setSortedArrayList();
                cameraItemsAdapter.notifyDataSetChanged();
            }
        } else {
            if (isChange) {
                isChange = false;

                parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> {
                    for (int i = 0; i < cameraItemsAdapter.sortedCameraList.size(); i++) {
                        CameraItemRealm cameraItem = cameraItemsAdapter.sortedCameraList.get(i);
                        if (cameraItem != null) {
                            CameraItemRealm tempCameraItem = CameraItemRealm.getCameraById(parentActivity.uiThreadRealm, cameraItem.getId());
                            if (tempCameraItem != null) {
                                tempCameraItem.setSort(i);
                            }
                        }
                    }
                    cameraItemsAdapter.notifyDataSetChanged();
                });
                camerasTouchHelper.attachToRecyclerView(null);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initNotification() {
        cameraItems.removeAllChangeListeners();

        if (parentActivity.currentFragment.equals("HOME")) {
            cameraItems.addChangeListener((collection, changeSet) -> {
                if (changeSet.getState() == OrderedCollectionChangeSet.State.INITIAL) {
                    cameraItemsAdapter.notifyDataSetChanged();
                    return;
                }

                try {
                    // For deletions, the adapter has to be notified in reverse order.
                    OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                    for (int i = deletions.length - 1; i >= 0; i--) {
                        OrderedCollectionChangeSet.Range range = deletions[i];
                        cameraItemsAdapter.notifyItemRangeRemoved(range.startIndex, range.length);
                    }
                    OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                    for (OrderedCollectionChangeSet.Range range : insertions) {
                        cameraItemsAdapter.notifyItemRangeInserted(range.startIndex, range.length);
                    }
                    OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                    for (OrderedCollectionChangeSet.Range range : modifications) {
                        cameraItemsAdapter.notifyItemRangeChanged(range.startIndex, range.length);
                    }

                } catch (Exception ignored) {
                    cameraItemsAdapter.notifyDataSetChanged();
                }
            });

            cameraItemsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (!isChange) {
            CameraItemRealm cameraItemRealm = cameraItemsAdapter.getItem(position);

            BottomSheetCameraDetail handler = new BottomSheetCameraDetail(parentActivity, this);

            for (int i = 0; cameraItemsAdapter.getItemCount() > i; i++) {
                CameraItemsAdapter.ViewHolder cameraViewHolder = (CameraItemsAdapter.ViewHolder) cameraItemsRecyclerView.findViewHolderForAdapterPosition(i);
                if (cameraViewHolder != null) {
                    cameraViewHolder.cameraLoaderLayout.setVisibility(View.VISIBLE);
                    cameraViewHolder.cameraVideoLayout.setVisibility(View.GONE);
                }
            }

            handler.openDetailCameraView(cameraItemRealm.getId());
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        changePositionCamera(true);
    }

    @Override
    public void onPause() {
        changePositionCamera(false);
        int cameraItemsSize = cameraItems.size();
        parentActivity.backUiThreadHandler.post(() -> {
            CameraControlFunctions.destroyAdapterStreams(cameraItemsAdapter, cameraItemsSize, cameraItemsRecyclerView);
        });

        super.onPause();
    }

    public boolean onBackPressed() {
        if (parentActivity.sheetPopupCamera.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_COLLAPSED);

            return false;
        }

        return true;
    }
}
