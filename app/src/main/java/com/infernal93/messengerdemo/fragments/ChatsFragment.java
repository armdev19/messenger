package com.infernal93.messengerdemo.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infernal93.messengerdemo.models.Contacts;
import com.infernal93.messengerdemo.R;
import com.infernal93.messengerdemo.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatsRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                        final String usersID = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.hasChild("image")) {

                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.ic_profile).into(holder.profileImage);
                                    }


                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state")) {

                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")) {

                                            holder.userStatus.setText("online" );

                                        } else if (state.equals("offline")) {

                                            holder.userStatus.setText("Last Seen: " + "\n" + date + " " + time);
                                        }

                                    } else {

                                        holder.userStatus.setText("offline" );
                                    }



                                    // recycler view onClick
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersID);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userName, userStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);


            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}
