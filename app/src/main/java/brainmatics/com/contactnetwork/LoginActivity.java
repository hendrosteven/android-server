package brainmatics.com.contactnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.UnsupportedEncodingException;

import brainmatics.com.api.APIClient;
import brainmatics.com.api.UserService;
import brainmatics.com.entity.LoginForm;
import brainmatics.com.entity.User;
import brainmatics.com.utility.MD5;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    @BindView(R.id.txtEmail)
    EditText txtEmail;

    @BindView(R.id.txtPassword)
    EditText txtPassword;

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;

    UserService userService;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        userService = APIClient.getClient().create(UserService.class);
        sharedpreferences = getSharedPreferences("APP_PREFERENCE", Context.MODE_PRIVATE);
        checkLogin();
    }

    private void checkLogin() {
       String token = sharedpreferences.getString("TOKEN",null);
       if(token!=null){
           Intent main = new Intent(LoginActivity.this, MainActivity.class);
           startActivity(main);
           finish();
       }
    }

    @OnClick(R.id.btnLogin)
    public void btnLoginOnClick(){
        onProsesStart();
        LoginForm form = new LoginForm();
        form.setEmail(txtEmail.getText().toString().trim());
        final String md5 = MD5.hash(txtPassword.getText().toString().trim());
        form.setPassword(md5);

        Call<User> call = userService.login(form);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                onProsesEnd();
                User loggedUser = response.body();
                Log.i("OUTPUT", "onResponse: "+ new Gson().toJson(response.body()));
                if(loggedUser!=null) {
                    //create base64 token
                    String basicAuth = loggedUser.getEmail() + ":" + md5;
                    try {
                        basicAuth = "Basic "+ Base64.encodeToString(basicAuth.getBytes("UTF-8"),
                                Base64.NO_WRAP);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //save token to preference
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("TOKEN", basicAuth);
                    editor.commit();
                    //call MainActivity
                    Intent main = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(main);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this,"Login gagal",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ERROR",t.getMessage());
                Toast.makeText(LoginActivity.this,"Login gagal",Toast.LENGTH_LONG).show();
                call.cancel();
                onProsesEnd();
            }
        });

    }

    private void onProsesStart(){
        avi.show();
        txtEmail.setEnabled(false);
        txtPassword.setEnabled(false);
        btnLogin.setText("Process..");
        btnLogin.setEnabled(false);
    }

    private void onProsesEnd(){
        avi.hide();
        txtEmail.setEnabled(true);
        txtPassword.setEnabled(true);
        txtPassword.setText("");
        btnLogin.setText("Login");
        btnLogin.setEnabled(true);
    }

    @OnClick(R.id.lblRegister)
    public void lblRegisterClick(){
        Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(register);
    }
}
