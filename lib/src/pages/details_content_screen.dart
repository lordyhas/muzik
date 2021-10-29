import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:muzik_audio_player/src/layout/music_layout.dart';
import 'package:muzik_audio_player/src/music_player.dart';
import 'package:muzik_audio_player/src/widget_model/no_data_widget.dart';
import 'package:on_audio_query/on_audio_query.dart';

class SingleAlbumScreen extends StatelessWidget {
  final Uint8List? appBarBackgroundImage;
  final String appBarTitle;
  final Widget? cover;
  final AlbumModel album;
  final void Function()? onSongTap;


  const SingleAlbumScreen({
    required this.album,
    required this.appBarTitle,
    this.appBarBackgroundImage,
    this.cover,
    this.onSongTap,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final PlayerController musicController =
    BlocProvider.of<PlayerController>(context);
    return Scaffold(
      //backgroundColor: background,
      floatingActionButton: FutureBuilder<List<SongModel>>(
          future: OnAudioQuery()
              .queryAudiosFrom(AudiosFromType.ALBUM, album.album),
          builder: (context, snapshot) {

            if (!snapshot.hasData) {
              return Container();
            } else {
              return FloatingActionButton(
                backgroundColor: Theme.of(context).primaryColor,
                child: Container(
                    margin: const EdgeInsets.only(left: 4.0),
                    child: const Icon(
                      CupertinoIcons.play_fill, size: 32,
                      color: Colors.white,
                    )
                ),
                onPressed: () async {
                  await musicController
                      .setListSong(
                      songs: snapshot.data!, initialIndex: 0)
                      .then((value) {
                    musicController.play();
                    onSongTap!();
                    Get.to(const MusicPlayerPage());
                    //Navigator.push(context, MusicPlayerPage.route());
                  });
                },
              );
            }
          }) ,

      /*FloatingActionButton(
        child: Container(
          margin: const EdgeInsets.only(left: 4.0),
            child: const Icon(CupertinoIcons.play_fill, size: 32,)
        ),
        onPressed: () {  },
      ),*/
      appBar: AppBar(
        elevation: 0.0,
        //backgroundColor: Theme.of(context).primaryColor,

        actions: [
          IconButton(
            icon: const Icon(CupertinoIcons.sort_down),
            onPressed: (){},
          ),

          IconButton(
            icon: const Icon(CupertinoIcons.ellipsis_vertical),
            onPressed: (){},
          ),
        ],
      ),
      body: NestedScrollView(
        physics: const BouncingScrollPhysics(),
        headerSliverBuilder: (context, innerBoxIsScrolled) {
          return <Widget>[
            /*SliverOverlapAbsorber(
              handle: NestedScrollView.sliverOverlapAbsorberHandleFor(context),
              sliver:
            ),*/

            SliverList(
              delegate: SliverChildBuilderDelegate(
                    (BuildContext context, int index) {
                  return SizedBox(
                    height: MediaQuery.of(context).size.height*.30,
                    child: Stack(
                      children: [
                        Container(
                          margin: const EdgeInsets.only(bottom: 72.0),
                          decoration:  BoxDecoration(
                              color: Theme.of(context).appBarTheme.backgroundColor,
                              borderRadius: const BorderRadius.only(
                                  //left: Radius.zero,
                                  bottomRight: Radius.circular(75))
                          ),
                        ),
                        Align(
                          alignment: Alignment.bottomLeft,
                          child: Container(
                            padding: const EdgeInsets.all(8.0).copyWith(left: 42.0),
                            //height: 150,
                            //width: MediaQuery.of(context).size.width,
                            child: SizedBox(
                              height: 150,
                              width: 150,
                              child: ClipRRect(
                                borderRadius: BorderRadius.circular(20.0),
                                child: cover,
                              ),
                            ),
                          ),
                        ),
                        Align(
                          alignment: Alignment.topLeft,
                          child: Container(
                            padding: const EdgeInsets.only(left: 32.0),
                            child: ListTile(
                              title: Text(album.album,
                                style: const TextStyle(
                                  fontSize: 20,
                                    fontWeight: FontWeight.bold),),
                              subtitle: Text(album.artist ?? 'unknown artist'),
                            ),
                          ),
                        ),
                      ],
                    ),
                  );
                },
                childCount: 1,
                addSemanticIndexes: false,
              ),
            ),

            /*SliverAppBar(
              //backgroundColor: background,
              expandedHeight: MediaQuery.of(context).size.width * 0.75,
              floating: false,
              pinned: true,
              snap: false,
              elevation: 0.0,
              flexibleSpace: FlexibleSpaceBar(
                centerTitle: true,
                title: Text(
                  appBarTitle,
                  maxLines: 1,
                  softWrap: true,
                  overflow: TextOverflow.fade,
                  style: const TextStyle(
                    color: Colors.white,
                  ),
                ),
                background: (appBarBackgroundImage == null)
                    ? cover
                    : Image.memory(
                        appBarBackgroundImage!,
                        fit: BoxFit.cover,
                      ),
              ),
            ),*/
          ];
        },
        body: FutureBuilder<List<SongModel>>(
            future: OnAudioQuery()
                .queryAudiosFrom(AudiosFromType.ALBUM, album.album),
            builder: (context, snapshot) {

              if (!snapshot.hasData) {
                return const Center(child: CircularProgressIndicator());
              } else {
                return SongListWidget(
                  onTapCallBack: onSongTap,
                  songList: snapshot.data!,
                  //length: length,
                );
              }
            }),
      ),
    );
  }
}

class SingleArtistScreen extends StatelessWidget {
  final Uint8List? appBarBackgroundImage;
  final String appBarTitle;
  final Widget? cover;
  final ArtistModel artist;
  final void Function()? onSongTap;



  const SingleArtistScreen({
    required this.artist,
    required this.appBarTitle,
    this.appBarBackgroundImage,
    this.cover,
    this.onSongTap,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final PlayerController musicController =
    BlocProvider.of<PlayerController>(context);
    return Scaffold(
      //backgroundColor: background,
      floatingActionButton: FutureBuilder<List<SongModel>>(
          future: OnAudioQuery()
              .queryAudiosFrom(AudiosFromType.ARTIST, artist.artist),
          builder: (context, snapshot) {

            if (!snapshot.hasData) {
              return Container();
            } else {
              return FloatingActionButton(
                backgroundColor: Theme.of(context).primaryColor,
                child: Container(
                    margin: const EdgeInsets.only(left: 4.0),
                    child: const Icon(
                      CupertinoIcons.play_fill, size: 32,
                      color: Colors.white,
                    )
                ),
                onPressed: () async {
                  await musicController
                      .setListSong(songs: snapshot.data!, initialIndex: 0)
                      .then((value) {
                    musicController.play();
                    Get.to(const MusicPlayerPage());
                    //Navigator.push(context, MusicPlayerPage.route());
                  });
                },
              );
            }
          }) ,
      body: NestedScrollView(
        headerSliverBuilder: (context, innerBoxIsScrolled) {
          return <Widget>[
            SliverAppBar(
              //backgroundColor: background,
              expandedHeight: MediaQuery.of(context).size.width * 0.75,
              floating: false,
              pinned: true,
              snap: false,
              elevation: 0.0,
              flexibleSpace: FlexibleSpaceBar(
                centerTitle: true,
                title: Title(
                  color: Colors.deepOrangeAccent,
                  child: Text(
                    appBarTitle,
                    maxLines: 1,
                    softWrap: true,
                    overflow: TextOverflow.fade,
                    style: const TextStyle(
                      color: Colors.white,
                    ),
                  ),
                ),
                background: (appBarBackgroundImage == null)
                    ? cover
                    : Image.memory(
                        appBarBackgroundImage!,
                        fit: BoxFit.cover,
                      ),
              ),
            ),
          ];
        },
        body: FutureBuilder<List<SongModel>>(
            future: OnAudioQuery()
                .queryAudiosFrom(AudiosFromType.ARTIST, artist.artist),
            builder: (context, snapshotSong) {
              //print(snapshotSong.data!);
              int length = 0;
              if (!snapshotSong.hasData) {
                return const Center(child: CircularProgressIndicator());
              } else {
                if (snapshotSong.data!.length > length) {
                  length = 10;
                } else {
                  length = snapshotSong.data!.length;
                }
                return SingleChildScrollView(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    children: [
                      Container(
                        //height: 50, //🎶
                        child: Text(
                              "${artist.numberOfAlbums} Albums ● "
                              "${artist.numberOfTracks} Songs ● "
                              "${Duration(minutes: 3*(artist.numberOfTracks ?? 0)).toString().substring(0,9)}",
                          style: const TextStyle(
                            color: Colors.grey,
                            fontSize: 18,
                          ),
                        ),
                      ),
                      Container(
                        height: 210,
                        margin: const EdgeInsets.symmetric(vertical: 16.0),
                        child: StreamBuilder<List<dynamic>>(
                            stream: OnAudioQuery()
                                .queryWithFilters(
                                  artist.artist,
                                  WithFiltersType.ALBUMS,
                                  args: AlbumsArgs.ARTIST,
                                )
                                .asStream(),
                            builder: (context, snapshotAlbum) {
                              if (!snapshotAlbum.hasData) {
                                return const Center(
                                  child: CircularProgressIndicator(),
                                );
                              }
                              if (snapshotAlbum.data!.isEmpty) {
                                return const NoDataWidget(
                                    title: "No Albums found");
                              }

                              final List<AlbumModel> albums =
                                  snapshotAlbum.data!.toAlbumModel();

                              return SizedBox(
                                //height: 220,
                                child: Column(
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  children: [
                                    const ListTile(
                                      title: Text(
                                        "Albums",
                                        textAlign: TextAlign.start,
                                        style: TextStyle(
                                          fontSize: 20.0,
                                        ),
                                      ),
                                      //trailing: Tex,
                                    ),
                                    Expanded(
                                      //height: 100,
                                      child: SingleChildScrollView(
                                        physics: const BouncingScrollPhysics(),
                                        scrollDirection: Axis.horizontal,
                                        child: Row(
                                          children: albums
                                              .map((album) => Container(
                                            margin : const EdgeInsets.symmetric(horizontal: 8.0),
                                            /*decoration: BoxDecoration(
                                              borderRadius:
                                              BorderRadiusDirectional
                                                  .circular(20.0),
                                              /*image: DecorationImage(
                                                    image: MemoryImage(bytes)
                                                ),*/
                                            ),*/
                                            child: Column(
                                              mainAxisAlignment: MainAxisAlignment.start,
                                              children: [
                                                InkWell(
                                                  onTap:() => _openAlbumPage(album),
                                                  child: SizedBox(
                                                    height:130,
                                                    width:130,
                                                    child: Stack(
                                                      children: [
                                                        ClipRRect(
                                                          borderRadius:
                                                          BorderRadius.circular(18.0),
                                                          child: GetImageCover(
                                                            height:130,
                                                            width:130,
                                                            //fit: BoxFit.contain,
                                                            futureResource: OnAudioQuery()
                                                                .queryArtwork(
                                                              album.id,
                                                              ArtworkType.ALBUM,
                                                            ),
                                                          ),
                                                        ),
                                                        Align(
                                                          alignment: Alignment.topRight,
                                                          child: InkWell(
                                                            child: Container(
                                                              margin: const EdgeInsets.all(4.0),
                                                                child: const Icon(Icons.play_circle_fill, color: Colors.grey,)),
                                                            onTap: (){},
                                                          ),
                                                        ),
                                                      ],
                                                    ),
                                                  ),
                                                ),

                                                Container(
                                                  //height:50,
                                                  margin: const EdgeInsets.symmetric(vertical: 4.0),
                                                    child: Text( (album.album.length > 12)
                                                        ? album.album.substring(0,10)+"..."
                                                        :album.album,)
                                                )

                                              ],
                                            ),
                                          ))
                                              .toList(),
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              );
                            }),
                      ),
                      Column(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          ListTile(
                            title: const Text(
                              "Songs",
                              style: TextStyle(
                                fontSize: 20.0,
                              ),
                            ),
                            trailing: InkWell(
                              onTap: () => Get.to(
                                  QuickListSongPage(
                                    onSongTap: onSongTap,
                                    artist: artist,
                                    songList: snapshotSong.data!,
                                  )
                              ),
                              child: Text('SEE ALL',style: TextStyle(
                                  color: Theme.of(context).primaryColor),
                              ),
                            ),
                          ),
                          SongListWithoutScrollingWidget(
                            onTapCallBack: onSongTap,
                            songList: snapshotSong.data!,
                            length: 7,
                            //length: length,
                          ),
                        ],
                      ),
                    ],
                  ),
                );
              }
            }),
      ),
    );
  }

  void _openAlbumPage(final AlbumModel album) {
    Get.to(() => SingleAlbumScreen(
      album: album,
      appBarBackgroundImage: null,
      appBarTitle: album.album,
      cover: GetImageCover(
        futureResource: OnAudioQuery().queryArtwork(
          album.id,
          ArtworkType.ALBUM,
        ),
        defaultValue: "assets/no_cover.png",
      ),

    ));
  }
}

class QuickListSongPage extends StatelessWidget {
  final ArtistModel artist;
  final List<SongModel> songList;
  final void Function()? onSongTap;

  const QuickListSongPage({
    required this.artist,
    required this.songList,
    this.onSongTap,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Songs of ${artist.artist}"),
        //centerTitle: true,
      ),
      body: SongListWidget(
        onTapCallBack: onSongTap,
        songList: songList,
        //length: length,
      ),
    );
  }
}

