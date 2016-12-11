package ru.ssau.mobile.lab2;

import android.content.Intent;
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

    private FirebaseApp app;
    private FirebaseDatabase database;
    private DatabaseReference meetingsRef, membersRef;
//    private ValueEventListener meetingsListener, membersListener;
    private ChildEventListener meetingsCListener, membersCListener;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FloatingActionButton fab;

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

        /*meetingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot part : dataSnapshot.getChildren()) {
                    Meeting m = part.getValue(Meeting.class);
                    ArrayList<Meeting> curList = rvAdapter.getData();
                    int res = Collections.binarySearch(curList, m);
                    int pos = (res < 0 ? -1 - res : res);
                    Log.d(TAG, "New index must be: "+pos+", binSearch returned: "+res);
                    curList.add(pos, m);
                    rvAdapter.notifyItemInserted(pos);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "On cancelled (meetings): ", databaseError.toException());
            }
        };

        membersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot part : dataSnapshot.getChildren()) {
                    Member m = part.getValue(Member.class);
                    HashMap<String, Member> curMap = rvAdapter.getMembers();
                    curMap.put(part.getKey(), m);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "On cancelled (members): ", databaseError.toException());
            }
        };*/

        meetingsCListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ArrayList<Meeting> curList = rvAdapter.getData();
                ArrayList<String> idsList = rvAdapter.getDataIds();
                int pos = idsList.indexOf(dataSnapshot.getKey());
                if (pos >= 0) {
                    Log.d(TAG, "Skipping duplicate value");
                    return;
                }
                Meeting m = dataSnapshot.getValue(Meeting.class);
                int res = Collections.binarySearch(curList, m);
                pos = (res < 0 ? -1 - res : res);
                Log.d(TAG, "New index must be: "+pos+", binSearch returned: "+res);
                curList.add(pos, m);
                idsList.add(pos, dataSnapshot.getKey());
                rvAdapter.notifyItemInserted(pos);
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
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ArrayList<Meeting> curList = rvAdapter.getData();
                ArrayList<String> idsList = rvAdapter.getDataIds();
                int pos = idsList.indexOf(dataSnapshot.getKey());
                curList.remove(pos);
                idsList.remove(pos);
                rvAdapter.notifyItemRemoved(pos);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.w(TAG, "Unexpected child move! (meetings)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "On cancelled (meetings): ", databaseError.toException());
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
        /*//test
        ArrayList<String> memIds = new ArrayList<>();
        ArrayList<String> meetIds = new ArrayList<>();
        meetIds.add("1");meetIds.add("2");meetIds.add("3");
        memIds.add("1");
        ArrayList<Meeting> meetings = new ArrayList<>();
        Meeting m1 = new Meeting("TtT!", "SUPER summary!", 1481285636000L, 1481285800000L,
                memIds, 1);
        meetings.add(m1);
        memIds.add("2");
        Meeting m2 = new Meeting("New Meeting!", "another SUPER summary!", 1481285740000L, 1481289990000L,
                memIds, 1);
        meetings.add(m2);
        m1 = new Meeting("TtT! v.2", "SUPER v.2 summary!", 1481287036000L, 1481287500000L,
                memIds, 1);
        meetings.add(m1);
        HashMap<String, Member> members = new HashMap<>();
        members.put("1", new Member("Johny Cage", "Actor"));
        members.put("2", new Member("SeKtor", "Robot"));
        rvAdapter.setData(meetings);
        rvAdapter.setMembers(members);
        rvAdapter.setDataIds(meetIds);
        //end*/
//        rvAdapter.setContext(getApplicationContext());
        rvAdapter.setContext(this);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
//        meetingsRef.addValueEventListener(meetingsListener);
//        membersRef.addValueEventListener(membersListener);
        meetingsRef.addChildEventListener(meetingsCListener);
        membersRef.addChildEventListener(membersCListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
//        if (membersListener != null)
//            membersRef.removeEventListener(membersListener);
//        if (meetingsListener != null)
//            meetingsRef.removeEventListener(meetingsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
