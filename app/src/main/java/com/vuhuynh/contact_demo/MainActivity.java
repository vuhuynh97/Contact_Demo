package com.vuhuynh.contact_demo;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import model.Contact;

public class MainActivity extends AppCompatActivity {
    ListView lvContact;
    ArrayAdapter<Contact> adapter;
    public static Contact selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        initEvents();
        readAllContact();
    }

    private void initEvents() {
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedContact = adapter.getItem(i);
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri uri = Uri.parse("tel:" + selectedContact.getPhone());
                intent.setData(uri);
                startActivity(intent);
            }
        });
        lvContact.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedContact = adapter.getItem(i);
                return false;
            }
        });
    }

    private void readAllContact() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null,
                null, null);
        adapter.clear();
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));


            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);

            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phone = cursor.getString(phoneIndex);

            Contact contact = new Contact();
            contact.setName(name);
            contact.setPhone(phone);
            contact.setId(contactId);
            adapter.add(contact);

        }
        cursor.close();
    }

    private void initControls() {
        lvContact = findViewById(R.id.lvContact);
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        lvContact.setAdapter(adapter);
        registerForContextMenu(lvContact);
    }

    public void clickToAddContact(View view) {
        Intent intent = new Intent(this, AddContactActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem mnuSearch = menu.findItem(R.id.mnu_search);
        SearchView searchView = (SearchView) mnuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnu_Update) {
            if (selectedContact != null) {
                Intent intent = new Intent(this, EditContactActivity.class);
                startActivity(intent);
            }
        } else if (item.getItemId() == R.id.mnu_Delete) {
            processDelete();
        }
        return super.onContextItemSelected(item);
    }

    //  TODO: implement delete method
    private void processDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm your action");
        builder.setMessage("Are you sure to delete " + selectedContact.getName() + " ?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Deleted success", Toast.LENGTH_SHORT).show();
                //  TODO: DELETE Contact
                final ArrayList ops = new ArrayList();
                final ContentResolver cr = getContentResolver();
                ops.add(ContentProviderOperation
                        .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                        .withSelection(
                                ContactsContract.RawContacts.CONTACT_ID
                                        + " = ?",
                                new String[]{selectedContact.getId()})
                        .build());


                try {
                    cr.applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //background_process();
                ops.clear();
                readAllContact();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

}
