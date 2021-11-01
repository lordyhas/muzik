

part of music.layout;

class AlbumListScreen extends StatefulWidget {
  final void Function()? onTapCallBack;

  const AlbumListScreen({Key? key, this.onTapCallBack}) : super(key: key);

  @override
  _AlbumListScreenState createState() => _AlbumListScreenState();
}

class _AlbumListScreenState extends State<AlbumListScreen> {
  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8.0),
      child: StreamBuilder<List<AlbumModel>>(
        stream: OnAudioQuery()
            .queryAlbums(
          sortType: AlbumSortType.ALBUM,
          orderType: OrderType.ASC_OR_SMALLER,
          uriType: UriType.EXTERNAL,
        ).asStream(),
        builder: (context, snapshot) {
          if (snapshot.data == null) {
            return const Center(
              child: CircularProgressIndicator()
          );
          }

          if (snapshot.data!.isEmpty) {
            return const NoDataWidget(title: "No Album Found");
          }
          return AlbumGridWidget(
            onAlbumClicked: _openAlbumPage ,
            albumList: snapshot.data!,

          );
        }
      ),
    );
  }

  void _openAlbumPage(final AlbumModel album) {
    Navigator.push(
      context,
      MaterialPageRoute(
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
      ),
    );
  }
}

typedef OnAlbumClicked = Function(AlbumModel info);

class AlbumGridWidget extends StatelessWidget {
  final bool checkIsCollapsed =  true;//isCollapsed
  final OnAlbumClicked _onItemTap;
  final List<AlbumModel> dataList;

  const AlbumGridWidget({
    required List<AlbumModel> albumList,
    required OnAlbumClicked onAlbumClicked,
    Key? key
  }): _onItemTap = onAlbumClicked,
        dataList = albumList, super(key: key);

  @override
  Widget build(BuildContext context) {
    const Duration duration = Duration(milliseconds: 400);

    double coverSize = 250.0;
    return Scrollbar(
      child: GridView.builder(
          physics: const BouncingScrollPhysics(),
          //shrinkWrap: true,
          //padding: const EdgeInsets.only(bottom: 16.0),
          itemCount: dataList.length,
          gridDelegate:
          const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2),
          itemBuilder: (context, index) {
            AlbumModel album = dataList[index];

            return Stack(
              children: <Widget>[
                CardWidget2(
                  height: coverSize,
                  title: album.album,
                  author: "by ${album.artist}",
                  infoText: ("${album.numOfSongs} Track"),
                  backgroundImage: null,
                  useBackgroundImage: false, // !(album.artwork == null),
                  imageCover: ClipRRect(
                    borderRadius: const BorderRadius.all(Radius.circular(15.0)),
                    child: SizedBox(
                      height: coverSize,
                      child: GetImageCover(
                        futureResource: OnAudioQuery().queryArtwork(
                          album.id,
                          ArtworkType.ALBUM,
                        ),
                        defaultValue: "assets/no_cover.png",
                      ),
                    ),
                  ),
                ),
                Positioned.fill(
                  child: Material(
                    color: Colors.transparent,
                    child: InkWell(onTap: () => _onItemTap(album),),
                  ),
                ),
              ],
            );

          }),
    );
  }
}

class CardWidget2 extends StatelessWidget {
  final double? width, height;
  final String title, author, infoText;
  final bool useBackgroundImage;
  Uint8List? backgroundImage;
  final imageCover;
   final titleMaxLines = 1;
   final titleTextStyle = const TextStyle(
       fontWeight: FontWeight.bold, fontSize: 16.0);

  CardWidget2({
    Key? key,
    this.backgroundImage,
    this.width,
    this.height,
    this.title = "unknown",
    this.author = "unknown",
    this.infoText = "unknown",
    this.useBackgroundImage = true,
    this.imageCover,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final mainContainer = Container(
      width: width,
      height: height,
      child: Stack(
        alignment: AlignmentDirectional.bottomCenter,
        children: <Widget>[
          (useBackgroundImage)?
          Container(
            decoration: BoxDecoration(
              image: DecorationImage(
                image: (backgroundImage == null)
                    ? const AssetImage("assets/no_cover.png") as ImageProvider
                    : MemoryImage(backgroundImage!),
                fit: BoxFit.cover,
                alignment: AlignmentDirectional.center,
              ),
              borderRadius: const BorderRadius.all(Radius.circular(15.0)),
            ),
          ) :imageCover,
          Card(
            color: Colors.black38, //Color.fromRGBO(0xff, 0xff, 0xff, 0.5),

            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Flexible(
                      child: Text( title,//(title.length < 20) ?title: "${title.substring(0,19)}...",
                        //maxLines: titleMaxLines,
                        style: titleTextStyle,
                        overflow: TextOverflow.ellipsis,
                      )),
                  Flexible(child: Text(author, maxLines: 1,)),
                  Flexible(child: Text(infoText, maxLines: 1,)),
                ],
              ),
            ),
          ),
        ],
      ),
    );

    return Card(
      color: Colors.transparent,
      elevation: 4.0,
      child: mainContainer,
    );
  }
}