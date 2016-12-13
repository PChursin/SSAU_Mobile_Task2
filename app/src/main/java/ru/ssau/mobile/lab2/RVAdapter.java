package ru.ssau.mobile.lab2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ru.ssau.mobile.lab2.models.Meeting;
import ru.ssau.mobile.lab2.models.Member;

/**
 * Created by Pavel on 04.12.2016.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>
        implements View.OnClickListener, Filterable {

    private int expandedPosition = -1;
    private ValueFilter valueFilter;
    private ArrayList<Meeting> dataShown = new ArrayList<>();
    private ArrayList<Meeting> dataStored = new ArrayList<>();
    private ArrayList<String> dataIdsShown = new ArrayList<>();
    private ArrayList<String> dataIdsStored = new ArrayList<>();
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
    public void onBindViewHolder(final RVAdapter.ViewHolder holder, int position) {
        Meeting m = dataShown.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //sdf.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
        holder.topicLabel.setText(m.getSubject());
        holder.startDateLabel.setText("Starts at: "+sdf.format(new Date(m.getStartTime())));
        holder.endDateLabel.setText("Ends at: "+sdf.format(new Date(m.getEndTime())));
        holder.summaryLabel.setText(m.getSummary());
        holder.takePartButton.setImageResource(R.drawable.ic_take_part);
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
                    //                holder.takePartButton.setBackgroundResource(R.drawable.ic_part);
                    holder.takePartButton.setImageResource(R.drawable.ic_part);
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

        holder.takePartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("meetings");
                List<String> participants = dataShown.get(holder.getAdapterPosition()).getMembers();
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
                ref.child(dataIdsShown.get(holder.getAdapterPosition())).child("members").setValue(participants);
//                ref.child(dataIdsShown.get(position)).child("members").
            }
        });

        holder.sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri= "smsto:";
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
                Meeting meeting = dataShown.get(holder.getAdapterPosition());
                intent.putExtra("sms_body", "Meeting: "+meeting.getSubject()+
                        "\nSummary: "+meeting.getSummary());
                intent.putExtra("compose_mode", true);
                context.startActivity(intent);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference meetingsRef = FirebaseDatabase.getInstance().getReference("meetings");
                int pos = holder.getAdapterPosition();
                meetingsRef.child(dataIdsShown.get(pos)).removeValue();
                expandedPosition = -1;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (dataShown == null) {
            Log.w(TAG, "dataShown is null o_O");
            dataShown = new ArrayList<>();
        }
        return dataShown.size();
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        //String theString = dataShown.get(holder.getPosition());
        int pos = holder.getAdapterPosition();
        // Check for an expanded view, collapse if you find one
        if (expandedPosition >= 0 && expandedPosition != pos) {
            //int prev = expandedPosition;
            notifyItemChanged(expandedPosition);
        }
        // Set the current position to "expanded"
        expandedPosition = (expandedPosition == pos ? -1 : pos);
        notifyItemChanged(pos);

        //Toast.makeText(context, "Clicked: "+dataShown.get(holder.getAdapterPosition()).getSubject(), Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Meeting> getData() {
        //return dataShown;
        return dataStored;
    }

    public void syncData() {
        dataShown = dataStored;
        dataIdsShown = dataIdsStored;
    }

    public void setData(ArrayList<Meeting> data) {
        this.dataShown = data;
        dataStored = data;
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
        //return dataIdsShown;
        return dataIdsStored;
    }

    public void setDataIds(ArrayList<String> dataIds) {
        this.dataIdsShown = dataIds;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicLabel;
        TextView startDateLabel;
        TextView endDateLabel;
        TextView summaryLabel;
        TextView membersLabel;
        ImageButton takePartButton;
        ImageButton sendSmsButton;
        ImageButton deleteButton;
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
            sendSmsButton = (ImageButton) itemView.findViewById(R.id.button_send_sms);
            takePartButton = (ImageButton) itemView.findViewById(R.id.button_take_part);
            deleteButton = (ImageButton) itemView.findViewById(R.id.button_delete);
        }
    }

    private class ValueFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            dataIdsShown = new ArrayList<>();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<Meeting> filterList = new ArrayList<>();
                for (int i = 0; i < dataStored.size(); i++) {
                    if ((dataStored.get(i).getSummary().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(dataStored.get(i));
                        dataIdsShown.add(dataIdsStored.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = dataStored.size();
                results.values = dataStored;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            dataShown = (ArrayList<Meeting>) results.values;
            notifyDataSetChanged();
        }
    }
}
