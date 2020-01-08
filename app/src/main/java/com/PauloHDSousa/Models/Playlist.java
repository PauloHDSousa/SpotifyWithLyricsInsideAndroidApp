package com.PauloHDSousa.Models;

public class Playlist {

    public String URL;
    public String ImageURL;
    public String Title;

    public Playlist(String _Title, String _URL, String _ImageURL) {
        URL ="spotify:playlist:"+_URL;
        ImageURL = _ImageURL;
    }
}
