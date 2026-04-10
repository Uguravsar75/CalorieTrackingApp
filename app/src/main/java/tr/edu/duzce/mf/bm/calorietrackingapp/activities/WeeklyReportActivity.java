package tr.edu.duzce.mf.bm.calorietrackingapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;
import tr.edu.duzce.mf.bm.calorietrackingapp.adapters.WeeklyReportAdapter;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.AppDatabase;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.dao.MealEntryDao;
import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.User;
import tr.edu.duzce.mf.bm.calorietrackingapp.repository.AppRepository;
import tr.edu.duzce.mf.bm.calorietrackingapp.utils.DateUtils;

/**
 * WeeklyReportActivity: Son 7 günün günlük kalori tüketimini gösterir.
 * Hedefe uyan günler yeşil, aşanlar kırmızı olarak işaretlenir.
 */
public class WeeklyReportActivity extends AppCompatActivity {

    private WeeklyReportAdapter adapter;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        repository = new AppRepository(getApplicationContext());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvWeeklyReport);
        adapter = new WeeklyReportAdapter(this, new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadReport();
    }

    private void loadReport() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = repository.getUser();
            int goal = user != null ? user.getDailyCalorieGoal() : 2000;

            String today = DateUtils.getToday();
            String sevenDaysAgo = DateUtils.getDaysAgo(6); // bugün dahil 7 gün

            List<MealEntryDao.DailyCalorieSummary> summaries =
                    repository.getDailyCaloriesBetween(sevenDaysAgo, today);

            // Tüm tarihler için map oluştur
            Map<String, Integer> calorieMap = new HashMap<>();
            for (MealEntryDao.DailyCalorieSummary s : summaries) {
                calorieMap.put(s.date, s.totalCalories);
            }

            // Son 7 günü en yeniden en eskiye listele
            List<WeeklyReportAdapter.ReportItem> items = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                String date = DateUtils.getDaysAgo(i);
                WeeklyReportAdapter.ReportItem item = new WeeklyReportAdapter.ReportItem();
                item.date = date;
                item.goal = goal;
                if (calorieMap.containsKey(date)) {
                    item.consumed = calorieMap.get(date);
                    item.hasData = true;
                } else {
                    item.consumed = 0;
                    item.hasData = false;
                }
                items.add(item);
            }

            runOnUiThread(() -> adapter.updateData(items));
        });
    }
}
