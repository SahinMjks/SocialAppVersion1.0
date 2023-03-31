package com.example.socialappversion10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Show_status extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StatusAdapter mAdapter;
    List<Status> statusList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);

        // Initialize RecyclerView and adapter
        mRecyclerView = findViewById(R.id.recyclep);
        mAdapter = new StatusAdapter(Show_status.this,statusList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        // Retrieve statuses from Firebase database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("statuses");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Status> statusList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Status status = dataSnapshot.getValue(Status.class);
                    statusList.add(status);
                }
                mAdapter.setStatusList(statusList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Show_status.this, "Error retrieving statuses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
