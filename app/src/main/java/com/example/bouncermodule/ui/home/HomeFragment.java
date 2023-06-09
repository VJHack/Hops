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
import com.example.bouncermodule.ui.CameraActivity;
import com.example.bouncermodule.ui.authentication.AuthenticationFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import android.Manifest;
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
    private Button signOutButton;
    private Button resetButton;
    private DatabaseReference mDatabase;
    private Map<String, Bars> barsMap = new HashMap<>();
    private int counterValInt = 0;

    private ImageView photoIdImageView;
    private Button photoIdButton;
    private FragmentHomeBinding binding;

    private String associatedBar;
    private Boolean Verified;

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
        signOutButton = (Button) myView.findViewById(R.id.signOut);
        signOutButton.setOnClickListener((View.OnClickListener) this);
        resetButton = (Button) myView.findViewById(R.id.Reset);
        resetButton.setOnClickListener((View.OnClickListener) this);

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

        Verified = true;
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference userRef = mDatabase.child("VerifiedUsers/");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String userEmail = parseEmail(AuthenticationFragment.getEmail());
                    associatedBar = snapshot.child(userEmail).getValue().toString();
                    Bars tempBar = barsMap.get(associatedBar);
                    counterValInt = tempBar.getLineCount();
                    counterValue.setText("Total:    " +String.valueOf(counterValInt));
                    currentLength.setText(tempBar.getLineLength());
                    lineLengthColor();
                }catch(Exception e) {
                    // If in here the user is a regular user and not a bouncer
                    Verified = false;
                    counterValInt = 0;
                    counterValue.setText("Total:    " +String.valueOf(counterValInt));
                    currentLength.setText("NONE");
                    lineLengthColor();
                }
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
            currentLength.setTextColor(Color.parseColor("#0000FF"));
        }else if(view.getId() == R.id.Medium){
            Log.d("LOG", "MEDIUM CLICKED");
            currentLength.setText("MEDIUM");
            currentLength.setTextColor(Color.parseColor("#FFA500"));
        }else if(view.getId() == R.id.Long){
            currentLength.setText("LONG");
            currentLength.setTextColor(Color.parseColor("#FF0000")); // Color Red
        }else if(view.getId() == R.id.None){
            currentLength.setText("NONE");
            currentLength.setTextColor(Color.parseColor("#028A0F"));
        }else if(view.getId() == R.id.plus){
            counterValInt++;
            counterValue.setText("Total:    " +String.valueOf(counterValInt));
        }else if(view.getId() == R.id.minus){
            if(counterValInt >= 1) {
                counterValInt--;
            }
            counterValue.setText("Total:    " + String.valueOf(counterValInt));
        }else if(view.getId() == R.id.signOut){
            AuthenticationFragment.signOut();
            Intent intent = new Intent(getActivity(), AuthenticationFragment.class);
            startActivity(intent);
        }else if(view.getId() == R.id.Reset) {
            counterValInt = 0;
            counterValue.setText("Total:    " + String.valueOf(counterValInt));
            currentLength.setText("NONE");
            currentLength.setTextColor(Color.parseColor("#028A0F"));

        }

        if (Verified) {
            Bars tempBar = barsMap.get(associatedBar);
            tempBar.setLineCount(counterValInt);
            tempBar.setLineLength((String) currentLength.getText());
            mDatabase.child("bars").child(associatedBar).setValue(tempBar);
        }
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

    private String parseEmail(String username) {
        return username.replaceAll("[.]", "_");
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
