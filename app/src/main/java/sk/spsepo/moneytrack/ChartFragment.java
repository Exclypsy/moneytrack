package sk.spsepo.moneytrack;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        WebView webView = view.findViewById(R.id.any_chart_view);
        webView.getSettings().setJavaScriptEnabled(true);

        // Firebase imports are assumed
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        java.util.Calendar now = java.util.Calendar.getInstance();
        int currentYear = now.get(java.util.Calendar.YEAR);
        int currentMonth = now.get(java.util.Calendar.MONTH) + 1;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    float[] monthlySums = new float[12];
                    java.util.Map<Integer, java.util.Map<String, Float>> dailyCategorySums = new java.util.HashMap<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : query) {
                        String dateStr = doc.getString("date");
                        String amountStr = doc.getString("amount");
                        String category = doc.getString("category");
                        if (dateStr == null || amountStr == null || category == null) continue;

                        try {
                            String[] parts = dateStr.split("-");
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]);
                            int day = Integer.parseInt(parts[2]);
                            float amount = Float.parseFloat(amountStr);

                            if (year == currentYear) {
                                monthlySums[month - 1] += amount;
                            }

                            if (year == currentYear && month == currentMonth) {
                                java.util.Map<String, Float> categoryMap = dailyCategorySums.getOrDefault(day, new java.util.HashMap<>());
                                categoryMap.put(category, categoryMap.getOrDefault(category, 0f) + amount);
                                dailyCategorySums.put(day, categoryMap);
                            }
                        } catch (Exception ignored) {}
                    }


                    StringBuilder monthlyData = new StringBuilder("[");
                    for (int i = 0; i < 12; i++) {
                        monthlyData.append("['").append(i + 1).append("', ").append(monthlySums[i]).append("]");
                        if (i < 11) monthlyData.append(", ");
                    }
                    monthlyData.append("]");

                    // Convert dailyCategorySums to stacked column series
                    StringBuilder script = new StringBuilder();
                    script.append("anychart.onDocumentReady(function() {");

                    script.append("var yearData = ").append(monthlyData).append(";");
                    script.append("var yearChart = anychart.column(yearData);");
                    script.append("yearChart.title('Výdavky podľa mesiacov');");
                    script.append("yearChart.container('yearChart');");
                    script.append("yearChart.draw();");

                    script.append("var monthChart = anychart.column();");
                    script.append("monthChart.title('Výdavky tento mesiac');");
                    script.append("monthChart.container('monthChart');");

                    java.util.Set<String> allCategories = new java.util.HashSet<>();
                    for (java.util.Map<String, Float> catMap : dailyCategorySums.values()) {
                        allCategories.addAll(catMap.keySet());
                    }

                    for (String category : allCategories) {
                        script.append("monthChart.column([");
                        java.util.List<Integer> sortedDays = new java.util.ArrayList<>(dailyCategorySums.keySet());
                        java.util.Collections.sort(sortedDays);
                        for (int i = 0; i < sortedDays.size(); i++) {
                            int day = sortedDays.get(i);
                            float val = dailyCategorySums.get(day).getOrDefault(category, 0f);
                            script.append("{x:'").append(day).append("', value:").append(val).append("}");
                            if (i < sortedDays.size() - 1) script.append(", ");
                        }
                        script.append("]).name('").append(category).append("');");
                    }

                    script.append("monthChart.legend(true);");
                    script.append("monthChart.draw();");
                    script.append("});");

                    String html = "<html><head>" +
                            "<script src='https://cdn.anychart.com/releases/8.11.0/js/anychart-bundle.min.js'></script>" +
                            "</head><body>" +
                            "<div id='yearChart' style='width:100%;height:45%'></div>" +
                            "<div id='monthChart' style='width:100%;height:45%'></div>" +
                            "<script>" + script + "</script>" +
                            "</body></html>";

                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                });

        return view;
    }
}