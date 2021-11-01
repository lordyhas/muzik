part of '../details_content_screen.dart';


class QuickSongListPage extends StatelessWidget {
  final ArtistModel artist;
  final List<SongModel> songList;
  final void Function()? onSongTap;

  const QuickSongListPage({
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