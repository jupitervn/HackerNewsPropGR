package vn.jupiter.propertygurutest.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import vn.jupiter.propertygurutest.R
import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.comment.CommentActivity
import vn.jupiter.propertygurutest.ui.common.*

class HomeActivity : SimpleListActivity<Story, HomeScreenVM, HomeView, HomePresenter>() {

    override fun createAdapter(): ArrayAdapter<Story> {
        return StoryAdapter(this) { story ->
            startActivity(CommentActivity.launchIntent(this, story))
        }
    }

    override fun createPresenter(): HomePresenter {
        return getApp().appComponent.homeComponent().createPresenter()
    }
}

class StoryAdapter(context: Context, onItemClickListener: ((Story) -> Unit)?) : ArrayAdapter<Story>(context, onItemClickListener) {
    override fun onViewHolderCreate(parent: ViewGroup?, viewType: Int): BindableVH<Story> {
        return StoryVH(layoutInflater.inflate(R.layout.item_story, parent, false))
    }
}

class StoryVH(itemView: View) : BindableVH<Story>(itemView) {
    private val tvStoryTitle by bindView<TextView>(R.id.tv_story_title)
    private val tvStoryTime by bindView<TextView>(R.id.tv_story_time)
    private val tvStoryAuthor by bindView<TextView>(R.id.tv_story_author)
    private val tvStoryPoints by bindView<TextView>(R.id.tv_story_point)

    override fun bindData(data: Story) {
        tvStoryTitle.text = data.title
        tvStoryAuthor.text = context.getString(R.string.story_author, data.by)
        tvStoryPoints.text = context.getString(R.string.story_points, data.score)
        tvStoryTime.text = data.time.formatString()
    }
}
