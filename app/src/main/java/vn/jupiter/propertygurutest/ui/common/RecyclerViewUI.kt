package vn.jupiter.propertygurutest.ui.common

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.properties.ReadOnlyProperty

abstract class ArrayAdapter<T>(val context: Context, val onItemClickListener: ((T) -> Unit)?) : RecyclerView.Adapter<BindableVH<T>>() {
    protected val layoutInflater by lazy {
        LayoutInflater.from(context)
    }
    private val data = mutableListOf<T>()
    private var dataDisposable: Disposable? = null

    override fun onBindViewHolder(holder: BindableVH<T>?, position: Int) {
        holder?.bindData(getItemAtPosition(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BindableVH<T> {
        val viewHolder = onViewHolderCreate(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            onItemClickListener?.invoke(getItemAtPosition(viewHolder.adapterPosition))
        }
        return viewHolder
    }

    abstract fun onViewHolderCreate(parent: ViewGroup?, viewType: Int): BindableVH<T>

    override fun getItemCount(): Int = data.size

    protected fun getItemAtPosition(position: Int) = data[position]

    fun setData(items: Collection<T>) {
        if (itemCount == 0 && items.isNotEmpty()) {
            data.addAll(items)
            notifyDataSetChanged()
        } else {
            dataDisposable?.dispose()
            dataDisposable = Observable.just(items)
                    .map { DiffUtil.calculateDiff(DataDiffCallback(data.toList(), it.toList())) }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        data.clear()
                        data.addAll(items)
                    }
                    .subscribe({
                        it.dispatchUpdatesTo(this)
                    })
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        dataDisposable?.dispose()
        dataDisposable = null
    }

    fun getData(): List<T> {
        return data
    }
}

class DataDiffCallback<T>(val oldData: List<T>, val newData: List<T>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }

    override fun getOldListSize(): Int = oldData.size

    override fun getNewListSize(): Int = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }
}

abstract class BindableVH<T>(itemView: View) : ViewHolder(itemView) {
    protected val context = itemView.context!!

    abstract fun bindData(data: T)
}


fun <V : View> ViewHolder.bindView(id: Int)
        : ReadOnlyProperty<ViewHolder, V> = Lazy { t, property ->
    t.viewFinder.invoke(this, id) as V? ?: viewNotFound(id, property)
}

private val ViewHolder.viewFinder: ViewHolder.(Int) -> View?
    get() = { itemView.findViewById(it) }


fun Long.formatString(): CharSequence {
    val prettyTime = PrettyTime()
    return prettyTime.format(Date(this * 1000))
}
