package sk.spsepo.moneytrack;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.DatePickerDialog;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFragment newInstance(String param1, String param2) {
        AddFragment fragment = new AddFragment();
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
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        TextView inputDate = view.findViewById(R.id.inputDate);
        Calendar calendar = Calendar.getInstance();

        inputDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view1, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + String.format("%02d", selectedMonth + 1) + "-" + String.format("%02d", selectedDay);
                    inputDate.setText(selectedDate);
                }, year, month, day);
            datePickerDialog.show();
        });

        EditText inputCategory = view.findViewById(R.id.inputCategory);
        EditText inputTitle = view.findViewById(R.id.inputTitle);
        EditText inputAmount = view.findViewById(R.id.inputAmount);
        Button saveButton = view.findViewById(R.id.saveButton);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> {
            String date = inputDate.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            String title = inputTitle.getText().toString().trim();
            String amount = inputAmount.getText().toString().trim();

            if (date.isEmpty() || category.isEmpty() || title.isEmpty() || amount.isEmpty()) {
                Toast.makeText(getContext(), "Vyplňte všetky polia", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("date", date);
            data.put("category", category);
            data.put("title", title);
            data.put("amount", amount);

            db.collection("transactions")
                    .add(data)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(getContext(), "Údaje uložené", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Chyba pri ukladaní", Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}