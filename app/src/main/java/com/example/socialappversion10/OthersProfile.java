package com.example.socialappversion10;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OthersProfile extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ImageView covertv;
    CircleImageView avatartv;
    TextView nam, email, phone;
    RecyclerView postrecycle;
    StorageReference storageReference;
    FloatingActionButton fab;
    List<ModelPost> posts;
    AdapterPosts adapterPosts;
    String uid,puid,pname,pemail;
    ProgressDialog pd;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        //This is for the user's uid
        puid = getIntent().getStringExtra("uid");
        //This is for the registerd user, not for the user profile i want to visit
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");


        //Initializing Buttons
        fab = findViewById(R.id.fab);
        avatartv = findViewById(R.id.avatartv);
        nam = findViewById(R.id.nametv);
        email =findViewById(R.id.emailtv);
        //No need right now for this uid
        //uid = FirebaseAuth.getInstance().getUid();

        //Handling the Recycler View
        postrecycle = findViewById(R.id.recyclerposts);

        posts = new ArrayList<>();
        pd = new ProgressDialog(OthersProfile.this);

        pd.setCanceledOnTouchOutside(false);

        // Retrieving user data from firebase
        Query query = databaseReference.orderByChild("uid").equalTo(puid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d1: snapshot.getChildren()){

                    pname=""+d1.child("name").getValue();
                    pemail=""+d1.child("email").getValue();
                    String pimage=""+d1.child("image").getValue();

                    nam.setText(pname);
                    email.setText(pemail);
                    loadMyPosts(pemail);

                    try {
                        Glide.with(OthersProfile.this).load(pimage).into(avatartv);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Doing The Chat
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OthersProfile.this,ChatActivity.class);
                intent.putExtra("uid",puid);
                startActivity(intent);
            }
        });



    }

    private void loadMyPosts(String pemail) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(OthersProfile.this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postrecycle.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = databaseReference.orderByChild("uemail").equalTo(pemail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelPost modelPost = dataSnapshot1.getValue(ModelPost.class);
                    posts.add(modelPost);
                    adapterPosts = new AdapterPosts(OthersProfile.this, posts);
                    postrecycle.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(OthersProfile.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}