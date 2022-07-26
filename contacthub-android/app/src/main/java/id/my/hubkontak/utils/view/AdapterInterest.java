package id.my.hubkontak.utils.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.my.hubkontak.R;

public class AdapterInterest extends BaseAdapter {
    private final String TAG = AdapterInterest.class.getSimpleName();
    public List<ItemInterest> listInterest;

    public Context context;
    public ArrayList<ItemInterest> arraylist;

    private static final int resource = R.layout.item_list_interest;
    public class ViewHolder {
        TextView txtJudul;
        CheckBox chk1;

    }

    public AdapterInterest(List<ItemInterest> apps, Context context) {
        this.listInterest = apps;
        this.context = context;
        arraylist = new ArrayList<ItemInterest>();
        arraylist.addAll(listInterest);

    }


    @Override
    public int getCount() {
        return listInterest.size();
    }

    @Override
    public ItemInterest getItem(int position) {
        return listInterest.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View rowView = convertView;
        final ViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(resource, null);
            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.txtJudul = (TextView) rowView.findViewById(R.id.judul);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemInterest item = listInterest.get(position);
        viewHolder.txtJudul.setText(item.getId());
        if (item.getId().equals("grupku")){
            viewHolder.chk1.setChecked(false);
        }else{
            viewHolder.chk1.setChecked(item.isCheckbox());
        }


        viewHolder.chk1.setVisibility(View.VISIBLE);
        viewHolder.chk1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i=0;i<arraylist.size();i++){
                    if (arraylist.get(i).getId().equals(item.getId())){
                        arraylist.get(i).setCheckbox(viewHolder.chk1.isChecked());
                        //listKontak.get(position).setCheckbox(viewHolder.chk1.isChecked());
                        break;
                    }
                }
                for (int i = 0; i< listInterest.size(); i++){
                    if (listInterest.get(i).getId().equals(item.getId())){
                        listInterest.get(i).setCheckbox(viewHolder.chk1.isChecked());
                        break;
                    }
                }
            }
        });
        return rowView;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        listInterest.clear();
        if (charText.length() == 0) {
            listInterest.addAll(arraylist);

        } else {
            for (ItemInterest postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listInterest.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}