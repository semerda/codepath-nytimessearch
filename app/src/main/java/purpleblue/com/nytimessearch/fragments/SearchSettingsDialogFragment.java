package purpleblue.com.nytimessearch.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import purpleblue.com.nytimessearch.Constants;
import purpleblue.com.nytimessearch.R;

/**
 * Created by ernest on 7/30/16.
 */
public class SearchSettingsDialogFragment extends DialogFragment {

    @BindView(R.id.dpBeginDate) DatePicker dpBeginDate;
    @BindView(R.id.sSortOrder) Spinner sSortOrder;
    @BindView(R.id.cbArts) CheckBox cbArts;
    @BindView(R.id.cbFashionStyle) CheckBox cbFashionStyle;
    @BindView(R.id.cbSports) CheckBox cbSports;
    @BindView(R.id.btnSave) Button saveButton;
    private Unbinder unbinder;

    public SearchSettingsDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static SearchSettingsDialogFragment newInstance(String title) {
        SearchSettingsDialogFragment frag = new SearchSettingsDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;
    }

    public String guaranteeDoubleDigit(int number) {
        return (number<=9) ? "0"+number : String.valueOf(number);
    }

    public String getBeginDate() throws NullPointerException {
        return String.format("%s-%s-%s",
                dpBeginDate.getYear(),
                guaranteeDoubleDigit(dpBeginDate.getMonth()),
                guaranteeDoubleDigit(dpBeginDate.getDayOfMonth())
        );
    }

    public long getSortOrderId() throws NullPointerException {
        return sSortOrder.getSelectedItemId();
    }
    public String getSortOrderName() throws NullPointerException {
        return sSortOrder.getSelectedItem().toString();
    }

    public String getNewsDesk() throws NullPointerException {
        String newsDesk = String.format("news_desk:(%s%s%s)",
                (cbArts.isChecked() ? "\"Arts\" " : ""),
                (cbFashionStyle.isChecked() ? "\"Fashion\" " : ""),
                (cbSports.isChecked() ? "\"Sports\" " : "")
        );
        return (cbArts.isChecked() || cbFashionStyle.isChecked() || cbSports.isChecked()) ? newsDesk : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_search_settings, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title");
        getDialog().setTitle(title);

        // Get existing settings and set the view accordingly
        SharedPreferences mSettings = getActivity().getSharedPreferences(Constants.SHAREDPREF_SEARCHSETTINGS, 0);
        String paramBeginDate = mSettings.getString("begin_date", "");
        long paramSort = mSettings.getLong("sort_id", 0);
        String paramNewsDesk = mSettings.getString("fq", "");

        // Set Content
        if (!paramBeginDate.isEmpty()) {
            String[] beginDateSeperated = paramBeginDate.split("-");
            dpBeginDate.updateDate(Integer.valueOf(beginDateSeperated[0]), Integer.valueOf(beginDateSeperated[1]), Integer.valueOf(beginDateSeperated[2]));
        }
        //Log.d(String.format("%s-%s-%s", dpBeginDate.getYear(), dpBeginDate.getYear(), dpBeginDate.getYear()));
        dpBeginDate.requestFocus();

        if (paramSort != 0) { sSortOrder.setSelection((int) paramSort); }

        // News Desk
        if (paramNewsDesk.contains("Arts")) { cbArts.setChecked(true); }
        if (paramNewsDesk.contains("Fashion")) { cbFashionStyle.setChecked(true); }
        if (paramNewsDesk.contains("Sports")) { cbSports.setChecked(true); }

        //saveButton.setOnClickListener(v -> Toast.makeText(getContext(), "Search Settings Saved", Toast.LENGTH_LONG).show());

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @OnClick(R.id.btnSave)
    public void onClickSaveSettings(Button button) {
        // Ref: http://guides.codepath.com/android/Storing-and-Accessing-SharedPreferences
        SharedPreferences mSettings = getActivity().getSharedPreferences(Constants.SHAREDPREF_SEARCHSETTINGS, 0);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("begin_date", getBeginDate());
        editor.putLong("sort_id", getSortOrderId());
        editor.putString("sort", getSortOrderName());
        editor.putString("fq", getNewsDesk());
        editor.apply();

        getDialog().dismiss();
    }

    // When binding a fragment in onCreateView, set the views to null in onDestroyView.
    // ButterKnife returns an Unbinder on the initial binding that has an unbind method to do this automatically.
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}