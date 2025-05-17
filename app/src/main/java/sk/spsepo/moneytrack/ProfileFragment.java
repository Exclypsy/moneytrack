package sk.spsepo.moneytrack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private Button changePhotoButton, saveProfileButton;
    private EditText nameEditText, surnameEditText, ageEditText, nicknameEditText;

    private FirebaseUser user;
    private FirebaseFirestore db;

    private Uri selectedImageUri = null;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        nameEditText = view.findViewById(R.id.nameEditText);
        surnameEditText = view.findViewById(R.id.surnameEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        nicknameEditText = view.findViewById(R.id.nicknameEditText);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Glide.with(this).load(selectedImageUri).circleCrop().into(profileImageView);
                    }
                }
        );

        loadUserProfile();

        changePhotoButton.setOnClickListener(v -> openImagePicker());
        saveProfileButton.setOnClickListener(v -> saveUserProfile());

        return view;
    }

    private void loadUserProfile() {
        if (user == null) {
            Toast.makeText(getContext(), "Nie si prihlásený", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String surname = documentSnapshot.getString("surname");
                Long age = documentSnapshot.getLong("age");
                String nickname = documentSnapshot.getString("nickname");
                String photoUrl = documentSnapshot.getString("photoUrl");

                if (name != null) nameEditText.setText(name);
                if (surname != null) surnameEditText.setText(surname);
                if (age != null) ageEditText.setText(String.valueOf(age));
                if (nickname != null) nicknameEditText.setText(nickname);
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).circleCrop().into(profileImageView);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Nepodarilo sa načítať profil", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "Firestore load error", e);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveUserProfile() {
        if (user == null) {
            Toast.makeText(getContext(), "Nie si prihlásený", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();

        int age = 0;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Zadaj platný vek", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.update(
                "name", name,
                "surname", surname,
                "age", age,
                "nickname", nickname
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Profil uložený", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Nepodarilo sa uložiť profil", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "Firestore save error", e);
        });

        // Ak chceš, môžeš sem pridať upload obrázka do Firebase Storage a uložiť URL do Firestore
        // (upload obrázka zatiaľ nechávame mimo, len vyber a zobrazíme)
    }
}