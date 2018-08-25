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
                // Запоминаем количество выделенных слов до изменения текста
                // Понадобится в конце
                int keywordsCount =
                        builder.getSpans(
                                0,
                                builder.length(),
                                ForegroundColorSpan.class
                        ).length;

                // Находим позицию курсора
                int cursor = start + count;

                // Обновляем нумерацию строк
                updateNumBar();

                // Вычисляем границы диапазона, в котором будем искать слова
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

                Matcher matcher;

                // Объявляем строки:
                // Эту добавим в конец
                CharSequence appending = null;
                // Эта пойдет на замену уже существующего отрезка
                CharSequence replacing = null;
                // А эту впихнем куда-нибудь
                CharSequence insertion = null;

                // Если длина строки уменьшилась - очевидно, произошло удаление
                if(s.length() < builder.length())
                {
                    // Создаем matcher на этом диапазоне
                    matcher = keywordPattern.matcher(s.subSequence(begin, end));

                    // Калькулируем границы для предыдущей версии текста
                    int builderEnd = cursor + 6;
                    if(builderEnd > builder.length())
                    {
                        builderEnd = builder.length();
                    }

                    // И находим в ней совпадения
                    Matcher builderMatcher = keywordPattern.matcher(builder.subSequence(begin, builderEnd));

                    boolean found = false;

                    // Будем искать различия в найденных словах
                    while(matcher.find() && builderMatcher.find())
                    {
                        // Нашли несоответствие
                        if(!matcher.group(0).equals(builderMatcher.group(0)))
                        {
                            found = true;

                            // Получаем список стилей для данного участка
                            ForegroundColorSpan[] spans =
                                    builder.getSpans(
                                            builderMatcher.start() + begin,
                                            builderMatcher.end()+ begin,
                                            ForegroundColorSpan.class
                                    );

                            // Он нам больше не нужен, сносим к чертям!
                            if(spans.length > 0)
                            {
                                builder.removeSpan(spans[0]);
                            }
                        }
                    }

                    // Убираем то, что пользователь удалил из текста
                    builder.delete(start, start + before);

                    // Ничего еще не нашли, но в билдере что-то осталось?
                    if(!found && builderMatcher.find()) {
                        ForegroundColorSpan[] spans =
                                builder.getSpans(
                                        builderMatcher.start() + begin,
                                        builderMatcher.end() + begin,
                                        ForegroundColorSpan.class
                                );

                        // Сносим к чертям!
                        if (spans.length > 0) {
                            builder.removeSpan(spans[0]);
                        }
                    }
                }
                else if(builder.length() <= start)
                {
                    // Длина билдера меньше позиции добавленного текста
                    // очевидно, это нужно запихнуть в конец
                    appending = s.subSequence(start, start + count);
                }
                else if (builder.length() < start + count)
                {
                    // Билдер уже не меньше позиции нового текста, но еще не дотягивает до конца
                    // Значит, часть перепишем, а часть добавим
                    replacing = s.subSequence(start, builder.length());
                    appending = s.subSequence(builder.length(), start + count);

                }
                else if(s.length() == builder.length() + count)
                {
                    // Длина билдера и нового текста вместе дают длину строки
                    // Получается, текст куда-то впихнули
                    insertion = s.subSequence(start, start + count);
                }
                else
                {
                    // Ничего выше не подошло, но строка изменилась?
                    // Значит, отрезок просто заменили
                    replacing = s.subSequence(start, start + count);
                }

                // Если какая-нибудь строка непустая - производим соответствующие действия
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

                // Получаем новый matcher,
                // Первый если и был, то он пал смертью храбрых во время нахождения несоответствий
                matcher = keywordPattern.matcher(s.subSequence(begin, end));
                boolean found = false;

                // Что-то нашли?
                while(matcher.find())
                {
                    found = true;

                    // Красим валиком в установленный цвет
                    SpannableString keyword = new SpannableString(matcher.group(0));
                    keyword.setSpan(new ForegroundColorSpan(color), 0, keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Получаем список стилей в этом диапазоне
                    ForegroundColorSpan[] spans =
                            builder.getSpans(
                                    matcher.start() + begin,
                                    matcher.end() + begin,
                                    ForegroundColorSpan.class
                            );

                    // На месте найденного слова что-то было?
                    // Правильно, сносим к чертям!
                    if(spans.length > 0)
                    {
                        builder.removeSpan(spans[0]);
                    }

                    // Заливаем наше свежепокрашенное слово
                    builder.replace(matcher.start() + begin, matcher.end() + begin, keyword);
                }

                // Получаем на этот раз ВСЕ стили для текста
                ForegroundColorSpan[] spans = builder.getSpans(
                        0,
                        builder.length(),
                        ForegroundColorSpan.class
                );

                // Выводим информацию о стилях
                Log.i("length", "" + spans.length);
                for(int i = 0; i < spans.length; i++)
                {
                    Log.i("[" + i + "]" + "colorSpan: ", spans[i].toString());
                }
                Log.i("-----------------------", "__-__-__");


                // Если мы нашли что-нибудь новое,
                // или количество стилей изменилось,
                // придется залить новый текст
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