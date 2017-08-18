package vn.jupiter.propertygurutest.ui.common

import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.home.HomePresenter
import vn.jupiter.propertygurutest.ui.home.HomeScreenVM
import vn.jupiter.propertygurutest.ui.home.HomeView
import java.io.IOException

class ListPresenterTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            // Tell RxAndroid to not use android main ui thread scheduler
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
        val STORY_LIST by lazy {
            val storiesList = mutableListOf<Story>()
            for (i in 1..10) {
                storiesList += Story("id_$i", emptyList(), 100, 10000000, "", "", "")
            }
            storiesList
        }
    }

    @Spy
    lateinit var viewRobot: HomeViewRobot

    @Mock
    lateinit var mockDataLoader: DataLoaderUseCase<Story>

    lateinit var homePresenter: HomePresenter
    lateinit var argumentCaptor: KArgumentCaptor<HomeScreenVM>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        argumentCaptor = argumentCaptor<HomeScreenVM>()
        whenever(mockDataLoader.refreshData()).thenReturn(Observable.fromIterable(STORY_LIST))
        whenever(mockDataLoader.loadNextData()).thenReturn(Observable.fromIterable(STORY_LIST))
        homePresenter = HomePresenter(mockDataLoader)
        homePresenter.attachView(viewRobot)
    }

    @After
    fun tearDown() {
        homePresenter.detachView(false)
    }

    @Test
    fun test_refresh_data_should_render_state_with_data() {
        viewRobot.fireRefreshDataAction()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        val values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(isLoading = true),
                HomeScreenVM(data = STORY_LIST)
        ))
    }

    @Test
    fun test_refresh_data_should_render_error_if_error_occurs() {
        val ioException = IOException()
        whenever(mockDataLoader.refreshData()).thenReturn(Observable.error(ioException))
        viewRobot.fireRefreshDataAction()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        val values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(isLoading = true),
                HomeScreenVM(error = ioException)
        ))
    }

    @Test
    fun test_load_more_should_trigger_render_more_data() {
        viewRobot.fireRefreshDataAction()
        viewRobot.fireLoadMoreDataAction()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        val expectedData = STORY_LIST.toMutableList()
        expectedData.addAll(STORY_LIST)
        val values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(isLoading = true),
                HomeScreenVM(data = STORY_LIST),
                HomeScreenVM(data = STORY_LIST, isLoading = true),
                HomeScreenVM(data = expectedData)
        ))
    }

    @Test
    fun test_load_more_error_should_render_old_data_and_error() {
        val ioException = IOException()
        whenever(mockDataLoader.loadNextData()).thenReturn(Observable.error(ioException))
        viewRobot.fireRefreshDataAction()
        viewRobot.fireLoadMoreDataAction()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        val values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(isLoading = true),
                HomeScreenVM(data = STORY_LIST),
                HomeScreenVM(data = STORY_LIST, isLoading = true),
                HomeScreenVM(data = STORY_LIST, error = ioException, isLoading = false)
        ))
    }

    @Test
    fun test_view_config_change_should_not_call_data_loader() {
        viewRobot.fireRefreshDataAction()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        var values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(isLoading = true),
                HomeScreenVM(data = STORY_LIST)
        ))
        verify(mockDataLoader, only()).refreshData()

        Mockito.reset(viewRobot)
        argumentCaptor = argumentCaptor<HomeScreenVM>()
        viewRobot.fireViewConfigurationChange()
        verify(viewRobot, atLeastOnce()).render(argumentCaptor.capture())
        verifyNoMoreInteractions(mockDataLoader)
        values = argumentCaptor.allValues
        assertThat(values, Matchers.contains(
                HomeScreenVM(data = STORY_LIST, isLoading = true),
                HomeScreenVM(data = STORY_LIST)
        ))
    }
}

open class HomeViewRobot : HomeView {
    private val refreshDataPublisher = PublishSubject.create<Boolean>()
    private val loadMoreDataPublisher = PublishSubject.create<Unit>()
    override val refreshDataIntent: Observable<Boolean> = refreshDataPublisher
    override val loadMoreDataIntent: Observable<Unit> = loadMoreDataPublisher

    override fun render(viewModel: HomeScreenVM) {
    }

    fun fireRefreshDataAction() {
        refreshDataPublisher.onNext(true)
    }

    fun fireViewConfigurationChange() {
        refreshDataPublisher.onNext(false)
    }
    fun fireLoadMoreDataAction() {
        loadMoreDataPublisher.onNext(Unit)
    }
}