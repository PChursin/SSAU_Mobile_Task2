package ru.ssau.mobile.lab2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ru.ssau.mobile.lab2.models.Meeting;
import ru.ssau.mobile.lab2.models.Member;

/**
 * Created by Pavel on 30.11.2016.
 */

public class MeetingsActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference meetingsRef, membersRef;
//    private ValueEventListener meetingsListener, membersListener;
    private ChildEventListener meetingsCListener, membersCListener;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;

    private RecyclerView recyclerView;
    private RVAdapter rvAdapter;

    private static final String TAG = "MeetingsActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        //app = FirebaseApp.getInstance()
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        meetingsRef = database.getReference("meetings");
        membersRef = database.getReference("members");

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                }
            }
        };

        meetingsCListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (insertMeeting(dataSnapshot))
                    return;
                hideProgressDialog();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Meeting m = dataSnapshot.getValue(Meeting.class);
                ArrayList<Meeting> curList = rvAdapter.getData();
                ArrayList<String> idsList = rvAdapter.getDataIds();
                int pos = idsList.indexOf(dataSnapshot.getKey());
                Log.d(TAG, "New index will be: "+pos);
                curList.get(pos).replace(m);
                rvAdapter.notifyItemChanged(pos);
                hideProgressDialog();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ArrayList<Meeting> curList = rvAdapter.getData();
                ArrayList<String> idsList = rvAdapter.getDataIds();
                int pos = idsList.indexOf(dataSnapshot.getKey());
                if (pos < 0) {
                    Log.w(TAG, "Problem on removing - reloading");
                    reloadTotally();
                } else {
                    curList.remove(pos);
                    idsList.remove(pos);
                    rvAdapter.notifyItemRemoved(pos);
                    hideProgressDialog();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.w(TAG, "Unexpected child move! (meetings)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "On cancelled (meetings): ", databaseError.toException());
                hideProgressDialog();
            }
        };

        membersCListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Member m = dataSnapshot.getValue(Member.class);
                HashMap<String, Member> curMap = rvAdapter.getMembers();
                curMap.put(dataSnapshot.getKey(), m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Member m = dataSnapshot.getValue(Member.class);
                HashMap<String, Member> curMap = rvAdapter.getMembers();
                curMap.put(dataSnapshot.getKey(), m);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                HashMap<String, Member> curMap = rvAdapter.getMembers();
                curMap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.w(TAG, "Unexpected child move! (members)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "On cancelled (members): ", databaseError.toException());
            }
        };

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetingsActivity.this, NewMeetingActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        rvAdapter = new RVAdapter();
        rvAdapter.setContext(this);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private boolean insertMeeting(DataSnapshot dataSnapshot) {
        ArrayList<Meeting> curList = rvAdapter.getData();
        ArrayList<String> idsList = rvAdapter.getDataIds();
        int pos = idsList.indexOf(dataSnapshot.getKey());
        if (pos >= 0) {
            Log.d(TAG, "Skipping duplicate value");
            return true;
        }
        Meeting m = dataSnapshot.getValue(Meeting.class);
        int res = Collections.binarySearch(curList, m);
        pos = (res < 0 ? -1 - res : res);
        Log.d(TAG, "New index must be: "+pos+", binSearch returned: "+res);
        curList.add(pos, m);
        idsList.add(pos, dataSnapshot.getKey());
        rvAdapter.notifyItemInserted(pos);
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
        meetingsRef.addChildEventListener(meetingsCListener);
        membersRef.addChildEventListener(membersCListener);
        reloadTotally();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null)
            auth.removeAuthStateListener(authListener);
        if (membersCListener != null)
            membersRef.removeEventListener(membersCListener);
        if (meetingsCListener != null)
            meetingsRef.removeEventListener(meetingsCListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SharedPreferences.Editor editor = new SharedPreferences.Editor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_meetings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_sign_out:
                auth.signOut();
                break;
            case R.id.action_refresh:
                reloadTotally();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(msg);
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void reloadTotally() {
        Log.d(TAG, "Full reloading started");
        showProgressDialog("Sync in progress...");
        rvAdapter.getData().clear();
        rvAdapter.getDataIds().clear();
        rvAdapter.notifyDataSetChanged();
        meetingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    insertMeeting(data);
                }
                hideProgressDialog();
                Log.d(TAG, "Reloaded!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
