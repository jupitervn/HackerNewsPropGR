package vn.jupiter.propertygurutest.ui.comment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import vn.jupiter.propertygurutest.R
import vn.jupiter.propertygurutest.data.model.Comment
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.common.*

class CommentActivity : SimpleListActivity<Comment, CommentScreenVM, CommentView, CommentPresenter>() {
    companion object {
        fun launchIntent(context: Context, story: Story): Intent {
            return Intent(context, CommentActivity::class.java).apply {
                putExtra("extra_story", story)
            }
        }
    }

    val story by lazy {
        intent.getParcelableExtra<Story>("extra_story")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = story.title
    }

    override fun createPresenter(): CommentPresenter {
        return appComponent().commentComponent(CommentModule(story)).createPresenter()
    }

    override fun createAdapter(): ArrayAdapter<Comment> {
        return CommentAdapter(this)
    }
}

class CommentAdapter(context: Context, onItemClickListener: ((Comment) -> Unit)? = null) : ArrayAdapter<Comment>(context, onItemClickListener) {
    override fun onViewHolderCreate(parent: ViewGroup?, viewType: Int): BindableVH<Comment> {
        return CommentVH(layoutInflater.inflate(viewType, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        val comment = getItemAtPosition(position)
        return if (comment.commentLevel == 0) {
            R.layout.item_comment
        } else {
            R.layout.item_comment_reply
        }
    }
}

class CommentVH(itemView: View) : BindableVH<Comment>(itemView) {
    private val tvCommentText by bindView<TextView>(R.id.tv_comment_text)
    private val tvCommentTime by bindView<TextView>(R.id.tv_comment_time)
    private val tvCommentAuthor by bindView<TextView>(R.id.tv_comment_author)

    override fun bindData(data: Comment) {
        tvCommentText.text = Html.fromHtml(data.text)
        if (data.by != null) {
            tvCommentAuthor.text = context.getString(R.string.comment_author, data.by)
            tvCommentAuthor.visibility = View.VISIBLE
        } else {
            tvCommentAuthor.visibility = View.INVISIBLE
        }
        tvCommentTime.text = data.time.formatString()
    }

}

