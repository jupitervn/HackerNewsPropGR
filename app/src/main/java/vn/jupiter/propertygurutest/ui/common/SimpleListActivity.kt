package vn.jupiter.propertygurutest.ui.common

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_simple_list.*
import vn.jupiter.propertygurutest.R
import vn.jupiter.propertygurutest.ui.home.ListScreenVM
import vn.jupiter.propertygurutest.ui.home.ListView
import java.util.concurrent.TimeUnit

abstract class SimpleListActivity<T, VM : ListScreenVM<T>, V : ListView<T, VM>, P : ListPresenter<T, VM, V>> : MviActivity<V, P>(), ListView<T, VM> {
    private val viewAttachedIntent = PublishSubject.create<Unit>()
    private val loadMorePublisher = PublishSubject.create<Unit>()
    private val adapter = createAdapter()

    override lateinit var refreshDataIntent: Observable<Unit>
    override val loadMoreDataIntent: Observable<Unit> = loadMorePublisher
            .debounce(400, TimeUnit.MILLISECONDS)

    private val layoutManager by lazy {
        LinearLayoutManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        refreshDataIntent = Observable.merge(viewAttachedIntent, swipe_refresh_layout.refreshes())
        rv_stories.layoutManager = layoutManager
        if (rv_stories.adapter == null) {
            rv_stories.adapter = adapter
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewAttachedIntent.onNext(Unit)
        }
    }

    override fun onStart() {
        super.onStart()
        rv_stories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (adapter.itemCount > 0 && lastVisibleItem >= adapter.itemCount - 3) {
                        loadMorePublisher.onNext(Unit)
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        rv_stories.clearOnScrollListeners()
    }

    override fun render(viewModel: VM) {
        Log.d("D.Vu", "Render ${viewModel.data.size}")
        swipe_refresh_layout.isRefreshing = viewModel.isLoading
        adapter.setData(viewModel.data)
        viewModel.error?.let {
            it.printStackTrace()
            renderError(it)
        }
    }

    open protected fun renderError(error: Throwable) = Toast.makeText(this, getString(R.string.list_general_error), Toast.LENGTH_LONG).show()

    abstract fun createAdapter(): ArrayAdapter<T>

//    abstract fun <Any, T> RecyclerView.Adapter<*>.setData(data: List<T>)
}



