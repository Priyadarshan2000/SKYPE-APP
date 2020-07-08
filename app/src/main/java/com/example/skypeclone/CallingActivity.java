package com.example.skypeclone;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private static final String TAG = "CallingActivity";
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;

    private String receiverUserId ="", receiverUserImage ="", receiverUserName ="";
    private String senderUserId ="", senderUserImage ="", senderUserName ="", checker ="";
    private String callingID ="", ringingID = "";

    private MediaPlayer mediaPlayer;

    private DatabaseReference userRef;
//    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                final HashMap<String, Object> callingPickUpMap = new HashMap<>();

                callingPickUpMap.put("picked", "picked");
                userRef.child(senderUserId).child("ringing").updateChildren(callingPickUpMap).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()){
                                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                );
            }
        });

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                checker = "clicked";

                cancelCallingUser();
            }
        });

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUserId).exists()){
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);

                }else {
                    if (dataSnapshot.child(senderUserId).exists()){
                        senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                        senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();

//                        nameContact.setText(senderUserName);
//                        Picasso.get().load(senderUserImagee).placeholder(R.drawable.profile_image).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();

        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("calling") && !dataSnapshot.hasChild("ringing")){

                    final HashMap<String, Object> callingInfo = new HashMap<>();

                    callingInfo.put("caling", receiverUserId);

                    userRef.child(senderUserId).child("calling").updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        final HashMap<String, Object> ringingInfo = new HashMap<>();

                                        ringingInfo.put("rigging", senderUserId);

                                        userRef.child(receiverUserId).child("ringing")
                                                .updateChildren(ringingInfo);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("ringing") && !dataSnapshot.child(senderUserId).hasChild("calling")){
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserId).child("ringing").hasChild("picked")){

                    mediaPlayer.stop();

                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void cancelCallingUser(){

//        cancel call from sender side

        userRef.child(senderUserId)
                .child("calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("caling")){

                            callingID = dataSnapshot.child("caling").getValue().toString();

                            userRef.child(callingID)
                                    .child("ringing").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                userRef.child(senderUserId)
                                                        .child("calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }else {
                            startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        

        userRef.child(senderUserId)
                .child("ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("rigging")){

                            ringingID = dataSnapshot.child("rigging").getValue().toString();

                            userRef.child(ringingID)
                                    .child("calling").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                userRef.child(senderUserId)
                                                        .child("ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                                                                                             finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }else {
                            startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}