part of '../details_content_screen.dart';

class SingleArtistScreen extends StatelessWidget {
  final Uint8List? appBarBackgroundImage;
  final String appBarTitle;
  final Widget? cover;
  final ArtistModel artist;
  final void Function()? onSongTap;

  static MaterialPageRoute route({required ArtistModel artist}) =>
      MaterialPageRoute(
        builder: (context) => SingleArtistScreen(
          appBarBackgroundImage: null,
          cover: GetImageCover(
            defaultValue: "assets/no_cover.png",
            futureResource: OnAudioQuery().queryArtwork(
              artist.id,
              ArtworkType.ALBUM,
            ),
            //Image.asset("assets/no_cover.png",),
          ),
          appBarTitle: artist.artist,
          artist: artist,
        ),
      );

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
    final PlayerControllerBloc musicController =
        BlocProvider.of<PlayerControllerBloc>(context);
    return Scaffold(
      //backgroundColor: background,

      extendBody: true,
      bottomNavigationBar: const BottomAppBar(
        color: Colors.transparent,
        child: Padding(
          padding: EdgeInsets.only(bottom: 1.0),
          child: BottomPlayerView(),
        ),
      ),
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
                          "${Duration(minutes: 3 * (artist.numberOfTracks ?? 0)).toString().substring(0, 9)}",
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
                            stream: OnAudioQuery().queryWithFilters(
                                  artist.artist,
                                  WithFiltersType.ALBUMS,
                                  args: AlbumsArgs.ARTIST,
                                ).asStream(),
                            builder: (context, snapshotAlbum) {
                              if (!snapshotAlbum.hasData) {
                                return const Center(
                                  child: CircularProgressIndicator(),
                                );
                              }
                              if (snapshotAlbum.data!.isEmpty) {
                                return const NoDataWidget(title: "No Albums found");
                              }

                              final List<AlbumModel> albums = snapshotAlbum.data!.toAlbumModel();

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
                                          children: albums.map((album) => Container(
                                                    margin: const EdgeInsets.symmetric(horizontal: 8.0),
                                                    child: Column(
                                                      mainAxisAlignment: MainAxisAlignment.start,
                                                      children: [
                                                        InkWell(
                                                          onTap: () => Navigator.push(context, SingleAlbumScreen.route(album: album)),
                                                          child: SizedBox(
                                                            height: 130,
                                                            width: 130,
                                                            child: Stack(
                                                              children: [
                                                                ClipRRect(
                                                                  borderRadius:
                                                                      BorderRadius
                                                                          .circular(
                                                                              18.0),
                                                                  child:
                                                                      GetImageCover(
                                                                    height: 130,
                                                                    width: 130,
                                                                    //fit: BoxFit.contain,
                                                                    futureResource:
                                                                        OnAudioQuery()
                                                                            .queryArtwork(
                                                                      album.id,
                                                                      ArtworkType
                                                                          .ALBUM,
                                                                    ),
                                                                  ),
                                                                ),
                                                                Align(
                                                                  alignment:
                                                                      Alignment
                                                                          .topRight,
                                                                  child:
                                                                      InkWell(
                                                                    child: Container(
                                                                        margin: const EdgeInsets.all(4.0),
                                                                        child: const Icon(
                                                                          Icons
                                                                              .play_circle_fill,
                                                                          color:
                                                                              Colors.grey,
                                                                        )),
                                                                    onTap:
                                                                        () {},
                                                                  ),
                                                                ),
                                                              ],
                                                            ),
                                                          ),
                                                        ),
                                                        Container(
                                                            //height:50,
                                                            margin: const EdgeInsets.symmetric(vertical: 4.0),
                                                            child: Text((album.album.length > 12) ? album.album.substring(0, 10) + "..." : album.album,))
                                                      ],
                                                    ),
                                                  )).toList(),
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
                              onTap: () => Get.to(QuickSongListPage(
                                onSongTap: onSongTap,
                                artist: artist,
                                songList: snapshotSong.data!,
                              )),
                              child: Text(
                                'SEE ALL',
                                style: TextStyle(
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

  void __openAlbumPage(context, final AlbumModel album) {
    Navigator.push(context, SingleAlbumScreen.route(album: album),);

    /*Get.to(() => SingleAlbumScreen(
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
        ));*/
  }
}
