part of data.model;

@Entity()
class ControllerState {
  @Id()
  int uid = 0;

  //
  bool isShuffle;

  /// enum LoopMode { off, one, all }
  /// 0 : off
  /// 1 : one
  /// 2 : all
  int repeatMode;

  int? _index;

  @Transient()
  Playlist<SongInfo>? _pendingPlaylist;

  @Transient()
  Playlist<SongInfo>? _shuffledPendingPlaylist = Playlist.empty();

  ControllerState({
    int? index,
    LoopMode loopMode = LoopMode.off,
    this.isShuffle = false,

    final Playlist? playlist,
  })  : repeatMode = loopMode.index,
        _pendingPlaylist = playlist,
        _index = index ?? 0;

  LoopMode get currentLoopMode => LoopMode.values[repeatMode];

  Playlist get pendingQueue => _pendingPlaylist ?? Playlist.empty();

  Playlist get shuffledPendingQueue =>
      _shuffledPendingPlaylist ?? Playlist.empty();

  set pendingQueue(Playlist<SongInfo> playlist) {
    _pendingPlaylist = playlist;
  }

  set shuffledPendingQueue(Playlist<SongInfo> playlist) {
    _shuffledPendingPlaylist = playlist;
  }

  int get index => _index!;
  set index(int i) => _index = i;
}
