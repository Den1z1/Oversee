package ru.oversee.Utils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.zoom.ZoomLayout;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.UUID;

import ru.oversee.Home.CameraItemRealm;
import ru.oversee.Home.CameraItemsAdapter;
import ru.oversee.MainActivity;
import ru.oversee.R;

public class CameraControlFunctions {

    @SuppressLint("ClickableViewAccessibility")
    public static void createStream(
            MainActivity parentActivity,
            CameraItemRealm cameraItem,
            VLCVideoLayout videoLayout,
            ZoomLayout zoomView,
            ConstraintLayout zoomStreamLayout,
            TextView cameraAddressInfo
    ) {
        parentActivity.streamLoaderLayout.setVisibility(View.VISIBLE);

        zoomView.setZoomEnabled(false);
        zoomView.setOnTouchListener((view, motionEvent) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            view.onTouchEvent(motionEvent);
            return true;
        });

        zoomStreamLayout.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels;
        zoomStreamLayout.getLayoutParams().height = Math.round(zoomStreamLayout.getLayoutParams().width * .5625f);

        cameraAddressInfo.setVisibility(View.VISIBLE);
        String ip = cameraItem.getAddress();

        parentActivity.streamRun = () -> {
            parentActivity.libVLC = new LibVLC(parentActivity, getLibVLCOptions());
            parentActivity.mediaPlayer = new MediaPlayer(parentActivity.libVLC);

            parentActivity.mediaPlayer.setEventListener(event -> {
                if (event.type == MediaPlayer.Event.Buffering) {
                    if (event.getBuffering() == 100f) {
                        parentActivity.streamLoaderLayout.setVisibility(View.GONE);
                        cameraAddressInfo.setVisibility(View.GONE);
                        zoomView.setZoomEnabled(true);
                    }
                } else if (event.type == MediaPlayer.Event.Stopped) {
                    cameraAddressInfo.setText(R.string.camera_not_found);
                }
            });

            parentActivity.mediaPlayer.attachViews(videoLayout, null, false, false);

            Media media = new Media(parentActivity.libVLC, Uri.parse(ip));
            media.setHWDecoderEnabled(true, false);

            parentActivity.mediaPlayer.setMedia(media);
            media.release();
            parentActivity.mediaPlayer.play();
        };

        parentActivity.streamLoaderLayout.post(parentActivity.streamRun);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void createAdapterStream(
            CameraItemRealm cameraItem,
            VLCVideoLayout videoLayout,
            LibVLC adapterLibVLC,
            MediaPlayer adapterMediaPlayer,
            ConstraintLayout adapterLoaderLayout
    ) {
        adapterLoaderLayout.setVisibility(View.VISIBLE);
        String ip = cameraItem.getAddress();

        adapterMediaPlayer.setEventListener(event -> {
            if (event.type == MediaPlayer.Event.Buffering) {
                if (event.getBuffering() == 100f) {
                    adapterLoaderLayout.setVisibility(View.GONE);
                }
            }
        });

        adapterMediaPlayer.attachViews(videoLayout, null, false, false);

        Media media = new Media(adapterLibVLC, Uri.parse(ip));
        media.setHWDecoderEnabled(true, false);

        adapterMediaPlayer.setMedia(media);
        media.release();
        adapterMediaPlayer.play();
    }

    public static ArrayList<String> getLibVLCOptions() {
        ArrayList<String> options = new ArrayList<>();

        options.add("--network-caching=300m");
        options.add("--rtsp-caching=300m");
        options.add("--tcp-caching=300m");
        options.add("--realrtsp-caching=300m");
        options.add("--file-caching");
        options.add("--no-drop-late-frames");
        options.add("--no-skip-frames");
        options.add("--rtsp-tcp");
        options.add("--fullscreen");

        return options;
    }

    public static void destroyStream(MainActivity parentActivity) {
        if (parentActivity.mediaPlayer != null) {
            parentActivity.mediaPlayer.detachViews();
            parentActivity.mediaPlayer.release();
            parentActivity.mediaPlayer = null;
        }
        if (parentActivity.libVLC != null) {
            parentActivity.libVLC.release();
            parentActivity.libVLC = null;
        }

        parentActivity.streamLoaderLayout.removeCallbacks(parentActivity.streamRun);
        parentActivity.streamRun = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void destroyAdapterStreams(CameraItemsAdapter cameraItemsAdapter, int cameraItemsSize, RecyclerView cameraItemsRecyclerView) {
        if (cameraItemsAdapter != null && cameraItemsSize > 0) {
            for (int i = 0; cameraItemsSize > i; i++) {
                CameraItemsAdapter.ViewHolder cameraViewHolder = (CameraItemsAdapter.ViewHolder) cameraItemsRecyclerView.findViewHolderForAdapterPosition(i);
                if (cameraViewHolder != null) {
                    if (cameraViewHolder.cameraMediaPlayer != null) {
                        cameraViewHolder.cameraMediaPlayer.detachViews();
                        cameraViewHolder.cameraMediaPlayer.release();
                        cameraViewHolder.cameraMediaPlayer = null;
                    }
                    if (cameraViewHolder.cameraLibVlc != null) {
                        cameraViewHolder.cameraLibVlc.release();
                        cameraViewHolder.cameraLibVlc = null;
                    }

                    cameraViewHolder.cameraLoaderLayout.removeCallbacks(cameraViewHolder.cameraRun);
                    cameraViewHolder.cameraRun = null;
                }
            }
        }
    }

    public static void saveCamera(MainActivity parentActivity, String address, String name) {
        CameraItemRealm newCamera = new CameraItemRealm();

        parentActivity.uiThreadRealm.executeTransaction(transactionRealm -> {
            long countDevices = CameraItemRealm.getCountAll(parentActivity.uiThreadRealm);

            newCamera.setId(UUID.randomUUID().toString());
            newCamera.setAddress(address);
            newCamera.setName(name);
            newCamera.setSort(Math.toIntExact(countDevices));

            transactionRealm.insert(newCamera);
        });

        BottomSheetCameraDetail handler = new BottomSheetCameraDetail(parentActivity, null);
        handler.openDetailCameraView(newCamera.getId());
    }
}
