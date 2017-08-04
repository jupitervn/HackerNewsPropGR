package vn.jupiter.propertygurutest.data.model

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import java.io.IOException

class HomeLoaderUseCaseTest {
    @Mock
    lateinit var mockHttpService: HackerNewsHttpService

    lateinit var homeLoader: HomeLoaderUseCase
    lateinit var testObserver: TestObserver<Story>

    private val stubStory = Story("id_1", emptyList(), 100, 10000000, "", "", "")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        homeLoader = HomeLoaderUseCase(mockHttpService)
        whenever(mockHttpService.getTopStories())
                .thenReturn(Single.just(listOf("id_1", "id_2", "id_3")))
        whenever(mockHttpService.getStoryWithId(any()))
                .thenReturn(Single.just(stubStory))
        testObserver = TestObserver<Story>()
    }

    @Test
    fun test_load_data_should_return_data_from_http() {
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertValues(stubStory, stubStory, stubStory)
        testObserver.assertValueCount(3)
    }

    @Test
    fun test_load_data_should_return_error_if_http_error() {
        whenever(mockHttpService.getTopStories()).thenReturn(Single.error(IOException()))
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun test_load_data_should_return_error_if_one_data_fail() {
        whenever(mockHttpService.getStoryWithId(eq("id_2"))).thenReturn(Single.error(IOException()))
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun test_refresh_data_return_top_page_size_first_items() {
        val listStoryIds = mutableListOf<String>()
        for (i in 0..100) {
            listStoryIds += "id_$i"

        }
        whenever(mockHttpService.getTopStories())
                .thenReturn(Single.just(listStoryIds))
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(10)
    }

    @Test
    fun test_load_next_data_return_empty_if_no_more() {
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertValues(stubStory, stubStory, stubStory)
        testObserver.assertValueCount(3)
        testObserver = TestObserver()
        homeLoader.loadNextData().subscribe(testObserver)
        testObserver.assertNoValues()
        testObserver.assertComplete()
    }

    @Test
    fun test_load_next_data_return_if_there_are_more() {
        val listStoryIds = mutableListOf<String>()
        for (i in 0..100) {
            listStoryIds += "id_$i"

        }
        whenever(mockHttpService.getTopStories())
                .thenReturn(Single.just(listStoryIds))
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(10)
        homeLoader.loadNextData().subscribe(testObserver)
        verify(mockHttpService).getTopStories()
        testObserver.assertValueCount(homeLoader.pageSize)
        homeLoader.loadNextData().subscribe(testObserver)
        testObserver.assertValueCount(homeLoader.pageSize)
    }

    @Test
    fun test_load_next_data_return_less_page_size_if_not_enough() {
        val listStoryIds = mutableListOf<String>()
        for (i in 0..15) {
            listStoryIds += "id_$i"

        }
        whenever(mockHttpService.getTopStories())
                .thenReturn(Single.just(listStoryIds))
        homeLoader.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(10)
        testObserver = TestObserver()
        homeLoader.loadNextData().subscribe(testObserver)
        testObserver.assertValueCount(6)
        verify(mockHttpService).getTopStories()
    }
}