package com.example.wzm.matrix.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix.model.Item;

import java.util.List;

public class ReportRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Item> mItems;
    private LayoutInflater mInflater;
    private OnClickListener mClickListener;

    public interface OnClickListener{
        void setItem(String item);
    }

    public ReportRecyclerViewAdapter(Context context, List<Item> items) {
        this.mInflater = LayoutInflater.from(context);
        this.mItems = items;
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public void setClickListener(ReportRecyclerViewAdapter.OnClickListener callback) {
        mClickListener = callback;
    }

    /**
     * Step1 : declare the view holder structure
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;
        View itemview;

        ViewHolder(View itemView) {
            super(itemView);
            itemview = itemView;
            mTextView = (TextView) itemView.findViewById(R.id.info_text);
            mImageView = (ImageView) itemView.findViewById(R.id.info_img);
        }
    }

    /**
     * Step 2: create holder prepare listview to show
     *
     * @param parent   the listview
     * @param viewType view type
     * @return created view holder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Step 3: render view holder on screen
     *
     * @param holder   view holder created by onCreateViewHolder
     * @param position corresponding position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mTextView.setText(mItems.get(position).getDrawable_label());
        viewHolder.mImageView.setImageResource(mItems.get(position).getDrawable_id());
        viewHolder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.setItem(mItems.get(position).getDrawable_label());
            }
        });

    }
}
