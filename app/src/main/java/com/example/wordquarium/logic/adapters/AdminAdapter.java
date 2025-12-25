package com.example.wordquarium.logic.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wordquarium.R;
import java.util.*;


public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {


    public interface ClickListener { void onClick(Item item); }


    private List<Item> items = new ArrayList<>();
    private final ClickListener listener;


    public AdminAdapter(ClickListener listener) {
        this.listener = listener;
    }


    public void setData(List<Item> data) {
        items = data;
        notifyDataSetChanged();
    }


    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin, p, false));
    }


    @Override
    public void onBindViewHolder(VH h, int pos) {
        Item i = items.get(pos);
        h.text.setText(i.text);
        h.itemView.setOnClickListener(v -> listener.onClick(i));
    }


    @Override public int getItemCount() { return items.size(); }


    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        VH(View v) { super(v); text = v.findViewById(R.id.tvText); }
    }
}