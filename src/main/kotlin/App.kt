import kotlinx.browser.window
import kotlinx.coroutines.*
import react.*
import react.dom.div
import react.dom.h1
import react.dom.h3

data class KotlinVideo(
    override val id: Int,
    override val title: String,
    override val speaker: String,
    override val videoUrl: String
) : Video

external interface AppState: RState {
    var currentVideo : Video?
    var unwatchedVideos: List<Video>
    var watchedVideos: List<Video>
}

suspend fun fetchVideos(): List<Video> = coroutineScope {
    (1..25).map { id ->
        async {
            fetchVideo(id)
        }
    }.awaitAll()
}

suspend fun fetchVideo(id: Int): Video {
    val response = window.fetch("https://my-json-server.typicode.com/kotlin-hands-on/kotlinconf-json/videos/$id")
        .await()
        .json()
        .await()
    return response as Video
}

@JsExport
class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        unwatchedVideos = listOf()
        watchedVideos = listOf()

        val mainScope = MainScope()
        mainScope.launch {
            val videos = fetchVideos()
            setState {
                unwatchedVideos = videos
            }
        }
    }

    override fun RBuilder.render() {
        h1 {
            +"KotlinConf Explorer"
        }
        div {
            h3 {
                +"Videos to watch"
            }

            videoList {
                videos = state.unwatchedVideos
                selectedVideo = state.currentVideo
                onSelectVideo = { video ->
                    setState {
                        currentVideo = video
                    }
                }
            }

            h3 {
                +"Videos watched"
            }

            videoList {
                videos = state.watchedVideos
                selectedVideo = state.currentVideo
                onSelectVideo = { video ->
                    setState {
                        currentVideo = video
                    }
                }
            }
        }

        state.currentVideo?.let { currentVideo ->
            videoPlayer {
                video = currentVideo
                unwatchedVideo = currentVideo in state.unwatchedVideos
                onWatchedButtonPressed = {
                    if (video in state.unwatchedVideos) {
                        setState {
                            unwatchedVideos -= video
                            watchedVideos += video
                        }
                    } else {
                        setState {
                            watchedVideos -= video
                            unwatchedVideos += video
                        }
                    }
                }
            }
        }
    }
}