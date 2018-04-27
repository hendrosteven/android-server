package brainmatics.com.api;

import java.util.List;

import brainmatics.com.entity.Contact;
import brainmatics.com.entity.SearchForm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Hendro Steven on 26/04/2018.
 */

public interface ContactService {

    @GET("/contact")
    Call<List<Contact>> getAllContact(@Header("Authorization") String token);

    @GET("/contact/{id}")
    Call<Contact> getContactById(@Header("Authorization") String token,@Path("id") String id);

    @POST("/contact")
    Call<Contact> saveContact(@Header("Authorization") String token, @Body Contact contact);

    @DELETE("/contact/{id}")
    Call<Boolean> removeContact(@Header("Authorization") String token, @Path("id") String id);

    @POST("/contact/search")
    Call<List<Contact>> findByName(@Header("Authorization") String token, @Body SearchForm form);
}
