package com.PauloHDSousa.Utils;

import com.PauloHDSousa.Models.Playlist;
import java.util.ArrayList;
import java.util.List;

public  class Playlists {

    public interface PlaylistType {
        String AUTOMOTIVE = "automotive";
        String DEFAULT = "default";
        String FITNESS = "fitness";
        String WAKE = "wake";
    }

    public static List<Playlist> getPlayList(String  type){

        if(type.equals(PlaylistType.FITNESS)){
            return  getFitnessPlaylists();
        }
        else  if(type.equals(PlaylistType.DEFAULT)){
            return  getBestWorldPlaylists();
        }
        else  if(type.equals(PlaylistType.WAKE)){
            return  getWakePlaylists();
        }
        else  if(type.equals(PlaylistType.AUTOMOTIVE)){
            return  getDrivePlaylists();
        }


        return  null;
    }

    //Fitness 10
    static List<Playlist> getFitnessPlaylists(){

        List<Playlist> items = new ArrayList<Playlist>();


        items.add(getNewPlaylist("Adrenaline Workout", "37i9dQZF1DXe6bgV3TmZOL","http://i.scdn.co/image/ab67706f00000002c1917a8a76396af1ec4abe83"));
        items.add(getNewPlaylist("Beast Mode", "37i9dQZF1DX76Wlfdnj7AP","http://i.scdn.co/image/ab67706f000000029249b35f23fb596b6f006a15"));
        items.add(getNewPlaylist("Cardio", "37i9dQZF1DWSJHnPb1f0X3","http://i.scdn.co/image/ab67706f00000002eee1365954b35d0def7bb1b5"));
        items.add(getNewPlaylist("Hype", "37i9dQZF1DX4eRPd9frC1m","http://i.scdn.co/image/ab67706f00000002122921a072ad2ca9ce90a456"));
        items.add(getNewPlaylist("Indie Workout", "37i9dQZF1DXaRL7xbcDl7X","http://i.scdn.co/image/ab67706f000000026c1c67b9afde89d78de5d828"));
        items.add(getNewPlaylist("Just Move it!", "37i9dQZF1DX5vVIxolcMKs","http://i.scdn.co/image/3c8a1c6f28a6026ca5172498a368511498f5fa26"));
        items.add(getNewPlaylist("Pilates", "37i9dQZF1DXdXj7otHefj3","http://i.scdn.co/image/d01def1f6d5f422e3042f047891f9a68a9e24687"));
        items.add(getNewPlaylist("Motivation Mix", "37i9dQZF1DXdxcBWuJkbcy","http://i.scdn.co/image/ab67706f00000002a088a35ef6e638c42f88deda"));
        items.add(getNewPlaylist("Power Hour", "37i9dQZF1DX32NsLKyzScr","http://i.scdn.co/image/ab67706f00000002a569692630cce3489db61a97"));
        items.add(getNewPlaylist("Rock Your Body", "37i9dQZF1DXbFRZSqP41al","http://i.scdn.co/image/ab67706f000000024b907e2af6e9f901a9ee91e1"));
        items.add(getNewPlaylist("Workout", "37i9dQZF1DX70RN3TfWWJh","http://i.scdn.co/image/ab67706f0000000252a4093409a93d2f13a35f3f"));
        items.add(getNewPlaylist("Yoga", "37i9dQZF1DWZvpVE2NxPV2","http://i.scdn.co/image/63b33c0e223a00851cfdebb5bcd85a53e4e84fa5"));

        return items;
    }

    //Default 6
    static List<Playlist> getBestWorldPlaylists(){

        List<Playlist> items = new ArrayList<Playlist>();

        items.add(getNewPlaylist("Soft Pop Hits", "37i9dQZF1DWTwnEm1IYyoj","http://i.scdn.co/image/ab67706f000000029faf94f891c36e9af9590e42"));
        items.add(getNewPlaylist("Today's Top Hits", "37i9dQZF1DXcBWIGoYBM5M","http://i.scdn.co/image/ab67706f000000026fb114f0e9ebb89e8c48d629"));
        items.add(getNewPlaylist("Dance Rising", "37i9dQZF1DX8tZsk68tuDw","http://i.scdn.co/image/ab67706f00000002bc1a71e68fe107f769762bae"));
        items.add(getNewPlaylist("Dance Hits", "37i9dQZF1DX0BcQWzuB7ZO","http://i.scdn.co/image/ab67706f00000002e78156a99d827269d24f6921"));
        items.add(getNewPlaylist("Hit Rewind", "37i9dQZF1DX0s5kDXi1oC5","http://i.scdn.co/image/ab67706f00000002859872b6e6325c6e4d95cffd"));
        items.add(getNewPlaylist("Pop Remix", "37i9dQZF1DXcZDD7cfEKhW","http://i.scdn.co/image/ab67706f000000023c328de57b4873ba75e15890"));

        return items;
    }
    //Drive 10
    static List<Playlist> getDrivePlaylists(){

        List<Playlist> items = new ArrayList<Playlist>();

        items.add(getNewPlaylist("Country Drive", "37i9dQZF1DXdfhOsjRMISB","http://i.scdn.co/image/ab67706f0000000267e2496f83cf1644c2987495"));
        items.add(getNewPlaylist("Mellow Drive", "37i9dQZF1DWUACcBjzMiIY","https://i.scdn.co/image/ab67706f00000002b815987d63f22530ff5c27b1"));
        items.add(getNewPlaylist("Mellow Classics Drive", "37i9dQZF1DWVMBt9UEs0qm","http://pl.scdn.co/images/pl/default/4d80c68590ea4d3d9c4bfef0ec72ce1ad621d6c5"));
        items.add(getNewPlaylist("Happy Drive", "37i9dQZF1DX1WSnLRtI26o","http://pl.scdn.co/images/pl/default/96b4e16e6c07690e7a4463c91c76be840e38bfc7"));
        items.add(getNewPlaylist("Funk Drive", "37i9dQZF1DX5p7tFA8Sla6","http://pl.scdn.co/images/pl/default/db0a5988b81e21c2fe998e598c0a5a0eab798b74"));
        items.add(getNewPlaylist("Classic Rock Drive", "37i9dQZF1DXdOEFt9ZX0dh","http://i.scdn.co/image/ab67706f00000002a019090ff288b5a4aacac170"));
        items.add(getNewPlaylist("Hip Hop Drive", "37i9dQZF1DWUFmyho2wkQU","http://i.scdn.co/image/ab67706f00000002b6b079ea1e8f5af584d7a897"));
        items.add(getNewPlaylist("Pop Drive", "37i9dQZF1DWSThc8QnzIme","http://i.scdn.co/image/ab67706f0000000275c5caa75c0a54430b68acd4"));
        items.add(getNewPlaylist("Sing in the car", "37i9dQZF1DWWMOmoXKqHTD","http://i.scdn.co/image/ab67706f0000000259c561e5369ab7f2894dfa68"));
        items.add(getNewPlaylist("Daily Drive", "37i9dQZF1EfVolYQ5qkgsI","http://misc.scdn.co/your-daily-drive/your-daily-drive-night.jpg"));


        return items;
    }

    //Wake 10
    static List<Playlist> getWakePlaylists(){

        List<Playlist> items = new ArrayList<Playlist>();

        items.add(getNewPlaylist("Cafe com Leche", "37i9dQZF1DXa3NnZWk6Z3T","https://i.scdn.co/image/ab67706f0000000236443688cd3454d9d92f9e9e"));
        items.add(getNewPlaylist("Cafe/Croissant", "37i9dQZF1DX0X277P8nU0l","https://i.scdn.co/image/ab67706f000000023724e4a55c5bb7f326ca4213"));
        items.add(getNewPlaylist("Music Cafe", "37i9dQZF1DX2VPTYZ31HYJ","https://pl.scdn.co/images/pl/default/10ff97e5d024a706d355e1d7206e8f70e534a5ab"));
        items.add(getNewPlaylist("Dalkom Cafe", "37i9dQZF1DX5g856aiKiDS","https://pl.scdn.co/images/pl/default/4366bedd2ff59f11710feefa87bc40eb59de3bea"));
        items.add(getNewPlaylist("Café bossa", "37i9dQZF1DX7ovYHwmjqZK","https://i.scdn.co/image/ab67706f0000000216d2d8270d614ef701470d5f"));
        items.add(getNewPlaylist("Lo-Fi Cafe", "37i9dQZF1DX9RwfGbeGQwP","https://pl.scdn.co/images/pl/default/649e5aec793a1b48784526c70e5980f6d00dc4bf"));
        items.add(getNewPlaylist("Cafe", "37i9dQZF1DWV6hddfqLW3Z","http://i.scdn.co/image/ab67706f0000000274ea894d6bcef53f524aeab0"));
        items.add(getNewPlaylist("Wake Up", "37i9dQZF1DX0UrRvztWcAU","http://i.scdn.co/image/ab67706f000000020bd6693bac1f89a70d623e4d"));
        items.add(getNewPlaylist("Pulando da cama", "37i9dQZF1DXa4ss91ghMY4","http://i.scdn.co/image/ab67706f00000002fc391db7e99c366118259367"));
        items.add(getNewPlaylist("Ótimo Dia", "37i9dQZF1DX7KTVQYEg01L","http://i.scdn.co/image/ab67706f000000028a4e8684645d3bf03bf1df3f"));

        return items;
    }

    static Playlist getNewPlaylist(String Title, String URL, String ImageURL) {
        return new Playlist(Title, URL, ImageURL);
    }
}
