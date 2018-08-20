package co.unpredictable_procrastination.codeeditor;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity
{
    private final static String FILENAME = "sample.txt"; // имя файла
    private EditText mainEditor;
    private ArrayList<String> keywords;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        final EditText numberBar = findViewById(R.id.lineNumbers);
        numberBar.setKeyListener(null);

        keywords = readKeywords();
        if(null != keywords)
        {
            Toast.makeText(getApplicationContext(),
                    keywords.get(0), Toast.LENGTH_LONG).show();
        }

        mainEditor = findViewById(R.id.mainEditor);
        final SpannableStringBuilder text = new SpannableStringBuilder("public static void main(){\n}");
        final ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 0, 255));
        text.setSpan(style, 0, 20, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mainEditor.setText(text);
        mainEditor.addTextChangedListener(new TextWatcher()
        {
            int start = 0, end = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                System.err.println("1"+"\n{\n\ttext: "+ s +
                        "\n\tstart: "+start+
                        "\n\tafter: "+after+
                        "\n\tcount: "+count+"}");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNumBar();

                int color = getResources().getColor(R.color.keyword);
                SpannableStringBuilder text = new SpannableStringBuilder(s);
                ForegroundColorSpan style = new ForegroundColorSpan(color);

                for(String word : keywords)
                {
                    Matcher m = Pattern.compile("(" + word + ")+").matcher(s);
                    while(m.find())
                    {
                        System.err.println("Found: " + m.group(0) + "\t" + m.start() + " " + m.end());

                    }
                }


                System.err.println("2"+"\n{\n\ttext: "+ s +
                        "\n\tstart: "+start+
                        "\n\tbefore: "+before+
                        "\n\tcount: "+count+"}");
            }

//            public void setColorWord()
//            {
//                final SpannableStringBuilder text = new SpannableStringBuilder(mainEditor.getText());
//                final ForegroundColorSpan style = new ForegroundColorSpan(Color.BLUE);
//                text.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                mainEditor.setText(text);
//            }

            @Override
            public void afterTextChanged(Editable s)
            {
                System.err.println("3"+"\n"+s);
            }
        });
    }

    public void updateNumBar()
    {
        EditText numberBar = findViewById(R.id.lineNumbers);

        if(numberBar.getLineCount() != mainEditor.getLineCount())
        {
            StringBuilder newText = new StringBuilder();
            int i;
            for(i = 1; i < mainEditor.getLineCount(); i++)
            {
                newText.append(i);
                newText.append('\n');
            }
            newText.append(i);
            numberBar.setText(newText.toString());
        }
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
                openFile(FILENAME);
                return true;
            case R.id.action_save:
                saveFile(FILENAME);
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return true;
        }
    }

    // Метод для открытия файла
    private void openFile(String fileName)
    {
        try
        {
            InputStream inputStream = openFileInput(fileName);

            if (inputStream != null)
            {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null)
                {
                    builder.append(line).append("\n");
                }

                inputStream.close();
                mainEditor.setText(builder.toString());
            }
        }
        catch (Throwable t)
        {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Метод для сохранения файла
    private void saveFile(String fileName)
    {
        try
        {
            OutputStream outputStream = openFileOutput(fileName, 0);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            osw.write(mainEditor.getText().toString());
            osw.close();
        }
        catch (Throwable t)
        {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void openSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    private void highlightKeywords()
    {

    }

    private ArrayList<String> readKeywords()
    {
        ArrayList<String> keywords;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(R.raw.keywords))))
        {
            String[] data = reader.readLine().split(" ");
            keywords = new ArrayList<>(Arrays.asList(data));
            return keywords;
        }
        catch(IOException exc)
        {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + exc.toString(), Toast.LENGTH_LONG).show();
        }

        return null;
    }
}
