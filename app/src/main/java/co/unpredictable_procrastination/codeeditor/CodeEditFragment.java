package co.unpredictable_procrastination.codeeditor;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.nio.file.Path;

public class CodeEditFragment extends Fragment
{
    private static final String ARG_PATH = "arg_path";

    private String path;
    private EditText mainEdit;
    private EditText numBar;

    public static CodeEditFragment newInstance(String path)
    {
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);

        CodeEditFragment fragment = new CodeEditFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
        {
            path = args.getString(ARG_PATH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.code_edit_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //mainEdit = view.findViewById()

    }
}
