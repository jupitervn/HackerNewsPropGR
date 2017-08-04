package vn.jupiter.propertygurutest.ui.common

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vn.jupiter.propertygurutest.ui.home.*

interface DataLoaderUseCase<T> {
    fun refreshData(): Observable<T>
    fun loadNextData(): Observable<T>
}


open class ListPresenter<T, VM: ListScreenVM<T>, V : ListView<T, VM>> (val dataLoaderUseCase: DataLoaderUseCase<T>, var initialState: VM)  : MviBasePresenter<V, VM>() {
    final override fun bindIntents() {
        val intentAction = intent { view ->
            val refreshDataAction = view.refreshDataIntent
                    .observeOn(Schedulers.computation())
                    .flatMap<ListScreenUpdate> {
                        dataLoaderUseCase
                                .refreshData()
                                .toList()
                                .toObservable()
                                .map { DataUpdate(it) }
                                .cast(ListScreenUpdate::class.java)
                                .onErrorReturn { DataError(it) }
                                .startWith(DataLoading)
                    }
            val loadMoreAction = view.loadMoreDataIntent
                    .flatMap {
                        dataLoaderUseCase
                                .loadNextData()
                                .toList()
                                .toObservable()
                                .map { DataInsert(it) }
                                .cast(ListScreenUpdate::class.java)
                                .onErrorReturn { DataError(it) }
                                .startWith(DataLoadMore)
                    }
            Observable.merge(refreshDataAction, loadMoreAction)
                    .scan(initialState, this::viewReducer)
                    .skip(1)
                    .observeOn(AndroidSchedulers.mainThread())
        }
        subscribeViewState(intentAction, ListView<T, VM>::render)
    }

    override fun detachView(retainInstance: Boolean) {
        super.detachView(retainInstance)
        //Trick to update initial state after configuration changes
        if (retainInstance) {
            initialState = viewStateObservable.blockingFirst()
        }
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