package tr.edu.duzce.mf.bm.calorietrackingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.MealEntry;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.FoodImageProvider;

/**
 * MealEntryAdapter: Bugünkü öğün kayıtlarını listeler.
 * Yemek fotoğraflarını Glide ile yükler:
 *   1. MealEntry.imageUrl → lokal yol (kamera) veya web URL
 *   2. FoodImageProvider → yemek adına göre Wikimedia resmi
 *   3. Placeholder → öğün tipine göre emoji
 */
public class MealEntryAdapter extends RecyclerView.Adapter<MealEntryAdapter.ViewHolder> {

    private final Context context;
    private List<MealEntry> entries;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(MealEntry entry);
    }

    public MealEntryAdapter(Context context, List<MealEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    public void setClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
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
        String gramText = entry.getAmountGrams() > 0
                ? mealLabel + " • " + (int) entry.getAmountGrams() + "g"
                : mealLabel;
        holder.tvMealInfo.setText(gramText);

        int protein = Math.max(0, entry.getProteinGrams());
        int carb = Math.max(0, entry.getCarbGrams());
        int fat = Math.max(0, entry.getFatGrams());
        if (protein > 0 || carb > 0 || fat > 0 || entry.isAiEstimated()) {
            String macroText = context.getString(R.string.meal_macro_summary_format, protein, carb, fat);
            if (entry.isAiEstimated()) {
                macroText += " • " + context.getString(R.string.meal_ai_estimated);
            }
            holder.tvMealMacroInfo.setText(macroText);
            holder.tvMealMacroInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvMealMacroInfo.setVisibility(View.GONE);
        }

        // Fotoğraf yükleme — öncelik sırası:
        // 1. Kayıtlı imageUrl (lokal ya da web)
        // 2. FoodImageProvider (yemek adına göre)
        // 3. Emoji placeholder
        String imageUrl = entry.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = FoodImageProvider.getUrl(entry.getFoodName());
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(R.drawable.bg_avatar)
                    .error(R.drawable.bg_avatar)
                    .centerCrop()
                    .into(holder.ivFoodPhoto);
        } else {
            // Emoji placeholder: öğün tipine göre farklı renk/emoji
            holder.ivFoodPhoto.setImageDrawable(null);
            holder.ivFoodPhoto.setBackgroundColor(getMealColor(entry.getMealType()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(entry);
        });
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    private String getMealLabel(String mealType) {
        if (mealType == null) return context.getString(R.string.label_meal_snack);
        switch (mealType) {
            case "BREAKFAST": return context.getString(R.string.label_meal_breakfast);
            case "LUNCH":     return context.getString(R.string.label_meal_lunch);
            case "DINNER":    return context.getString(R.string.label_meal_dinner);
            default:          return context.getString(R.string.label_meal_snack);
        }
    }

    /** Öğün tipine göre placeholder arka plan rengi */
    private int getMealColor(String mealType) {
        if (mealType == null) return 0xFF9E9E9E;
        switch (mealType) {
            case "BREAKFAST": return 0xFFFFB300; // amber
            case "LUNCH":     return 0xFF43A047; // green
            case "DINNER":    return 0xFF1E88E5; // blue
            default:          return 0xFF8E24AA; // purple (snack)
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories, tvMealInfo, tvMealMacroInfo;
        ShapeableImageView ivFoodPhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName  = itemView.findViewById(R.id.tvFoodName);
            tvCalories  = itemView.findViewById(R.id.tvCalories);
            tvMealInfo  = itemView.findViewById(R.id.tvMealInfo);
            tvMealMacroInfo = itemView.findViewById(R.id.tvMealMacroInfo);
            ivFoodPhoto = itemView.findViewById(R.id.ivFoodPhoto);
        }
    }
}
