package net.coding.program.network;

import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.wiki.Wiki;

import java.util.List;
import java.util.Map;

import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface CodingRequest {

    @GET("user/{user}/project/{project}/wikis")
    Observable<HttpResult<List<Wiki>>> getWikis(@Path("user") String user, @Path("project") String project);

    @DELETE("user/{user}/project/{project}/wiki/{iid}")
    Observable<HttpResult<Boolean>> deleteWiki(@Path("user") String user, @Path("project") String project, @Path("iid") int iid);

    @FormUrlEncoded
    @POST("user/{user}/project/{project}/wiki")
    Observable<HttpResult<Wiki>> postWiki(@Path("user") String user, @Path("project") String project,
                                          @FieldMap Map<String, String> map);
}
