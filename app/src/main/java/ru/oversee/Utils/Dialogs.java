package ru.oversee.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import ru.oversee.R;
import ru.oversee.MainActivity;

public class Dialogs {

    public static void showInfoDialog(final Context context, String headerText, String infoText, DialogActionCallback callback) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View elemView = layoutInflater.inflate(R.layout.dialog_info, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setView(elemView);

        TextView headerView = elemView.findViewById(R.id.header);
        headerView.setVisibility(View.GONE);
        TextView infoView = elemView.findViewById(R.id.textInfo);

        TextView closeButton = elemView.findViewById(R.id.textCancel);

        if (headerText != null) {
            headerView.setVisibility(View.VISIBLE);
            headerView.setText(headerText);
        }

        infoView.setText(infoText);

        closeButton.setOnClickListener(view -> {
            callback.onComplete();
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public static void showActionDialog(final MainActivity parentActivity, String headerText, String infoText, String completeButtonText, DialogActionCallback callback) {
        LayoutInflater layoutInflater = LayoutInflater.from(parentActivity);
        View elemView = layoutInflater.inflate(R.layout.dialog_action, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(parentActivity).create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setView(elemView);

        TextView header = elemView.findViewById(R.id.header);
        header.setVisibility(View.GONE);
        TextView info = elemView.findViewById(R.id.textInfo);

        TextView cancelButton = elemView.findViewById(R.id.textCancel);
        TextView completeButton = elemView.findViewById(R.id.textComplete);

        if (headerText != null) {
            header.setVisibility(View.VISIBLE);
            header.setText(headerText);
        }

        info.setText(infoText);

        completeButton.setText(completeButtonText);
        completeButton.setOnClickListener(view -> {
            callback.onComplete();
            alertDialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> alertDialog.dismiss());

        alertDialog.show();
    }

    public static void setupFullHeight(BottomSheetDialog bottomSheetDialog, ConstraintLayout layoutForTopMargin) {
        if (layoutForTopMargin != null)
            GeneralFunctions.setMargins(layoutForTopMargin, 0, (int) GeneralFunctions.convertDpToPixel(bottomSheetDialog.getContext(), 18), 0, 0);

        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = bottomSheetDialog.getBehavior();
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
