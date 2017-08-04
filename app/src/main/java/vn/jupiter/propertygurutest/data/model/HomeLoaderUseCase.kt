package vn.jupiter.propertygurutest.data.model

import io.reactivex.Observable
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase

class HomeLoaderUseCase(val storyHttpService: HackerNewsHttpService, val pageSize: Int = 10) : DataLoaderUseCase<Story> {
    private val cacheData = mutableListOf<String>()
    private var currentPage = 0

    override fun refreshData(): Observable<Story> =
            storyHttpService.getTopStories()
                    .doOnSubscribe {
                        currentPage = 0
                    }
                    .doOnSuccess {
                        cacheData.clear()
                        cacheData.addAll(it)
                        currentPage = 1
                    }
                    .flattenAsObservable { it }
                    .take(pageSize.toLong())
                    .concatMap {
                        storyHttpService
                                .getStoryWithId(it)
                                .toObservable()
                    }

    override fun loadNextData(): Observable<Story> {
        return if (cacheData.isNotEmpty()) {
            Observable.fromIterable(cacheData)
        } else {
            storyHttpService.getTopStories()
                    .flattenAsObservable { it }
        }
                .skip((currentPage * pageSize).toLong())
                .take(pageSize.toLong())
                .doOnComplete { currentPage++ }
                .concatMap {
                    storyHttpService
                            .getStoryWithId(it)
                            .toObservable()
                }

    }
}