part of data.model;

@Entity()
class PlaylistInfo {
  @Id()
  int uid = 0;
  int recentIndex;
  bool isCurrentList;
  String title;
  final songsFromBox = ToMany<SongInfo>();

  @Transient()
  Playlist? songs;

  PlaylistInfo({
    required this.title,
    this.isCurrentList = false,
    this.recentIndex = 0,
    this.songs,
  });

  factory PlaylistInfo.fromModel(Playlist songList, {
        required int index,
        required String title,
      }) => PlaylistInfo(
    title: title,
    songs: songList,
    recentIndex: index,
  );

  Playlist get playlist => Playlist(songs??[]);
//Playlist get playlist => songs.map((e) => e).toList() as Playlist;
}