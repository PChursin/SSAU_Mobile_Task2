package ru.ssau.mobile.lab2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ru.ssau.mobile.lab2.models.Meeting;
import ru.ssau.mobile.lab2.models.Member;

/**
 * Created by Pavel on 10.12.2016.
 */

public class NewMeetingActivity extends AppCompatActivity {
    TextView fromDateLabel, fromTimeLabel, toDateLabel, toTimeLabel, membersLabel;
    EditText topicField, summaryField;
    Spinner prioritySpinner;
    SimpleDateFormat dateOnly, timeOnly;
    Calendar dateStart, dateEnd;
    DatePickerDialog.OnDateSetListener startDateListener, endDateListener;
    TimePickerDialog.OnTimeSetListener startTimeListener, endTimeListener;
    Button submitButton;
    ProgressDialog progressDialog;

    FirebaseDatabase db;
    DatabaseReference meetings, members;

    ArrayList<String> chosenMembers = new ArrayList<>(),
            memIds = new ArrayList<>();
    ArrayList<Member> allMembers = new ArrayList<>();

    private final String TAG = "NewMeetingActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting);
        topicField = (EditText) findViewById(R.id.nm_topic);
        summaryField = (EditText) findViewById(R.id.nm_summary);
        fromDateLabel = (TextView) findViewById(R.id.nm_from_date);
        fromTimeLabel = (TextView) findViewById(R.id.nm_from_time);
        toDateLabel = (TextView) findViewById(R.id.nm_to_date);
        toTimeLabel = (TextView) findViewById(R.id.nm_to_time);
        membersLabel = (TextView) findViewById(R.id.nm_members);
        prioritySpinner = (Spinner) findViewById(R.id.nm_spinner);
        submitButton = (Button) findViewById(R.id.nm_submit);

        dateOnly = new SimpleDateFormat("EE, dd MMM yyyy");
        timeOnly = new SimpleDateFormat("HH:mm");

        if (savedInstanceState != null) {
            dateStart = (Calendar) savedInstanceState.getSerializable("dateStart");
            dateEnd = (Calendar) savedInstanceState.getSerializable("dateEnd");
            chosenMembers = savedInstanceState.getStringArrayList("chosenMembers");
            memIds = savedInstanceState.getStringArrayList("memIds");
            membersLabel.setText(savedInstanceState.getString("participants"));
        } else {
            dateStart = Calendar.getInstance();
            dateEnd = Calendar.getInstance();
            dateEnd.add(Calendar.HOUR, 1);
        }

        fromDateLabel.setText(dateOnly.format(dateStart.getTime()));
        fromTimeLabel.setText(timeOnly.format(dateStart.getTime()));
        toDateLabel.setText(dateOnly.format(dateEnd.getTime()));
        toTimeLabel.setText(timeOnly.format(dateEnd.getTime()));

        db = FirebaseDatabase.getInstance();
        meetings = db.getReference("meetings");
        members = db.getReference("members");

        members.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot part : dataSnapshot.getChildren()) {
                    memIds.add(part.getKey());
                    allMembers.add(part.getValue(Member.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Cancelled");
            }
        });

        setUpPickers();

        membersLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(NewMeetingActivity.this);
                builderSingle.setTitle("Select participants:");

                String[] namesArray = new String[allMembers.size()];
                for (int i = 0; i < namesArray.length; i++) {
                    Member m = allMembers.get(i);
                    namesArray[i] = m.getName() + '\n' + m.getPosition();
                }
                //TODO: fill checked from chosenMembers
                final boolean[] checked = new boolean[allMembers.size()];
                for (String id : chosenMembers) {
                    checked[memIds.indexOf(id)] = true;
                }
                final StringBuilder sb = new StringBuilder("Participants: ");

                builderSingle.setMultiChoiceItems(namesArray, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        /*if (b) {
                            cho
                        }*/
                        checked[i] = b;
                    }
                });

                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setPositiveButton(
                        "ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO logic
                                chosenMembers.clear();
                                for (int i = 0; i < checked.length; i++) {
                                    if (checked[i]) {
                                        chosenMembers.add(memIds.get(i));
                                        sb.append(allMembers.get(i).getName()).append(", ");
                                    }
                                }
                                int delPos = sb.lastIndexOf(", ");
                                if (delPos > 0)
                                    sb.delete(delPos, sb.length());
                                else
                                    sb.append("<click here to add>");
                                membersLabel.setText(sb);
                                dialog.dismiss();
                            }
                        });

                builderSingle.show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields())
                    return;
                Meeting m = new Meeting(topicField.getText().toString(), summaryField.getText().toString(),
                        dateStart.getTimeInMillis(), dateEnd.getTimeInMillis(), chosenMembers,
                        prioritySpinner.getSelectedItemPosition()+1);
                meetings.push().setValue(m);
                Toast toast = Toast.makeText(NewMeetingActivity.this, "New meeting \""+m.getSubject()+"\" created!",
                        Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });
    }

    private void setUpPickers() {
        //START DATE PICKER SETUP
        startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                dateStart.set(Calendar.YEAR, i);
                dateStart.set(Calendar.MONTH, i1);
                dateStart.set(Calendar.DAY_OF_MONTH, i2);
                fromDateLabel.setText(dateOnly.format(dateStart.getTime()));
            }
        };

        final DatePickerDialog startDatePicker = new DatePickerDialog(NewMeetingActivity.this, startDateListener,
                dateStart.get(Calendar.YEAR), dateStart.get(Calendar.MONTH), dateStart.get(Calendar.DAY_OF_MONTH));


        fromDateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDatePicker.show();
            }
        });

        //END DATE PICKER SETUP
        endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                dateEnd.set(Calendar.YEAR, i);
                dateEnd.set(Calendar.MONTH, i1);
                dateEnd.set(Calendar.DAY_OF_MONTH, i2);
                toDateLabel.setText(dateOnly.format(dateEnd.getTime()));
            }
        };

        final DatePickerDialog endDatePicker = new DatePickerDialog(NewMeetingActivity.this, endDateListener,
                dateEnd.get(Calendar.YEAR), dateEnd.get(Calendar.MONTH), dateEnd.get(Calendar.DAY_OF_MONTH));


        toDateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDatePicker.show();
            }
        });

        //START TIME PICKER SETUP
        startTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                dateStart.set(Calendar.HOUR_OF_DAY, i);
                dateStart.set(Calendar.MINUTE, i1);
                fromTimeLabel.setText(timeOnly.format(dateStart.getTime()));
            }
        };

        final TimePickerDialog startTimePicker = new TimePickerDialog(NewMeetingActivity.this, startTimeListener,
                dateStart.get(Calendar.HOUR_OF_DAY), dateStart.get(Calendar.MINUTE), true);

        fromTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimePicker.show();
            }
        });

        //START TIME PICKER SETUP
        endTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                dateEnd.set(Calendar.HOUR_OF_DAY, i);
                dateEnd.set(Calendar.MINUTE, i1);
                toTimeLabel.setText(timeOnly.format(dateEnd.getTime()));
            }
        };

        final TimePickerDialog endTimePicker = new TimePickerDialog(NewMeetingActivity.this, endTimeListener,
                dateEnd.get(Calendar.HOUR_OF_DAY), dateEnd.get(Calendar.MINUTE), true);

        toTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimePicker.show();
            }
        });
    }

    private boolean validateFields() {
        EditText[] fields = {topicField, summaryField};
        boolean valid = true;
        for (EditText field : fields) {
            if (field.getText().length() == 0) {
                valid = false;
                field.setError("Required");
            }
        }
        return valid;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("dateStart", dateStart);
        outState.putSerializable("dateEnd", dateEnd);
        outState.putStringArrayList("chosenMembers", chosenMembers);
        outState.putStringArrayList("memIds", memIds);
        outState.putString("participants", membersLabel.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
