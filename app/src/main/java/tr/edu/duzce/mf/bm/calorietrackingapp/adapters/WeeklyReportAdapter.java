package tr.edu.duzce.mf.bm.calorietrackingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;

public class WeeklyReportAdapter extends RecyclerView.Adapter<WeeklyReportAdapter.ViewHolder> {

    public static class ReportItem {
        public String date;
        public int consumed;
        public int goal;
        public boolean hasData;
        public boolean adherent;
        public int streakCount;
    }

    private final Context context;
    private List<ReportItem> items;

    public WeeklyReportAdapter(Context context, List<ReportItem> items) {
        this.context = context;
        this.items = items;
    }

    public void updateData(List<ReportItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weekly_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportItem item = items.get(position);

        holder.tvReportDate.setText(DateUtils.toDisplayFormat(item.date));
        holder.tvReportGoal.setText(String.valueOf(item.goal));

        if (!item.hasData) {
            holder.tvReportConsumed.setText(context.getString(R.string.label_report_no_data));
            holder.tvReportStatus.setText(context.getString(R.string.report_status_empty));
            holder.tvReportStreak.setText(context.getString(R.string.report_streak_empty));
            return;
        }

        holder.tvReportConsumed.setText(String.valueOf(item.consumed));
        holder.tvReportStreak.setText(item.adherent
                ? repeatFire(item.streakCount)
                : context.getString(R.string.report_streak_break));

        if (item.consumed <= item.goal * 0.9f) {
            holder.tvReportStatus.setText(context.getString(R.string.label_report_goal_met));
        } else if (item.consumed <= item.goal) {
            holder.tvReportStatus.setText(context.getString(R.string.label_report_goal_close));
        } else {
            holder.tvReportStatus.setText(context.getString(R.string.label_report_goal_exceeded));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String repeatFire(int streakCount) {
        int count = Math.max(1, Math.min(streakCount, 5));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("🔥");
        }
        return builder.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportDate, tvReportConsumed, tvReportGoal, tvReportStatus, tvReportStreak;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportConsumed = itemView.findViewById(R.id.tvReportConsumed);
            tvReportGoal = itemView.findViewById(R.id.tvReportGoal);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
            tvReportStreak = itemView.findViewById(R.id.tvReportStreak);
        }
    }
}
