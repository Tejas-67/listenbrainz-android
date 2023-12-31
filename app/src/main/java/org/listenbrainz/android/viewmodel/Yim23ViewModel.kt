package org.listenbrainz.android.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.viewModelScope
import com.caverock.androidsvg.SVG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.yimdata.*
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.repository.yim23.Yim23Repository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.saveBitmap
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class Yim23ViewModel @Inject constructor(
    private val repository: Yim23Repository,
    private val appPreferences: AppPreferences
) : BaseYimViewModel() {
    // Yim data resource
    var yimData:
            MutableState<
                    Resource<Yim23Payload>
                    >
            = mutableStateOf(Resource.loading())
    val loginFlow = appPreferences.getLoginStatusFlow()
    var themeType : MutableState<Int> = mutableStateOf(0)

    init {
        getData()
    }

    override fun getData() {
        viewModelScope.launch {
            val response = repository.getYimData(username = getUsername() , 2023)
            when (response.status){
                Resource.Status.SUCCESS -> yimData.value = response
                Resource.Status.LOADING -> yimData.value = Resource.loading()
                Resource.Status.FAILED -> yimData.value = Resource.failure()
            }
        }
    }

    // Username related functions
    suspend fun getUsername() : String {
        return appPreferences.getUsername()
    }

    fun getUsernameFlow() : Flow<String> {
        return appPreferences.getUsernameFlow()
    }

    /** Get Data functions
     *  NOTE : Every get must be null checked.
     */

    fun getArtistMap() : ArrayList<ArtistMap>? {
        return yimData.value.data?.payload?.data?.artistMap
    }

    /** Get list of listen count of whole year.*/
    fun getListensListOfYear() : List<Int> {
        val list = arrayListOf<Int>()
        yimData.value.data?.payload?.data?.listensPerDay?.forEach {
            list.add(it.listenCount)
        }
        return list
    }

    fun getMostListenedMonth () : Pair<String , Int> {
        var jan: Int = 0
        var feb: Int = 0
        var mar: Int = 0
        var apr: Int = 0
        var may: Int = 0
        var jun: Int = 0
        var jul: Int = 0
        var aug: Int = 0
        var sep: Int = 0
        var oct: Int = 0
        var nov: Int = 0
        var dec: Int = 0

        yimData.value.data?.payload?.data?.listensPerDay?.forEach {
            if (it.timeRange.lowercase().contains("jan")) {
                jan += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("feb")) {
                feb += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("mar")) {
                mar += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("apr")) {
                apr += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("may")) {
                may += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("jun")) {
                jun += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("jul")) {
                jul += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("aug")) {
                aug += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("sept")) {
                sep += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("oct")) {
                oct += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("nov")) {
                nov += (it.listenCount)
            }
            if (it.timeRange.lowercase().contains("dec")) {
                dec += (it.listenCount)
            }
        }

        val list: List<Int> = listOf(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec)

        var max_list = 0
        var index = 0
        for (i in 1..12) {
            if(list[i-1] > max_list){
                max_list = list[i-1]
                index = i
            }
        }

        val month : String = when (index) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }

        val ans : Pair<String , Int> = Pair(month , max_list)
        return ans
    }

    /** List of new releases of those artists that the user listens to.*/
    fun getNewReleasesOfTopArtists() : ArrayList<NewReleasesOfTopArtist>? {
        return yimData.value.data?.payload?.data?.newReleasesOfTopArtists
    }

    /** The year of which the user listened most songs of. */
    fun getYearListens() : Map<String , Int> {
        return yimData.value.data?.payload?.data?.mostListenedYear!!
    }

    fun getMostListenedYear() : Int? {
        val mapEntry = yimData.value.data?.payload?.data?.mostListenedYear?.maxBy {
            it.value
        }
        return mapEntry?.value
    }

    /** The day user listens the most music, every week.*/
    fun getDayOfWeek() : String {
        return yimData.value.data?.payload?.data?.dayOfWeek.toString()
    }

    /** List of other ListenBrainz users with the same taste as user.
     *
     *  @return `null` for users with less listens.
     */
    fun getSimilarUsers(): List<Pair<String, Double>>? {
        val list = yimData.value.data?.payload?.data?.similarUsers?.toList()
        return list?.sortedByDescending {
            it.second
        }
    }

    /** List of top artists of which user listened songs of*/
    fun getTopArtists() : ArrayList<TopArtist>? {
        return yimData.value.data?.payload?.data?.topArtists
    }

    /** Warning: Volatile fields (Might be null) :
     * @param caaId
     * @param caaReleaseMbid
     * @param releaseMbid
     */
    fun getTopRecordings() : ArrayList<TopRecording>? {
        return yimData.value.data?.payload?.data?.topRecordings
    }

    /** Top releases user listened to.*/
    fun getTopReleases() : ArrayList<TopReleaseYim23>? {
        return yimData.value.data?.payload?.data?.topReleases
    }

    /** Total of all artists the user listened to.*/
    fun getTotalArtistCount() : Int? {
        return yimData.value.data?.payload?.data?.totalArtistsCount
    }

    fun getTotalListenCount() : Int? {
        return yimData.value.data?.payload?.data?.totalListenCount
    }

    fun getTotalListeningTime() : Double? {
        return yimData.value.data?.payload?.data?.totalListeningTime
    }

    fun getTotalNewArtistsDiscovered() : Int? {
        return yimData.value.data?.payload?.data?.totalNewArtistsDiscovered
    }

    fun getTotalRecordingsCount() : Int? {
        return yimData.value.data?.payload?.data?.totalRecordingsCount
    }

    fun getTotalReleasesCount() : Int? {
        return yimData.value.data?.payload?.data?.totalReleasesCount
    }

    fun getTopGenres () : List<TopGenre> {
        return yimData.value.data?.payload?.data?.topGenres!!.toList()
    }

    /** [getUrlsForAlbumArt]
     * @return Url list for Album Art. To be used with `R.drawable.yim_frame` */
    fun getUrlsForAlbumArt(isTopDiscoveriesPlaylist: Boolean) : List<String> {
        val map = if (isTopDiscoveriesPlaylist){
            yimData.value.data?.payload?.data?.topDiscoveriesPlaylistCoverArt
        }else{
            yimData.value.data?.payload?.data?.topMissedPlaylistCoverArt
        }
        val list = arrayListOf<String>()
        map?.onEachIndexed { index, entry ->
            if (index < 9){
                list.add(entry.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg"))   // This is done to scale down images.
            }
        }
        return list
    }

    /** [getTopDiscoveriesPlaylistAndArtCover]
     * @return map of [Track] (track) and [String] (Url of art cover) for [TopDiscoveriesPlaylist].*/
    fun getTopDiscoveriesPlaylistAndArtCover() : Map<Track, String>{
        val listOfTracks = yimData.value.data?.payload?.data?.topDiscoveriesPlaylist?.tracksList
        val artCoverMap = yimData.value.data?.payload?.data?.topDiscoveriesPlaylistCoverArt

        val resultMap = mutableMapOf<Track, String>()

        listOfTracks?.forEach { track ->
            val mbid = track.identifier.substringAfterLast('/')

            if (artCoverMap!!.containsKey(mbid)){
                artCoverMap.forEach {
                    if (it.key == mbid){
                        resultMap[track] = it.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg")
                    }
                }
            }else{
                resultMap[track] = "null"
            }
        }

        return resultMap
    }

    /** [getTopMissedPlaylistAndArtCover]
     * @return map of [Track] (track) and [String] (Url of art cover) for [TopMissedPlaylist].*/
    fun getTopMissedPlaylistAndArtCover() : Map<Track, String>{
        val listOfTracks = yimData.value.data?.payload?.data?.topMissedPlaylist?.tracksList
        val artCoverMap = yimData.value.data?.payload?.data?.topMissedPlaylistCoverArt

        val resultMap = mutableMapOf<Track, String>()

        listOfTracks!!.forEach { track ->
            val mbid = track.identifier.substringAfterLast('/')

            if (artCoverMap!!.containsKey(mbid)){
                artCoverMap.forEach {
                    if (it.key == mbid){
                        resultMap[track] = it.value.replaceAfterLast(delimiter = '_', replacement = "thumb250.jpg")
                    }
                }
            }else{
                resultMap[track] = "null"
            }
        }

        return resultMap
    }

    /** Shareable types : "stats", "artists", "albums", "tracks", "discovery-playlist", "missed-playlist".*/
    fun saveSharableImage(sharableType: String, context: Context)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap: Bitmap = Bitmap.createBitmap(924,924,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val imageURL = "https://api.listenbrainz.org/1/art/year-in-music/2022/${getUsername()}?image=$sharableType"

            try {
                // Download Image from URL
                URL(imageURL).openStream().use {
                    // Decode Bitmap
                    SVG.getFromInputStream(it).renderToCanvas(canvas)
                }

                saveBitmap(
                    context = context,
                    bitmap = bitmap,
                    format = Bitmap.CompressFormat.PNG,
                    displayName = "${getUsername()}'s $sharableType",
                    launchShareIntent = true
                )

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT).show()
                }
                e.localizedMessage?.let { Log.e("YimShareError", it) }
            }

        }
    }
}