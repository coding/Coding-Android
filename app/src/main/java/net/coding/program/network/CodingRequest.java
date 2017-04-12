package net.coding.program.network;

import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.wiki.Wiki;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface CodingRequest {

    @GET("user/{user}/project/{project}/wikis")
    Observable<HttpResult<List<Wiki>>> getWikis(@Path("user") String user, @Path("project") String project);

}
