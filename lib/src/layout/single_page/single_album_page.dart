part of '../details_content_screen.dart';

// ignore: must_be_immutable
class SingleAlbumScreen extends StatelessWidget {

  static MaterialPageRoute route({required AlbumModel album}) => MaterialPageRoute(
    builder: (context) => SingleAlbumScreen(
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
    ),
  );


  final Uint8List? appBarBackgroundImage;
  final String appBarTitle;
  final Widget? cover;
  final AlbumModel album;
  final void Function()? onSongTap;


   SingleAlbumScreen({
    required this.album,
    required this.appBarTitle,
    this.appBarBackgroundImage,
    this.cover,
    this.onSongTap,
    Key? key,
  }) : super(key: key);




  final TextStyle primaryTextStyle20sp = const TextStyle(
      color: Colors.white,
      fontSize: 20,
      fontFamily: "ubuntu"
  );

  var iconWhiteColor = Colors.white;
  void _defaultOnTap(context) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          content: Text('En développment | Soon :) ')));
  }

  void __openArtisPage(context, final ArtistModel artist) {
    Navigator.push(context, SingleArtistScreen.route(artist: artist),);
  }


  @override
  Widget build(BuildContext context) {

    final PlayerControllerBloc musicController = BlocProvider.of<PlayerControllerBloc>(context);
    return Scaffold(
      //backgroundColor: background,
      /*floatingActionButton: FutureBuilder<List<SongModel>>(
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
          }) ,*/

      /*FloatingActionButton(
        child: Container(
          margin: const EdgeInsets.only(left: 4.0),
            child: const Icon(CupertinoIcons.play_fill, size: 32,)
        ),
        onPressed: () {  },
      ),*/

      //extendBodyBehindAppBar: true,
      extendBody: true,
      bottomNavigationBar: const BottomAppBar(
        color: Colors.transparent,
        child: Padding(
          padding: EdgeInsets.only(bottom: 1.0),
          child: BottomPlayerView(),
        ),
      ),
      appBar: AppBar(
        elevation: 0.0,
        //backgroundColor: Colors.transparent,
        leading: IconButton(
            onPressed: Navigator.of(context).pop,
            icon: const Icon(Icons.arrow_back)
        ),
        actions: [
          IconButton(
            icon: const Icon(CupertinoIcons.sort_down),
            onPressed: (){},
          ),

          StreamBuilder<List<dynamic>>(
              stream: OnAudioQuery()
                  .queryWithFilters(
                album.artist ?? "",
                WithFiltersType.ARTISTS,
                args: AlbumsArgs.ARTIST,
              ).asStream(),
            builder: (context, snapshot) {
              return PopupMenuButton(
                color: Theme.of(context)
                    .backgroundColor
                    .withOpacity(0.7),
                icon: const Icon(
                  CupertinoIcons.ellipsis_vertical,
                ),
                onSelected: (index) => Navigator.of(context).pop(),
                itemBuilder: (BuildContext context) {
                  var list = <Widget>[
                    ListTile(
                      leading: Icon(CupertinoIcons.shuffle, color: iconWhiteColor),
                      title: Text('Shuffle Play', style: primaryTextStyle20sp),
                      onTap: () => _defaultOnTap(context),
                    ),
                    ListTile(
                      leading:
                      Icon(CupertinoIcons.person_crop_rectangle, color: iconWhiteColor),
                      title: Text('See artist', style: primaryTextStyle20sp),
                      onTap: () => Navigator.push(
                        context,
                        SingleArtistScreen.route(artist: snapshot.data?.first)
                      ), //_openArtisPage(context, snapshot.data?.first),
                    ),
                    ListTile(
                      leading:
                      Icon(CupertinoIcons.text_append, color: iconWhiteColor),
                      title: Text('Add to playing queue', style: primaryTextStyle20sp),
                      onTap: () => _defaultOnTap(context),
                    ),

                    ListTile(
                      leading: Icon(CupertinoIcons.text_badge_plus, color: iconWhiteColor),
                      title: Text('Add to playlist ', style: primaryTextStyle20sp),
                      onTap: null, //() => _addToPlaylist(),
                    ),
                    ListTile(
                      leading: Icon(CupertinoIcons.arrowshape_turn_up_right,
                          color: iconWhiteColor),
                      title: Text('Share', style: primaryTextStyle20sp),
                      onTap: () => _defaultOnTap(context),
                    ),
                  ];
                  return list.map((e) => PopupMenuItem(child: e)).toList();
                }
              );
            }
          ),
        ],
      ),
      body: NestedScrollView(
        physics: const BouncingScrollPhysics(),
        headerSliverBuilder: (context, innerBoxIsScrolled) {
          return <Widget>[
            SliverList(
              delegate: SliverChildBuilderDelegate(
                    (BuildContext context, int index) {
                  return SizedBox(
                    height: 230,
                    //color: Colors.red,
                    child: Stack(
                      children: [
                        Container(
                          margin: const EdgeInsets.only(bottom: 80.0),
                          decoration:  BoxDecoration(
                              color: Theme.of(context).appBarTheme.backgroundColor,
                              borderRadius: const BorderRadius.only(
                                //left: Radius.zero,
                                  bottomRight: Radius.circular(75))
                          ),
                        ),
                        SizedBox(
                          //alignment: Alignment.bottomLeft,
                          child: Column(
                            children: [
                              //const Spacer(),
                              Container(
                                padding: const EdgeInsets.only(left: 32.0),
                                child: ListTile(
                                  title: Text(album.album,
                                    style: const TextStyle(
                                        fontSize: 20,
                                        fontWeight: FontWeight.bold),),
                                  subtitle: Text(album.artist ?? 'unknown artist'),
                                ),
                              ),
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.only(left: 42.0),
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
                                  Padding(
                                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                                    child: FutureBuilder<List<SongModel>>(
                                        future: OnAudioQuery()
                                            .queryAudiosFrom(AudiosFromType.ALBUM, album.album),
                                        builder: (context, snapshot) {

                                          if (!snapshot.hasData) {
                                            return Container();
                                          } else {
                                            return FloatingActionButton(
                                              //backgroundColor: Theme.of(context).primaryColor,
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
                                                  //onSongTap!();
                                                  Get.to(const MusicPlayerPage());
                                                  //Navigator.push(context, MusicPlayerPage.route());
                                                });
                                              },
                                            );
                                          }
                                        }),
                                  )
                                ],
                              ),
                            ],
                          ),
                        ),
                        /*Align(
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
                        ),*/
                      ],
                    ),
                  );
                },
                childCount: 1,
                addSemanticIndexes: false,
              ),
            ),


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
