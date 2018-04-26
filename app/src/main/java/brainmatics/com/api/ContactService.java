package brainmatics.com.api;

import java.util.List;

import brainmatics.com.entity.Contact;
import brainmatics.com.entity.SearchForm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Hendro Steven on 26/04/2018.
 */

public interface ContactService {

    @GET("/contact")
    Call<List<Contact>> getAllContact();

    @GET("/contact/{id}")
    Call<Contact> getContactById(@Path("id") String id);

    @POST("/contact")
    Call<Contact> saveContact(@Body Contact contact);

    @DELETE("/contact/{id}")
    Call<Boolean> removeContact(@Path("id") String id);

    @POST("/contact/search")
    Call<List<Contact>> findByName(@Body SearchForm form);
}
