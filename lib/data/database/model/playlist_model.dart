part of data.model;

@Entity()
class PlaylistInfo {
  @Id()
  int uid = 0;
  int recentIndex;
  final songsFromBox = ToMany<SongInfo>();


  List<SongInfo> songs;
  PlaylistInfo({
    this.recentIndex = 0,
    this.songs =  const [] ,
  });

  factory PlaylistInfo.fromModel(List<SongModel> songList,
      {required int index}) => PlaylistInfo(
    songs: songList.map((songModel) => SongInfo.fromModel(songModel)).toList(),
    recentIndex: index,
  );

  Playlist get playlist => Playlist(songs);
//Playlist get playlist => songs.map((e) => e).toList() as Playlist;
}