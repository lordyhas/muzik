part of music.layout;

class SongListScreen extends StatefulWidget {
  final void Function()? onTapCallBack;

  const SongListScreen({Key? key, this.onTapCallBack}) : super(key: key);

  @override
  _SongListScreenState createState() => _SongListScreenState();
}

class _SongListScreenState extends State<SongListScreen> {
  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8.0),
      child: StreamBuilder<List<SongModel>>(
        stream: OnAudioQuery()
            .querySongs(
              sortType: SongSortType.TITLE,
              orderType: OrderType.ASC_OR_SMALLER,
              uriType: UriType.EXTERNAL,
            )
            .asStream(),
        builder: (context, item) {
          if (item.data == null) {
            return const Center(child: CircularProgressIndicator());
          }

          if (item.data!.isEmpty) {
            return const NoDataWidget(title: "No Songs Found");
          }

          // You can use [item.data!] direct or you can create a:

          //List<SongModel> songs = item.data!.where((element) => element.isMusic == true).toList();
          List<SongModel> songs = item.data!.musicOnly;
          return SongListWidget(
            songList: songs, //item.data!,
            //..retainWhere((element) => element.isMusic ?? false),
            onTapCallBack: widget.onTapCallBack,
          );
        },
      ),
    );
  }
}

class SongListWidget extends StatelessWidget {
  final List<SongModel> songList;

  //final int? length;
  final void Function()? onTapCallBack;

  const SongListWidget({
    required this.songList,
    this.onTapCallBack,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final PlayerControllerBloc musicController =
        BlocProvider.of<PlayerControllerBloc>(context);
    return Scrollbar(
      child: ListView.builder(
        physics: const BouncingScrollPhysics(),
        itemCount: songList.length,
        itemBuilder: (context, index) {
          return Row(
            children: [
              SizedBox(
                width: MediaQuery.of(context).size.width * 0.82,
                child: ListTile(
                  title: Text(
                    songList[index].title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  subtitle: Text(
                    songList[index].artist ?? "Unknown Artist",
                    maxLines: 1,
                  ),
                  //trailing: const Icon(CupertinoIcons.play_arrow ),

                  leading: ClipRRect(
                      borderRadius: BorderRadius.circular(100.0,),
                      child: SizedBox(
                        //width: 50,
                        child: GetImageCoverItem(
                          fit: BoxFit.cover,
                          futureResource: OnAudioQuery().queryArtwork(
                            songList[index].id,
                            ArtworkType.AUDIO,
                          ),
                        ),
                      )),

                  onTap: () async {
                    await musicController
                        .setListSong(songs: songList, initialIndex: index)
                        .then((value) {
                      musicController.play();
                      onTapCallBack!();
                      //Get.to(() => const MusicPlayerPage());
                      Navigator.push(context, MusicPlayerPage.route());
                    });
                  },
                ),
              ),
              PopupMenuButton(
                color: Theme.of(context).backgroundColor.withOpacity(0.9),
                icon: const Icon(
                  CupertinoIcons.ellipsis_vertical,
                ),
                onSelected: (index) => Navigator.of(context).pop(),
                itemBuilder: (BuildContext context) {
                  return <PopupMenuEntry>[
                    PopupMenuItem(
                        onTap: () async {
                          await musicController
                              .setListSong(songs: songList, initialIndex: index)
                              .then((value) {
                            musicController.play();
                            onTapCallBack!();
                            //Get.to(() => const MusicPlayerPage());
                            Navigator.push(context, MusicPlayerPage.route());
                          });
                        },
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.start,
                          children: const [
                            Icon(CupertinoIcons.play_arrow),
                            SizedBox(
                              width: 4.0,
                            ),
                            Text("Play"),
                          ],
                        )),
                    PopupMenuItem(
                        onTap: () {
                          musicController.addNext(
                            song: songList[index],
                            index: index,
                          );
                        },
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.start,
                          children: const [
                            Icon(CupertinoIcons.arrow_uturn_right),
                            SizedBox(
                              width: 4.0,
                            ),
                            Text("Play next"),
                          ],
                        )),
                    PopupMenuItem(
                        onTap: () {},
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.start,
                          children: const [
                            Icon(CupertinoIcons.text_append),
                            SizedBox(
                              width: 4.0,
                            ),
                            Text("Add to playing queue"),
                          ],
                        )),
                    PopupMenuItem(
                        onTap: () {},
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.start,
                          children: const [
                            Icon(CupertinoIcons.plus_square_fill_on_square_fill),
                            SizedBox(width: 4.0),
                            Text("Add to playlist"),
                          ],
                        )),
                    const PopupMenuDivider(),
                    PopupMenuItem(
                      child: StreamBuilder<List<dynamic>>(
                          stream: OnAudioQuery().queryWithFilters(
                                songList[index].album ?? "",
                                WithFiltersType.ALBUMS,
                                args: AlbumsArgs.ALBUM,
                              ).asStream(),
                          builder: (context, snapshot) {

                            return InkWell(
                                onTap: () {
                                  if (snapshot.hasData) {
                                    final List<AlbumModel> albums = snapshot.data!.toAlbumModel();
                                    Navigator.push(
                                        context,
                                        SingleAlbumScreen.route(
                                            album: albums.first));
                                  }
                                },
                                child: Row(
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  children: const [
                                    Icon(CupertinoIcons.music_albums),
                                    SizedBox(
                                      width: 4.0,
                                    ),
                                    Text("Go to album"),
                                  ],
                                ));
                          }),
                    ),
                    PopupMenuItem(

                      child: StreamBuilder<List<dynamic>>(
                          stream: OnAudioQuery().queryWithFilters(
                                songList[index].artist ?? "",
                                WithFiltersType.ARTISTS,
                                //args: ArtistsArgs.ARTIST,
                              ).asStream(),
                          builder: (context, snapshot) {
                            return InkWell(
                                onTap: () {
                                  if (snapshot.hasData) {
                                    final List<ArtistModel> artists = snapshot.data!.toArtistModel();
                                    Navigator.push(context, SingleArtistScreen
                                        .route(artist: artists.first,));
                                  }
                                },
                                child: Row(
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  children: const [
                                    Icon(CupertinoIcons.person_crop_circle),
                                    SizedBox(
                                      width: 4.0,
                                    ),
                                    Text("Go to artist"),
                                  ],
                                ));
                          },
                      ),
                    ),
                    const PopupMenuDivider(),
                    PopupMenuItem(

                        child: InkWell(
                          onTap: () {
                            Navigator.pop(context);
                            ShowOver.musicInfo(context,
                                song: SongInfo.fromModel(songList[index]));
                          },
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.start,
                            children: const [
                              Icon(CupertinoIcons.info),
                              SizedBox(
                                width: 4.0,
                              ),
                              Text("Details"),
                            ],
                          ),
                        ),
                    ),
                  ];
                },
              )
            ],
          );
        },
      ),
    );
  }
}

class SongListWithoutScrollingWidget extends StatelessWidget {
  final List<SongModel> songList;
  final int length;
  final void Function()? onTapCallBack;

  const SongListWithoutScrollingWidget({
    required this.songList,
    required this.length,
    this.onTapCallBack,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final PlayerControllerBloc musicController =
        BlocProvider.of<PlayerControllerBloc>(context);
    int newLength = songList.length;
    if (length < newLength) {
      newLength = length;
    }
    return Scrollbar(
      child: Column(
        children: songList.map((song) {
          return Row(
            children: [
              SizedBox(
                width: MediaQuery.of(context).size.width * 0.82,
                child: ListTile(
                  title: Text(
                    song.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  subtitle: Text(song.artist ?? "Unknown Artist"),
                  //trailing: const Icon(CupertinoIcons.play_arrow ),

                  leading: ClipRRect(
                      borderRadius: const BorderRadius.vertical(
                        top: Radius.circular(50.0),
                        bottom: Radius.circular(50.0),
                      ),
                      child: SizedBox(
                        //width: 50,
                        child: GetImageCoverItem(
                          //fit: BoxFit.fitWidth,
                          futureResource: OnAudioQuery().queryArtwork(
                            song.id,
                            ArtworkType.AUDIO,
                          ),
                        ),
                      )),
                  /*QueryArtworkWidget(
                              id: item.data![index].id,
                              type: ArtworkType.AUDIO,
                            ),*/
                  onTap: () async {
                    await musicController
                        .setListSong(
                            songs: songList,
                            initialIndex: songList.indexOf(song))
                        .then((value) {
                      musicController.play();
                      onTapCallBack!();
                      Navigator.push(context, MusicPlayerPage.route());

                      //Get.to(() => const MusicPlayerPage());
                      //Navigator.push(context, MusicPlayerPage.route());
                    });
                  },
                ),
              ),
              InkWell(
                  child: PopupMenuButton(
                    color: Theme.of(context).backgroundColor.withOpacity(0.9),
                    icon: const Icon(
                      CupertinoIcons.ellipsis_vertical,
                    ),
                    onSelected: (index) => Navigator.of(context).pop(),
                    itemBuilder: (BuildContext context) {
                      return <PopupMenuEntry>[
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.play_arrow),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Play"),
                              ],
                            )),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.arrow_uturn_right),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Play next"),
                              ],
                            )),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(
                                    CupertinoIcons.plus_square_fill_on_square_fill),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Add to playing queue"),
                              ],
                            )),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.text_badge_plus),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Add to playlist"),
                              ],
                            )),
                        const PopupMenuDivider(),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.music_albums),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Go to album"),
                              ],
                            )),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.person_crop_circle),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Go to album"),
                              ],
                            )),
                        const PopupMenuDivider(),
                        PopupMenuItem(
                            onTap: () {},
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: const [
                                Icon(CupertinoIcons.info),
                                SizedBox(
                                  width: 4.0,
                                ),
                                Text("Details"),
                              ],
                            )),
                      ];
                    },
                  )
                /*const Icon(CupertinoIcons.ellipsis_vertical),
                          onTap: () {
                            ;
                          },*/
              )
            ],
          );
        }).toList()
          ..length = newLength,
      ),
    );
  }
}

String durationStr(Duration dur) {
  String two(int n) {
    if (n >= 10) return "$n";
    return "0$n";
  }

  int hour = dur.inHours;
  int minute = dur.inMinutes.remainder(60);
  int second = dur.inSeconds.remainder(60);

  if (hour > 0) {
    return '${two(hour)}:${two(minute)}:${two(second)}';
  }

  return '${two(minute)}:${two(second)}';
}
