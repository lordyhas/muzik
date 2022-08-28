part of music.layout;

class ArtistListScreen extends StatefulWidget {
  final void Function()? onTapCallBack;

  const ArtistListScreen({Key? key, this.onTapCallBack}) : super(key: key);

  @override
  _ArtistListScreenState createState() => _ArtistListScreenState();
}

class _ArtistListScreenState extends State<ArtistListScreen> {
  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8.0),
      child: StreamBuilder<List<ArtistModel>>(
          stream: OnAudioQuery()
              .queryArtists(
                sortType: ArtistSortType.ARTIST,
                orderType: OrderType.ASC_OR_SMALLER,
                uriType: UriType.EXTERNAL,
              )
              .asStream(),
          builder: (context, snapshot) {
            if (snapshot.data == null) {
              return const Center(child: CircularProgressIndicator());
            }

            if (snapshot.data!.isEmpty) {
              return const NoDataWidget(title: "No Artist Found");
            }
            return ArtistListWidget(
              artistList: snapshot.data!,
              onArtistSelected: _openArtisPage,
            );
          }),
    );
  }

  void _openArtisPage(final ArtistModel artist) {
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
}

class ArtistListWidget extends StatelessWidget {
  final List<ArtistModel> artistList;
  final Function(ArtistModel info) _callback;
  final Widget? cover;

  const ArtistListWidget(
      {required this.artistList,
      required void Function(ArtistModel info) onArtistSelected,
      this.cover,
      Key? key})
      : _callback = onArtistSelected,
        super(key: key);


  String artistLabel(String artist){
    if(artist.toLowerCase().contains('unknown')){
      return '#';
    }
    if(artist.length > 2) {
      if(artist[0] == '"'){
        return '...';
      }
      return artist.substring(0,2).toUpperCase();
    }
    else {
      return artist;
    }

  }
  Widget artistCard(ArtistModel artist, ctx) {

    const double coverSize = 130;
    return Container(
      //height: 100,
      padding: const EdgeInsets.all(8.0),
      child: InkWell(
        //onDoubleTap: () {},
        onTap: () => _callback(artist),
          //debugPrint("Artist clicked ${artist.artist}");

        child: Column(
          children: [
            ClipRRect(
              //clipBehavior: Clip.antiAliasWithSaveLayer,
              borderRadius: BorderRadius.circular(150),
              child: Stack(
                children: [
                  GetImageCover(
                    defaultValue: "assets/no_cover.png",
                    futureResource: OnAudioQuery().queryArtwork(
                      artist.id,
                      ArtworkType.ALBUM,
                    ),
                    //Image.asset("assets/no_cover.png",),
                  ),
                  Container(
                    //height: ,
                    color: Theme.of(ctx).primaryColor.withOpacity(0.5),
                    child: Center(
                      child: Text(artistLabel(artist.artist),
                          style: const TextStyle(
                              fontSize: 42.0,
                              color: Colors.white
                          )
                      ),
                    ),
                  ),
                ],
              ),
            ),
            //Card(clipBehavior: Clip.,)
            const SizedBox(
              height: 8.0,
            ),
            Text(
              artist.artist,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    double screenWith, screenHeight;
    Size size = MediaQuery.of(context).size;
    screenHeight = size.height;
    screenWith = size.width;
    double wSize = screenWith - 70;
    double widthSize = wSize / 2;
    double heightSize = 150;
    return Scrollbar(
      child: GridView.builder(
          physics: const BouncingScrollPhysics(),
          //shrinkWrap: true,
          //padding: const EdgeInsets.only(bottom: 16.0),
          itemCount: artistList.length,
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2, childAspectRatio: 0.8),
          itemBuilder: (context, index) {
            ArtistModel artist = artistList.elementAt(index);
            return artistCard(artist, context);
            /*Card(
              child: Stack(
                children: <Widget>[
                  CardItemWidget(
                    height: 250.0,
                    title: "${artist.name}",
                    subtitle:"Albums: ${artist.numberOfAlbums}",
                    infoText: "Songs: ${artist.numberOfTracks}",
                    backgroundImage: artist.artistArtPath,
                  ),
                  Positioned.fill(
                    child: Material(
                      color: Colors.transparent,
                      child: InkWell(onTap: () {
                        if (_callback != null) _callback(artist);
                        print("Artist clicked ${artist.name}");
                      }),
                    ),
                  ),
                ],
              ),
            );*/
          }),
    );
  }
}
