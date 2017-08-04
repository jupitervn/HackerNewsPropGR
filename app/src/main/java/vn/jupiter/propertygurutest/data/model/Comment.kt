package vn.jupiter.propertygurutest.data.model

data class Comment(val id: String,
                   val text: String,
                   val commentLevel: Int = 0,
                   val time: Long,
                   val by: String?)