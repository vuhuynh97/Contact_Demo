package com.vuhuynh.contact_demo;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class EditContactActivity extends AppCompatActivity {
    EditText edtName, edtPhone;
    private Uri dataUri = ContactsContract.Data.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        initControls();
        setControlValue();
    }

    private void setControlValue() {
        edtName.setText(MainActivity.selectedContact.getName());
        edtPhone.setText(MainActivity.selectedContact.getPhone());
    }

    private void initControls() {
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
    }

    public void clickToUpdate(View view) {
        String name = edtName.getText().toString();
        String phone = edtPhone.getText().toString();
        String id = MainActivity.selectedContact.getId();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Name
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
        builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(id),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        ops.add(builder.build());

        // Number
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
        builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "=?",
                new String[]{String.valueOf(id), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                });
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        ops.add(builder.build());

        // Update
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Edit contact success", Toast.LENGTH_LONG).show();
        clickToCancel(view);
    }

    public void clickToCancel(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
