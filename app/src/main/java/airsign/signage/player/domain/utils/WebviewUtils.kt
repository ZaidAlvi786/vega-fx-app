package airsign.signage.player.domain.utils

object WebviewUtils {
    /**
     * Extract Vimeo video ID from various Vimeo URL formats
     */
    fun getVimeoId(url: String): String {
        return when {
            // Standard Vimeo URLs: https://vimeo.com/827569779
            url.contains("vimeo.com/") && !url.contains("player.vimeo.com") -> {
                val regex = Regex("""vimeo\.com/(\d+)""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            // Player URLs: https://player.vimeo.com/video/827569779
            url.contains("player.vimeo.com/video/") -> {
                val regex = Regex("""player\.vimeo\.com/video/(\d+)""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            // Legacy method for HTML content
            else -> {
                val regex = Regex("""https://player\.vimeo\.com/video/(\d+)\?""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
        }
    }

    /**
     * Check if URL is a Vimeo video
     */
    fun isVimeoUrl(url: String): Boolean {
        return url.contains("vimeo.com") && getVimeoId(url).isNotEmpty()
    }

    /**
     * Generate Vimeo embed URL for video playback
     */
    fun getVimeoEmbedUrl(url: String): String {
        val videoId = getVimeoId(url)
        return if (videoId.isNotEmpty()) {
            "https://player.vimeo.com/video/$videoId?autoplay=1&mute=0&loop=0&title=0&byline=0&portrait=0"
        } else {
            ""
        }
    }

    /**
     * Legacy method for HTML content (kept for backward compatibility)
     */
    fun vimeoHtml(html: String): String {
        return "<iframe src=\"https://player.vimeo.com/video/${getVimeoId(html)}?autoplay=1\" width=\"100%\" height=\"100%\" frameborder=\"0\" allow=\"autoplay; fullscreen\" allowfullscreen></iframe>"
    }

    /**
     * Extract YouTube video ID from various YouTube URL formats
     */
    fun youtubeID(url: String): String {
        return when {
            // Standard watch URLs
            url.contains("youtube.com/watch") -> {
                val regex = Regex("""[?&]v=([a-zA-Z0-9_-]+)""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            // Short URLs
            url.contains("youtu.be/") -> {
                val regex = Regex("""youtu\.be/([a-zA-Z0-9_-]+)""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            // Embed URLs
            url.contains("youtube.com/embed/") -> {
                val regex = Regex("""youtube\.com/embed/([a-zA-Z0-9_-]+)""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            // Playlist URLs (extract first video)
            url.contains("youtube.com/playlist") -> {
                val regex = Regex("""[?&]list=([a-zA-Z0-9_-]+)""")
                val matchResult = regex.find(url)
                // For playlists, we'll use the playlist ID and let YouTube handle it
                matchResult?.groupValues?.getOrNull(1) ?: ""
            }
            else -> ""
        }
    }

    /**
     * Check if URL is a YouTube playlist
     */
    fun isYouTubePlaylist(url: String): Boolean {
        return url.contains("youtube.com/playlist") || url.contains("&list=")
    }

    /**
     * Generate YouTube embed URL for single video or playlist
     */
    fun getYouTubeEmbedUrl(url: String): String {
        val videoId = youtubeID(url)
        return if (isYouTubePlaylist(url)) {
            // For playlists, use playlist embed
            "https://www.youtube.com/embed/videoseries?list=$videoId&autoplay=1&mute=0"
        } else {
            // For single videos, use video embed
            "https://www.youtube.com/embed/$videoId?autoplay=1&mute=0"
        }
    }

    fun scrollingWidget(htmlString: String): String {
        return htmlString.replace("300px", "100%")
    }
}