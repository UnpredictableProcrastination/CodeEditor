package co.unpredictable_procrastination.codeeditor;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
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

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        final EditText numberBar = findViewById(R.id.lineNumbers);
        numberBar.setKeyListener(null);

        mainEditor = findViewById(R.id.mainEditor);
//        SpannableStringBuilder mainBuilder = new SpannableStringBuilder();
//        final ForegroundColorSpan style = new ForegroundColorSpan(Color.BLUE);
//
//        SpannableString first = new SpannableString("public abcd ");
//        first.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 6, 0);
//
//        SpannableString second = new SpannableString("static void main()");
//        second.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 11, 0);
//
//        mainBuilder.append(first);
//        mainBuilder.append(second);
//
//        mainEditor.setText(mainBuilder, TextView.BufferType.SPANNABLE);

        final TextWatcher mainWatcher;
        mainWatcher = new TextWatcher()
        {
            Pattern p = Pattern.compile(makeKeywordPattern());
            Matcher m;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                System.err.println("1"+"\n{\n\ttext: "+ s +
                        "\n\tstart: "+start+
                        "\n\tafter: "+after+
                        "\n\tcount: "+count+"}");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int cursor = mainEditor.getSelectionStart();
                int counter = 0;
                updateNumBar();

                if(before>=1) counter = 0;

                m = p.matcher(s);
                int color = getResources().getColor(R.color.keyword);
                SpannableStringBuilder builder = new SpannableStringBuilder(s);
                //ForegroundColorSpan style = new ForegroundColorSpan(color);

                for (int i = 0;i<counter;i++)
                {
                    System.err.println(m.group());
                    m.find();
                }

                if(m.find())
                {
                    SpannableString keyword = new SpannableString(m.group(0));
                    keyword.setSpan(new ForegroundColorSpan(color), 0, keyword.length(), 0);

                    //builder.replace(m.start(), m.end(), keyword);
                    builder.delete(m.start(), m.end());
                    builder.insert(m.start(), keyword);
                    counter++;
                    while (m.find())
                    {

                        keyword = new SpannableString(m.group(0));
                        keyword.setSpan(new ForegroundColorSpan(color), 0, keyword.length(), 0);

                        //builder.replace(m.start(), m.end(), keyword);
                        builder.delete(m.start(), m.end());
                        builder.insert(m.start(), keyword);
                        counter++;
                    }
                    mainEditor.removeTextChangedListener(this);
                    mainEditor.setText(builder, TextView.BufferType.SPANNABLE);
                    mainEditor.setSelection(cursor);
                    mainEditor.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        };

        mainEditor.addTextChangedListener(mainWatcher);
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

    private String makeKeywordPattern(){

        StringBuilder pattern = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(R.raw.keywords))))
        {
            String[] data = reader.readLine().split(" ");
            pattern.append("(" + data[0] + ")");
            for (int i = 1;i<data.length;i++)
            {
                pattern.append("|(" + data[i]  + ")");
            }
            return pattern.toString();
        }
        catch(IOException exc)
        {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + exc.toString(), Toast.LENGTH_LONG).show();
        }
        return "";
    }


}
