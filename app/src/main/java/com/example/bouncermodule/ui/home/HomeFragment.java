package com.example.bouncermodule.ui.home;

import static android.content.Context.CAMERA_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.bouncermodule.R;
import com.example.bouncermodule.databinding.FragmentHomeBinding;
import com.example.bouncermodule.ui.CameraActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.example.bouncermodule.Bars;
import com.example.bouncermodule.R;
import com.example.bouncermodule.databinding.ActivityMainBinding;
import com.example.bouncermodule.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment  implements View.OnClickListener {
    private TextView counterValue;
    private TextView currentLength;
    private Button noneButton;
    private Button shortButton;
    private Button mediumButton;
    private Button longButton;

    private Button plusButton;
    private Button minusButton;
    private int counterValInt;

    private ImageView photoIdImageView;
    private Button photoIdButton;
    private FragmentHomeBinding binding;
    // Firebase variables
    private DatabaseReference mDatabase;
    private Map<String, Bars> barsMap = new HashMap<>();

    // For input images of face detection
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View myView = inflater.inflate(R.layout.fragment_home, container, false);
        noneButton = (Button) myView.findViewById(R.id.None);
        noneButton.setOnClickListener(this);
        shortButton = (Button) myView.findViewById(R.id.Short);
        shortButton.setOnClickListener((View.OnClickListener) this);
        mediumButton = (Button) myView.findViewById(R.id.Medium);
        mediumButton.setOnClickListener((View.OnClickListener) this);
        longButton = (Button) myView.findViewById(R.id.Long);
        longButton.setOnClickListener((View.OnClickListener) this);

        currentLength = (TextView) myView.findViewById(R.id.CurrentLengthValue);
        counterValue = (TextView) myView.findViewById(R.id.Total_Value);
        plusButton = (Button) myView.findViewById(R.id.plus);
        plusButton.setOnClickListener((View.OnClickListener) this);
        minusButton = (Button) myView.findViewById(R.id.minus);
        minusButton.setOnClickListener((View.OnClickListener) this);

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // For face detection stuff
        // setup detector options
        FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        // prepare input image (from camera)
        Button cameraButton = (Button) myView.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 10);
                }
                else{
                    Intent intent = new Intent(getActivity(), CameraActivity.class);
                    startActivity(intent);
                }
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference barRef = mDatabase.child("bars/");
        barRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    String barName = data.getKey();
                    String lineLength = data.child("lineLength").getValue().toString();
                    Integer lineCount = Integer.parseInt(data.child("lineCount").getValue().toString());
                    Double latitude = Double.parseDouble(data.child("latitude").getValue().toString());
                    Double longitude = Double.parseDouble(data.child("longitude").getValue().toString());;
                    Bars tempBar = new Bars(lineLength, lineCount, longitude, latitude);
                    barsMap.put(barName, tempBar);
                }

                // Make "Mondays" a variable for which bouncer is using counter
                Bars tempBar = barsMap.get("Mondays");
                counterValInt = tempBar.getLineCount();
                counterValue.setText("Total:    " +String.valueOf(counterValInt));
                currentLength.setText(tempBar.getLineLength());
                lineLengthColor();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        return myView;
    }

    public void onClick(View view) {
        if(view.getId() == R.id.Short) {
            Log.d("LOG", "NONE CLICKED");
            currentLength.setText("SHORT");
            lineLengthColor();
        }else if(view.getId() == R.id.Medium){
            Log.d("LOG", "MEDIUM CLICKED");
            currentLength.setText("MEDIUM");
            lineLengthColor();
        }else if(view.getId() == R.id.Long){
            currentLength.setText("LONG");
            lineLengthColor();
        }else if(view.getId() == R.id.None){
            currentLength.setText("NONE");
            lineLengthColor();
        }else if(view.getId() == R.id.plus){
            counterValInt++;
            counterValue.setText("Total:    " +String.valueOf(counterValInt));
        }else if(view.getId() == R.id.minus){
            if(counterValInt >= 1) {
                counterValInt--;
            }
            counterValue.setText("Total:    " + String.valueOf(counterValInt));
        }

        // Changing the line count and line length of Mondays
        Bars tempBar = barsMap.get("Mondays");
        tempBar.setLineCount(counterValInt);
        tempBar.setLineLength((String) currentLength.getText());
        mDatabase.child("bars").child("Mondays").setValue(tempBar);
    }

    public void lineLengthColor() {
        switch (currentLength.getText().toString()) {
            case("LONG"):
                currentLength.setTextColor(Color.parseColor("#FF0000")); // Red
                break;
            case("MEDIUM"):
                currentLength.setTextColor(Color.parseColor("#FFA500")); // Yellow
                break;
            case("SHORT"):
                currentLength.setTextColor(Color.parseColor("#0000FF")); // Blue
                break;
            default:
                currentLength.setTextColor(Color.parseColor("#028A0F")); // Green
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation. Used for facial detection.
     */
    // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }

}