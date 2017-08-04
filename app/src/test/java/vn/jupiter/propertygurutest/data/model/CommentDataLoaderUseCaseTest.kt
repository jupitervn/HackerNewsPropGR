package vn.jupiter.propertygurutest.data.model

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.data.http.entity.CommentEntity
import java.io.IOException

class CommentDataLoaderUseCaseTest {
    @Spy
    lateinit var commentStubHttpService: CommentStubHttpService

    lateinit var commentLoaderUseCase: CommentDataLoaderUseCase
    lateinit var testObserver: TestObserver<Comment>

    private val stubStory = Story("id_1", listOf("comment_1", "comment_2", "comment_3", "comment_with_child"), 100, System.currentTimeMillis(), "", "", "")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        commentLoaderUseCase = CommentDataLoaderUseCase(stubStory, commentStubHttpService)
        testObserver = TestObserver<Comment>()
    }

    @Test
    fun test_should_return_comment_and_replies() {
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(7)
        assertThat(commentLoaderUseCase.currentPage, equalTo(1))
    }

    @Test
    fun test_deleted_comment_should_not_have_replies() {
        commentLoaderUseCase = CommentDataLoaderUseCase(stubStory.copy(kids = listOf("comment_with_child", "deleted_comment")), commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(5)
    }

    @Test
    fun test_should_return_error_if_cannot_get_comment() {
        commentLoaderUseCase = CommentDataLoaderUseCase(stubStory.copy(kids = listOf("comment_1", "error_comment")), commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun test_should_transform_delete_text() {
        commentLoaderUseCase = CommentDataLoaderUseCase(stubStory.copy(kids = listOf("deleted_comment")), commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(1)
        val receivedComment = testObserver.values()[0]
        MatcherAssert.assertThat(receivedComment.text, equalToIgnoringCase("Deleted"))
    }

    @Test
    fun test_refresh_data_return_top_page_size_first_items() {
        val commentList = mutableListOf<String>()
        for (i in 0..100) {
            commentList += "comment_$i"
        }
        val newStory = Story("id_1", commentList, 100, System.currentTimeMillis(), "", "", "")
        commentLoaderUseCase = CommentDataLoaderUseCase(newStory, commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(10)
    }

    @Test
    fun test_load_next_data_return_empty_if_no_more() {
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(7)
        testObserver = TestObserver()
        commentLoaderUseCase.loadNextData().subscribe(testObserver)
        testObserver.assertNoValues()
        testObserver.assertComplete()
    }

    @Test
    fun test_load_next_data_return_if_there_are_more() {
        val commentList = mutableListOf<String>()
        for (i in 0..100) {
            commentList += "comment_$i"
        }
        val newStory = Story("id_1", commentList, 100, System.currentTimeMillis(), "", "", "")
        commentLoaderUseCase = CommentDataLoaderUseCase(newStory, commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(commentLoaderUseCase.pageSize)
        testObserver = TestObserver.create()
        commentLoaderUseCase.loadNextData().subscribe(testObserver)
        testObserver.assertValueCount(commentLoaderUseCase.pageSize)
        assertThat(testObserver.values(), contains(
                hasProperty(Comment::id.name, equalTo("comment_10")),
                hasProperty(Comment::id.name, equalTo("comment_11")),
                hasProperty(Comment::id.name, equalTo("comment_12")),
                hasProperty(Comment::id.name, equalTo("comment_13")),
                hasProperty(Comment::id.name, equalTo("comment_14")),
                hasProperty(Comment::id.name, equalTo("comment_15")),
                hasProperty(Comment::id.name, equalTo("comment_16")),
                hasProperty(Comment::id.name, equalTo("comment_17")),
                hasProperty(Comment::id.name, equalTo("comment_18")),
                hasProperty(Comment::id.name, equalTo("comment_19"))
        ))
        verify(commentStubHttpService, times(20)).getCommentWithId(any())
    }

    @Test
    fun test_load_next_data_return_less_page_size_if_not_enough() {
        val commentList = mutableListOf<String>()
        for (i in 0..12) {
            commentList += "comment_$i"
        }
        commentList += "comment_with_child"
        val newStory = Story("id_1", commentList, 100, System.currentTimeMillis(), "", "", "")
        commentLoaderUseCase = CommentDataLoaderUseCase(newStory, commentStubHttpService)
        commentLoaderUseCase.refreshData().subscribe(testObserver)
        testObserver.assertValueCount(commentLoaderUseCase.pageSize)
        assertThat(commentLoaderUseCase.currentPage, equalTo(1))
        testObserver = TestObserver.create()
        commentLoaderUseCase.loadNextData().subscribe(testObserver)
        assertThat(commentLoaderUseCase.currentPage, equalTo(2))
        verify(commentStubHttpService, times(17)).getCommentWithId(any())
        testObserver.assertValueCount(7)
        assertThat(testObserver.values(), contains(
                hasProperty(Comment::id.name, equalTo("comment_10")),
                hasProperty(Comment::id.name, equalTo("comment_11")),
                hasProperty(Comment::id.name, equalTo("comment_12")),
                hasProperty(Comment::id.name, equalTo("comment_with_child")),
                hasProperty(Comment::id.name, equalTo("reply_1")),
                hasProperty(Comment::id.name, equalTo("reply_2")),
                hasProperty(Comment::id.name, equalTo("reply_3"))
        ))
    }
}

class CommentStubHttpService : HackerNewsHttpService {

    private val stubComment = CommentEntity("comment_1", emptyList(), "comment_text", System.currentTimeMillis(), "by", "", false)
    private val stubCommentWithKids = CommentEntity("comment_with_child", listOf("reply_1", "reply_2", "reply_3"), "comment_text", System.currentTimeMillis(), "by", "", false)
    private val deletedComment = CommentEntity("comment_1", emptyList(), null, System.currentTimeMillis(), "by", "", true)

    override fun getTopStories(): Single<List<String>> = Single.never()

    override fun getStoryWithId(id: String): Single<Story> = Single.never()

    override fun getCommentWithId(id: String): Single<CommentEntity> {
        return when {
            id.startsWith("comment_with_child") -> Single.just(stubCommentWithKids)
            id.startsWith("deleted_comment") -> Single.just(deletedComment)
            id.startsWith("error_comment") -> Single.error(IOException())
            else -> Single.just(stubComment.copy(id = id))
        }
    }

}