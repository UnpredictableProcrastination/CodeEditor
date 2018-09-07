package co.unpredictable_procrastination.codeeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(this, "CANNOT READ", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }
            return;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(this, "CANNOT WRITE", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }
            return;
        }

        OpenFileDialog fileDialog = new OpenFileDialog(this, viewPagerAdapter)
                .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName)
                    {
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
                        viewPagerAdapter.newWindow(fileName);
                    }
                })
                .setFilter(".*\\.txt")
                .setFolderIcon(getDrawable(R.drawable.ic_folder))
                .setFileIcon(getDrawable(R.drawable.ic_file));
        fileDialog.show();
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