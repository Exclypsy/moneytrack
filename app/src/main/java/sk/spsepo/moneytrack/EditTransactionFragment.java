package sk.spsepo.moneytrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditTransactionFragment extends Fragment {

    private EditText inputDate, inputCategory, inputTitle, inputAmount;
    private Button buttonSave;

    private String transactionId;

    public EditTransactionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        inputDate = view.findViewById(R.id.inputDate);
        inputCategory = view.findViewById(R.id.inputCategory);
        inputTitle = view.findViewById(R.id.inputTitle);
        inputAmount = view.findViewById(R.id.inputAmount);
        buttonSave = view.findViewById(R.id.buttonSave);

        // Date picker for inputDate
        inputDate.setFocusable(false);
        inputDate.setOnClickListener(v -> showDatePicker());

        if (getArguments() != null) {
            transactionId = getArguments().getString("transactionId");
            loadTransactionData(transactionId);
        }

        buttonSave.setOnClickListener(v -> saveTransaction());

        return view;
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    inputDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadTransactionData(String id) {
        FirebaseFirestore.getInstance().collection("transactions").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        inputDate.setText(documentSnapshot.getString("date"));
                        inputCategory.setText(documentSnapshot.getString("category"));
                        inputTitle.setText(documentSnapshot.getString("title"));
                        inputAmount.setText(documentSnapshot.getString("amount"));
                    } else {
                        Toast.makeText(requireContext(), "Transakcia nebola nájdená", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Chyba načítania transakcie", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }

    private void saveTransaction() {
        String date = inputDate.getText().toString().trim();
        String category = inputCategory.getText().toString().trim();
        String title = inputTitle.getText().toString().trim();
        String amount = inputAmount.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(category) || TextUtils.isEmpty(title) || TextUtils.isEmpty(amount)) {
            Toast.makeText(requireContext(), "Vyplňte všetky polia", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("date", date);
        updatedData.put("category", category);
        updatedData.put("title", title);
        updatedData.put("amount", amount);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("transactions").document(transactionId);
        docRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Transakcia upravená", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Chyba pri ukladaní", Toast.LENGTH_SHORT).show());
    }
}