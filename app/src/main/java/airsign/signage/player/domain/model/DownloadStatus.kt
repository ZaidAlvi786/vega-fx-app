package airsign.signage.player.domain.model

data class DownloadStatus(
    var fileName: String = "",
    var progress: Int = 0, // 0 to 100
    var totalFiles: Int = 0, // 0 to 100
    var downloaded: Int = 0,
    var isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val downloadComplete: Boolean = false
)
