package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;

public final class CalorieGoalExplainer {

    private CalorieGoalExplainer() {
    }

    public static String build(Context context, User user) {
        float bmr = CalorieCalculator.calculateBMR(
                user.getGender(),
                user.getWeight(),
                user.getHeight(),
                user.getAge()
        );
        float factor = CalorieCalculator.getActivityFactor(user.getActivityLevel());
        int suggestedGoal = Math.round(bmr * factor);
        return context.getString(
                R.string.calorie_goal_explanation_format,
                suggestedGoal,
                Math.round(bmr),
                factor
        );
    }
}
