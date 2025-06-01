package sk.spsepo.moneytrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public static class Transaction {
        public String id;
        public String date;
        public String category;
        public String title;
        public String amount;

        public Transaction(String id, String date, String category, String title, String amount) {
            this.id = id;
            this.date = date;
            this.category = category;
            this.title = title;
            this.amount = amount;
        }
    }

    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private final List<Transaction> transactions;

        public TransactionAdapter(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, categoryTextView, dateTextView, amountTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.textViewTitle);
                categoryTextView = itemView.findViewById(R.id.textViewCategory);
                dateTextView = itemView.findViewById(R.id.textViewDate);
                amountTextView = itemView.findViewById(R.id.textViewAmount);

                itemView.setOnLongClickListener(v -> {
                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.transaction_item_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Transaction transaction = transactions.get(position);
                            if (item.getItemId() == R.id.action_delete) {
                                FirebaseFirestore.getInstance().collection("transactions").document(transaction.id)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        transactions.remove(position);
                                        notifyItemRemoved(position);
                                    });
                                return true;
                            } else if (item.getItemId() == R.id.action_edit) {
                                Bundle bundle = new Bundle();
                                bundle.putString("transactionId", transaction.id);

                                EditTransactionFragment editFragment = new EditTransactionFragment();
                                editFragment.setArguments(bundle);

                                FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                    .replace(R.id.homeContent, editFragment)
                                    .addToBackStack(null)
                                    .commit();

                                return true;
                            }
                        }
                        return false;
                    });
                    popup.show();
                    return true;
                });
            }

            public void bind(Transaction transaction) {
                titleTextView.setText(transaction.title);
                categoryTextView.setText(transaction.category);
                dateTextView.setText(transaction.date);
                amountTextView.setText(transaction.amount + " €");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(transactions.get(position));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Transaction> transactionList = new ArrayList<>();
        TransactionAdapter adapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);

        Spinner sortSpinner = view.findViewById(R.id.sortSpinner);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                if (position == 1) {
                    transactionList.sort((a, b) -> Float.compare(Float.parseFloat(a.amount), Float.parseFloat(b.amount)));
                } else if (position == 2) {
                    transactionList.sort((a, b) -> Float.compare(Float.parseFloat(b.amount), Float.parseFloat(a.amount)));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("transactions")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String date = doc.getString("date");
                        String category = doc.getString("category");
                        String title = doc.getString("title");
                        String amount = doc.getString("amount");
                        transactionList.add(new Transaction(id, date, category, title, amount));
                    }
                    adapter.notifyDataSetChanged();
                });
        }

        return view;
    }
}