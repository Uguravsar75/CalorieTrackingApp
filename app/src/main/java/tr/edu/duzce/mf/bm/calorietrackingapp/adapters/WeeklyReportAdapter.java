package tr.edu.duzce.mf.bm.calorietrackingapp.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        if (!item.hasData) {
            holder.tvReportCalories.setText(context.getString(R.string.label_report_no_data));
            holder.tvReportStatus.setText("—");
            setStatusColor(holder.tvReportStatus, R.color.report_grey);
            return;
        }

        holder.tvReportCalories.setText(item.consumed + " / " + item.goal + " kcal");

        if (item.consumed <= item.goal) {
            holder.tvReportStatus.setText(context.getString(R.string.label_report_goal_met));
            setStatusColor(holder.tvReportStatus, R.color.report_green);
        } else {
            holder.tvReportStatus.setText(context.getString(R.string.label_report_goal_exceeded));
            setStatusColor(holder.tvReportStatus, R.color.report_red);
        }
    }

    private void setStatusColor(TextView tv, int colorResId) {
        GradientDrawable bg = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.bg_report_status);
        if (bg != null) {
            bg = (GradientDrawable) bg.mutate();
            bg.setColor(ContextCompat.getColor(context, colorResId));
            tv.setBackground(bg);
        }
        tv.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportDate, tvReportCalories, tvReportStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportCalories = itemView.findViewById(R.id.tvReportCalories);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
        }
    }
}
