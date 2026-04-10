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
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;

public class MealEntryAdapter extends RecyclerView.Adapter<MealEntryAdapter.ViewHolder> {

    private final Context context;
    private List<MealEntry> entries;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(MealEntry entry);
    }

    public MealEntryAdapter(Context context, List<MealEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    public void setLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateData(List<MealEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealEntry entry = entries.get(position);
        holder.tvFoodName.setText(entry.getFoodName());
        holder.tvCalories.setText(entry.getTotalCalories() + " kcal");

        String mealLabel = getMealLabel(entry.getMealType());
        holder.tvMealInfo.setText(mealLabel + " • " + (int) entry.getAmountGrams() + "g");

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    private String getMealLabel(String mealType) {
        switch (mealType) {
            case "BREAKFAST": return context.getString(R.string.label_meal_breakfast);
            case "LUNCH":     return context.getString(R.string.label_meal_lunch);
            case "DINNER":    return context.getString(R.string.label_meal_dinner);
            default:          return context.getString(R.string.label_meal_snack);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories, tvMealInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvMealInfo = itemView.findViewById(R.id.tvMealInfo);
        }
    }
}
