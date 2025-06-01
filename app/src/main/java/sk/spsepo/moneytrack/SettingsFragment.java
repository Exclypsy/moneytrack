package sk.spsepo.moneytrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

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
    private RadioButton langSlovak;
    private RadioButton langEnglish;
    private RadioButton themeLight;
    private RadioButton themeDark;
    private RadioButton themeAuto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Inicializácia UI komponentov
        switchNotifications = view.findViewById(R.id.switchNotifications);
        languageGroup = view.findViewById(R.id.languageGroup);
        themeGroup = view.findViewById(R.id.themeGroup);

        langSlovak = view.findViewById(R.id.langSlovak);
        langEnglish = view.findViewById(R.id.langEnglish);

        themeLight = view.findViewById(R.id.themeLight);
        themeDark = view.findViewById(R.id.themeDark);
        themeAuto = view.findViewById(R.id.themeAuto);

        // Načítanie uložených hodnôt
        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        if (prefs.getString("language", "sk").equals("sk")) {
            langSlovak.setChecked(true);
        } else {
            langEnglish.setChecked(true);
        }
        String themePref = prefs.getString("theme", "auto");
        switch (themePref) {
            case "light":
                themeLight.setChecked(true);
                break;
            case "dark":
                themeDark.setChecked(true);
                break;
            default:
                themeAuto.setChecked(true);
                break;
        }

        // Reakcia na zmenu témy
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.themeLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putString("theme", "light").apply();
            } else if (checkedId == R.id.themeDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putString("theme", "dark").apply();
            } else if (checkedId == R.id.themeAuto) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                editor.putString("theme", "auto").apply();
            }
            requireActivity().recreate();
        });

        // Uloženie ostatných nastavení ihneď po zmene
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notifications", isChecked).apply();
        });

        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putString("language", checkedId == R.id.langSlovak ? "sk" : "en").apply();
        });

        return view;
    }
}