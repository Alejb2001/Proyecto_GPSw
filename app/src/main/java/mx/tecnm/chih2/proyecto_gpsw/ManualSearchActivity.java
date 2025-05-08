package mx.tecnm.chih2.proyecto_gpsw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManualSearchActivity extends AppCompatActivity {
    private AutoCompleteTextView actvFoodSearch;
    private EditText etCalories;
    private List<FatSecretFood> searchResults = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_search);

        actvFoodSearch = findViewById(R.id.actvFoodSearch);
        etCalories = findViewById(R.id.etCalories);
        Button btnSave = findViewById(R.id.btnSave);

        setupAutoComplete();
        setupSaveButton(btnSave);
    }

    private void setupAutoComplete() {
        actvFoodSearch.setOnItemClickListener((parent, view, position, id) -> {
            FatSecretFood selectedFood = searchResults.get(position);
            etCalories.setText(String.valueOf(selectedFood.getCalories()));
        });

        actvFoodSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.length() >= 3) {
                    searchFood(s.toString());
                }
            }
        });
    }

    private void searchFood(String query) {
        executor.execute(() -> {
            List<FatSecretFood> results = FatSecretAPI.searchFood(query);
            mainHandler.post(() -> {
                searchResults = results;
                List<String> displayResults = new ArrayList<>();
                for (FatSecretFood food : results) {
                    displayResults.add(food.toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ManualSearchActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        displayResults);
                actvFoodSearch.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void setupSaveButton(Button btnSave) {
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                String foodName = actvFoodSearch.getText().toString();
                int calories = Integer.parseInt(etCalories.getText().toString());
                
                FoodEntry entry = new FoodEntry(foodName, calories);
                returnEntry(entry);
            }
        });
    }

    private boolean validateInputs() {
        if (actvFoodSearch.getText().toString().isEmpty()) {
            showError("Ingresa un nombre de alimento");
            return false;
        }
        if (etCalories.getText().toString().isEmpty()) {
            showError("Ingresa las calorías");
            return false;
        }
        return true;
    }

    private void returnEntry(FoodEntry entry) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("foodEntry", entry);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
