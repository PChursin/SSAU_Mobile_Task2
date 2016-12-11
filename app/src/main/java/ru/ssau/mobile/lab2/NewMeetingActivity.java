package ru.ssau.mobile.lab2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ru.ssau.mobile.lab2.models.Meeting;

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

    FirebaseDatabase db;
    DatabaseReference meetings, members;

    ArrayList<String> chosenMembers;

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

        dateStart = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateEnd.add(Calendar.HOUR, 1);

        fromDateLabel.setText(dateOnly.format(dateStart.getTime()));
        fromTimeLabel.setText(timeOnly.format(dateStart.getTime()));
        toDateLabel.setText(dateOnly.format(dateEnd.getTime()));
        toTimeLabel.setText(timeOnly.format(dateEnd.getTime()));

        db = FirebaseDatabase.getInstance();
        meetings = db.getReference("meetings");
        members = db.getReference("members");

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

        final DatePickerDialog endDatePicker = new DatePickerDialog(NewMeetingActivity.this, startDateListener,
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

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields())
                    return;
                Meeting m = new Meeting(topicField.toString(), summaryField.toString(), dateStart.getTimeInMillis(),
                        dateEnd.getTimeInMillis(), chosenMembers, prioritySpinner.getSelectedItemPosition()+1);
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
}
