package com.droidmentor.mlkitbarcodescan.ContactsListing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmentor.mlkitbarcodescan.ListingSetup.CustomItemClickListener;
import com.droidmentor.mlkitbarcodescan.ListingSetup.CustomRecyclerViewAdapter;
import com.droidmentor.mlkitbarcodescan.LocalData.ContactDetail;
import com.droidmentor.mlkitbarcodescan.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jaison.
 */
public class ContactsListingAdapter extends CustomRecyclerViewAdapter {

    String TAG = "ContactsListingAdapter";

    Context context;
    ArrayList<ContactDetail> contactDetailArrayList;
    CustomItemClickListener customItemClickListener;


    public ContactsListingAdapter(Activity activity, ArrayList<ContactDetail> contactsData,
                                  CustomItemClickListener customItemClickListener) {
        super(activity, contactsData, customItemClickListener);
        this.context = activity;
        this.contactDetailArrayList = contactsData;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts_list, parent, false);
        final ViewHolder mViewHolder = new ViewHolder(mView);
        mView.setOnClickListener(v -> {
            if (customItemClickListener != null)
                customItemClickListener.onItemClick(v, mViewHolder.getAdapterPosition());
        });
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder type_item = (ViewHolder) holder;
        final ContactDetail contactDetail = getListing().get(position);

        setText(type_item.tvText,contactDetail.getText());
        setText(type_item.tvType,contactDetail.getType().toString());
        setDrawable(type_item.ivDelete,"enable_delete",4);

        type_item.ivDelete.setColorFilter(ContextCompat.getColor(context,R.color.cl_delete_drawable_color));

        // Redirects the user'sclick action to the respective callbacks

        type_item.cvDetails.setOnClickListener(view -> {
            if (customItemClickListener != null)
                customItemClickListener.onItemClick(view, type_item.getAdapterPosition());
        });

        type_item.ivDelete.setOnClickListener(view -> {
            if (customItemClickListener != null)
                customItemClickListener.onItemClick(view, type_item.getAdapterPosition(),ActionItem.DELETE);
        });
    }

    public void setText(TextView tvSelectedControl, String textValue)
    {
        if(TextUtils.isEmpty(textValue))
            tvSelectedControl.setVisibility(View.GONE);
        else
        {
            tvSelectedControl.setVisibility(View.VISIBLE);
            tvSelectedControl.setText(textValue);
            Log.d(TAG, "setText: "+textValue);
        }
    }

    private void setDrawable(ImageView imageView, String value, int index)
    {
        String activeColor[]={"","#65AC58","#FF8B00","#0492FF","#EA0404"};
        String inActiveColor[]={"","#D1D1D1","#D1D1D1","#D1D1D1","#EA0404"};

        if(TextUtils.isEmpty(value))
        {
            imageView.setClickable(false);
            imageView.setColorFilter(Color.parseColor(inActiveColor[index]));

        }
        else
        {
            imageView.setClickable(true);
            imageView.setColorFilter(Color.parseColor(activeColor[index]));

        }
    }


    public enum ActionItem
    {
        CALL, MAIL, WEB, DELETE
    }
    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public ArrayList<ContactDetail> getListing() {
        return super.getListing();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvText)
        TextView tvText;
        @BindView(R.id.tvType)
        TextView tvType;
        @BindView(R.id.ivDelete)
        ImageView ivDelete;
        @BindView(R.id.cvDetails)
        CardView cvDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
