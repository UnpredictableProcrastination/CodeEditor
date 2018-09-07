package co.unpredictable_procrastination.codeeditor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenFileDialog extends AlertDialog.Builder
{

    private OpenDialogListener listener;
    private TextView title;
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<>();
    private CodeEditFragmentPagerAdapter viewPagerAdapter;
    private int selectedIndex = -1;
    private FilenameFilter filenameFilter;
    private Drawable folderIcon;
    private Drawable fileIcon;
    private String accessDeniedMessage;

    public OpenFileDialog setFolderIcon(Drawable image)
    {
        this.folderIcon = image;
        return this;
    }

    public OpenFileDialog setFileIcon(Drawable drawable){
        this.fileIcon = drawable;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    public interface OpenDialogListener
    {
        public void OnSelectedFile(String fileName);
    }

    public class FileAdapter extends ArrayAdapter<File> {

        FileAdapter(Context context, List<File> files)
        {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            String name = file.getName();
            view.setText(name);
            if (file.isDirectory())
            {
                setDrawable(view, folderIcon);
            }
            else
            {
                setDrawable(view, fileIcon);
                if (selectedIndex == position)
                {
                    view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
                }
                else
                {
                    view.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                }
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable)
        {
            if (view != null)
            {
                if (drawable != null)
                {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                }
                else
                {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }

    public OpenFileDialog setFilter(final String filter)
    {
        filenameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                File tempFile = new File(String.format("%s/%s", dir.getPath(), name));
                if (tempFile.isFile())
                {
                    return tempFile.getName().matches(filter);
                }
                return true;
            }
        };
        return this;
    }

    public OpenFileDialog(Context context, CodeEditFragmentPagerAdapter adapter)
    {
        super(context);
        title = createTitle(context);
        LinearLayout layout = createMainLayout(context);
        viewPagerAdapter = adapter;
        files.addAll(getFiles(currentPath));
        final ListView listView = createListView(context);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new FileAdapter(context, files));
        layout.addView(listView);
        setView(layout)
                .setCustomTitle(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selectedIndex > -1 && listener != null)
                        {
                            listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                //.setAdapter(new FileAdapter(context, getFiles(currentPath)), null);
    }

    private List<File> getFiles(String directoryPath)
    {

        File directory = new File(directoryPath);
        List<File> files = Arrays.asList(directory.listFiles());
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file, File file2)
            {
                if(file.isDirectory() && file2.isFile())
                {
                    return -1;
                }
                else if(file.isFile() && file2.isDirectory())
                {
                    return 1;
                }
                else
                {
                    return file.getPath().compareTo(file2.getPath());
                }
            }
        });
        return files;
    }

    private void rebuildFiles(ArrayAdapter<File> adapter)
    {
        try
        {
            files.clear();
            selectedIndex = -1;
            File parent = new File(currentPath).getParentFile();
            if(parent != null && parent.canRead())
            {
                File directory  = new File("../");
                files.add(directory);
            }
            files.addAll(getFiles(currentPath));
        }
        catch(NullPointerException exc)
        {
            String message = getContext().getResources().getString(android.R.string.unknownName);
            if (!accessDeniedMessage.equals(""))
            {
                message = accessDeniedMessage;
            }
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        finally
        {
            adapter.notifyDataSetChanged();
            changeTitle();
        }
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener)
    {
        this.listener = listener;
        return this;
    }

    private ListView createListView(Context context)
    {
        ListView listView = new ListView(context);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
            {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);

                if(null == file)
                {
                    return;
                }

                if (file.isDirectory())
                {
                    if(0 == index && file.getName().equals(".."))
                    {
                        String directory = new File(currentPath).getParent();
                        if (null != directory)
                        {
                            currentPath = directory;
                        }
                    }
                    else
                    {
                        currentPath = file.getPath();
                    }

                    rebuildFiles(adapter);
                }
                else
                {
                    if (index != selectedIndex)
                        selectedIndex = index;
                    else
                        selectedIndex = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return listView;
    }

    private static Display getDefaultDisplay(Context context)
    {
        return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context)
    {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }

    private static int getLinearLayoutMinHeight(Context context)
    {
        return getScreenSize(context).y;
    }

    private LinearLayout createMainLayout(Context context)
    {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private  int getItemHeight(Context context)
    {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.rowHeight, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int)TypedValue.complexToDimension(value.data, metrics);
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(getContext()).x;
        int maxWidth = (int) (screenWidth * 0.9);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth)
            {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    private TextView createTitle(Context context)
    {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        textView.setText(currentPath);
        return textView;
    }
}
