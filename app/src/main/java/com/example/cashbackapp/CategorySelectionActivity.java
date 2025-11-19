package com.example.cashbackapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class CategorySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        // ДОБАВЛЕННЫЙ КОД - приветствие с именем пользователя
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Пользователь");

        TextView title = findViewById(R.id.textTitle); // если есть
        if (title != null) {
            title.setText("Добро пожаловать, " + userName + "!");
        }
        // КОНЕЦ ДОБАВЛЕННОГО КОДА

        GridView gridViewCategories = findViewById(R.id.gridViewCategories);
        List<Category> categoryList = initializeCategories();

        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categoryList);
        gridViewCategories.setAdapter(categoryAdapter);

        gridViewCategories.setOnItemClickListener((parent, view, position, id) -> {
            Category selectedCategory = categoryList.get(position);
            Toast.makeText(CategorySelectionActivity.this,
                    "Выбрана категория: " + selectedCategory.getName() +
                            "\nКешбек: " + selectedCategory.getCashback() + "%",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private List<Category> initializeCategories() {
        List<Category> categories = new ArrayList<>();

        // ТОЛЬКО СТАНДАРТНЫЕ ИКОНКИ ANDROID
        categories.add(new Category("Продукты", android.R.drawable.ic_input_add, 5));
        categories.add(new Category("Рестораны", android.R.drawable.ic_media_play, 10));
        categories.add(new Category("Транспорт", android.R.drawable.ic_menu_directions, 3));
        categories.add(new Category("Одежда", android.R.drawable.ic_menu_edit, 7));
        categories.add(new Category("Электроника", android.R.drawable.ic_menu_preferences, 2));
        categories.add(new Category("Развлечения", android.R.drawable.ic_media_ff, 8));
        categories.add(new Category("Путешествия", android.R.drawable.ic_menu_mapmode, 5));
        categories.add(new Category("АЗС", android.R.drawable.ic_menu_compass, 4));
        categories.add(new Category("Аптеки", android.R.drawable.ic_menu_help, 6));
        categories.add(new Category("Красота", android.R.drawable.ic_menu_camera, 9));

        return categories;
    }
}