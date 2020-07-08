package com.example.skypeclone;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID = "", receiveruserImage="",receiverUserName="";
    private ImageView background_profile_image;
    private TextView name_profile;
    private Button decline_friend_request,add_friend;
    private FirebaseAuth mAuth;
    private String SenderUserID;
    private String currentState = "new";
    private DatabaseReference friendRequestRef,contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        SenderUserID = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        receiveruserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();

        background_profile_image = findViewById(R.id.profile_details_image);
        name_profile = findViewById(R.id.name_profile);
        add_friend = findViewById(R.id.add_friend);
        decline_friend_request = findViewById(R.id.decline_friend_request);


        Picasso.get().load(receiveruserImage).into(background_profile_image);
        name_profile.setText(receiverUserName);

        manageClickEvents();
    }

    private void manageClickEvents() {
        friendRequestRef.child(SenderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID)){
                            String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if(requestType.equals("sent")){
                                currentState = "request_sent";
                                add_friend.setText("Cancel Friend Request");
                            }else if(requestType.equals("received")){
                                currentState = "request_received";
                                add_friend.setText("Accept Friend Request");

                                decline_friend_request.setVisibility(View.VISIBLE);
                                decline_friend_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }else{
                                contactsRef.child(SenderUserID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(receiverUserID)){
                                                    currentState = "friends";
                                                    add_friend.setText("Delete Contact");
                                                }else{
                                                    currentState = "new";
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        if(SenderUserID.equals(receiverUserID)){
            add_friend.setVisibility(View.INVISIBLE);
        }else{
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentState.equals("new")){
                        sendFriendRequest();

                    }else if(currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }else if(currentState.equals("request_received")){
                        AcceptFriendRequest();

                    }else if(currentState.equals("request_decline")){
                        CancelFriendRequest();
                    }
                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(SenderUserID).child(receiverUserID)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(SenderUserID)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                friendRequestRef.child(SenderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUserID).child(SenderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        currentState = "friends";
                                                                                        add_friend.setText("Delete Contact");
                                                                                        decline_friend_request.setVisibility(View.INVISIBLE);

                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(SenderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(SenderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "new";
                                                add_friend.setText("Add Friend");

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(SenderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(SenderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "request_sent";
                                                add_friend.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}