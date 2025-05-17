package sk.spsepo.moneytrack;

import androidx.fragment.app.Fragment;
import sk.spsepo.moneytrack.HomeFragment;
import sk.spsepo.moneytrack.ChartFragment;
import sk.spsepo.moneytrack.AddFragment;
import sk.spsepo.moneytrack.SettingsFragment;
import sk.spsepo.moneytrack.ProfileFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        TextView emailTextView = findViewById(R.id.emailTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailTextView.setText(user.getEmail());
        }

        emailTextView.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        // Set user name in drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.navHeaderUsername);
        if (user != null) {
            navUsername.setText(user.getEmail());
        }

        // Navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Home.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_settings) {
                // startActivity(new Intent(Home.this, SettingsActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_chart) {
                selectedFragment = new ChartFragment();
            } else if (id == R.id.nav_add) {
                selectedFragment = new AddFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeContent, selectedFragment)
                    .commit();
                return true;
            }
            return false;
        });
    }
}