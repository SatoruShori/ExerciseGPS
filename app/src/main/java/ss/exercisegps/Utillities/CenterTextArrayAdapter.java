package ss.exercisegps.Utillities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ss.exercisegps.R;

/**
 * Created by Satoru on 10/1/2559.
 */

public class CenterTextArrayAdapter extends ArrayAdapter<String> {
    LayoutInflater inflater;
    Integer res, resText;

    public CenterTextArrayAdapter() {
        super(SystemUtils.getActivity(), android.R.layout.simple_list_item_1);
        inflater = (LayoutInflater) SystemUtils.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private List<String> listObjects = new ArrayList<>();
    List<String> suggestions = new ArrayList<>();
    public CenterTextArrayAdapter(List<String> listObjects) {
        this();
        this.listObjects.clear();
        this.listObjects.addAll(listObjects);
    }

    public CenterTextArrayAdapter(String[] data) {
        this();
        this.listObjects.clear();
        this.listObjects.addAll(Arrays.asList(data));
    }


    public void setRes(int resource, int resText) {
        res = resource;
        this.resText = resText;
    }

    @Override
    public void add(String object) {
        super.add(object);
        listObjects.add(object);
        suggestions.add(object);
    }

    @Override
    public void addAll(Collection<? extends String> collection) {
        super.addAll(collection);
        listObjects.addAll(collection);
        suggestions.addAll(collection);
    }

    @Override
    public void addAll(String[] object) {
        super.addAll(object);
        listObjects.addAll(Arrays.asList(object));
        suggestions.addAll(Arrays.asList(object));
    }


    @Override
    public void clear() {
        super.clear();
        listObjects.clear();
        suggestions.clear();
    }

    class Holder {
        View view;
        TextView textView;

        Holder(ViewGroup parent) {
            view = inflater.inflate(R.layout.center_text_item, parent, false);
            textView = (TextView) view.findViewById(R.id.item_text);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder(parent);
            convertView = holder.view;
            if(res!=null) {
                holder.view.setBackgroundResource(res);
            }
            if(resText!=null) {
                holder.textView.setTextColor(resText);
            }
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.textView.setText(getItem(position));
        return convertView;
    }

    private Filter mFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if(constraint != null) {
                suggestions.clear();
                for(String object : listObjects){
                    if(object.toLowerCase().contains(constraint.toString().toLowerCase())){
                        suggestions.add(object);
                    }
                }

                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(results == null){
                return;
            }
            try {
                List<String> filteredList = (List<String>) results.values;
                if(results.count > 0) {
                    CenterTextArrayAdapter.super.clear();
                    for (String filteredObject : filteredList) {
                        CenterTextArrayAdapter.super.add(filteredObject);
                    }
                    CenterTextArrayAdapter.super.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
