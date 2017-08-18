package vn.jupiter.propertygurutest.ui.common

import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable

/**
 * @author jupiter (vu.cao.duy@gmail.com) on 8/18/17.
 */
interface ListScreenVM<T> {
    val data: List<T>
    val isLoading: Boolean
    val error: Throwable?

    fun withLoading(isLoading: Boolean): ListScreenVM<T>
    fun withData(data: Collection<T>): ListScreenVM<T>
    fun withError(error: Throwable?): ListScreenVM<T>
}

sealed class ListScreenUpdate

data class DataUpdate<out T>(val data: List<T>) : ListScreenUpdate()
data class DataInsert<out T>(val data: List<T>) : ListScreenUpdate()
object DataLoading : ListScreenUpdate()
object DataLoadMore : ListScreenUpdate()
data class DataError(val error: Throwable) : ListScreenUpdate()

interface ListView<T, in VM : ListScreenVM<T>> : MvpView {
    val refreshDataIntent: Observable<Boolean>
    val loadMoreDataIntent: Observable<Unit>

    fun render(viewModel: VM)
}