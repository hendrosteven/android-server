package brainmatics.com.brainmatics.com.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import brainmatics.com.contactnetwork.R;
import brainmatics.com.entity.Contact;

/**
 * Created by Hendro Steven on 26/04/2018.
 */

public class ContactAdapter extends ArrayAdapter<Contact> {

    private final Context context;
    private final List<Contact> data;

    public ContactAdapter(Context context, List<Contact> data) {
        super(context, R.layout.list_contact, data);
        this.context = context;
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_contact, parent, false);
        TextView nama = (TextView) rowView.findViewById(R.id.lblNama);
        TextView email = (TextView) rowView.findViewById(R.id.lblEmail);
        ImageView img = (ImageView)rowView.findViewById(R.id.imPhoto);

        Contact contact = data.get(position);
        nama.setText(contact.getFullName());
        email.setText(contact.getEmail());

        if(contact.getPhoto()!= null) {
            if(!contact.getPhoto().trim().equals("")) {
                Bitmap bm = decodeBase64(contact.getPhoto());
                img.setImageBitmap(bm);
            }
        }
        return rowView;
    }

    private Bitmap decodeBase64(String input){
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
