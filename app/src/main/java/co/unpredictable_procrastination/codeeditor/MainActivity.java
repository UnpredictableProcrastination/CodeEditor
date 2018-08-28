package co.unpredictable_procrastination.codeeditor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
{
    //private final static String FILENAME = "sample.txt"; // имя файла

    CodeEditFragmentPagerAdapter viewPagerAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Получаем ViewPager
        CustomViewPager viewPager = findViewById(R.id.viewpager);

        viewPagerAdapter = new CodeEditFragmentPagerAdapter(
                getSupportFragmentManager(),
                MainActivity.this,
                viewPager
        );

        // устанавливаем в него адаптер
        viewPager.setAdapter(viewPagerAdapter);

        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_open:
                openFile();
                return true;
            case R.id.action_save:
                //saveFile(FILENAME);
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_new_window:
                openNewWindow();
                return true;
            default:
                return true;
        }
    }

    private void openNewWindow()
    {
        viewPagerAdapter.newWindow();
    }

    // Метод для открытия файла
    private void openFile()
    {
        String fileName = "";
        viewPagerAdapter.newWindow(fileName);
    }
//
//    // Метод для сохранения файла
//    private void saveFile(String fileName)
//    {
//        try
//        {
//            OutputStream outputStream = openFileOutput(fileName, 0);
//            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
//            osw.write(mainEditor.getText().toString());
//            osw.close();
//        }
//        catch (Throwable t)
//        {
//            Toast.makeText(getApplicationContext(),
//                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
//        }
//    }

    private void openSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }
}