package sk.spsepo.moneytrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    private Switch switchNotifications;
    private RadioGroup languageGroup;
    private RadioGroup themeGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inicializácia UI komponentov
        switchNotifications = view.findViewById(R.id.switchNotifications);
        languageGroup = view.findViewById(R.id.languageGroup);
        themeGroup = view.findViewById(R.id.themeGroup);

        // Nastavenie predvolených hodnôt
        switchNotifications.setChecked(true); // Notifikácie zapnuté
        ((RadioButton) view.findViewById(R.id.langSlovak)).setChecked(true); // Slovenčina

        // Nastavenie témy podľa aktuálneho módu aplikácie/systému
        int currentNightMode = getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case android.content.res.Configuration.UI_MODE_NIGHT_NO:
                ((RadioButton) view.findViewById(R.id.themeLight)).setChecked(true);
                break;
            case android.content.res.Configuration.UI_MODE_NIGHT_YES:
                ((RadioButton) view.findViewById(R.id.themeDark)).setChecked(true);
                break;
            default:
                ((RadioButton) view.findViewById(R.id.themeAuto)).setChecked(true);
                break;
        }

        // Pridanie jednoduchých listenerov (len pre príklad)
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: implementovať uloženie stavu
        });

        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // TODO: implementovať zmenu jazyka
        });

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.themeLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.themeDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (checkedId == R.id.themeAuto) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });

        return view;
    }
}