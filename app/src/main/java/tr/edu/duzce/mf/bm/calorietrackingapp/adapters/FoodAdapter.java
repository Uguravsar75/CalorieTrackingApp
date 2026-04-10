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
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private final Context context;
    private List<Food> foods;
    private OnFoodClickListener clickListener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(Context context, List<Food> foods) {
        this.context = context;
        this.foods = foods;
    }

    public void setClickListener(OnFoodClickListener listener) {
        this.clickListener = listener;
    }

    public void updateData(List<Food> newFoods) {
        this.foods = newFoods;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);

        // Sistem diline göre Türkçe veya İngilizce ad göster
        String lang = context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        String name = "tr".equals(lang) ? food.getNameTr() : food.getNameEn();
        holder.tvFoodName.setText(name);
        holder.tvCalorieInfo.setText(food.getCaloriesPer100g() + " kcal/100g");
        holder.tvCategory.setText(getCategoryLabel(food.getCategory()));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onFoodClick(food);
        });
    }

    @Override
    public int getItemCount() {
        return foods != null ? foods.size() : 0;
    }

    private String getCategoryLabel(String category) {
        switch (category) {
            case "BREAKFAST": return context.getString(R.string.category_breakfast);
            case "LUNCH":     return context.getString(R.string.category_lunch);
            case "DINNER":    return context.getString(R.string.category_dinner);
            default:          return context.getString(R.string.category_snack);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalorieInfo, tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCalorieInfo = itemView.findViewById(R.id.tvCalorieInfo);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
