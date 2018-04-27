package brainmatics.com.contactnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import brainmatics.com.api.APIClient;
import brainmatics.com.api.ContactService;
import brainmatics.com.brainmatics.com.adapter.ContactAdapter;
import brainmatics.com.entity.Contact;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.listContact)
    ListView listContact;

    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;

    List<Contact> contacts = new ArrayList<Contact>();

    ContactService contactService;
    SharedPreferences sharedpreferences;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        contactService = APIClient.getClient().create(ContactService.class);
        registerForContextMenu(listContact);
        sharedpreferences = getSharedPreferences("APP_PREFERENCE", Context.MODE_PRIVATE);
        token = sharedpreferences.getString("TOKEN",null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.mnuDelete:
                Contact contact = (Contact)listContact.getItemAtPosition(info.position);
                deleteContact(contact.getId());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteContact(String id){
        avi.show();
        Call<Boolean> call = contactService.removeContact(token,id);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call,
                                   Response<Boolean> response) {
                avi.hide();
                if(response.body()){
                    Toast.makeText(MainActivity.this,"Contact terhapus",Toast.LENGTH_LONG).show();
                    loadAllContact();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                avi.hide();
                Log.e("ERROR",t.getMessage());
                call.cancel();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("INFO", "onResume: Load All Contact");
        loadAllContact();
    }

    private void loadAllContact(){
        avi.show();
        Call<List<Contact>> call = contactService.getAllContact(token);
        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call,
                                   Response<List<Contact>> response) {
                avi.hide();
                contacts = response.body();
                listContact.setAdapter(new ContactAdapter(MainActivity.this,contacts));
            }
            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                avi.hide();
                Log.e("ERROR",t.getMessage());
                call.cancel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuAddContact:
                Intent intent = new Intent(this, InputActivity.class);
                startActivity(intent);
                break;
            case R.id.mnuInfo:
                Toast.makeText(this,"Info Aplikasi",Toast.LENGTH_LONG).show();
                break;
            case R.id.mnuLogout:
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

}
