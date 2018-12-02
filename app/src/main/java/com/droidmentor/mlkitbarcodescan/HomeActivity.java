package com.droidmentor.mlkitbarcodescan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.droidmentor.mlkitbarcodescan.BarcodeScanner.BarcodeScannerActivity;
import com.droidmentor.mlkitbarcodescan.ContactsListing.ContactsListingAdapter;
import com.droidmentor.mlkitbarcodescan.ListingSetup.CustomItemClickListener;
import com.droidmentor.mlkitbarcodescan.LocalData.ContactDetail;
import com.droidmentor.mlkitbarcodescan.LocalData.DBHandler;
import com.droidmentor.mlkitbarcodescan.Util.AlertDialog.AlertDialogHelper;
import com.droidmentor.mlkitbarcodescan.Util.AlertDialog.AlertDialogModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    String TAG = "HomeActivity";

    private Toast toast;

    public final static int BARCODE_RESULT = 1;
    DBHandler dbHandler;
    @BindView(R.id.rvContactsList)
    RecyclerView rvContactsList;
    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;

    ArrayList<ContactDetail> contactDetailArrayList;
    LinearLayoutManager layoutManager;

    ContactsListingAdapter contactsListingAdapter;
    AlertDialogHelper alertDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        dbHandler = new DBHandler(this);
        alertDialogHelper=new AlertDialogHelper(this);
        listSetup();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(dbHandler!=null&&contactsListingAdapter!=null)
        {
            contactDetailArrayList=(ArrayList<ContactDetail>) dbHandler.getAccountsList();
            loadAdapter(contactDetailArrayList);
        }
    }

    public void listSetup()
    {
        contactDetailArrayList= (ArrayList<ContactDetail>) dbHandler.getAccountsList();
        Log.d(TAG, "listSetup: "+contactDetailArrayList.size());
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvContactsList.setLayoutManager(layoutManager);
        contactsListingAdapter=new ContactsListingAdapter(this,contactDetailArrayList,getItemCLickListener());
        rvContactsList.setAdapter(contactsListingAdapter);
        Log.d(TAG, "listSetup: item count"+contactsListingAdapter.getItemCount());
    }

    public void loadAdapter(final ArrayList<ContactDetail> itemsList) {

            rvContactsList.post(() -> {
                rvContactsList.getRecycledViewPool().clear();
                contactsListingAdapter.updateListing(itemsList);
                contactsListingAdapter.notifyDataSetChanged();
            });
    }

    @OnClick(R.id.fabAdd)
    public void onViewClicked() {
        startActivityForResult(BarcodeScannerActivity.getStartingIntent(HomeActivity.this),BARCODE_RESULT);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode==BARCODE_RESULT){
            String barcode = data.getStringExtra(BarcodeScannerActivity.RETURN_BARCODE);
            if (barcode!=null){
                showToast(barcode);
            }
        }
        else if (resultCode==RESULT_CANCELED && requestCode==BARCODE_RESULT){
            String barcode = data.getStringExtra(BarcodeScannerActivity.RETURN_BARCODE);
            if (barcode!=null){
                showToast("Already added: " + barcode);
            }
        }
    }

    public CustomItemClickListener getItemCLickListener()
    {
        CustomItemClickListener customItemClickListener=new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }

            @Override
            public void onItemClick(View v, int position, ContactsListingAdapter.ActionItem actionItem) {
                switch(actionItem)
                {
                    case DELETE:
                        // Get confirmation and delete
                        AlertDialogModel deleteAlertDialog=new AlertDialogModel();
                        deleteAlertDialog.setTitle("Delete");
                        deleteAlertDialog.setDesc("Are you sure you want to delete this item?");
                        deleteAlertDialog.setCancelable(true);
                        deleteAlertDialog.setPositiveText("Delete");
                        deleteAlertDialog.setNegativeText("Cancel");
                        alertDialogHelper.showAlertDialog(deleteAlertDialog, new AlertDialogHelper.AlertDialogListener() {
                            @Override
                            public void onPositiveClick(int from) {
                                if(dbHandler!=null&&contactsListingAdapter!=null)
                                {
                                    dbHandler.deleteAccountDetails(contactDetailArrayList.get(position).getId());
                                    contactDetailArrayList=(ArrayList<ContactDetail>) dbHandler.getAccountsList();
                                    loadAdapter(contactDetailArrayList);
                                }

                            }

                            @Override
                            public void onNegativeClick(int from) {

                            }

                            @Override
                            public void onNeutralClick(int from) {

                            }
                        });

                        break;

                }
            }
        };

        return customItemClickListener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
    }

    public void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
