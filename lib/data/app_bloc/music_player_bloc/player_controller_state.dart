
part of 'player_controller_cubit.dart';
/*

enum MusicPlayerState {playing, stopped, paused}
enum LectureMode { noRepeat, repeat, repeatOne}
*/

class PlayerControllerState {
  //Playlist<SongInfo> songList;
  final AudioPlayer player;
  //int index;

  ControllerState controllerState;


  PlayerControllerState._({
    required this.player ,
    required this.controllerState,
    //this.index = -1
  });

  PlayerControllerState.init() : this._(
    controllerState : ControllerState(),
    player: AudioPlayer(),
  );

  int get songIndex => controllerState.index ;

  set songIndex(int value) {
    controllerState.index = value;
  }

  //SongInfo get currentSong => songList[songIndex];
  Playlist<SongInfo> get currentPlaylist {
    if( controllerState.isShuffle) {
      return controllerState.shuffledPendingQueue;
    } else {
      return controllerState.pendingQueue;
    }
  }
  set currentPlaylist(Playlist<SongInfo> songs){
    if(controllerState.isShuffle) {
      controllerState.shuffledPendingQueue = songs;
    } else {
      controllerState.pendingQueue = songs;
    }
  }


  //
  // AudioPlayer get status => player;


}

