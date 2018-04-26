package brainmatics.com.contactnetwork;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import com.wang.avi.AVLoadingIndicatorView;

import brainmatics.com.api.APIClient;
import brainmatics.com.api.ContactService;
import brainmatics.com.entity.Contact;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputActivity extends AppCompatActivity implements Validator.ValidationListener{

    @BindView(R.id.txtFullName)
    @NotEmpty
    EditText txtFullName;

    @BindView(R.id.txtEmail)
    @NotEmpty
    @Email
    EditText txtEmail;

    @BindView(R.id.txtPhone)
    @NotEmpty
    EditText txtPhone;

    @BindView(R.id.txtAddress)
    @NotEmpty
    EditText txtAddress;

    @BindView(R.id.imgPhoto)
    ImageView imgPhoto;

    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;

    Validator validator;
    boolean isValid;

    private Bitmap bitmap;

    static final int CAMERA_INTENT = 1999;
    static final int GALLERY_INTENT = 1888;

    ContactService contactService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        ButterKnife.bind(this);
        contactService = APIClient.getClient().create(ContactService.class);

        validator = new Validator(this);
        validator.setValidationListener(this);

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.CAMERA",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @OnClick(R.id.btnSave)
    public void btnSaveOnClick(){
        if(validate()) {
            Contact contact = new Contact();
            contact.setFullName(txtFullName.getText().toString().trim());
            contact.setEmail(txtEmail.getText().toString().trim());
            contact.setPhone(txtPhone.getText().toString().trim());
            contact.setAddress(txtAddress.getText().toString().trim());
            if(bitmap!=null) {
                contact.setPhoto(encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100));
            }else{
                contact.setPhoto(null);
            }
            saveContact(contact);

        }
    }

    public void saveContact(Contact contact){
        avi.show();
        Call<Contact> call = contactService.saveContact(contact);
        call.enqueue(new Callback<Contact>() {
            @Override
            public void onResponse(Call<Contact> call,
                                   Response<Contact> response) {
                avi.hide();
                txtFullName.setText("");
                txtEmail.setText("");
                txtPhone.setText("");
                txtAddress.setText("");
                Toast.makeText(InputActivity.this, "Contact Tersimpan", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onFailure(Call<Contact> call, Throwable t) {
                avi.hide();
                Log.e("ERROR",t.getMessage());
                call.cancel();
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        isValid = true;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            isValid = false;
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean validate() {
        if (validator != null)
            validator.validate();
        return isValid;
    }

    @OnClick(R.id.imgPhoto)
    public void imgPhotoOnClick(){
        PopupMenu popup = new PopupMenu(InputActivity.this, imgPhoto, Gravity.CENTER);
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString().toUpperCase().trim()){
                    case "AMBIL DARI GALLERY" :
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, GALLERY_INTENT);
                        break;
                    case "AMBIL DENGAN CAMERA" :
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_INTENT);
                        break;
                    default:break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_INTENT && resultCode == RESULT_OK) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            bitmap = (Bitmap) data.getExtras().get("data");
            bitmap = getResizedBitmap(bitmap,100,100);
            imgPhoto.setImageBitmap(bitmap);
        } else  if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {
            InputStream stream = null;
            try {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                bitmap = getResizedBitmap(bitmap,100,100);
                imgPhoto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("GALLERY ERROR", e.getMessage());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat,
                                  int quality){
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

}
