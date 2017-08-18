package vn.jupiter.propertygurutest.ui.home

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.data.model.HomeLoaderUseCase
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase
import vn.jupiter.propertygurutest.ui.common.ListScreenVM
import vn.jupiter.propertygurutest.ui.common.ListView

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