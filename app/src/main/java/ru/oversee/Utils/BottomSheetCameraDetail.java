package ru.oversee.Utils;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.otaliastudios.zoom.ZoomLayout;

import org.videolan.libvlc.util.VLCVideoLayout;

import ru.oversee.R;
import ru.oversee.Home.CameraItemRealm;
import ru.oversee.Home.CameraItemsAdapter;
import ru.oversee.MainActivity;
import ru.oversee.MainFragments.HomeFragment;

public class BottomSheetCameraDetail {

    MainActivity parentActivity;
    HomeFragment subParentActivity;

    CameraItemsAdapter cameraItemsAdapter;
    RecyclerView cameraItemsRecyclerView;

    CameraItemRealm cameraItem;
    ZoomLayout zoomView;
    EditText addressInput;
    EditText nameInput;
    TextView detailHeader;
    VLCVideoLayout videoLayout;

    public BottomSheetCameraDetail(MainActivity parentActivity, HomeFragment subParentActivity) {
        this.parentActivity = parentActivity;
        this.subParentActivity = subParentActivity;

        if (subParentActivity != null) {
            this.cameraItemsAdapter = subParentActivity.cameraItemsAdapter;
            this.cameraItemsRecyclerView = subParentActivity.cameraItemsRecyclerView;
        }

        parentActivity.sheetPopupCamera.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        parentActivity.cameraChange = true;
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        if (cameraItem != null && parentActivity.cameraChange) {
                            setDeviceName();
                            endCameraStream();
                            try {
                                if (cameraItemsAdapter != null && cameraItemsRecyclerView != null) {
                                    for (int i = 0; cameraItemsAdapter.getItemCount() > i; i++) {
                                        CameraItemsAdapter.ViewHolder cameraViewHolder = (CameraItemsAdapter.ViewHolder) cameraItemsRecyclerView.findViewHolderForAdapterPosition(i);
                                        if (cameraViewHolder != null){
                                            cameraViewHolder.cameraLoaderLayout.setVisibility(View.GONE);
                                            cameraViewHolder.cameraVideoLayout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            } catch (IllegalStateException ignored) {}

                            cameraItem.removeAllChangeListeners();
                            parentActivity.tabLayoutContainer = null;
                            parentActivity.cameraChange = false;
                        }

                        if (zoomView != null) zoomView.zoomTo(1, true);
                        cameraItem = null;
                    }
                    break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    public void openDetailCameraView(String cameraId) {
        nameInput = parentActivity.findViewById(R.id.input_name_detail);

        if (cameraId != null) {
            cameraItem = CameraItemRealm.getCameraById(parentActivity.uiThreadRealm, cameraId);

            if (cameraItem == null) return;

            ConstraintLayout lookLayout = parentActivity.findViewById(R.id.look_layout);
            lookLayout.setVisibility(View.VISIBLE);
            ConstraintLayout settingLayout = parentActivity.findViewById(R.id.settings_layout);
            settingLayout.setVisibility(View.GONE);

            detailHeader = parentActivity.findViewById(R.id.camera_detail_header);
            addressInput = parentActivity.findViewById(R.id.input_ip_detail);

            detailHeader.setText(cameraItem.getName());
            nameInput.setText(cameraItem.getName());
            addressInput.setText(cameraItem.getAddress());

            nameInput.setOnFocusChangeListener((view112, hasFocus) -> {
                if (!hasFocus) setDeviceName();
            });

            parentActivity.tabLayoutContainer = parentActivity.findViewById(R.id.tabLayoutContainer);
            TabLayout tabLayout = parentActivity.findViewById(R.id.tabLayout);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            GeneralFunctions.hideKeyboard(parentActivity);
                            lookLayout.setVisibility(View.VISIBLE);
                            settingLayout.setVisibility(View.GONE);

                            if (cameraItem != null) {
                                if (!cameraItem.getAddress().equals(addressInput.getText().toString()))
                                    endCameraStream();
                            }
                            break;
                        case 1:
                            lookLayout.setVisibility(View.GONE);
                            settingLayout.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            tabLayout.getTabAt(0).select();

            zoomView = parentActivity.findViewById(R.id.zoom_view);
            zoomView.zoomTo(1, true);
            ConstraintLayout zoomStreamLayout = parentActivity.findViewById(R.id.zoom_stream_layout);
            videoLayout = parentActivity.findViewById(R.id.videoLayout);
            parentActivity.streamLoaderLayout = parentActivity.findViewById(R.id.loader_layout);

            TextView cameraConnectInfo = parentActivity.findViewById(R.id.camera_connect_info);

            if (parentActivity.restartStream) {
                CameraControlFunctions.createStream(
                        parentActivity,
                        cameraItem,
                        videoLayout,
                        zoomView,
                        zoomStreamLayout,
                        cameraConnectInfo
                );
                parentActivity.restartStream = false;
            }

            TextView deleteButton = parentActivity.findViewById(R.id.delete_device);
            deleteButton.setOnClickListener(view -> {
                Dialogs.showActionDialog(
                        parentActivity,
                        parentActivity.getResources().getString(R.string.delete_camera_header),
                        parentActivity.getResources().getString(R.string.delete_camera_text),
                        parentActivity.getResources().getString(R.string.delete),
                        () -> {
                            if (cameraItemsAdapter != null)
                                cameraItemsAdapter.notifyItemRemoved(cameraItem.getSort());

                            CameraItemRealm.deleteCamera(parentActivity.uiThreadRealm, cameraItem.getId());
                            parentActivity.backUiThreadHandler.post(() -> CameraControlFunctions.destroyStream(parentActivity));
                            parentActivity.restartStream = true;

                            if (parentActivity.sheetPopupCamera.getState() != BottomSheetBehavior.STATE_COLLAPSED)
                                parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        });
            });

            ImageView comeBackArrow = parentActivity.findViewById(R.id.camera_detail_close_arrow);
            comeBackArrow.setOnClickListener(view -> {
                if (parentActivity.sheetPopupCamera.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });

            initNotificationCamera();
        }

        if (parentActivity.sheetPopupCamera.getState() != BottomSheetBehavior.STATE_EXPANDED)
            parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void endCameraStream() {
        String ip = String.valueOf(addressInput.getText());

        parentActivity.backUiThreadHandler.post(() -> CameraControlFunctions.destroyStream(parentActivity));
        parentActivity.restartStream = true;

        if (!cameraItem.getAddress().equals(ip))
            parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> cameraItem.setAddress(ip));
    }

    private void setDeviceName() {
        String name = String.valueOf(nameInput.getText());
        detailHeader.setText(name);
        try {
            if (cameraItem != null && cameraItem.isValid() && !name.equals(cameraItem.getName()))
                parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> cameraItem.setName(name));
        } catch (IllegalStateException ignored) {}
    }

    private void initNotificationCamera() {
        try {
            cameraItem.addChangeListener((changeDevice, changeSet) -> {
                parentActivity.runOnUiThread(() -> {
                    if (parentActivity.sheetPopupCamera.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        if (cameraItem != null && cameraItem.isValid()) {
                            if (parentActivity.restartStream) {
                                openDetailCameraView(cameraItem.getId());
                            }
                        }
                    }
                });
            });
        } catch (IllegalStateException ignored){}
    }
}
