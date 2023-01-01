part of '../details_content_screen.dart';

// ignore: must_be_immutable
class SingleAlbumScreen extends StatelessWidget {
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

  void _openArtisPage(context, final ArtistModel artist) {
    Navigator.push(
      context,
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
      ),
    );
  }


  @override
  Widget build(BuildContext context) {

    //final PlayerControllerBloc musicController =
    BlocProvider.of<PlayerControllerBloc>(context);
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
      appBar: AppBar(
        elevation: 0.0,
        //backgroundColor: Theme.of(context).primaryColor,

        actions: [
          IconButton(
            icon: const Icon(CupertinoIcons.sort_down),
            onPressed: (){},
          ),

          // IconButton(
          //   icon: const Icon(CupertinoIcons.ellipsis_vertical),
          //   onPressed: (){
          //
          //   },
          // ),


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
                      leading: Icon(CupertinoIcons.play_arrow_solid, color: iconWhiteColor),
                      title: Text('Shuffle', style: primaryTextStyle20sp),
                      onTap: () => _defaultOnTap(context),
                    ),
                    ListTile(
                      leading:
                      Icon(CupertinoIcons.person_crop_rectangle, color: iconWhiteColor),
                      title: Text('See artist', style: primaryTextStyle20sp),
                      onTap: () => _openArtisPage(context, snapshot.data?.first),
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
      body: Stack(
            children: [
              NestedScrollView(
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
              const Align(
                alignment: Alignment.bottomCenter,
                child: BottomPlayerView(),

              ),
            ],
          ),

      );

  }

}
