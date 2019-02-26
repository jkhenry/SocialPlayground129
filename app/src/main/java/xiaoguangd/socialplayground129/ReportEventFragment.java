package xiaoguangd.socialplayground129;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportEventFragment extends Fragment {

    private final static String TAG = ReportEventFragment.class.getSimpleName();

    private EditText mTextViewLocation;
    private EditText getmTextViewDest;
    private EditText mTextViewTitle;
    private Button mSelectButton;
    private ImageView mImageView;
    private Button mReportButton;

    private DatabaseReference database;
    private String username;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private static int RESULT_LOAD_IMAGE = 1;

    // This is the path of the picture from disk.
    private String mPicturePath = "";





    public ReportEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_event, container, false);
        mTextViewLocation = view.findViewById(R.id.text_event_location);
        checkPermission();
        mImageView = view.findViewById(R.id.img_event_pic);
        mSelectButton = view.findViewById(R.id.button_select);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        getmTextViewDest = view.findViewById(R.id.text_event_description);
        mReportButton = view.findViewById(R.id.button_report);
        username = ((EventActivity)getActivity()).getUsername();
        database = FirebaseDatabase.getInstance().getReference();

        mTextViewTitle = view.findViewById(R.id.text_event_title);


        //auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mAuth.signInAnonymously().addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException());
                }
            }
        });

        //upload event into Firebase database and then upload the picture if there is one.
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = uploadEvent();
                if (!mPicturePath.equals("")) {
                    Log.i(TAG, "key" + key);
                    uploadImage(mPicturePath, key);
                    mPicturePath = "";
                }
            }
        });

        // Select picture from gallery
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This intent is for picking a picture from another activity(gallery).
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //get the result(in this case - the picture from gallery) from activity and then show it on the view.
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });


        return view;
    }


    /**
     * upload data and set path
     */
    private String uploadEvent() {
        String location = mTextViewLocation.getText().toString();
        String description = getmTextViewDest.getText().toString();
        if (location.equals("") || description.equals("")) {
            return "";
        }
        //create event instance
        Event event = new Event();
        event.setLocation(location);
        event.setDescription(description);
        event.setTime(System.currentTimeMillis());
        event.setUser(username);
        //get id from firebase database
        String key = database.child("events").push().getKey();
        event.setId(key);
        String title = mTextViewTitle.getText().toString();
        event.setTitle(title);
        //upload event object into the firebase database
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast toast = Toast.makeText(getContext(), "The event is failed, please check you network status.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getContext(), "The event is reported", Toast.LENGTH_SHORT);
                    toast.show();
                    mTextViewLocation.setText("");
                    getmTextViewDest.setText("");
                    mTextViewTitle.setText("");
                    mImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        return key;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    //Allow receiving intent from another activity when back from that activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            // get the returned data from gallery or photo app.
            Uri selectedImage = data.getData();
            //format the file path to the disk of this particular pic.
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            //get only the path row from the query.
            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            //locate the path.
            cursor.moveToFirst();
            // get the index of the path.
            int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
            // get a string format of the path.
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.e(TAG, picturePath);
            mPicturePath = picturePath;
            // decode a file path into a bitmap and set the bitmap as the content of the view.
            mImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    // Ask for accessibility of WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE.
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//Can add more as per requirement
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }
    }


    /**
     * Upload image
     */
    private void uploadImage(final String mPicturePath, final String eventId) {
        Uri file = Uri.fromFile(new File(mPicturePath));
        StorageReference imgRef = storageRef.child("images/" + file.getLastPathSegment()/* + "_" + System.currentTimeMillis() */);
        UploadTask uploadTask = imgRef.putFile(file);

//        //new added code
//        imgRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                //Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Log.i(TAG, "upload successfully");
//                database.child("events").child(eventId).child("imgUri").setValue(uri.toString());
//            }
//        });

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "upload successfully");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.child("events").child(eventId).child("imgUri").setValue(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        });
    }
}
