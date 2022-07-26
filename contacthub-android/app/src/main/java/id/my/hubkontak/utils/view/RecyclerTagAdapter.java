package id.my.hubkontak.utils.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.my.hubkontak.R;

public class RecyclerTagAdapter extends RecyclerView.Adapter<RecyclerTagAdapter.ViewHolder>  {
    private List<ItemRecyclerTag> list;
    private Context context;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemRecyclerTag item);
    }
    public RecyclerTagAdapter(List<ItemRecyclerTag> list, Context context, OnItemClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerTagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tag, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.txtTitle.setText(list.get(i).getTitle());
        viewHolder.bind(list.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView txtTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imgClose);
            txtTitle= (TextView) itemView.findViewById(R.id.itemTitle);
        }
        public void bind(final ItemRecyclerTag item, final RecyclerTagAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
