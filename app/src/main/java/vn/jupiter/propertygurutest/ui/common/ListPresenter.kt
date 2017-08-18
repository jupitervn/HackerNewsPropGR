package vn.jupiter.propertygurutest.ui.common

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface DataLoaderUseCase<T> {
    fun refreshData(): Observable<T>
    fun loadNextData(): Observable<T>
}

open class ListPresenter<T, VM: ListScreenVM<T>, V : ListView<T, VM>> (val dataLoaderUseCase: DataLoaderUseCase<T>, var initialState: VM)  : MviBasePresenter<V, VM>() {
    private val cachedListData = mutableListOf<T>()
    final override fun bindIntents() {
        val intentAction = intent { view ->
            val refreshDataAction = view.refreshDataIntent
                    .observeOn(Schedulers.computation())
                    .switchMap { isFreshView ->
                        if (!isFreshView) {
                            Observable.just(cachedListData)
                        } else {
                            dataLoaderUseCase
                                    .refreshData()
                                    .toList()
                                    .toObservable()
                                    .doOnNext {
                                        cachedListData.clear()
                                        cachedListData.addAll(it)
                                    }
                        }
                                .map { DataUpdate(it) }
                                .cast(ListScreenUpdate::class.java)
                                .onErrorReturn {
                                    it.printStackTrace()
                                    DataError(it)
                                }
                                .startWith(DataLoading)
                    }
            val loadMoreAction = view.loadMoreDataIntent
                    .observeOn(Schedulers.computation())
                    .switchMap {
                        dataLoaderUseCase
                                .loadNextData()
                                .toList()
                                .toObservable()
                                .map { DataInsert(it) }
                                .cast(ListScreenUpdate::class.java)
                                .onErrorReturn {
                                    DataError(it)
                                }
                                .startWith(DataLoadMore)
                    }
            Observable.merge(refreshDataAction, loadMoreAction)
                    .scan(initialState, this::viewReducer)
                    .skip(1)
                    .doOnNext { initialState = it }
                    .observeOn(AndroidSchedulers.mainThread())
        }
        subscribeViewState(intentAction, ListView<T, VM>::render)
    }

    open protected fun viewReducer(viewState: VM, update: ListScreenUpdate): VM {
        return when (update) {
            is DataLoading -> viewState.withLoading(true).withError(null)
            is DataLoadMore -> viewState.withLoading(true)
            is DataInsert<*> -> {
                val newData = viewState.data.toMutableList()
                newData.addAll(update.data as Collection<T>)
                viewState.withData(newData).withLoading(false).withError(null)
            }
            is DataUpdate<*> -> viewState.withData(update.data as Collection<T>).withLoading(false).withError(null)
            is DataError -> viewState.withLoading(false).withError(update.error)
        } as VM
    }

}