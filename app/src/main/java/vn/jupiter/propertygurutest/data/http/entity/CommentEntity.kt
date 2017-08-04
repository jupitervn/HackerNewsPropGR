package vn.jupiter.propertygurutest.data.http.entity
//{
//    "by" : "norvig",
//    "id" : 2921983,
//    "kids" : [ 2922097, 2922429, 2924562, 2922709, 2922573, 2922140, 2922141 ],
//    "parent" : 2921506,
//    "text" : "Aw shucks, guys ... you make me blush with your compliments.<p>Tell you what, Ill make a deal: I'll keep writing if you keep reading. K?",
//    "time" : 1314211127,
//    "type" : "comment"
//}
data class CommentEntity(val id: String,
                         val kids: List<String>? = emptyList(),
                         val text: String? = "",
                         val time: Long,
                         val by: String? = "",
                         val parent: String?,
                         val deleted:Boolean = true
)