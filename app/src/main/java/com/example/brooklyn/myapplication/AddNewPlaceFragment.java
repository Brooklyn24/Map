package com.example.brooklyn.myapplication;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;

@RuntimePermissions
public class AddNewPlaceFragment extends Fragment {

    public static final String LAT_LNG_KEY = "latLngKey";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int GET_IMAGE_FROM_GALLERY_REQUEST_CODE = 1023;
    public static final String URI_IMAGE_KEY = "uri_image_key";
    public static final String FULL_DATE_KEY = "full_date_key";

    private LatLng latLng;
    private Uri imageUri;
    private Unbinder unbinder;
    private SimpleDateFormat sdfDateTextView;
    private SimpleDateFormat sdfTimeTextView;
    private GeoPoint geoPoint;
    private Calendar finalDate;
    private String mCurrentPhotoPath;
    private Context context;

    @BindView(R.id.imageButton) ImageButton addImageFromGalleryButton;
    @BindView(R.id.editTextTitle) EditText editTextTitle;
    @BindView(R.id.timePick) TextView timePick;
    @BindView(R.id.datePick) TextView datePick;
    @BindView(R.id.imageView) ImageView imagePreview;

    public AddNewPlaceFragment() {
    }

    public static AddNewPlaceFragment newInstance(LatLng latLng) {

        Bundle args = new Bundle();
        args.putParcelable(LAT_LNG_KEY, latLng);

        AddNewPlaceFragment fragment = new AddNewPlaceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("logz", "onActivityResult: ");
            imageUri = Uri.parse(mCurrentPhotoPath);
            Picasso.with(context)
                    .load(imageUri)
                    .resize(0, 300)
                    .into(imagePreview);

            //TODO: MediaScannerConnection
            MediaScannerConnection.scanFile(context,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        }
        else if(requestCode == GET_IMAGE_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK
                && null != data)
        {
            imageUri = data.getData();
            Picasso.with(context)
                    .load(imageUri)
                    .resize(0, 300)
                    .into(imagePreview);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finalDate = Calendar.getInstance();
        sdfTimeTextView = new SimpleDateFormat("K:mm a");
        sdfDateTextView = new SimpleDateFormat("EEE, d MMM yyyy");
        latLng = getArguments().getParcelable(LAT_LNG_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("logz", "onAttach: new");
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("logz", "onAttach: old");
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_place, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        Log.d("destroyed", "onDestroyView: ");
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            imageUri = (Uri) savedInstanceState.get(URI_IMAGE_KEY);
            finalDate.setTimeInMillis((Long) savedInstanceState.get(FULL_DATE_KEY));
            Picasso.with(context)
                    .load(imageUri)
                    .resize(0, 300)
                    .into(imagePreview);
        }
        timePick.setText(sdfTimeTextView.format(finalDate.getTime()));
        datePick.setText(sdfDateTextView.format(finalDate.getTime()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddNewPlaceFragmentPermissionsDispatcher
                .onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick(R.id.imageButton3)
    public void addImageFromGalleryClick() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GET_IMAGE_FROM_GALLERY_REQUEST_CODE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(URI_IMAGE_KEY, imageUri);
        outState.putLong(FULL_DATE_KEY, finalDate.getTimeInMillis());
    }

    @OnClick(R.id.timePick)
    public void timePickOnClick() {
        TimePickerDialog.OnTimeSetListener onTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                finalDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                finalDate.set(Calendar.MINUTE, minute);
                timePick.setText(sdfTimeTextView.format(finalDate.getTime()));
            }
        };
        TimePickerDialog newFragment = new TimePickerDialog(getActivity(),
                onTime,
                finalDate.get(Calendar.HOUR_OF_DAY),
                finalDate.get(Calendar.MINUTE),
                false
        );
        newFragment.show();
    }

    @OnClick(R.id.imageButton)

    public void addImageByCamera() {
        AddNewPlaceFragmentPermissionsDispatcher.onLaunchCameraWithCheck(this);
    }

    @OnClick(R.id.datePick)
    public void datePickOnClick() {
        DatePickerDialog.OnDateSetListener onDate = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                finalDate.set(Calendar.YEAR, year);
                finalDate.set(Calendar.MONTH, monthOfYear);
                finalDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                datePick.setText(sdfDateTextView.format(finalDate.getTime()));
            }
        };
        DatePickerDialog newFragment = new DatePickerDialog(getActivity(),
                onDate,
                finalDate.get(Calendar.YEAR),
                finalDate.get(Calendar.MONTH),
                finalDate.get(Calendar.DAY_OF_MONTH)
        );
        newFragment.show();
    }

    @OnClick(R.id.buttonDone)
    public void buttonDoneClick() {
        geoPoint = new GeoPoint(
                        latLng,
                        editTextTitle.getText().toString(),
                        imageUri,
                        finalDate
                );
                try{
                    ((INewPlaceAdded) context).addNewPlace(geoPoint);
                }catch (ClassCastException cce){
                    cce.printStackTrace();
                }
    }

    public interface INewPlaceAdded {
        void addNewPlace(GeoPoint geoPoint);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onLaunchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = null;
        storageDir.mkdirs();
        image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d("logz", "createImageFile: " + mCurrentPhotoPath);
        return image;
    }
}
