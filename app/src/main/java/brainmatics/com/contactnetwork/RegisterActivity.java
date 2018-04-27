package brainmatics.com.contactnetwork;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import brainmatics.com.api.APIClient;
import brainmatics.com.api.UserService;
import brainmatics.com.entity.User;
import brainmatics.com.utility.MD5;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener{

    @BindView(R.id.txtFullName)
    @NotEmpty
    EditText txtFullName;

    @BindView(R.id.txtEmail)
    @NotEmpty
    @Email
    EditText txtEmail;

    @BindView(R.id.txtPassword)
    @NotEmpty
    EditText txtPassword;

    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;

    Validator validator;
    boolean isValid;

    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        userService = APIClient.getClient().create(UserService.class);
        validator = new Validator(this);
        validator.setValidationListener(this);
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

    @OnClick(R.id.btnRegister)
    public void btnRegisterOnClick(){
        if(validate()) {
            User user = new User();
            user.setFullName(txtFullName.getText().toString().trim());
            user.setEmail(txtEmail.getText().toString().trim());
            user.setPassword(MD5.hash(txtPassword.getText().toString().trim()));
            saveUser(user);
        }
    }

    private void saveUser(User user) {
        avi.show();
        Call<Object> call = userService.register(user);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                avi.hide();
//                Gson gson = new Gson();
//                JsonParser parse = new JsonParser();
//                JsonObject  obj = parse.parse(gson.toJson(response.body())).getAsJsonObject();
//                User user = gson.fromJson(obj.get("payload"),User.class);
                //Log.i("USER", "onResponse: "+ user.getFullName());
                Toast.makeText(RegisterActivity.this,"Register berhasil",Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                avi.hide();
                Log.e("ERROR",t.getMessage());
                Toast.makeText(RegisterActivity.this,"Register gagal",Toast.LENGTH_LONG).show();
                call.cancel();
            }
        });
    }

}
