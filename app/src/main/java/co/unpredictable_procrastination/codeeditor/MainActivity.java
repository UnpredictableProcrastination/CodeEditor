package co.unpredictable_procrastination.codeeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
        final int color = getResources().getColor(R.color.keyword);

        final TextWatcher mainWatcher;
        final Pattern keywordPattern = Pattern.compile(makeKeywordPattern());
        final SpannableStringBuilder builder = new SpannableStringBuilder();

        mainWatcher = new TextWatcher()
        {
            Matcher matcher;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                builder.clearSpans();
                CharSequence appending = null;
                CharSequence replacing = null;

                if(s.length() < builder.length())
                {
                    builder.delete(start, start + before);
                }
                else if(builder.length() <= start)
                {
                    appending = s.subSequence(start, start + count);
                }
                else if (builder.length() < start + count)
                {
                    replacing = s.subSequence(start, builder.length());
                    appending = s.subSequence(builder.length(), start + count);

                }
                else
                {
                    replacing = s.subSequence(start, start + count);
                }
                if(replacing != null)
                {
                    builder.replace(start, replacing.length(), replacing);
                }
                if(appending != null)
                {
                    builder.append(appending);
                }

                int cursor = mainEditor.getSelectionStart();
                updateNumBar();

                matcher = keywordPattern.matcher(s);

                boolean found = false;

                while(matcher.find())
                {
                    found = true;


                    ForegroundColorSpan[] spans = builder.getSpans(
                            matcher.start(),
                            matcher.end() - 1,
                            ForegroundColorSpan.class
                    );

                    for(int i = 0; i < spans.length; i++)
                    {
                        Log.i("[" + i + "]" + "colorSpan: ", spans[i].toString());

//                        Log.i("Help",
//                                Boolean.toString(colorSpan.getForegroundColor() == color) + " "
//                                + builder.getSpans(matcher.start(), matcher.end(),ForegroundColorSpan.class).length);
                    }
                    Log.i("-----------------------", "__-__-__");
                    SpannableString keyword = new SpannableString(matcher.group(0));
                    keyword.setSpan(new ForegroundColorSpan(color), 0, keyword.length(), 0);

                    builder.replace(matcher.start(), matcher.end(), keyword);

                }

                if(found)
                {
                    mainEditor.removeTextChangedListener(this);
                    mainEditor.clearComposingText();
                    mainEditor.setText(builder, TextView.BufferType.SPANNABLE);
                    mainEditor.setSelection(cursor);
                    mainEditor.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
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
            pattern.append("(")
                    .append(data[0])
                    .append(")");
            for (int i = 1;i<data.length;i++)
            {
                pattern.append("|(")
                        .append(data[i])
                        .append(")");
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
