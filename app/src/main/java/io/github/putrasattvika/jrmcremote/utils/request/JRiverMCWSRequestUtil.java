package io.github.putrasattvika.jrmcremote.utils.request;

import android.graphics.Bitmap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.model.Song;

/**
 * Created by Sattvika on 14-Jan-17.
 */

public class JRiverMCWSRequestUtil {
    private static volatile JRiverMCWSRequestUtil instance;

    private String host;
    private String port;
    private Map<String, String> playbackInfo;
    private Song playingSong;

    private static final String BASE_PATH            = "MCWS/v1";
    private static final String ALIVE_PATH           = "Alive";
    private static final String PLAY_PAUSE_PATH      = "Playback/PlayPause?Zone=-1&ZoneType=ID";
    private static final String PAUSE_PATH           = "Playback/Pause?State=-1&Zone=-1&ZoneType=ID";
    private static final String STOP_PATH            = "Playback/Stop?Zone=-1&ZoneType=ID";
    private static final String PLAY_BY_INDEX_PATH   = "Playback/PlayByIndex";
    private static final String PLAY_BY_KEY_PATH     = "Playback/PlayByKey";
    private static final String PLAY_PLAYLIST_PATH   = "Playback/PlayPlaylist";
    private static final String PLAYBACK_INFO_PATH   = "Playback/Info?Zone=-1";
    private static final String PLAY_NEXT_PATH       = "Playback/Next?Zone=-1&ZoneType=ID";
    private static final String PLAY_PREV_PATH       = "Playback/Previous?Zone=-1&ZoneType=ID";
    private static final String GET_ALBUM_ART        = "File/GetImage";
    private static final String POSITION_PATH        = "Playback/Position";
    private static final String FILE_INFO_PATH       = "File/GetInfo";
    private static final String PLAYLISTS_PATH       = "Playlists/List";
    private static final String PLAYING_NOW_PATH     = "Playback/Playlist";
    private static final String PLAYLIST_DETAIL_PATH = "Playlist/Files";
    private static final String SET_PLAYLIST_PATH    = "Playback/PlayPlaylist";

    private JRiverMCWSRequestUtil() {
        this.host = null;
        this.port = null;
        this.playingSong = new Song();
    }

    public static JRiverMCWSRequestUtil getInstance() {
        if (instance == null) {
            synchronized (JRiverMCWSRequestUtil.class) {
                if (instance == null) {
                    instance = new JRiverMCWSRequestUtil();
                }
            }
        }

        return instance;
    }

    public boolean connect(String host, int port) {
        synchronized (JRiverMCWSRequestUtil.class) {
            instance.host = host;
            instance.port = Integer.toString(port);

            return isConnected();
        }
    }

    public boolean isConnected() {
        try {
            URL url = getURL(ALIVE_PATH);
            Document doc = ResourceRequestUtil.requestDocument(url);
            Map<String, String> result = parseDocumentNameField(doc);

            refetchPlaybackInfo();

            return result.get("AccessKey") != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean play() {
        try {
            URL url = getURL(PLAY_PAUSE_PATH);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean playByIndex(int songIndex) {
        try {
            Map<String, String> getQueries = new HashMap<>();
            getQueries.put("Index", Integer.toString(songIndex));
            getQueries.put("Zone", "-1");

            URL url = getURL(JRiverMCWSRequestUtil.PLAY_BY_INDEX_PATH, getQueries);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {}

        return false;
    }

    public boolean playSong(Song song) {
        try {
            Map<String, String> getQueries = new HashMap<>();
            getQueries.put("Key", Integer.toString(song.getJrmcID()));
            getQueries.put("Zone", "-1");
            getQueries.put("ZoneType", "ID");

            URL url = getURL(JRiverMCWSRequestUtil.PLAY_BY_KEY_PATH, getQueries);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {}

        return false;
    }

    public boolean playPlaylist(PlayList playList) {
        try {
            Map<String, String> getQueries = new HashMap<>();
            getQueries.put("Playlist", Integer.toString(playList.getId()));
            getQueries.put("PlaylistType", "ID");
            getQueries.put("Zone", "-1");
            getQueries.put("ZoneType", "ID");

            URL url = getURL(JRiverMCWSRequestUtil.PLAY_PLAYLIST_PATH, getQueries);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {}

        return false;
    }

    public boolean playPlaylist(PlayList playList, int index) {
        boolean result = true;

        if (playList.getId() != 0) {
            result = playPlaylist(playList);
        }

        result &= pause();
        result &= playByIndex(index);

        return result;
    }

    public boolean pause() {
        try {
            URL url = getURL(PAUSE_PATH);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean playNext() {
        try {
            URL url = getURL(PLAY_NEXT_PATH);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean playPrev() {
        try {
            URL url = getURL(PLAY_PREV_PATH);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPlaying() {
        if (playbackInfo == null) {
            return false;
        }

        return "Playing".equals(playbackInfo.get("Status"));
    }

    public int getProgress() {
        try {
            return Integer.parseInt(playbackInfo.get("PositionMS"));
        } catch (Exception e) {
            return 0;
        }
    }

    public Bitmap getSongAlbumArt(Song song) {
        Bitmap albumArt;
        Map<String, String> getQueries = new HashMap<>();

        getQueries.put("File", Integer.toString(song.getJrmcID()));
        getQueries.put("FileType", "Key");
        getQueries.put("Type", "Thumb");
        getQueries.put("Format", "png");

        try {
            URL url = getURL(JRiverMCWSRequestUtil.GET_ALBUM_ART, getQueries);
            albumArt = ResourceRequestUtil.requestBitmap(url);
        } catch (Exception e) {
            albumArt = null;
        }

        return albumArt;
    }

    public boolean seekTo(int progress) {
        try {
            Map<String, String> getQueries = new HashMap<>();
            getQueries.put("Position", Integer.toString(progress));

            URL url = getURL(JRiverMCWSRequestUtil.POSITION_PATH, getQueries);
            ResourceRequestUtil.request(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<PlayList> getPlaylists() {
        List<PlayList> playLists = new ArrayList<>();

        try {
            URL url = getURL(JRiverMCWSRequestUtil.PLAYLISTS_PATH);
            Document doc = ResourceRequestUtil.requestDocument(url);
            List<Map<String, String>> playlistMaps = parseDocumentItems(doc);

            for (int i = 0; i < playlistMaps.size(); i++) {
                Map<String, String> playlistInfo = playlistMaps.get(i);
                String type = playlistInfo.get("Type");

                if (!"Playlist".equals( type ) && !"Smartlist".equals( type )) {
                    continue;
                }

                int playlistID = Integer.parseInt( playlistInfo.get("ID") );

                try {
                    PlayList pl = getPlaylist(playlistID);

                    pl.setName(playlistInfo.get("Name"));
                    pl.setId(playlistID);
                    playLists.add(pl);
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}

        return playLists;
    }

    public PlayList getPlayingNow() {
        try {
            Map<String, String> getQueries = new HashMap<>();

            getQueries.put("Zone", "-1");
            getQueries.put("Action", "mpl");

            URL url = getURL(JRiverMCWSRequestUtil.PLAYING_NOW_PATH, getQueries);
            Document songsDoc = ResourceRequestUtil.requestDocument(url);
            List<Map<String, String>> songsInfo = parseDocumentItems(songsDoc);
            PlayList pl = new PlayList();

            for (int i = 0; i < songsInfo.size(); i++) {
                Song song = parseSongMap(songsInfo.get(i));
                pl.addSong(song);
            }

            pl.setFavorite(true);
            pl.setName("Playing Now");
            pl.setId(0);

            return pl;
        } catch (Exception e) {
            return null;
        }
    }

    public PlayList getPlaylist(int playlistID) {
        try {
            Map<String, String> getQueries = new HashMap<>();

            getQueries.put("PlaylistType", "ID");
            getQueries.put("Action", "mpl");
            getQueries.put("Playlist", Integer.toString(playlistID));

            URL url = getURL(JRiverMCWSRequestUtil.PLAYLIST_DETAIL_PATH, getQueries);
            Document songsDoc = ResourceRequestUtil.requestDocument(url);
            List<Map<String, String>> songsInfo = parseDocumentItems(songsDoc);
            PlayList pl = new PlayList();

            for (int i = 0; i < songsInfo.size(); i++) {
                Song song = parseSongMap(songsInfo.get(i));
                pl.addSong(song);
            }

            return pl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Song getPlayingSong() {
        return playingSong;
    }

    public Song getSong(int jrmcID) {
        try {
            Map<String, String> getQueries = new HashMap<>();
            getQueries.put("Action", "mpl");
            getQueries.put("File", Integer.toString(jrmcID));

            URL url = getURL(JRiverMCWSRequestUtil.FILE_INFO_PATH, getQueries);
            Document doc = ResourceRequestUtil.requestDocument(url);
            Map<String, String> songMap = parseDocumentItems(doc).get(0);

            return parseSongMap(songMap);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean refetchPlaybackInfo() {
        try {
            playbackInfo = getPlaybackInfo();

            int id = Integer.parseInt(playbackInfo.get("FileKey"));

            if (id != playingSong.getJrmcID()) {
                playingSong = new Song();
            }

            playingSong.setJrmcID( id );
            playingSong.setDisplayName( playbackInfo.get("Name") );
            playingSong.setTitle( playbackInfo.get("Name") );
            playingSong.setAlbum( playbackInfo.get("Album") );
            playingSong.setArtist( playbackInfo.get("Artist") );
            playingSong.setDuration( Integer.parseInt(playbackInfo.get("DurationMS")) );

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Song parseSongMap(Map<String, String> map) {
        Song song = new Song();
        int durationMS = (int) Math.floor(Double.parseDouble(map.get("Duration")) * 1000);

        song.setJrmcID( Integer.parseInt(map.get("Key")) );
        song.setDisplayName( map.get("Name") );
        song.setTitle( map.get("Name") );
        song.setAlbum( map.get("Album") );
        song.setArtist( map.get("Artist") );
        song.setDuration( durationMS );

        return song;
    }

    private Map<String, String> parseDocumentNameField(Document doc) {
        Map<String, String> values = new HashMap<>();
        NodeList items = doc.getElementsByTagName("Item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            values.put(item.getAttribute("Name"), item.getTextContent());
        }

        return values;
    }

    private List<Map<String, String>> parseDocumentItems(Document doc) {
        List<Map<String, String>> values = new ArrayList<>();
        NodeList items = doc.getElementsByTagName("Item");

        for (int i = 0; i < items.getLength(); i++) {
            Map<String, String> itemValues = new HashMap<>();

            Element item = (Element) items.item(i);
            NodeList fields = item.getElementsByTagName("Field");

            for (int j = 0; j < fields.getLength(); j++) {
                Element field = (Element) fields.item(j);
                itemValues.put(field.getAttribute("Name"), field.getTextContent());
            }

            values.add(itemValues);
        }

        return values;
    }

    private URL getURL(String path) throws MalformedURLException {
        return new URL("http://" + host + ":" + port + "/" + BASE_PATH + "/" + path);
    }

    private URL getURL(String path, Map<String, String> getQueries) throws MalformedURLException {
        String urlString = "http://" + host + ":" + port + "/" + BASE_PATH + "/" + path;
        boolean first = true;

        for (Map.Entry<String, String> e: getQueries.entrySet()) {
            if (first) {
                urlString += "?";
                first = false;
            } else {
                urlString += "&";
            }

            urlString += e.getKey() + "=" + e.getValue();
        }

        return new URL(urlString);
    }

    private Map<String, String> getPlaybackInfo() throws MalformedURLException, ExecutionException, InterruptedException, NullPointerException {
        Document doc;

        URL url = getURL(PLAYBACK_INFO_PATH);
        doc = ResourceRequestUtil.requestDocument(url);

        return parseDocumentNameField(doc);
    }
}
