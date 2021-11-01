
part of 'player_controller_cubit.dart';
/*

enum MusicPlayerState {playing, stopped, paused}
enum LectureMode { noRepeat, repeat, repeatOne}
*/

class PlayerControllerState {
  Playlist<SongInfo> songList;
  final AudioPlayer player;
  int index;


  PlayerControllerState._({
    required this.player ,
    required this.songList,
    this.index = -1
  });

  PlayerControllerState.init() : this._(
      player: AudioPlayer(),
      songList: Playlist.empty(),
  );

  int get songIndex => index==-1 ? player.currentIndex! : index;

  set songIndex(int value) {
    index = value;
  }

  SongInfo get currentSong => songList[songIndex];
  Playlist<SongInfo> get currentPlaylist => songList;
  set currentPlaylist(Playlist<SongInfo> songs){
    songList = songs;
  }


  //
  // AudioPlayer get status => player;


}
/*bool _isPlaying = false;
  bool _isLoading = false;

  //Duration  duration ;
  //Duration  position ;


  Duration get duration => _duration;
  Duration get position => _position;

  bool get isPlaying => _isPlaying;
  bool get isLoading => _isLoading;

  startLoading() {
    _isLoading = true;
  }

  stopLoading() {
    _isLoading = false;
  }

  play() {
    _isPlaying = true;
  }

  pause() {
    _isPlaying = false;
  }

  stop() {
    _isPlaying = false;
  }

  set duration(Duration value) {
    _duration = value;
  }

  set position(Duration value) {
    _position = value;
  }*//*




}


*/
