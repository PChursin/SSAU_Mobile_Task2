package ru.ssau.mobile.lab2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.ssau.mobile.lab2.models.Meeting;
import ru.ssau.mobile.lab2.models.Member;

/**
 * Created by Pavel on 04.12.2016.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>
        implements View.OnClickListener {

    private int expandedPosition = -1;
    private ArrayList<Meeting> data = new ArrayList<>();
    private ArrayList<String> dataIds = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();
    private Context context;

    private final String TAG = "RVAdapter";

    @Override
    public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        ViewHolder holder = new ViewHolder(v);

        // Sets the click adapter for the entire cell
        // to the one in this class.
        holder.itemView.setOnClickListener(RVAdapter.this);
        holder.itemView.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(RVAdapter.ViewHolder holder, final int position) {
        Meeting m = data.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //sdf.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
        holder.topicLabel.setText(m.getSubject());
        holder.startDateLabel.setText("Starts at: "+sdf.format(new Date(m.getStartTime())));
        holder.endDateLabel.setText("Ends at: "+sdf.format(new Date(m.getEndTime())));
        holder.summaryLabel.setText(m.getSummary());
        holder.takePart.setImageResource(R.drawable.ic_take_part);
        switch (m.getPriority()) {
            case 1:
                holder.priorLine.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPriorAsap));
                break;
            case 2:
                holder.priorLine.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPriorPlan));
                break;
            default:
                holder.priorLine.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPriorLow));
                break;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Participants: ");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (m.getMembers() != null) {
            for (String memId : m.getMembers()) {
                if (memId.equals(uid)) {
                    sb.append("You, ");
                    //                holder.takePart.setBackgroundResource(R.drawable.ic_part);
                    holder.takePart.setImageResource(R.drawable.ic_part);
                } else {
                    Member member = members.get(memId);
                    if (member != null)
                        sb.append(member.getName()).append(", ");
                    else
                        sb.append("=(, ");
                }
            }
        }
        int delPos = sb.lastIndexOf(", ");
        if (delPos > 0)
            sb.delete(delPos, sb.length());
        else
            sb.append("there is nobody yet...");
        holder.membersLabel.setText(sb);
        if (position == expandedPosition) {
            holder.expandArea.setVisibility(View.VISIBLE);
        } else {
            holder.expandArea.setVisibility(View.GONE);
        }

        holder.takePart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("meetings");
                List<String> participants = data.get(position).getMembers();
                if (participants == null)
                    participants = new ArrayList<String>();
                int pos = participants.indexOf(uid);
                if (pos < 0) {
                    participants.add(uid);
                    Log.d(TAG, "Trying to take part...");
                } else {
                    participants.remove(uid);
                    Log.d(TAG, "Trying to leave...");
                }
                ref.child(dataIds.get(position)).child("members").setValue(participants);
//                ref.child(dataIds.get(position)).child("members").
            }
        });

        holder.sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri= "smsto:";
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
                Meeting meeting = data.get(position);
                intent.putExtra("sms_body", "Meeting: "+meeting.getSubject()+
                        "\nSummary: "+meeting.getSummary());
                intent.putExtra("compose_mode", true);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        //String theString = data.get(holder.getPosition());
        int pos = holder.getAdapterPosition();
        // Check for an expanded view, collapse if you find one
        if (expandedPosition >= 0 && expandedPosition != pos) {
            //int prev = expandedPosition;
            notifyItemChanged(expandedPosition);
        }
        // Set the current position to "expanded"
        expandedPosition = (expandedPosition == pos ? -1 : pos);
        notifyItemChanged(pos);

        //Toast.makeText(context, "Clicked: "+data.get(holder.getAdapterPosition()).getSubject(), Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Meeting> getData() {
        return data;
    }

    public void setData(ArrayList<Meeting> data) {
        this.data = data;
    }

    public HashMap<String, Member> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Member> members) {
        this.members = members;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<String> getDataIds() {
        return dataIds;
    }

    public void setDataIds(ArrayList<String> dataIds) {
        this.dataIds = dataIds;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicLabel;
        TextView startDateLabel;
        TextView endDateLabel;
        TextView summaryLabel;
        TextView membersLabel;
        ImageButton takePart;
        ImageButton sendSms;
        LinearLayout expandArea;
        FrameLayout priorLine;

        public ViewHolder(View itemView) {
            super(itemView);

            topicLabel = (TextView) itemView.findViewById(R.id.label_meeting_name);
            startDateLabel = (TextView) itemView.findViewById(R.id.label_meeting_start_date);
            endDateLabel = (TextView) itemView.findViewById(R.id.label_meeting_end_date);
            summaryLabel = (TextView) itemView.findViewById(R.id.label_meeting_summary);
            membersLabel = (TextView) itemView.findViewById(R.id.label_meeting_members);
            expandArea = (LinearLayout) itemView.findViewById(R.id.expandable_part);
            priorLine = (FrameLayout) itemView.findViewById(R.id.prior_line);
            sendSms = (ImageButton) itemView.findViewById(R.id.button_send_sms);
            takePart = (ImageButton) itemView.findViewById(R.id.button_take_part);
        }
    }
}
