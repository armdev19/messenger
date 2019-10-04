package com.infernal93.messengerdemo.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infernal93.messengerdemo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendGroupMessageButton;
    private EditText userMessageInput;
    private ScrollView groupScrollView;
    private TextView displayTextMessages;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersDatabase, groupNameDatabase, groupMessageKeyDatabase;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        mToolbar = findViewById(R.id.group_chat_bar_layout);
        mToolbar.inflateMenu(R.menu.options_menu);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setTitle(currentGroupName);

        sendGroupMessageButton = findViewById(R.id.send_group_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        groupScrollView = findViewById(R.id.group_chat_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);


        getUserInfo();

        sendGroupMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageInfoToDatabase();

                userMessageInput.setText("");

                groupScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){

                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){

                    displayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo() {
        usersDatabase.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageInfoToDatabase() {

        String message = userMessageInput.getText().toString();
        String messageKEY = groupNameDatabase.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(GroupChatActivity.this, "Please write message", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameDatabase.updateChildren(groupMessageKey);

            groupMessageKeyDatabase = groupNameDatabase.child(messageKEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyDatabase.updateChildren(messageInfoMap);

        }
    }

    private void displayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate =  (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage =  (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName =  (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime =  (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");

            groupScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
