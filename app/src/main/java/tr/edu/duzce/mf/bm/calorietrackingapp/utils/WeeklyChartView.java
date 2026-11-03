package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.R;

/**
 * Haftalık kalori trend grafiği.
 * Dış kütüphane gerektirmeden Canvas ile çizilir.
 * - Dolu mavi çizgi: günlük tüketilen kalori
 * - Kırmızı kesik çizgi: günlük hedef
 * - Altında şeffaf gradyan dolgu
 * - Değer aşımında çizgi kırmızıya döner
 */
public class WeeklyChartView extends View {

    // --- Veri ---
    private List<Integer> calorieValues = new ArrayList<>();
    private int goalCalories = 2000;
    private List<String> dayLabels = new ArrayList<>();

    // --- Paint nesneleri ---
    private Paint linePaint;
    private Paint overLimitLinePaint;
    private Paint goalLinePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint overLimitDotPaint;
    private Paint labelPaint;
    private Paint valuePaint;
    private Paint axisPaint;

    // --- Sabitler ---
    private static final int   PADDING_TOP    = 40;
    private static final int   PADDING_BOTTOM = 48;
    private static final int   PADDING_LEFT   = 16;
    private static final int   PADDING_RIGHT  = 16;
    private static final float DOT_RADIUS_DP  = 5f;
    private static final float STROKE_DP      = 2.5f;

    public WeeklyChartView(Context context) {
        super(context);
        init();
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float stroke   = STROKE_DP  * density;
        float dotR     = DOT_RADIUS_DP * density;

        int colorPrimary  = ContextCompat.getColor(getContext(), R.color.primary);
        int colorRed      = ContextCompat.getColor(getContext(), R.color.report_red);
        int colorMuted    = ContextCompat.getColor(getContext(), R.color.text_muted);

        // Kalori çizgisi (normal)
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(stroke);
        linePaint.setColor(colorPrimary);
        linePaint.setPathEffect(new CornerPathEffect(16 * density));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        // Kalori çizgisi (limit aşımı — kırmızı segment)
        overLimitLinePaint = new Paint(linePaint);
        overLimitLinePaint.setColor(colorRed);

        // Hedef kesik çizgisi
        goalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalLinePaint.setStyle(Paint.Style.STROKE);
        goalLinePaint.setStrokeWidth(stroke * 0.8f);
        goalLinePaint.setColor(colorRed);
        goalLinePaint.setAlpha(180);
        goalLinePaint.setPathEffect(new DashPathEffect(new float[]{12 * density, 6 * density}, 0));

        // Gradyan dolgu
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        // Noktalar (normal)
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(colorPrimary);

        // Noktalar (limit aşımı)
        overLimitDotPaint = new Paint(dotPaint);
        overLimitDotPaint.setColor(colorRed);

        // Gün etiketleri (Paz, Pzt …)
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextSize(10 * density);
        labelPaint.setColor(colorMuted);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        // Kalori değerleri (nokta üstü)
        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setTextSize(9 * density);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        // X ekseni çizgisi
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(1 * density);
        axisPaint.setColor(colorMuted);
        axisPaint.setAlpha(60);
    }

    /**
     * Grafik verilerini günceller.
     * @param calories 7 günlük kalori listesi (veri yoksa 0)
     * @param goal     kullanıcının günlük kalori hedefi
     * @param labels   gün etiketleri (Paz, Pzt …) — 7 eleman
     */
    public void setData(List<Integer> calories, int goal, List<String> labels) {
        this.calorieValues = calories;
        this.goalCalories  = goal;
        this.dayLabels     = labels;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = (int) (w * 0.42f); // en-boy oranı ~2.4:1
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (calorieValues == null || calorieValues.isEmpty()) return;

        float density     = getResources().getDisplayMetrics().density;
        float dotR        = DOT_RADIUS_DP * density;
        int   w           = getWidth();
        int   h           = getHeight();
        float left        = PADDING_LEFT  * density;
        float right       = w - PADDING_RIGHT * density;
        float top         = PADDING_TOP   * density;
        float bottom      = h - PADDING_BOTTOM * density;
        float chartH      = bottom - top;
        float chartW      = right - left;
        int   n           = calorieValues.size();

        // Ölçek: maksimum değer ya goal*1.3 ya da gerçek max
        int maxVal = goalCalories;
        for (int v : calorieValues) maxVal = Math.max(maxVal, v);
        maxVal = (int) (maxVal * 1.15f);
        if (maxVal == 0) maxVal = 1;

        // X aralığı
        float step = (n > 1) ? chartW / (n - 1) : chartW;

        // Koordinatları hesapla
        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = left + i * step;
            int v = calorieValues.get(i);
            ys[i] = bottom - (v / (float) maxVal) * chartH;
        }

        // Hedef çizgisi Y koordinatı
        float goalY = bottom - (goalCalories / (float) maxVal) * chartH;

        // --- Gradyan dolgu ---
        Path fillPath = new Path();
        fillPath.moveTo(xs[0], bottom);
        fillPath.lineTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            fillPath.lineTo(xs[i], ys[i]);
        }
        fillPath.lineTo(xs[n - 1], bottom);
        fillPath.close();

        int colorPrimary = ContextCompat.getColor(getContext(), R.color.primary);
        fillPaint.setShader(new LinearGradient(
                0, top, 0, bottom,
                colorPrimary & 0x55FFFFFF,   // %33 alpha üst
                colorPrimary & 0x08FFFFFF,   // ~%3  alpha alt
                Shader.TileMode.CLAMP
        ));
        canvas.drawPath(fillPath, fillPaint);

        // --- X ekseni ---
        canvas.drawLine(left, bottom, right, bottom, axisPaint);

        // --- Hedef kesik çizgisi ---
        canvas.drawLine(left, goalY, right, goalY, goalLinePaint);

        // --- Kalori çizgisi (segmentlere bölerek renk ataması) ---
        for (int i = 1; i < n; i++) {
            boolean over = calorieValues.get(i) > goalCalories && calorieValues.get(i) > 0;
            Paint p = over ? overLimitLinePaint : linePaint;
            canvas.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i], p);
        }

        // --- Noktalar + değer etiketleri + gün etiketleri ---
        for (int i = 0; i < n; i++) {
            int v = calorieValues.get(i);
            boolean over = v > goalCalories && v > 0;
            boolean noData = v == 0;

            // Nokta
            if (!noData) {
                canvas.drawCircle(xs[i], ys[i], dotR, over ? overLimitDotPaint : dotPaint);
            }

            // Değer etiketi (0 ise gösterme)
            if (!noData) {
                valuePaint.setColor(over
                        ? ContextCompat.getColor(getContext(), R.color.report_red)
                        : colorPrimary);
                String label = v >= 1000 ? (v / 1000) + "k" : String.valueOf(v);
                float valueY = ys[i] - dotR - 4 * density;
                canvas.drawText(label, xs[i], valueY, valuePaint);
            }

            // Gün etiketi (altda)
            if (dayLabels != null && i < dayLabels.size()) {
                canvas.drawText(dayLabels.get(i), xs[i], h - 6 * density, labelPaint);
            }
        }
    }
}
