package com.example.socialappversion10;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class PostStatus extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE = 1;
    private static final int REQUEST_CODE_VIDEO = 2;

    private EditText etStatusText;
    private ImageButton btnChooseImage;
    private ImageButton btnChooseVideo;
    private ImageView ivStatusImage;
    private VideoView vvStatusVideo;
    Bitmap mediaBitmap;
    Boolean isVideo;
    private FloatingActionButton btnPostStatus,btnShowStatus;

    private Uri mediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status);

        // Find views by ID
        etStatusText = findViewById(R.id.et_status_text);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        btnChooseVideo = findViewById(R.id.btn_choose_video);
        ivStatusImage = findViewById(R.id.iv_status_image);
        vvStatusVideo = findViewById(R.id.vv_status_video);
        btnPostStatus = findViewById(R.id.btn_post_status);
        btnShowStatus=findViewById(R.id.show_status);

        // Set click listeners for media buttons
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_IMAGE);
        });

        btnChooseVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_CODE_VIDEO);
        });

        // Set click listener for post status button
        btnPostStatus.setOnClickListener(v -> {
            postStatus(mediaBitmap, isVideo);
        });

        btnShowStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PostStatus.this,Show_status.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void postStatus(Bitmap mediaBitmap, Boolean isVideo) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        // Generate a unique file name for the media
        String mediaFileName = UUID.randomUUID().toString();

        // Get a reference to the Firebase storage location where the media will be saved
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("media").child(mediaFileName);

        // Compress the media and convert it to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (isVideo) {
            // If it's a video, get the byte array from the video file
            try {
                InputStream inputStream = getContentResolver().openInputStream(mediaUri);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                baos.flush();
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error reading video file: " + e.getMessage());
                progressDialog.dismiss();
                return;
            }
        } else {
            // If it's an image, compress the bitmap and get the byte array
            mediaBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        }
        byte[] mediaBytes = baos.toByteArray();

        // Upload the media to Firebase storage
        UploadTask uploadTask = storageRef.putBytes(mediaBytes);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Media upload successful, get the download URL
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Create a new status object with the media URL
                Status status = new Status();
                status.setMediaUrl(uri.toString());
                status.setIsVideo(isVideo);
                status.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                status.setTimestamp(System.currentTimeMillis());

                // Save the status to Firebase database
                DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference().child("statuses").push();
                statusRef.setValue(status);

                // Schedule the deletion of the media after 24 hours
                scheduleDeletion(storageRef, System.currentTimeMillis() +60*1000);

                // Dismiss progress dialog
                progressDialog.dismiss();

                // Show a success message
                Toast.makeText(this, "Status posted successfully", Toast.LENGTH_SHORT).show();

                // Close the activity
                finish();
            }).addOnFailureListener(e -> {
                // Error getting download URL
                Log.e(TAG, "Error getting media download URL: " + e.getMessage());
                progressDialog.dismiss();
            });
        }).addOnFailureListener(e -> {
            // Media upload failed
            Log.e(TAG, "Error uploading media: " + e.getMessage());
            progressDialog.dismiss();
        });
    }
    private void scheduleDeletion(StorageReference storageRef, long deletionTime) {
        final Handler handler = new Handler();
        Runnable deleteStatusRunnable = () -> {
            // Delete the status from Firebase Storage
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Status deleted from Firebase Storage");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error deleting status from Firebase Storage: " + e.getMessage());
                }
            });
        };
        handler.postDelayed(deleteStatusRunnable, deletionTime - System.currentTimeMillis());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE || requestCode == REQUEST_CODE_VIDEO) {
                mediaUri = data.getData();

                mediaBitmap = null;

                isVideo=Boolean.FALSE;

                if (requestCode == REQUEST_CODE_IMAGE) {
                    ivStatusImage.setVisibility(View.VISIBLE);
                    vvStatusVideo.setVisibility(View.GONE);
                    ivStatusImage.setImageURI(mediaUri);

                    try {
                        mediaBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mediaUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (requestCode == REQUEST_CODE_VIDEO) {
                    ivStatusImage.setVisibility(View.GONE);
                    vvStatusVideo.setVisibility(View.VISIBLE);
                    vvStatusVideo.setVideoURI(mediaUri);
                    vvStatusVideo.start();

                    isVideo=Boolean.TRUE;
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(PostStatus.this, mediaUri);
                    mediaBitmap = retriever.getFrameAtTime();

                }
                postStatus(mediaBitmap,isVideo);

            }
        }
    }
}
