package vn.jupiter.propertygurutest.data.model

import io.reactivex.Observable
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.data.http.entity.CommentEntity
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase

class CommentDataLoaderUseCase(val story: Story, val hackerNewsHttpService: HackerNewsHttpService, val pageSize: Int = 10) : DataLoaderUseCase<Comment> {
    var currentPage = 0
    override fun refreshData(): Observable<Comment> {
        return Observable.fromIterable(story.kids)
                .doOnSubscribe {
                    currentPage = 0
                }
                .take(pageSize.toLong())
                .doOnComplete {
                    currentPage = 1
                }
                .concatMap(this::getReplyForComment)
    }

    private fun getReplyForComment(commentId: String): Observable<Comment> {
        return hackerNewsHttpService
                .getCommentWithId(commentId)
                .flatMapObservable {
                    val topCommentObs = Observable.just(convertEntity(it))
                    var replyObs = Observable.empty<Comment>()
                    if (it.kids != null && !it.kids.isEmpty()) {
                        replyObs = Observable.fromIterable(it.kids)
                                .concatMap {
                                    hackerNewsHttpService.getCommentWithId(it)
                                            .map { convertEntity(it, 1) }
                                            .toObservable()
                                }
                    }
                    Observable.merge(topCommentObs, replyObs)
                }
    }

    override fun loadNextData(): Observable<Comment> {
        return Observable.fromIterable(story.kids)
                .skip((currentPage * pageSize).toLong())
                .take(pageSize.toLong())
                .doOnComplete { currentPage++ }
                .concatMap(this::getReplyForComment)
    }

    private fun convertEntity(entity: CommentEntity, commentLevel: Int = 0): Comment {
        return if (entity.deleted) {
            Comment(entity.id, "Deleted", commentLevel, entity.time, entity.by)
        } else {
            Comment(entity.id, entity.text!!, commentLevel, entity.time, entity.by)
        }
    }
}