package vn.jupiter.propertygurutest.ui.comment

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.data.model.Comment
import vn.jupiter.propertygurutest.data.model.CommentDataLoaderUseCase
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase
import vn.jupiter.propertygurutest.ui.common.ListScreenVM
import vn.jupiter.propertygurutest.ui.common.ListView

data class CommentScreenVM(override val data: List<Comment> = emptyList(),
                           override val isLoading: Boolean = false,
                           override val error: Throwable? = null) : ListScreenVM<Comment> {
    override fun withLoading(isLoading: Boolean): CommentScreenVM {
        return copy(isLoading = isLoading)
    }

    override fun withData(data: Collection<Comment>): CommentScreenVM {
        return copy(data = data.toList())
    }

    override fun withError(error: Throwable?): CommentScreenVM {
        return copy(error = error)
    }
}


interface CommentView : ListView<Comment, CommentScreenVM>

@Module
class CommentModule(val story: Story) {
    @Provides
    fun providesCommentDataLoaderUseCase(httpService: HackerNewsHttpService): DataLoaderUseCase<Comment> {
        return CommentDataLoaderUseCase(story, httpService)
    }

    @Provides
    fun providesCommentPresenter(dataLoaderUseCase: DataLoaderUseCase<Comment>): CommentPresenter {
        return CommentPresenter(dataLoaderUseCase)
    }
}

@Subcomponent(modules = arrayOf(CommentModule::class))
interface CommentComponent {
    fun createPresenter(): CommentPresenter
}