package co.unpredictable_procrastination.codeeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
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

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int keywordsCount =
                        builder.getSpans(
                                0,
                                builder.length(),
                                ForegroundColorSpan.class
                        ).length;

                int cursor = start + count;
                updateNumBar();

                int begin = start - 6;
                int end = cursor + 6;
                if(begin < 0)
                {
                    begin = 0;
                }
                if(end > s.length())
                {
                    end = s.length();
                }

                Matcher matcher = keywordPattern.matcher(s.subSequence(begin, end));

                //builder.clearSpans();
                CharSequence appending = null;
                CharSequence replacing = null;
                CharSequence insertion = null;

                if(s.length() < builder.length())
                {
                    int builderEnd = cursor + 6;
                    if(builderEnd > builder.length())
                    {
                        builderEnd = builder.length();
                    }
                    Matcher builderMatcher = keywordPattern.matcher(builder.subSequence(begin, builderEnd));
                    builder.delete(start, start + before);

                    boolean found = false;
                    while(matcher.find() && builderMatcher.find())
                    {
                        if(!matcher.group(0).equals(builderMatcher.group(0)))
                        {
                            found = true;
                            ForegroundColorSpan[] spans =
                                    builder.getSpans(
                                            builderMatcher.start() + begin,
                                            builderMatcher.end()+ begin,
                                            ForegroundColorSpan.class
                                    );
                            if(spans.length > 0)
                            {
                                builder.removeSpan(spans[0]);
                            }
                        }
                    }

                    if(!found && builderMatcher.find()) {
                        ForegroundColorSpan[] spans =
                                builder.getSpans(
                                        builderMatcher.start() + begin,
                                        builderMatcher.end() + begin,
                                        ForegroundColorSpan.class
                                );
                        if (spans.length > 0) {
                            builder.removeSpan(spans[0]);
                        }
                    }
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
                else if(s.length() == builder.length() + count)
                {
                    insertion = s.subSequence(start, start + count);
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
                if(insertion != null)
                {
                    builder.insert(start, insertion);
                }

                matcher = keywordPattern.matcher(s.subSequence(begin, end));
                boolean found = false;

                while(matcher.find())
                {
                    found = true;

                    SpannableString keyword = new SpannableString(matcher.group(0));
                    keyword.setSpan(new ForegroundColorSpan(color), 0, keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    ForegroundColorSpan[] spans =
                            builder.getSpans(
                                    matcher.start() + begin,
                                    matcher.end() + begin,
                                    ForegroundColorSpan.class
                            );
                    if(spans.length > 0)
                    {
                        builder.removeSpan(spans[0]);
                    }

                    builder.replace(matcher.start() + begin, matcher.end() + begin, keyword);
                }

                ForegroundColorSpan[] spans = builder.getSpans(
                        0,
                        builder.length(),
                        ForegroundColorSpan.class
                );
                Log.i("length", "" + spans.length);
                for(int i = 0; i < spans.length; i++)
                {
                    Log.i("[" + i + "]" + "colorSpan: ", spans[i].toString());
                }
                Log.i("-----------------------", "__-__-__");


                if(found || keywordsCount != spans.length)
                {
                    mainEditor.removeTextChangedListener(this);
                    //mainEditor.clearComposingText();
                    mainEditor.setText(builder, TextView.BufferType.SPANNABLE);
                    mainEditor.addTextChangedListener(this);
                    mainEditor.setSelection(cursor);
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
            pattern.append("\\b(")
                    .append(data[0]);
                    //.append(")");
            for (int i = 1;i<data.length;i++)
            {
                pattern.append("|")
                        .append(data[i]);
                        //.append(")");
            }
            pattern.append(")\\b");
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
