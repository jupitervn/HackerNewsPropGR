package vn.jupiter.propertygurutest.data.http

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import vn.jupiter.propertygurutest.data.http.entity.CommentEntity
import vn.jupiter.propertygurutest.data.model.Story

interface HackerNewsHttpService {
    @GET("topstories.json")
    fun getTopStories(): Single<List<String>>
    @GET("item/{itemId}.json")
    fun getStoryWithId(@Path("itemId") id: String): Single<Story>
    @GET("item/{itemId}.json")
    fun getCommentWithId(@Path("itemId") id: String): Single<CommentEntity>
}