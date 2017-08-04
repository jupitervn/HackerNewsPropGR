package vn.jupiter.propertygurutest.data.model

import android.os.Parcel
import android.os.Parcelable

//{
//    "by" : "dhouston",
//    "descendants" : 71,
//    "id" : 8863,
//    "kids" : [ 8952, 9224, 8917, 8884, 8887, 8943, 8869, 8958, 9005, 9671, 8940, 9067, 8908, 9055, 8865, 8881, 8872, 8873, 8955, 10403, 8903, 8928, 9125, 8998, 8901, 8902, 8907, 8894, 8878, 8870, 8980, 8934, 8876 ],
//    "score" : 111,
//    "time" : 1175714200,
//    "title" : "My YC app: Dropbox - Throw away your USB drive",
//    "type" : "story",
//    "url" : "http://www.getdropbox.com/u/2/screencast.html"
//}
data class Story(val id: String,
                 val kids: List<String> = emptyList(),
                 val score: Int,
                 val time: Long,
                 val title: String,
                 val url: String,
                 val by: String
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeStringList(kids)
        parcel.writeInt(score)
        parcel.writeLong(time)
        parcel.writeString(title)
        parcel.writeString(url)
        parcel.writeString(by)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Story> {
        override fun createFromParcel(parcel: Parcel): Story {
            return Story(parcel)
        }

        override fun newArray(size: Int): Array<Story?> {
            return arrayOfNulls(size)
        }
    }
}