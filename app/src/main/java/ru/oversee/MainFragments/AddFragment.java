package ru.oversee.MainFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ru.oversee.R;
import ru.oversee.MainActivity;
import ru.oversee.Utils.CameraControlFunctions;

public class AddFragment extends Fragment {

    public MainActivity parentActivity;
    private View rootView;

    public AddFragment(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public AddFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add, container, false);
        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (parentActivity == null) {
            parentActivity = (MainActivity) getActivity();
            assert parentActivity != null;
            parentActivity.restartApp();
        }

        EditText ip = rootView.findViewById(R.id.input_ip);
        EditText name = rootView.findViewById(R.id.input_name);

        Button saveButton = rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(view1 -> {
            if (ip.getText().toString().equals("")) {
                Toast.makeText(parentActivity, parentActivity.getResources().getString(R.string.enter_ip_toast), Toast.LENGTH_SHORT).show();
            } else if (name.getText().toString().equals("")) {
                Toast.makeText(parentActivity, parentActivity.getResources().getString(R.string.enter_name_toast), Toast.LENGTH_SHORT).show();
            } else {
                CameraControlFunctions.saveCamera(parentActivity, ip.getText().toString(), name.getText().toString());
                ip.setText("");
                name.setText("");
            }
        });
    }

    public boolean onBackPressed() {
        if (parentActivity.sheetPopupCamera.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            parentActivity.sheetPopupCamera.setState(BottomSheetBehavior.STATE_COLLAPSED);

            return false;
        }
        return true;
    }
}
