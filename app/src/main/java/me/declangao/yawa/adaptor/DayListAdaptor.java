package me.declangao.yawa.adaptor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import me.declangao.yawa.model.Day;
import me.declangao.yawa.R;
import me.declangao.yawa.util.Util;
import me.declangao.yawa.app.AppController;

/**
 * Custom list adaptor for Day object
 */
public class DayListAdaptor extends BaseAdapter {
    private Activity activity; // Context
    private LayoutInflater layoutInflater;
    private List<Day> days; // Store all the Day objects
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    // Constructor
    public DayListAdaptor(Activity activity, List<Day> days) {
        this.activity = activity;
        this.days = days;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (layoutInflater == null) {
            layoutInflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row, null);
        }
        if (imageLoader == null) {
            imageLoader = AppController.getInstance().getImageLoader();
        }

        // Create the widgets
        NetworkImageView icon = (NetworkImageView) convertView.findViewById(R.id.icon);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tv_date);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.tv_desc);
        TextView tvMax = (TextView) convertView.findViewById(R.id.tv_max);
        TextView tvMin = (TextView) convertView.findViewById(R.id.tv_min);

        // Setup the widgets
        Day day = days.get(position);
        icon.setImageUrl(day.getIconUrl(), imageLoader);
        tvDate.setText(day.getDate());
        tvDescription.setText(day.getDescription());
        tvMax.setText(activity.getString(R.string.max) + Util.formatTemperature(day.getMax()) + "°C");
        tvMin.setText(activity.getString(R.string.min) + Util.formatTemperature(day.getMin()) + "°C");

        return convertView;
    }
}
