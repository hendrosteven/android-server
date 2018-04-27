package brainmatics.com.api;

import brainmatics.com.entity.LoginForm;
import brainmatics.com.entity.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {

    @POST("/user/register")
    Call<Object> register(@Body User user);

    @POST("/user/login")
    Call<User> login(@Body LoginForm form);
}
