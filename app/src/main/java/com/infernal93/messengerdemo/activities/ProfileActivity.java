package com.infernal93.messengerdemo.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infernal93.messengerdemo.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, currentState;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestBtn, declineMessageRequestBtn;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        senderUserID = firebaseAuth.getCurrentUser().getUid();


        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestBtn = findViewById(R.id.send_message_request_button);
        declineMessageRequestBtn = findViewById(R.id.decline_message_request_button);
        currentState = "new";

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.ic_profile).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    manageChatRequests();

                } else {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID)) {

                            String requestType = dataSnapshot.child(receiverUserID).child("requestType").getValue().toString();

                            if (requestType.equals("sent")) {

                                currentState = "requestSent";
                                sendMessageRequestBtn.setText("Cancel Chat Request");
                            } else if (requestType.equals("received")) {

                                currentState = "requestReceived";
                                sendMessageRequestBtn.setText("Accept Chat Request");

                                declineMessageRequestBtn.setVisibility(View.VISIBLE);
                                declineMessageRequestBtn.setEnabled(true);

                                declineMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        cancelChatRequest();
                                    }
                                });
                            }
                        } else {

                            contactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(receiverUserID)) {

                                                currentState = "friends";
                                                sendMessageRequestBtn.setText("Remove This Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)) {

            sendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sendMessageRequestBtn.setEnabled(false);

                    if (currentState.equals("new")) {

                        sendChatRequest();
                    }

                    if (currentState.equals("requestSent")) {

                        cancelChatRequest();
                    }

                    if (currentState.equals("requestReceived")) {

                        acceptChatRequest();
                    }

                    if (currentState.equals("friends")) {

                        removeContact();
                    }
                }
            });

        } else {

            sendMessageRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact() {

        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestBtn.setText("Send Message");

                                                declineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                declineMessageRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void acceptChatRequest() {

        contactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    sendMessageRequestBtn.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    sendMessageRequestBtn.setText("Remove this Contact");

                                                                                    declineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestBtn.setEnabled(false);
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

    private void cancelChatRequest() {

        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestBtn.setText("Send Message");

                                                declineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                declineMessageRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("requestType").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("requestType").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                // notification
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(senderUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    sendMessageRequestBtn.setEnabled(true);
                                                                    currentState = "requestSent";
                                                                    sendMessageRequestBtn.setText("Cancel Chat Request");
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