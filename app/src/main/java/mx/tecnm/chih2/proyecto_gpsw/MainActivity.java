package mx.tecnm.chih2.proyecto_gpsw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.EditText;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvFoodEntries;
    private FoodAdapter adapter;
    private List<FoodEntry> foodEntries = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvTotalCalories, tvGoalCalories;
    private LinearProgressIndicator progressCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        setupViews();
        setupRecyclerView();
        loadFoodEntries();
        setupButtons();
        updateCalorieSummary();
    }

    private void setupViews() {
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvGoalCalories = findViewById(R.id.tvGoalCalories);
        progressCalories = findViewById(R.id.progressCalories);
    }

    private void updateCalorieSummary() {
        int total = dbHelper.getTotalCalories();
        int goal = dbHelper.getDailyGoal();
        
        tvTotalCalories.setText(total + " kcal");
        tvGoalCalories.setText("Meta: " + goal + " kcal");
        
        // Actualizar progreso
        int progress = Math.min((total * 100) / goal, 100);
        progressCalories.setProgress(progress);
        
        // Cambiar color según el progreso
        if (progress > 100) {
            progressCalories.setIndicatorColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (progress > 80) {
            progressCalories.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            progressCalories.setIndicatorColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void setupRecyclerView() {
        rvFoodEntries = findViewById(R.id.rvFoodEntries);
        adapter = new FoodAdapter(foodEntries, this::deleteFoodEntry);
        rvFoodEntries.setLayoutManager(new LinearLayoutManager(this));

        // Agregar divisor entre items
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rvFoodEntries.addItemDecoration(divider);

        rvFoodEntries.setAdapter(adapter);
    }

    private void loadFoodEntries() {
        foodEntries.clear();
        foodEntries.addAll(dbHelper.getAllFoodEntries());
        adapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        findViewById(R.id.btnBarcode).setOnClickListener(v ->
                startActivityForResult(new Intent(this, BarcodeScannerActivity.class), 1));

        findViewById(R.id.btnManual).setOnClickListener(v ->
                startActivityForResult(new Intent(this, ManualSearchActivity.class), 2));

        // Añadir botón para cambiar meta diaria
        findViewById(R.id.tvGoalCalories).setOnClickListener(v -> showGoalDialog());
    }

    private void showGoalDialog() {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(dbHelper.getDailyGoal()));

        new AlertDialog.Builder(this)
                .setTitle("Cambiar meta diaria")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        int newGoal = Integer.parseInt(input.getText().toString());
                        if (newGoal > 0) {
                            dbHelper.updateDailyGoal(newGoal);
                            updateCalorieSummary();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Por favor ingresa un número válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            FoodEntry entry = (FoodEntry) data.getSerializableExtra("foodEntry");
            if (entry != null) {
                dbHelper.addFoodEntry(entry);
                loadFoodEntries();
                updateCalorieSummary();
            }
        }
    }

    public void deleteFoodEntry(int position) {
        FoodEntry entry = foodEntries.get(position);
        dbHelper.deleteFoodEntry(entry.getId());
        foodEntries.remove(position);
        adapter.notifyItemRemoved(position);
        updateCalorieSummary();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}