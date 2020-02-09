package ejm.adminclient;

import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;



/**
 * @author Ken Finnigan
 */
public class AdminClient {
    private String url;

    public AdminClient(String url) {
        this.url = url;
    }

    public Category getCategory(final Integer categoryId) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath("/admin/category/" + categoryId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String jsonResponse =
                Request
                    .Get(uriBuilder.toString())
                    .execute()
                        .returnContent().asString();

        if (jsonResponse.isEmpty()) {
            return null;
        }

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(jsonResponse, Category.class);
    }

    public Category[] allCategories() throws IOException{
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath("/admin/category/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String jsonResponse =
                Request
                    .Get(uriBuilder.toString())
                    .execute()
                        .returnContent().asString();

        if (jsonResponse.isEmpty()) {
            return null;
        }

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(jsonResponse, Category[].class);
    }

    public Category addCategory(final Category category) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath("/admin/category/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        String categoryAsJson = gson.toJson(category);

        String jsonResponse =
            Request
                .Post(uriBuilder.toString())
                .bodyString(categoryAsJson, ContentType.APPLICATION_JSON)
                .execute()
                    .returnContent().asString();

        if (jsonResponse.isEmpty()) {
            return null;
        }

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(jsonResponse, Category.class);
    }

    public Category updateCategory(final Integer categoryId, final Category category) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath("/admin/category/" + categoryId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        String categoryAsJson = gson.toJson(category);

        String jsonResponse =
                Request
                        .Put(uriBuilder.toString())
                        .bodyString(categoryAsJson, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnContent().asString();

        if (jsonResponse.isEmpty()) {
            return null;
        }

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(jsonResponse, Category.class);
    }

    public void deleteCategory(final Integer categoryId) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath("/admin/category/" + categoryId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        Request
                .Delete(uriBuilder.toString())
                .execute();
    }
}
