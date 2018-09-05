package co.unpredictable_procrastination.codeeditor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileAdapter extends ArrayAdapter<File> {

    private int selectedIndex = -1;

    public FileAdapter(Context context, List<File> files)
    {
        super(context, android.R.layout.simple_list_item_1, files);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        TextView view = (TextView) super.getView(position, convertView, parent);
        File file = getItem(position);
        view.setText(file.getName());

        view.setSelected(selectedIndex == position);
        return view;
    }
}
