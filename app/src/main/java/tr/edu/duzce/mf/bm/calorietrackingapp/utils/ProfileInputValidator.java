package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;

public final class ProfileInputValidator {

    public static final int MIN_AGE = 13;
    public static final int MAX_AGE = 100;
    public static final float MIN_HEIGHT = 120f;
    public static final float MAX_HEIGHT = 230f;
    public static final float MIN_WEIGHT = 30f;
    public static final float MAX_WEIGHT = 250f;
    public static final int MIN_DAILY_CALORIES = 800;
    public static final int MAX_DAILY_CALORIES = 5000;

    private ProfileInputValidator() {
    }

    public static ValidationResult validate(Context context, String age, String height, String weight, String goal) {
        ValidationResult result = new ValidationResult();
        result.age = parseInt(age);
        result.height = parseFloat(height);
        result.weight = parseFloat(weight);
        result.goal = parseInt(goal);

        if (result.age == null || result.age < MIN_AGE || result.age > MAX_AGE) {
            result.ageError = context.getString(R.string.error_age_range, MIN_AGE, MAX_AGE);
        }
        if (result.height == null || result.height < MIN_HEIGHT || result.height > MAX_HEIGHT) {
            result.heightError = context.getString(R.string.error_height_range, (int) MIN_HEIGHT, (int) MAX_HEIGHT);
        }
        if (result.weight == null || result.weight < MIN_WEIGHT || result.weight > MAX_WEIGHT) {
            result.weightError = context.getString(R.string.error_weight_range, (int) MIN_WEIGHT, (int) MAX_WEIGHT);
        }
        if (result.goal == null || result.goal < MIN_DAILY_CALORIES || result.goal > MAX_DAILY_CALORIES) {
            result.goalError = context.getString(R.string.error_calorie_goal_range, MIN_DAILY_CALORIES, MAX_DAILY_CALORIES);
        }
        return result;
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class ValidationResult {
        public Integer age;
        public Float height;
        public Float weight;
        public Integer goal;
        public String ageError;
        public String heightError;
        public String weightError;
        public String goalError;

        public boolean isValid() {
            return ageError == null
                    && heightError == null
                    && weightError == null
                    && goalError == null;
        }
    }
}
