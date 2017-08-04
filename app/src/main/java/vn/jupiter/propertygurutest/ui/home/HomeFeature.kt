package vn.jupiter.propertygurutest.ui.home

import com.hannesdorfmann.mosby3.mvp.MvpView
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.reactivex.Observable
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.data.model.HomeLoaderUseCase
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase

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
    val refreshDataIntent: Observable<Unit>
    val loadMoreDataIntent: Observable<Unit>

    fun render(viewModel: VM)
}

data class HomeScreenVM(override val data: List<Story> = emptyList(),
                        override val isLoading: Boolean = false,
                        override val error: Throwable? = null) : ListScreenVM<Story> {
    override fun withLoading(isLoading: Boolean): ListScreenVM<Story> {
        return copy(isLoading = isLoading)
    }

    override fun withData(data: Collection<Story>): ListScreenVM<Story> {
        return copy(data = data.toList())
    }

    override fun withError(error: Throwable?): ListScreenVM<Story> {
        return copy(error = error)
    }
}


interface HomeView : ListView<Story, HomeScreenVM>

@Module
class HomeModule {
    @Provides
    fun providesHomeDataLoaderUseCase(httpService: HackerNewsHttpService): DataLoaderUseCase<Story> {
        return HomeLoaderUseCase(httpService)
    }

    @Provides
    fun provideHomePresenter(dataLoaderUseCase: DataLoaderUseCase<Story>): HomePresenter {
        return HomePresenter(dataLoaderUseCase)
    }
}

@Subcomponent(modules = arrayOf(HomeModule::class))
interface HomeComponent {
    fun createPresenter(): HomePresenter
}